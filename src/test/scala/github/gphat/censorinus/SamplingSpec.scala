package github.gphat.censorinus

import org.scalatest._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import github.gphat.censorinus.statsd.Encoder

class SamplingSpec extends AnyFlatSpec with Matchers {

  val s = new TestSender()

  "Client" should "sample things" in {
    val client = new Client(encoder = Encoder, sender = s, asynchronous = false)
    client.enqueue(CounterMetric(name = "foobar", value = 1.0), sampleRate = 0.0)
    s.buffer.size should be (0)
    client.shutdown
  }

  it should "bypass the sampler and send it anyway" in {
    val client = new Client(encoder = Encoder, sender = s, asynchronous = false)
    client.enqueue(CounterMetric(name = "foobar", value = 1.0), sampleRate = 0.0, bypassSampler = true)
    s.buffer.size should be (1)
    client.shutdown
  }

  "DogStatsD Client" should "sample things" in {
    val client = new DogStatsDClient(prefix = "poop", defaultSampleRate = 0.0)
    client.counter("foobar", value = 1.0)
    client.decrement("foobar")
    client.increment("foobar")
    client.gauge("foobar", value = 1.0)
    client.histogram("foobar", value = 1.0)
    client.set("foobar", value = "fart")
    client.timer("foobar", milliseconds = 1.0)
    client.queue.size should be (0)
    client.shutdown
  }

  "StatsD Client" should "sample things" in {
    val client = new StatsDClient(prefix = "poop", defaultSampleRate = 0.0)
    client.counter("foobar", value = 1.0)
    client.decrement("foobar")
    client.increment("foobar")
    client.gauge("foobar", value = 1.0)
    client.meter("foobar", value = 1.0)
    client.set("foobar", value = "fart")
    client.timer("foobar", milliseconds = 1.0)
    client.queue.size should be (0)
    client.shutdown
  }
}
