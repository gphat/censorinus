package github.gphat.censorinus.dogstatsd

import github.gphat.censorinus.{Metric,MetricEncoder}

/** A Metric to String encoder for DogStatsD protocol.
  * @see See [[http://docs.datadoghq.com/guides/dogstatsd/#datagram-format]] for full spec
  */
object Encoder extends MetricEncoder {

  def encode(metric: Metric): String = metric.metricType match {
    case "c" => encodeCounter(metric)
    case "g" => encodeGauge(metric)
    case "h" => encodeHistogram(metric)
    case "m" => encodeMeter(metric)
    case "ms" => encodeTimer(metric)
    case "s" => encodeSet(metric)
    case _ => "" // TODO Complain!
  }

  def encodeCounter(metric: Metric): String = {
    s"${metric.name}:${metric.value}|c|${encodeSampleRate(metric)}${encodeTags(metric)}"
  }

  def encodeGauge(metric: Metric): String = {
    s"${metric.name}:${metric.value}|g|${encodeTags(metric)}"
  }

  def encodeHistogram(metric: Metric): String = {
    s"${metric.name}:${metric.value}|h|${encodeTags(metric)}"
  }

  def encodeMeter(metric: Metric): String = {
    s"${metric.name}:${metric.value}|m|${encodeTags(metric)}"
  }

  def encodeSampleRate(metric: Metric): String = {
    if(metric.sampleRate == 1.0) {
      ""
    } else {
      s"@${df.format(metric.sampleRate)}"
    }
  }

  def encodeSet(metric: Metric): String = {
    s"${metric.name}:${metric.value}|s|${encodeTags(metric)}"
  }

  def encodeTags(metric: Metric): String = {
    // TODO Make sure this is well formed and such
    if(metric.tags.isEmpty) {
      ""
    } else {
      "#" + metric.tags.mkString(",")
    }
  }

  def encodeTimer(metric: Metric): String = {
    s"${metric.name}:${metric.value.toString}|ms|${encodeTags(metric)}"
  }
}
