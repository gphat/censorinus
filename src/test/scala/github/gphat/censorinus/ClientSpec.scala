package github.gphat.censorinus

import org.scalatest._
import scala.collection.mutable.ArrayBuffer
import github.gphat.censorinus.statsd.Encoder

class TestSender extends MetricSender {
  val buffer = new ArrayBuffer[String]()

  def awaitMessages(n: Int): List[String] = {
    var i = 0
    while (i < 100 && buffer.size < n) {
      Thread.sleep(10)
      i += 1
    }

    this.synchronized {
      if (buffer.size < n) {
        throw new Exception("didn't get enough messages!")
      } else {
        buffer.toList
      }
    }
  }

  def clear = buffer.clear

  def send(message: String): Unit = this.synchronized {
    buffer.append(message)
  }

  def shutdown: Unit = ()

  def getBuffer = buffer
}

class ClientSpec extends FlatSpec with Matchers {

  "ClientSpec" should "deal with gauges" in {
    val sender = new TestSender()
    val client = new Client(encoder = Encoder, sender = sender)
    client.enqueue(Metric(name = "foobar", value = "1.0", metricType = "g"))
    val msg :: Nil = sender.awaitMessages(1)
    msg should be ("foobar:1.0|g")
    client.shutdown
  }
}
