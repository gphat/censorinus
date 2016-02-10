import org.scalatest._
import github.gphat.censorinus.Metric
import github.gphat.censorinus.statsd.Encoder

class EncoderSpec extends FlatSpec with Matchers {

  "Encoder" should "encode gauges" in {

    val g = Metric(name = "foobar", value = 1.0, metricType = "g")
    Encoder.encodeGauge(g) should be ("foobar:1.0|g")
  }

  it should "encode counters" in {
    val m = Metric(name = "foobar", value = 1.0, metricType = "c")
    Encoder.encodeCounter(m) should be ("foobar:1.0|c")

    // Counter with optional sample rate
    val m1 = Metric(name = "foobar", value = 1.0, metricType = "c", sampleRate = 0.5)
    Encoder.encodeCounter(m1) should be ("foobar:1.0|c@0.5")
  }

  it should "encode timers" in {
    val m = Metric(name = "foobar", value = 1.0, metricType = "ms")
    Encoder.encodeTimer(m) should be ("foobar:1.0|ms")
  }

  it should "encode histograms" in {
    val m = Metric(name = "foobar", value = 1.0, metricType = "h")
    Encoder.encodeHistogram(m) should be ("foobar:1.0|h")
  }

  it should "encode meters" in {
    val m = Metric(name = "foobar", value = 1.0, metricType = "m")
    Encoder.encodeMeter(m) should be ("foobar:1.0|m")
  }
}
