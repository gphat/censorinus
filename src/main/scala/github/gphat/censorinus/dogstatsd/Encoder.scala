package github.gphat.censorinus.dogstatsd

import github.gphat.censorinus._
import java.text.DecimalFormat

/** A Metric to String encoder for DogStatsD protocol.
  * @see See [[http://docs.datadoghq.com/guides/dogstatsd/#datagram-format]] for full spec
  */
object Encoder extends MetricEncoder {

  val format = new DecimalFormat("0.################")

  def encode(metric: Metric): Option[String] = metric match {
    case sc: ServiceCheckMetric =>
      Some(encodeServiceCheck(sc))

    case em: EventMetric =>
      Some(encodeEvent(em))

    case nm: NumericMetric if(nm.value.isInfinite || nm.value.isNaN) =>
      // Note, we protect against infinity, or NaN which we'll drop via the
      // default case.
      None

    case sm: SampledMetric =>
      val sb = new StringBuilder()
      encodeBaseMetric(sb, metric)
      encodeSampleRate(sb, sm.sampleRate)
      encodeTags(sb, metric.tags)
      Some(sb.toString)

    case sm: StringMetric =>
      Some(encodeSimpleMetric(metric))

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
      case nm: NumericMetric => {
        System.out.println(nm.value)
        format.format(nm.value)
      }
      case sm: StringMetric => sm.value
    }
    sb.append(finalValue)
    sb.append('|')
    val metricType = metric match {
      case _: CounterMetric => "c"
      case _: GaugeMetric => "g"
      case _: HistogramMetric => "h"
      case _: SetMetric => "s"
      case _: TimerMetric => "ms"
    }
    val _ = sb.append(metricType)
  }

  def encodeEvent(sc: EventMetric): String = {
    val sb = new StringBuilder()
    val escapedText = escapeDogstatsd(sc.text)
    sb.append("_e{")
    sb.append(sc.name.length.toString)
    sb.append(",")
    sb.append(escapedText.length.toString)
    sb.append("}:")
    sb.append(sc.name)
    sb.append("|")
    sb.append(escapedText)
    sc.timestamp.foreach({ d =>
      sb.append("|d:")
      sb.append(d.toString)
    })
    sc.hostname.foreach({ h =>
      sb.append("|h:")
      sb.append(h)
    })
    sc.aggregationKey.foreach({ ak =>
      sb.append("|k:")
      sb.append(ak)
    })
    sc.priority.foreach({ p =>
      sb.append("|p:")
      sb.append(p)
    })
    sc.sourceTypeName.foreach({ stn =>
      sb.append("|s:")
      sb.append(stn)
    })
    sc.alertType.foreach({ at =>
      sb.append("|t:")
      sb.append(at)
    })
    encodeTags(sb, sc.tags)

    sb.toString
  }

  // Encodes the datadog specific tags.
  private def encodeTags(sb: StringBuilder, tags: Seq[String]): Unit = {
    if(!tags.isEmpty) {
      sb.append("|#")
      val it = tags.iterator
      var first = true
      while (it.hasNext) {
        if(!first) sb.append(",")
        sb.append(it.next)
        first = false
      }
    }
  }

  // Encodes the sample rate, so that counters are adjusted appropriately.
  def encodeSampleRate(sb: StringBuilder, sampleRate: Double): Unit = {
    if(sampleRate < 1.0) {
      sb.append("|@")
      val _ = sb.append(format.format(sampleRate))
    }
  }

  def encodeServiceCheck(sc: ServiceCheckMetric): String = {
    val sb = new StringBuilder()
    sb.append("_sc|")
    sb.append(sc.name)
    sb.append("|")
    sb.append(sc.status.toString)
    sc.timestamp.foreach({ d =>
      sb.append("|d:")
      sb.append(d.toString)
    })
    sc.hostname.foreach({ h =>
      sb.append("|h:")
      sb.append(h)
    })
    encodeTags(sb, sc.tags)
    sc.message.foreach({ m =>
      sb.append("|m:")
      sb.append(escapeDogstatsd(m))
    })
    sb.toString
  }

  private def escapeDogstatsd(value: String): String = {
    value.replace("\n", "\\\\n")
  }

  // Encodes the base metric and tags only. This covers most metrics.
  private def encodeSimpleMetric(metric: Metric): String = {
    val sb = new StringBuilder()
    encodeBaseMetric(sb, metric)
    encodeTags(sb, metric.tags)
    sb.toString
  }
}
