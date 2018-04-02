package github.gphat.censorinus

import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.util.concurrent.{CountDownLatch, LinkedBlockingQueue, TimeUnit}
import org.scalacheck.{Arbitrary, Gen}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.prop.GeneratorDrivenPropertyChecks
import org.scalatest._
import org.scalatest.concurrent.Eventually
import github.gphat.censorinus.statsd.Encoder

object TestSender {
  def defaultDeadline: Long = System.currentTimeMillis + 1000
}

// Create a test Sender implementation that accumulates metrics in a buffer
// until we empty it. This enables us to block the Client's sending thread
// and artificially cause the client to accumulate metrics.
class TestSender(val maxMessages: Int = Int.MaxValue) extends MetricSender {
  var countDownLatch: CountDownLatch = new CountDownLatch(0)
  val buffer: LinkedBlockingQueue[String] =
    new LinkedBlockingQueue(maxMessages)

  def awaitMessage(deadline: Long = TestSender.defaultDeadline): String = {
    val duration = math.max(5, deadline - System.currentTimeMillis)
    Option(buffer.poll(duration, TimeUnit.MILLISECONDS)).getOrElse {
      throw new Exception("didn't get enough messages in time!")
    }
  }

  def awaitMessages(n: Int, deadline: Long = TestSender.defaultDeadline): List[String] =
    List.fill(n)(awaitMessage(deadline))

  def send(message: ByteBuffer): Unit = {
    countDownLatch.countDown()
    synchronized {
      val strMessage = StandardCharsets.UTF_8.newDecoder().decode(message).toString
      if (!buffer.offer(strMessage, 1, TimeUnit.MINUTES)) {
        throw new Exception("too much time required for test")
      }
    }
  }

  def shutdown: Unit = ()
}

class ClientSpec extends FlatSpec with Matchers with Eventually with GeneratorDrivenPropertyChecks {

  "ClientSpec" should "deal with gauges" in {
    val sender = new TestSender()
    val client = new Client(encoder = Encoder, sender = sender)
    client.enqueue(GaugeMetric(name = "foobar", value = 1.0))
    val msg :: Nil = sender.awaitMessages(1)
    msg should be ("foobar:1|g")
    client.shutdown
  }

  it should "batch metrics when batching is configured" in {
    val sender = new TestSender()
    val client = new Client(
      encoder = Encoder,
      sender = sender,
      maxQueueSize = Some(10),
      maxBatchSize = Some(20))

    for (_ <- (0 to 2)) {
      // By synchronizing on sender, we can block the client on the first metric
      // which lets us fill up the queue with the remaining 5 metrics. The
      // batcher will then work with a list of 5 metrics.
      sender.countDownLatch = new CountDownLatch(1)
      sender.synchronized {
        client.enqueue(GaugeMetric(name = "a", value = 1.0))
        // Wait for `send` to be called, otherwise we end up racing the polling
        // thread in `client` to fill/drain the queue.
        sender.countDownLatch.await()
        client.enqueue(CounterMetric(name = "b", value = 1.0))
        client.enqueue(GaugeMetric(name = "c", value = 2.0))
        client.enqueue(CounterMetric(name = "d", value = 3.0))
        client.enqueue(GaugeMetric(name = "e", value = -1.0))
        client.enqueue(GaugeMetric(name = "f", value = 1.0))
      }

      val messages = sender.awaitMessages(3)
      assert(messages == List("a:1|g", "b:1|c\nc:2|g\nd:3|c", "e:-1|g\nf:1|g"))
    }

    client.shutdown
  }

  it should "not enqueue metrics when queue is full" in {
    // We'll send 2 messages successfully, then block in the sender.
    val sender = new TestSender(2)
    // After the sender blocks, we'll still be able to queue up 1 more message.
    val client = new Client(encoder = Encoder, sender = sender, maxQueueSize = Some(1))
    // Hacky, but prevent things being pulled without our knowledge
    client.shutdown
    // Now fill up the client's 1 and only buffer spot
    client.enqueue(GaugeMetric(name = "a", value = 1.0))

    // All good. The sender's buffer is full and so is the client's queue.
    // This metric will be dropped instead of queued up.
    client.enqueue(GaugeMetric(name = "b", value = 4.0))

    // All good. The sender's buffer is full and so is the client's queue.
    // This metric will be dropped instead of queued up.
    client.enqueue(GaugeMetric(name = "b", value = 4.0))

    client.consecutiveDroppedMetrics.get should be (2)

    // // We drain the buffer and the client's queue.
    // val messages = sender.awaitMessages(3)
    client.queue.peek should be (GaugeMetric(name = "a", value = 1.0))

    // Clear things out
    client.queue.clear

    // Now that things are empty again, this will flow through.
    client.enqueue(GaugeMetric(name = "c", value = 5.0))
    client.queue.peek should be (GaugeMetric(name = "c", value = 5.0))

    client.queue.size should be (1)
  }

  // Batches `lines` using `batcher` and then decodes the resulting
  // `ByteBuffer`s as UTF-8 strings and puts them back into a `Vector`.
  private def batchAndDecode(batcher: Client.Batcher, lines: Iterator[String]): Vector[String] = {
    var result = Vector.empty[String]
    batcher.batch(lines) { buf =>
      result = result :+ StandardCharsets.UTF_8.newDecoder().decode(buf).toString
    }
    result
  }

  // Maximum size of a metric we'll use for property-based testing.
  // This is, basically, the max size of the "UDP packet".
  val maxMetricLength: Int = 30

  // A generator whose strings, when UTF-8 encoded, are long larger than
  // `maxMetricLength`.
  val shortLine: Gen[String] = arbitrary[String]
    .map { s =>
      // We want the final encoded size to be less than maxMetricLength, so
      // we do a janky thing here.
      Iterator.from(1)
        .map { k =>
          val len = s.length / k
          s.substring(0, math.min(s.length, len))
        }
        .find(_.getBytes("utf-8").size < maxMetricLength)
        .get
    }

  case class MetricLines(lines: Vector[String]) {
    def iterator: Iterator[String] = lines.iterator
  }

  implicit val arbMetricLines: Arbitrary[MetricLines] =
    Arbitrary(Gen.listOf(shortLine).map(_.toVector).map(MetricLines))

  def batcher(newBatcher: => Client.Batcher): Unit = {
    it should "batch metrics with new lines" in {
      forAll { (lines: MetricLines) =>
        assert(batchAndDecode(newBatcher, lines.iterator).mkString("\n") == lines.iterator.mkString("\n"))
      }
    }

    it should "batch a single metric" in {
      forAll(shortLine) { (line: String) =>
        assert(batchAndDecode(newBatcher, Iterator.single(line)) == Vector(line))
      }
    }
  }

  "Client.Batched" should "batch multiple metrics into byte buffers" in {
    val batcher = Client.Batched(10)
    assert(batchAndDecode(batcher, Iterator("abc", "def", "ghi")) == Vector("abc\ndef", "ghi"))
    assert(batchAndDecode(batcher, Iterator("abc", "de", "ghi")) == Vector("abc\nde\nghi"))
    assert(batchAndDecode(batcher, Iterator("abc", "def", "ghi", "jk", "lm", "no")) == Vector("abc\ndef", "ghi\njk\nlm", "no"))
  }

  it should "truncate the first metric if it is too long" in {
    val batcher = Client.Batched(11)
    assert(batchAndDecode(batcher, Iterator("123456789011")) == Vector("12345678901"))
    assert(batchAndDecode(batcher, Iterator("123456789011121314")) == Vector("12345678901"))
  }

  it should behave like batcher(Client.Batched(maxMetricLength))

  "Client.Unbatched" should "not batch metrics" in {
    forAll { (lines: Vector[String]) =>
      val batcher = Client.Unbatched
      assert(batchAndDecode(batcher, lines.iterator) == lines)
    }
  }

  it should behave like batcher(Client.Batched(maxMetricLength))
}
