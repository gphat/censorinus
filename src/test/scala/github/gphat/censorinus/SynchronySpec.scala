package github.gphat.censorinus

import org.scalatest._
import org.scalatest.concurrent.Eventually
import github.gphat.censorinus.statsd.Encoder

class SynchronySpec extends FlatSpec with Matchers with Eventually {

  "Client" should "deal with gauges" in {
    val s = new TestSender()
    val client = new Client(encoder = Encoder, sender = s)

    client.enqueue(Metric(name = "foobar", value = "1.0", metricType = "g"))
    s.getBuffer.size should be (0) // Won't be there yet
    eventually {
      val m = s.getBuffer(0)
      m should include ("foobar")
      client.shutdown
    }
  }

  it should "be synchronous" in {
    val s = new TestSender()
    val client = new Client(encoder = Encoder, sender = s, asynchronous = false)

    client.enqueue(Metric(name = "foobar", value = "1.0", metricType = "g"))
    val m = s.getBuffer(0)
    m should include ("foobar")
    client.shutdown
  }
}
