package github.gphat.censorinus.ssf

import github.gphat.censorinus._
import _root_.ssf.Sample

/** A Metric to String encoder for DogStatsD protocol.
  * @see See [[http://docs.datadoghq.com/guides/dogstatsd/#datagram-format]] for full spec
  */
object Encoder extends MetricEncoder {

  def encode(metric: Metric): Option[String] = {

    val sample = Sample.SSFSample.newBuilder()
      .setName(metric.name)
      .build
    System.out.println(sample.toByteArray)
    // val sample = SSFSample(
    //   name = metric.name
    // )
    // sample.toByteArray()
    return None
  }
}
