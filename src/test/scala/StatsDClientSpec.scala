import org.scalatest._
import scala.collection.mutable.ArrayBuffer
import github.gphat.censorinus.{Metric,MetricSender,StatsDClient}
import github.gphat.censorinus.statsd.Encoder

class StatsDClientSpec extends FlatSpec with Matchers {

  val s = new TestSender()

  "StatsDClient" should "deal with gauges" in {
    val client = new StatsDClient(flushInterval = 1000)
    client.gauge("foobar", 1.0)
    val m = client.getQueue.poll
    m.name should be ("foobar")
    m.value should be ("1.00000000")
    m.metricType should be ("g")
    client.shutdown
  }

  it should "deal with counters" in {
    val client = new StatsDClient(flushInterval = 1000)
    client.counter("foobar", 1.0)
    val m = client.getQueue.poll
    m.name should be ("foobar")
    m.value should be ("1.00000000")
    m.metricType should be ("c")
    client.shutdown
  }

  it should "deal with increments" in {
    val client = new StatsDClient(flushInterval = 1000)
    client.increment("foobar")
    val m = client.getQueue.poll
    m.name should be ("foobar")
    m.value should be ("1.00000000")
    m.metricType should be ("c")
    client.shutdown
  }

  it should "deal with decrements" in {
    val client = new StatsDClient(flushInterval = 1000)
    client.increment("foobar")
    val m = client.getQueue.poll
    m.name should be ("foobar")
    m.value should be ("1.00000000")
    m.metricType should be ("c")
    client.shutdown
  }

  it should "deal with histograms" in {
    val client = new StatsDClient(flushInterval = 1000)
    client.histogram("foobar", 1.0)
    val m = client.getQueue.poll
    m.name should be ("foobar")
    m.value should be ("1.00000000")
    m.metricType should be ("h")
    client.shutdown
  }

  it should "deal with meters" in {
    val client = new StatsDClient(flushInterval = 1000)
    client.meter("foobar", 1.0)
    val m = client.getQueue.poll
    m.name should be ("foobar")
    m.value should be ("1.00000000")
    m.metricType should be ("m")
    client.shutdown
  }

  it should "deal with sets" in {
    val client = new StatsDClient(flushInterval = 1000)
    client.set("foobar", "fart")
    val m = client.getQueue.poll
    m.name should be ("foobar")
    m.value should be ("fart")
    m.metricType should be ("s")
    client.shutdown
  }

  it should "deal with big doubles" in {
    val client = new StatsDClient(flushInterval = 1000)
    client.meter("foobar", 1.01010101010101010101)
    val m = client.getQueue.poll
    m.name should be ("foobar")
    m.value should be ("1.01010101")
    client.shutdown
  }
}
