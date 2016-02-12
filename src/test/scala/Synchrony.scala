import org.scalatest._
import github.gphat.censorinus.{Client,Metric,MetricSender}

class SynchronySpec extends FlatSpec with Matchers {

  "Client" should "deal with gauges" in {
    val s = new TestSender()
    val client = new Client(sender = s)

    client.gauge("foobar", 1.0)
    s.getBuffer.size should be (0) // Won't be there yet
    Thread.sleep(110) // Give it a bit more than 100ms to send
    val m = s.getBuffer(0)
    m.name should be ("foobar")
  }

  it should "be synchronous" in {
    val s = new TestSender()
    val client = new Client(sender = s, asynchronous = false)

    client.counter("foobar", 1.0)
    val m = s.getBuffer(0)
    m.name should be ("foobar")
  }
}
