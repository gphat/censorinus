package github.gphat.censorinus

import github.gphat.censorinus._

/** A Metric to String encoder
  */
trait MetricEncoder {

  def encode(metric: Metric): String
}
