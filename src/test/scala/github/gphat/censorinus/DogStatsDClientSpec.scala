package github.gphat.censorinus

import org.scalatest._
import scala.collection.mutable.ArrayBuffer
import scala.util.matching.Regex
import github.gphat.censorinus.statsd.Encoder

class DogStatsDClientSpec extends FlatSpec with Matchers with BeforeAndAfter {

  var client: DogStatsDClient = null

  before {
    client = new DogStatsDClient(prefix = "poop")
    // SOOOOOOOOoooooo hacky, but this will ensure the worker thread doesn't
    // steal our metrics before we can read them.
    client.shutdown
  }

  "DogStatsDClient" should "deal with gauges" in {
    client.gauge("foobar", value = 1.0, tags = Seq("foo:bar"))
    val m = client.queue.poll
    m.name should be ("poop.foobar")
    m.tags should be (Seq("foo:bar"))
    m shouldBe a [GaugeMetric]
    val gm = m.asInstanceOf[GaugeMetric]
    gm.value should be (1.0)
  }

  it should "deal with events" in {
    client.event("foobar", "i frozzled the wozjob", tags = Seq("foo:bar"))
    val m = client.queue.poll
    m shouldBe a [EventMetric]
    val c = m.asInstanceOf[EventMetric]
    c.name should be ("foobar")
    c.text should be ("i frozzled the wozjob")
    c.tags should be (Seq("foo:bar"))
  }

  it should "deal with service checks" in {
    client.serviceCheck("foobar", DogStatsDClient.SERVICE_CHECK_OK, tags = Seq("foo:bar"))
    val m = client.queue.poll
    m shouldBe a [ServiceCheckMetric]
    val c = m.asInstanceOf[ServiceCheckMetric]
    c.name should be ("poop.foobar")
    c.tags should be (Seq("foo:bar"))
  }

  it should "deal with counters" in {
    client.counter("foobar", 1.0, tags = Seq("foo:bar"))
    val m = client.queue.poll
    m shouldBe a [CounterMetric]
    val c = m.asInstanceOf[CounterMetric]
    c.name should be ("poop.foobar")
    c.value should be (1.0)
    c.tags should be (Seq("foo:bar"))
  }

  it should "deal with increments" in {
    client.increment("foobar", tags = Seq("foo:bar"))
    val m = client.queue.poll
    m shouldBe a [CounterMetric]
    val c = m.asInstanceOf[CounterMetric]
    c.name should be ("poop.foobar")
    c.value should be (1.0)
    c.tags should be (Seq("foo:bar"))
  }

  it should "deal with decrements" in {
    client.increment("foobar", tags = Seq("foo:bar"))
    val m = client.queue.poll
    m shouldBe a [CounterMetric]
    val c = m.asInstanceOf[CounterMetric]
    c.name should be ("poop.foobar")
    c.value should be (1.0)
    c.tags should be (Seq("foo:bar"))
  }

  it should "deal with histograms" in {
    client.histogram("foobar", 1.0, tags = Seq("foo:bar"))
    val m = client.queue.poll
    m shouldBe a [HistogramMetric]
    val c = m.asInstanceOf[HistogramMetric]
    c.name should be ("poop.foobar")
    c.value should be (1.0)
    c.tags should be (Seq("foo:bar"))
  }

  it should "deal with sets" in {
    client.set("foobar", "fart", tags = Seq("foo:bar"))
    val m = client.queue.poll
    m shouldBe a [SetMetric]
    val s = m.asInstanceOf[SetMetric]
    s.name should be ("poop.foobar")
    s.value should be ("fart")
    s.tags should be (Seq("foo:bar"))
  }

  it should "cleanse metric names" in {
    val client2 = new DogStatsDClient(prefix = "poop", metricRegex = Some(new Regex("[^a-zA-Z_\\.]")))
    val result = client2.makeName("this.is.foo-bar")
    result should be ("poop.this.is.foo_bar")
    client2.shutdown
  }
}
