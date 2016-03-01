package github.gphat.censorinus.dogstatsd

import github.gphat.censorinus.{Metric,MetricEncoder}

/** A Metric to String encoder for DogStatsD protocol.
  * @see See [[http://docs.datadoghq.com/guides/dogstatsd/#datagram-format]] for full spec
  */
object Encoder extends MetricEncoder {

  def encode(metric: Metric): Option[String] = metric.metricType match {
    case "c" | "h" | "ms" =>
      val sb = new StringBuilder()
      encodeBaseMetric(sb, metric)
      encodeSampleRate(sb, metric.sampleRate)
      encodeTags(sb, metric.tags)
      Some(sb.toString)

    case "g" | "m" | "s" =>
      Some(encodeSimpleMetric(metric))

    case _ =>
      None
  }

  // Encodes the initial prefix used by all metrics.
  private def encodeBaseMetric(sb: StringBuilder, metric: Metric): Unit = {
    sb.append(metric.name)
    sb.append(':')
    sb.append(metric.value)
    sb.append('|')
    sb.append(metric.metricType)
  }

  // Encodes the datadog specific tags.
  private def encodeTags(sb: StringBuilder, tags: Seq[String]): Unit = {
    if (!tags.isEmpty) {
      sb.append("|#")
      val it = tags.iterator
      var first = true
      while (it.hasNext) {
        if (!first) sb.append(",")
        sb.append(it.next)
        first = false
      }
    }
  }

  // Encodes the sample rate, so that counters are adjusted appropriately.
  def encodeSampleRate(sb: StringBuilder, sampleRate: Double): Unit = {
    if(sampleRate != 1.0) {
      sb.append("|@%.4f".format(sampleRate))
    }
  }

  // Encodes the base metric and tags only. This covers most metrics.
  private def encodeSimpleMetric(metric: Metric): String = {
    val sb = new StringBuilder()
    encodeBaseMetric(sb, metric)
    encodeTags(sb, metric.tags)
    sb.toString
  }
}
