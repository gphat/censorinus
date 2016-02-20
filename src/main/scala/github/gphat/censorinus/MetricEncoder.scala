package github.gphat.censorinus

import java.text.DecimalFormat
import github.gphat.censorinus._

/** A Metric to String encoder
  */
trait MetricEncoder {

  def encode(metric: Metric): String
}
