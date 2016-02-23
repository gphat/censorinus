import org.scalatest._
import github.gphat.censorinus.{Client,Metric,MetricSender}
import github.gphat.censorinus.statsd.Encoder

class SamplingSpec extends FlatSpec with Matchers with BeforeAndAfter {

  var client: Client = null
  val s = new TestSender()

  before {
    client = new Client(encoder = Encoder, sender = s, asynchronous = false)
  }

  after {
    client.shutdown
  }

  "Client" should "sample things" in {

    client.enqueue(Metric(name = "foobar", value = "1.0", metricType = "g"), sampleRate = 0.0)
    s.getBuffer.size should be (0)
  }

  it should "bypass the sampler and send it anyway" in {
    client.enqueue(Metric(name = "foobar", value = "1.0", metricType = "g"), sampleRate = 0.0, bypassSampler = true)
    s.getBuffer.size should be (1)
  }
}
