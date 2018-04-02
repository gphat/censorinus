package github.gphat.censorinus.statsd

import github.gphat.censorinus._
import java.text.DecimalFormat

/** A Metric to String encoder for StatsD protocol.
  * @see See [[https://github.com/b/statsd_spec]] for full spec
  */
object Encoder extends MetricEncoder {

  val format = new DecimalFormat("0.################")

  def encode(metric: Metric): Option[String] = metric match {
    case sm: SampledMetric =>
      val sb = new StringBuilder()
      encodeBaseMetric(sb, metric)
      encodeSampleRate(sb, sm.sampleRate)
      Some(sb.toString)

    case nm: NumericMetric if(nm.value.isInfinite || nm.value.isNaN) =>
      None

    case _: Metric =>
      Some(encodeSimpleMetric(metric))

    case _ =>
      None
  }

  // Encodes the initial prefix used by all metrics.
  private def encodeBaseMetric(sb: StringBuilder, metric: Metric): Unit = {
    sb.append(metric.name)
    sb.append(':')
    val finalValue = metric match {
      // This is the only string based-metric
      case nm: NumericMetric => format.format(nm.value)
      case sm: StringMetric => sm.value
    }
    sb.append(finalValue)
    sb.append('|')
    val metricType = metric match {
      case _: CounterMetric => "c"
      case _: GaugeMetric => "g"
      case _: MeterMetric => "m"
      case _: SetMetric => "s"
      case _: TimerMetric => "ms"
    }
    val _ = sb.append(metricType)
  }

  // Encodes the sample rate, so that counters are adjusted appropriately.
  def encodeSampleRate(sb: StringBuilder, sampleRate: Double): Unit = {
    if(sampleRate < 1.0) {
      sb.append("|@")
      val _ = sb.append(format.format(sampleRate))
    }
  }

  // Encodes the base metric and tags only. This covers most metrics.
  private def encodeSimpleMetric(metric: Metric): String = {
    val sb = new StringBuilder()
    encodeBaseMetric(sb, metric)
    sb.toString
  }
}
