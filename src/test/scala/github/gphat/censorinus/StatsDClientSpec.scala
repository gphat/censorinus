package github.gphat.censorinus

import org.scalatest._
import scala.collection.mutable.ArrayBuffer
import github.gphat.censorinus.statsd.Encoder

class StatsDClientSpec extends FlatSpec with Matchers with BeforeAndAfter {

  var client: StatsDClient = null

  before {
    client = new StatsDClient(prefix = "poop")
    // SOOOOOOOOoooooo hacky, but this will ensure the worker thread doesn't
    // steal our metrics before we can read them.
    client.shutdown
  }

  "StatsDClient" should "deal with gauges" in {
    client.gauge("foobar", 1.0)
    val m = client.queue.poll
    m shouldBe a [GaugeMetric]
    val g = m.asInstanceOf[GaugeMetric]
    g.name should be ("poop.foobar")
    g.value should be (1.00000000)
  }

  it should "deal with counters" in {
    client.counter("foobar", 1.0)
    val m = client.queue.poll
    m shouldBe a [CounterMetric]
    val c = m.asInstanceOf[CounterMetric]
    c.name should be ("poop.foobar")
    c.value should be (1.0)
  }

  it should "deal with increments" in {
    client.increment("foobar")
    val m = client.queue.poll
    m shouldBe a [CounterMetric]
    val c = m.asInstanceOf[CounterMetric]
    c.name should be ("poop.foobar")
    c.value should be (1.0)
  }

  it should "deal with decrements" in {
    client.increment("foobar")
    val m = client.queue.poll
    m shouldBe a [CounterMetric]
    val c = m.asInstanceOf[CounterMetric]
    c.name should be ("poop.foobar")
    c.value should be (1.0)
  }

  it should "deal with meters" in {
    client.meter("foobar", 1.0)
    val m = client.queue.poll
    m shouldBe a [MeterMetric]
    val mm = m.asInstanceOf[MeterMetric]
    mm.name should be ("poop.foobar")
    mm.value should be (1.0)
  }

  it should "deal with sets" in {
    client.set("foobar", "fart")
    val m = client.queue.poll
    m shouldBe a [SetMetric]
    val s = m.asInstanceOf[SetMetric]
    s.name should be ("poop.foobar")
    s.value should be ("fart")
  }

  it should "deal with big doubles" in {
    client.meter("foobar", 1.01010101010101010101)
    val m = client.queue.poll
    m shouldBe a [MeterMetric]
    val mm = m.asInstanceOf[MeterMetric]
    mm.name should be ("poop.foobar")
    mm.value should be (1.01010101010101010101)
  }
}
