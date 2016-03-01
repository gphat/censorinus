package github.gphat.censorinus.statsd

import github.gphat.censorinus._

/** A Metric to String encoder for StatsD protocol.
  * @see See [[https://github.com/b/statsd_spec]] for full spec
  */
object Encoder extends MetricEncoder {

  def encode(metric: Metric): Option[String] = metric.metricType match {
    case "c" => Some(encodeCounter(metric))
    case "g" => Some(encodeGauge(metric))
    case "h" => Some(encodeTimer(metric)) // StatsD doesn't support histograms, use timer instead?
    case "m" => Some(encodeMeter(metric))
    case "ms" => Some(encodeTimer(metric))
    case "s" => Some(encodeSet(metric))
    case _ => None
  }

  def encodeCounter(metric: Metric): String = {
    s"${metric.name}:${metric.value}|c${encodeSampleRate(metric)}"
  }

  def encodeGauge(metric: Metric): String = {
    s"${metric.name}:${metric.value}|g"
  }

  def encodeMeter(metric: Metric): String = {
    s"${metric.name}:${metric.value}|m"
  }

  def encodeSampleRate(metric: Metric): String = {
    if(metric.sampleRate == 1.0) {
      ""
    } else {
      s"@${"%.4f".format(metric.sampleRate)}"
    }
  }

  def encodeSet(metric: Metric): String = {
    s"${metric.name}:${metric.value}|s"
  }

  def encodeTimer(metric: Metric): String = {
    s"${metric.name}:${metric.value}|ms"
  }
}
