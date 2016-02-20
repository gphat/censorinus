import org.scalatest._
import scala.collection.mutable.ArrayBuffer
import github.gphat.censorinus.{Metric,MetricSender,DogStatsDClient}
import github.gphat.censorinus.statsd.Encoder

class DogStatsDClientSpec extends FlatSpec with Matchers {

  val s = new TestSender()

  "DogStatsDClient" should "deal with gauges" in {
    val client = new DogStatsDClient(flushInterval = 1000)
    client.gauge("foobar", value = 1.0, tags = Seq("foo:bar"))
    val m = client.getQueue.poll
    m.name should be ("foobar")
    m.value should be ("1.00000000")
    m.metricType should be ("g")
    m.tags should be (Seq("foo:bar"))
    client.shutdown
  }

  it should "deal with counters" in {
    val client = new DogStatsDClient(flushInterval = 1000)
    client.counter("foobar", 1.0, tags = Seq("foo:bar"))
    val m = client.getQueue.poll
    m.name should be ("foobar")
    m.value should be ("1.00000000")
    m.metricType should be ("c")
    m.tags should be (Seq("foo:bar"))
    client.shutdown
  }

  it should "deal with increments" in {
    val client = new DogStatsDClient(flushInterval = 1000)
    client.increment("foobar", tags = Seq("foo:bar"))
    val m = client.getQueue.poll
    m.name should be ("foobar")
    m.value should be ("1.00000000")
    m.metricType should be ("c")
    m.tags should be (Seq("foo:bar"))
    client.shutdown
  }

  it should "deal with decrements" in {
    val client = new DogStatsDClient(flushInterval = 1000)
    client.increment("foobar", tags = Seq("foo:bar"))
    val m = client.getQueue.poll
    m.name should be ("foobar")
    m.value should be ("1.00000000")
    m.metricType should be ("c")
    m.tags should be (Seq("foo:bar"))
    client.shutdown
  }

  it should "deal with histograms" in {
    val client = new DogStatsDClient(flushInterval = 1000)
    client.histogram("foobar", 1.0, tags = Seq("foo:bar"))
    val m = client.getQueue.poll
    m.name should be ("foobar")
    m.value should be ("1.00000000")
    m.metricType should be ("h")
    m.tags should be (Seq("foo:bar"))
    client.shutdown
  }

  it should "deal with meters" in {
    val client = new DogStatsDClient(flushInterval = 1000)
    client.meter("foobar", 1.0, tags = Seq("foo:bar"))
    val m = client.getQueue.poll
    m.name should be ("foobar")
    m.value should be ("1.00000000")
    m.metricType should be ("m")
    m.tags should be (Seq("foo:bar"))
    client.shutdown
  }

  it should "deal with sets" in {
    val client = new DogStatsDClient(flushInterval = 1000)
    client.set("foobar", "fart", tags = Seq("foo:bar"))
    val m = client.getQueue.poll
    m.name should be ("foobar")
    m.value should be ("fart")
    m.metricType should be ("s")
    m.tags should be (Seq("foo:bar"))
    client.shutdown
  }

  it should "deal with big doubles" in {
    val client = new DogStatsDClient(flushInterval = 1000)
    client.meter("foobar", 1.01010101010101010101)
    val m = client.getQueue.poll
    m.name should be ("foobar")
    m.value should be ("1.01010101")
    client.shutdown
  }
}
