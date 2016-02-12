import org.scalatest._
import scala.collection.mutable.ArrayBuffer
import github.gphat.censorinus.{Client,Metric,MetricSender}
import github.gphat.censorinus.statsd.Encoder

class TestSender extends MetricSender {
  val buffer = new ArrayBuffer[String]()

  def send(message: String): Unit = {
    buffer.append(message)
  }

  def getBuffer = buffer
}

class ClientSpec extends FlatSpec with Matchers {

  val client = new Client(encoder = Encoder, sender = new TestSender())

  "ClientSpec" should "deal with gauges" in {
    client.enqueue(Metric(name = "foobar", value = "1.0", metricType = "g"))
    val m = client.getQueue.poll
    m.name should be ("foobar")
    m.value should be ("1.0")
    m.metricType should be ("g")
  }
}
