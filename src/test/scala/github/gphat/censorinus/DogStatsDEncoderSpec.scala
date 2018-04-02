package github.gphat.censorinus

import org.scalatest._
import github.gphat.censorinus.dogstatsd.Encoder

class DogStatsDEncoderSpec extends FlatSpec with Matchers {

  "DogStatsD Encoder" should "encode gauges" in {

    val g = GaugeMetric(name = "foobar", value = 1.0, tags = Array("foo:bar"))
    Encoder.encode(g).get should be ("foobar:1|g|#foo:bar")
  }

  it should "drop infinite values" in {
    val g1 = GaugeMetric(name = "foobar", value = java.lang.Double.NEGATIVE_INFINITY, tags = Array("foo:bar"))
    Encoder.encode(g1) shouldBe None

    val g2 = GaugeMetric(name = "foobar", value = java.lang.Double.POSITIVE_INFINITY, tags = Array("foo:bar"))
    Encoder.encode(g2) shouldBe None
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

    // Timer with optional sample rate
    val m1 = TimerMetric(name = "foobar", value = 1.0, sampleRate = 0.5)
    Encoder.encode(m1).get should be ("foobar:1|ms|@0.5")

    // Timer with float
    val m2 = TimerMetric(name = "foobar", value = 1.001)
    Encoder.encode(m2).get should be ("foobar:1.001|ms")
  }

  it should "encode histograms" in {
    val m = HistogramMetric(name = "foobar", value = 1.0)
    Encoder.encode(m).get should be ("foobar:1|h")

    // Histogram with optional sample rate
    val m1 = HistogramMetric(name = "foobar", value = 1.0, sampleRate = 0.5)
    Encoder.encode(m1).get should be ("foobar:1|h|@0.5")
  }

  it should "encode sets" in {
    val m = SetMetric(name = "foobar", value = "fart")
    Encoder.encode(m).get should be ("foobar:fart|s")
  }

  it should "encode service checks" in {
    val now = System.currentTimeMillis() / 1000L
    val m = ServiceCheckMetric(
      name = "foobar", status = DogStatsDClient.SERVICE_CHECK_OK, tags = Array("foo:bar"),
      hostname = Some("fart"), timestamp = Some(now), message = Some("wheeee")
    )
    Encoder.encode(m).get should be ("_sc|foobar|0|d:%d|h:fart|#foo:bar|m:wheeee".format(now))
  }

  it should "encode service checks with newlines" in {
    val now = System.currentTimeMillis() / 1000L
    val m = ServiceCheckMetric(
      name = "foobar", status = DogStatsDClient.SERVICE_CHECK_OK, tags = Array("foo:bar"),
      hostname = Some("fart"), timestamp = Some(now), message = Some("hello\nworld")
    )
    Encoder.encode(m).get should be ("_sc|foobar|0|d:%d|h:fart|#foo:bar|m:hello\\\\nworld".format(now))
  }

  it should "encode events" in {
    val now = System.currentTimeMillis() / 1000L
    val m = EventMetric(
      name = "foobar", text = "derp derp derp", tags = Array("foo:bar"),
      hostname = Some("fart"), timestamp = Some(now), aggregationKey = Some("agg_key"),
      priority = Some(DogStatsDClient.EVENT_PRIORITY_LOW),
      sourceTypeName = Some("user"),
      alertType = Some(DogStatsDClient.EVENT_ALERT_TYPE_ERROR)
    )
    Encoder.encode(m).get should be ("_e{6,14}:foobar|derp derp derp|d:%d|h:fart|k:agg_key|p:low|s:user|t:error|#foo:bar".format(now))
  }

  it should "encode events with newlines" in {
    val now = System.currentTimeMillis() / 1000L
    val m = EventMetric(
      name = "foobar", text = "derp derp\nderp", tags = Array("foo:bar"),
      hostname = Some("fart"), timestamp = Some(now), aggregationKey = Some("agg_key"),
      priority = Some(DogStatsDClient.EVENT_PRIORITY_LOW),
      sourceTypeName = Some("user"),
      alertType = Some(DogStatsDClient.EVENT_ALERT_TYPE_ERROR)
    )
    Encoder.encode(m).get should be ("_e{6,16}:foobar|derp derp\\\\nderp|d:%d|h:fart|k:agg_key|p:low|s:user|t:error|#foo:bar".format(now))
  }
}
