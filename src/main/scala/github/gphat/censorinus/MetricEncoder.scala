package github.gphat.censorinus

import java.text.DecimalFormat
import github.gphat.censorinus._

/** A Metric to String encoder
  */
trait MetricEncoder {

  val df = new DecimalFormat("0.0")
  df.setMaximumFractionDigits(8)

  def encode(metric: Metric): String
}
