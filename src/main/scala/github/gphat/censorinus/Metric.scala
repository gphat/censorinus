package github.gphat.censorinus

sealed trait Metric {
  def name: String
  def tags: Seq[String]
}

sealed trait NumericMetric {
  def value: Double
}

sealed trait SampledMetric {
  def sampleRate: Double
}

sealed trait StringMetric {
  def value: String
}

case class CounterMetric(
  name: String,
  value: Double,
  sampleRate: Double = 1.0,
  tags: Seq[String] = Seq.empty
) extends Metric with NumericMetric with SampledMetric

case class EventMetric(
  name: String,
  text: String,
  timestamp: Option[Long],
  hostname: Option[String],
  aggregationKey: Option[String],
  priority: Option[String],
  sourceTypeName: Option[String],
  alertType: Option[String],
  tags: Seq[String] = Seq.empty
) extends Metric

case class GaugeMetric(
  name: String,
  value: Double,
  tags: Seq[String] = Seq.empty
) extends Metric with NumericMetric

case class HistogramMetric(
  name: String,
  value: Double,
  sampleRate: Double = 1.0,
  tags: Seq[String] = Seq.empty
) extends Metric with NumericMetric with SampledMetric

case class MeterMetric(
  name: String,
  value: Double,
  tags: Seq[String] = Seq.empty
) extends Metric with NumericMetric

case class ServiceCheckMetric(
  name: String,
  status: Int,
  timestamp: Option[Long] = None,
  hostname: Option[String] = None,
  message: Option[String] = None,
  tags: Seq[String] = Seq.empty
) extends Metric

case class SetMetric(
  name: String,
  value: String,
  tags: Seq[String] = Seq.empty
) extends Metric with StringMetric

case class TimerMetric(
  name: String,
  value: Double,
  sampleRate: Double = 1.0,
  tags: Seq[String] = Seq.empty
) extends Metric with NumericMetric with SampledMetric
