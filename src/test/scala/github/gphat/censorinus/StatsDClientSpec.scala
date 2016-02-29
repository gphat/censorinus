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
    client.shutdown()
  }

  "StatsDClient" should "deal with gauges" in {
    client.gauge("foobar", 1.0)
    val m = client.queue.poll
    m.name should be ("poop.foobar")
    m.value should be ("1.00000000")
    m.metricType should be ("g")
  }

  it should "deal with counters" in {
    client.counter("foobar", 1.0)
    val m = client.queue.poll
    m.name should be ("poop.foobar")
    m.value should be ("1.00000000")
    m.metricType should be ("c")
  }

  it should "deal with increments" in {
    client.increment("foobar")
    val m = client.queue.poll
    m.name should be ("poop.foobar")
    m.value should be ("1.00000000")
    m.metricType should be ("c")
  }

  it should "deal with decrements" in {
    client.increment("foobar")
    val m = client.queue.poll
    m.name should be ("poop.foobar")
    m.value should be ("1.00000000")
    m.metricType should be ("c")
  }

  it should "deal with histograms" in {
    client.histogram("foobar", 1.0)
    val m = client.queue.poll
    m.name should be ("poop.foobar")
    m.value should be ("1.00000000")
    m.metricType should be ("h")
  }

  it should "deal with meters" in {
    client.meter("foobar", 1.0)
    val m = client.queue.poll
    m.name should be ("poop.foobar")
    m.value should be ("1.00000000")
    m.metricType should be ("m")
  }

  it should "deal with sets" in {
    client.set("foobar", "fart")
    val m = client.queue.poll
    m.name should be ("poop.foobar")
    m.value should be ("fart")
    m.metricType should be ("s")
  }

  it should "deal with big doubles" in {
    client.meter("foobar", 1.01010101010101010101)
    val m = client.queue.poll
    m.name should be ("poop.foobar")
    m.value should be ("1.01010101")
  }
}
