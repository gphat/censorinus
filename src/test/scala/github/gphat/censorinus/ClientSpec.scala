package github.gphat.censorinus

import java.util.concurrent.{ LinkedBlockingQueue, TimeUnit }
import org.scalatest._
import org.scalatest.concurrent.Eventually
import scala.collection.mutable.ArrayBuffer
import github.gphat.censorinus.statsd.Encoder

object TestSender {
  def defaultDeadline: Long = System.currentTimeMillis + 1000
}

// Create a test Sender implementation that accumulates metrics in a buffer
// until we empty it. This enables us to block the Client's sending thread
// and artificially cause the client to accumulate metrics.
class TestSender(val maxMessages: Int = Int.MaxValue) extends MetricSender {
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

  def send(message: String): Unit = {
    if (!buffer.offer(message, 1, TimeUnit.MINUTES)) {
      throw new Exception("too much time required for test")
    }
  }

  def shutdown: Unit = ()
}

class ClientSpec extends FlatSpec with Matchers with Eventually {

  "ClientSpec" should "deal with gauges" in {
    val sender = new TestSender()
    val client = new Client(encoder = Encoder, sender = sender)
    client.enqueue(GaugeMetric(name = "foobar", value = 1.0))
    val msg :: Nil = sender.awaitMessages(1)
    msg should be ("foobar:1|g")
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
}
