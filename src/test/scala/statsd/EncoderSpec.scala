import org.scalatest._
import github.gphat.censorinus.Metric
import github.gphat.censorinus.statsd.Encoder

class EncoderSpec extends FlatSpec with Matchers {

  "Encoder" should "encode gauges" in {

    val g = Metric(name = "foobar", value = 1.0, metricType = "g")
    Encoder.encodeGauge(g) should be ("foobar:1.0|g")
  }

  "Encoder" should "encode counters" in {

    val g = Metric(name = "foobar", value = 1.0, metricType = "c")
    Encoder.encodeCounter(g) should be ("foobar:1.0|c")

    val g1 = Metric(name = "foobar", value = 1.0, metricType = "c", sampleRate = 0.5)
    Encoder.encodeCounter(g1) should be ("foobar:1.0|c@0.5")

    val g2 = Metric(name = "foobar", value = 1.0, metricType = "ms")
    Encoder.encodeTimer(g2) should be ("foobar:1.0|ms")
  }

  // it should "silently handle candidate failures" in {
  //   val ex = new Experiment[String](name = Some("better_string"), control = slowOK, candidate = fastFail)
  //   whenReady(ex.perform)({ res =>
  //     res should be("OK")
  //   })
  // }
}
