package github.gphat.censorinus

import github.gphat.censorinus.dogstatsd.Encoder

import java.util.concurrent._
import scala.util.Random

object DogStatsDClient {
  val OK = 0
  val WARNING = 1
  val CRITICAL = 2
  val UNKNOWN = 3
}

/** A DStatsD client! You should create one of these and reuse it across
  * your application.
  * @constructor Creates a new client instance
  * @param hostname the host to send metrics to, defaults to localhost
  * @param port the port to send metrics to, defaults to 8125
  * @param prefix A prefix to add to all metric names. A period will be added to the end, resulting in prefix.metricname.
  * @param defaultSampleRate A sample rate default to be used for all metric methods. Defaults to 1.0
  * @param asynchronous True if you want the client to asynch, false for blocking!
  * @param floatFormat Allows control of the precision of the double output via strings from [[java.util.Formatter]]. Defaults to "%.8f".
  */
class DogStatsDClient(
  hostname: String = "localhost",
  port: Int = MetricSender.DEFAULT_STATSD_PORT,
  prefix: String = "",
  defaultSampleRate: Double = 1.0,
  asynchronous: Boolean = true,
  maxQueueSize: Option[Int] = None
) extends Client(
  sender = new UDPSender(hostname = hostname, port = port),
  encoder = Encoder,
  prefix = prefix,
  defaultSampleRate = defaultSampleRate,
  asynchronous = asynchronous,
  maxQueueSize = maxQueueSize
) {
  /** Emit a counter metric.
    * @param name The name of the metric
    * @param value The value of the metric, or how much to increment by
    * @param sampleRate The rate at which to sample this metric.
    * @param bypassSampler If true, the metric will always be passed through, but the sample rate will be included in the emitted metric.
    *                      This is useful for when you occasionally do your own sampling.
    */
  def counter(
    name: String,
    value: Double,
    sampleRate: Double = defaultSampleRate,
    tags: Seq[String] = Seq.empty,
    bypassSampler: Boolean = false
  ): Unit = enqueue(
    CounterMetric(name = makeName(name), value = value, sampleRate = sampleRate, tags = tags),
    sampleRate,
    bypassSampler
  )

  /** Emit a decrement metric.
    * @param name The name of the metric
    * @param value The value of the metric, or how much to decrement by. Defaults to -1
    * @param sampleRate The rate at which to sample this metric.
    * @param bypassSampler If true, the metric will always be passed through, but the sample rate will be included in the emitted metric.
    *                      This is useful for when you occasionally do your own sampling.
    */
  def decrement(
    name: String,
    value: Double = 1,
    sampleRate: Double = defaultSampleRate,
    tags: Seq[String] = Seq.empty,
    bypassSampler: Boolean = false
  ): Unit = enqueue(
    CounterMetric(name = makeName(name), value = value, sampleRate = sampleRate, tags = tags),
    sampleRate,
    bypassSampler
  )

  /** Emit a gauge metric.
    * @param name The name of the metric
    * @param value The value of the metric, or current value of the gauge
    * @param sampleRate The rate at which to sample this metric.
    * @param bypassSampler If true, the metric will always be passed through, but the sample rate will be included in the emitted metric.
    *                      This is useful for when you occasionally do your own sampling.
    */
  def gauge(
    name: String,
    value: Double,
    sampleRate: Double = defaultSampleRate,
    tags: Seq[String] = Seq.empty,
    bypassSampler: Boolean = false
  ): Unit = enqueue(
    GaugeMetric(name = makeName(name), value = value, tags = tags),
    sampleRate,
    bypassSampler
  )

  /** Emit a histogram metric.
    * @param name The name of the metric
    * @param value The value of the metric, or a value to be sampled for the histogram
    * @param sampleRate The rate at which to sample this metric.
    * @param bypassSampler If true, the metric will always be passed through, but the sample rate will be included in the emitted metric.
    *                      This is useful for when you occasionally do your own sampling.
    */
  def histogram(
    name: String,
    value: Double,
    sampleRate: Double = defaultSampleRate,
    tags: Seq[String] = Seq.empty,
    bypassSampler: Boolean = false
  ): Unit = enqueue(
    HistogramMetric(name = makeName(name), value = value, sampleRate = sampleRate, tags = tags),
    sampleRate,
    bypassSampler
  )

  /** Emit an increment metric.
    * @param name The name of the metric
    * @param value The value of the metric, or the amount to increment by. Defaults to 1
    * @param sampleRate The rate at which to sample this metric.
    * @param bypassSampler If true, the metric will always be passed through, but the sample rate will be included in the emitted metric.
    *                      This is useful for when you occasionally do your own sampling.
    */
  def increment(
    name: String, value: Double = 1,
    sampleRate: Double = defaultSampleRate,
    tags: Seq[String] = Seq.empty,
    bypassSampler: Boolean = false
  ): Unit = enqueue(
    CounterMetric(name = makeName(name), value = value, sampleRate = sampleRate, tags = tags),
    sampleRate,
    bypassSampler
  )

  /** Emit a service check.
   *
   */
  def serviceCheck(
    name: String,
    status: Int,
    timestamp: Option[Long] = None,
    hostname: Option[String] = None,
    message: Option[String] = None,
    tags: Seq[String] = Seq.empty
  ): Unit = enqueue(
    ServiceCheckMetric(
      name = makeName(name),
      status = status,
      message = message,
      timestamp = timestamp,
      hostname = hostname,
      tags = tags
    )
  )

  /** Emit a set metric.
   * @param name The name of the metric
   * @param value The item to add to the set
   */
  def set(
    name: String,
    value: String,
    tags: Seq[String] = Seq.empty
  ): Unit = enqueue(
    SetMetric(name = makeName(name), value = value, tags = tags)
  )

  /** Emit a timer metric.
    * @param name The name of the metric
    * @param value The value of the timer in milliseconds
    * @param sampleRate The rate at which to sample this metric.
    * @param bypassSampler If true, the metric will always be passed through, but the sample rate will be included in the emitted metric.
    *                      This is useful for when you occasionally do your own sampling.
    */
  def timer(
    name: String,
    milliseconds: Double,
    sampleRate: Double = defaultSampleRate,
    tags: Seq[String] = Seq.empty,
    bypassSampler: Boolean = false
  ): Unit = enqueue(
    TimerMetric(name = makeName(name), value = milliseconds, sampleRate = sampleRate, tags = tags),
    sampleRate,
    bypassSampler
  )
}
