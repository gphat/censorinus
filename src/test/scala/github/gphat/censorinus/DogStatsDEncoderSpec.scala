package github.gphat.censorinus

import org.scalatest._
import github.gphat.censorinus.dogstatsd.Encoder

class DogStatsDEncoderSpec extends FlatSpec with Matchers {

  "DogStatsD Encoder" should "encode gauges" in {

    val g = Metric(name = "foobar", value = "1.0", metricType = "g", tags = Array("foo:bar"))
    Encoder.encode(g).get should be ("foobar:1.0|g|#foo:bar")
  }

  it should "encode counters" in {
    val m = Metric(name = "foobar", value = "1.0", metricType = "c")
    Encoder.encode(m).get should be ("foobar:1.0|c")

    // Counter with optional sample rate
    val m1 = Metric(name = "foobar", value = "1.0", metricType = "c", sampleRate = 0.5)
    Encoder.encode(m1).get should be ("foobar:1.0|c|@0.5000")
  }

  it should "encode timers" in {
    val m = Metric(name = "foobar", value = "1.0", metricType = "ms")
    Encoder.encode(m).get should be ("foobar:1.0|ms")

    // Counter with optional sample rate
    val m1 = Metric(name = "foobar", value = "1.0", metricType = "ms", sampleRate = 0.5)
    Encoder.encode(m1).get should be ("foobar:1.0|ms|@0.5000")
  }

  it should "encode histograms" in {
    val m = Metric(name = "foobar", value = "1.0", metricType = "h")
    Encoder.encode(m).get should be ("foobar:1.0|h")

    // Counter with optional sample rate
    val m1 = Metric(name = "foobar", value = "1.0", metricType = "h", sampleRate = 0.5)
    Encoder.encode(m1).get should be ("foobar:1.0|h|@0.5000")
  }

  it should "encode meters" in {
    val m = Metric(name = "foobar", value = "1.0", metricType = "m")
    Encoder.encode(m).get should be ("foobar:1.0|m")
  }

  it should "encode sets" in {
    val m = Metric(name = "foobar", value = "fart", metricType = "s")
    Encoder.encode(m).get should be ("foobar:fart|s")
  }

  it should "encode counters with sample rate" in {
    val m = Metric(name = "foobar", value = "1.0", sampleRate = 0.5, metricType = "c")
    Encoder.encode(m).get should be ("foobar:1.0|c|@0.5000")
  }
}
