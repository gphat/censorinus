package github.gphat.censorinus.statsd

import github.gphat.censorinus.Metric

/** A Metric to String encoder for StatsD protocol.
  * @see See [[https://github.com/b/statsd_spec]] for full spec
  */
object Encoder {

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

  def encodeTimer(metric: Metric): String = {
    s"${metric.name}:${metric.value.toString}|ms"
  }
}
