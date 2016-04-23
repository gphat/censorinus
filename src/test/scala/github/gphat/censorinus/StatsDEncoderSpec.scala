package github.gphat.censorinus

import org.scalatest._
import github.gphat.censorinus.statsd.Encoder

class StatsDEncoderSpec extends FlatSpec with Matchers {

  "StatsD Encoder" should "encode gauges" in {

    val g = GaugeMetric(name = "foobar", value = 1.0)
    Encoder.encode(g).get should be ("foobar:1|g")
  }

  it should "encode counters" in {
    val m = CounterMetric(name = "foobar", value = 1.0)
    Encoder.encode(m).get should be ("foobar:1|c")

    // Counter with optional sample rate
    val m1 = CounterMetric(name = "foobar", value = 1.0, sampleRate = 0.5)
    Encoder.encode(m1).get should be ("foobar:1|c|@0.5")
  }

  it should "encode timers" in {
    val m = TimerMetric(name = "foobar", value = 1.0)
    Encoder.encode(m).get should be ("foobar:1|ms")
  }

  it should "encode meters" in {
    val m = MeterMetric(name = "foobar", value = 1.0)
    Encoder.encode(m).get should be ("foobar:1|m")
  }

  it should "encode sets" in {
    val m = SetMetric(name = "foobar", value = "fart")
    Encoder.encode(m).get should be ("foobar:fart|s")
  }

  it should "encode counters with sample rate" in {
    val m = CounterMetric(name = "foobar", value = 1.0, sampleRate = 0.5)
    Encoder.encode(m).get should be ("foobar:1|c|@0.5")
  }
}
