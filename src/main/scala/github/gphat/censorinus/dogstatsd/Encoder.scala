package github.gphat.censorinus.dogstatsd

import github.gphat.censorinus.Metric

/** A Metric to String encoder for DogStatsD protocol.
  * @see See [[http://docs.datadoghq.com/guides/dogstatsd/#datagram-format]] for full spec
  */
object Encoder {

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
    s"${metric.name}:${metric.value.toString}|c${encodeSampleRate(metric)}"
  }

  def encodeGauge(metric: Metric): String = {
    s"${metric.name}:${metric.value.toString}|g"
  }

  def encodeHistogram(metric: Metric): String = {
    s"${metric.name}:${metric.value.toString}|h"
  }

  def encodeMeter(metric: Metric): String = {
    s"${metric.name}:${metric.value.toString}|m"
  }

  def encodeSampleRate(metric: Metric): String = {
    if(metric.sampleRate == 1.0) {
      ""
    } else {
      s"@${metric.sampleRate.toString}"
    }
  }

  def encodeSet(metric: Metric): String = {
    s"${metric.name}:${metric.value}|s"
  }

  def encodeTags(metric: Metric): String = {
    // TODO Make sure this is well formed and such
    metric.tags.mkString(",")
  }

  def encodeTimer(metric: Metric): String = {
    s"${metric.name}:${metric.value.toString}|ms"
  }
}
