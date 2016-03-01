package github.gphat.censorinus

import java.text.DecimalFormat
import github.gphat.censorinus._

/** A Metric to String encoder
  */
trait MetricEncoder {

  /**
   * Returns an encoded version of the metirc, suitable for sending over the
   * wire. If the metric type is not supported, then `None` is returned.
   */
  def encode(metric: Metric): Option[String]
}
