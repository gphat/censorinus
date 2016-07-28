package github.gphat.censorinus

import github.gphat.censorinus.dogstatsd.Encoder

import java.util.concurrent._
import scala.util.Random

object DogStatsDClient {
  val SERVICE_CHECK_OK = 0
  val SERVICE_CHECK_WARNING = 1
  val SERVICE_CHECK_CRITICAL = 2
  val SERVICE_CHECK_UNKNOWN = 3
  val EVENT_PRIORITY_LOW = "low"
  val EVENT_PRIORITY_NORMAL = "normal"
  val EVENT_ALERT_TYPE_ERROR = "error"
  val EVENT_ALERT_TYPE_INFO = "info"
  val EVENT_ALERT_TYPE_SUCCESS = "success"
  val EVENT_ALERT_TYPE_WARNING = "warning"
}

/** A DogStatsD client! You should create one of these and reuse it across
  * your application.
  * @constructor Creates a new client instance
  * @param hostname the host to send metrics to, defaults to localhost
  * @param port the port to send metrics to, defaults to 8125
  * @param prefix A prefix to add to all metric names. A period will be added to the end, resulting in prefix.metricname.
  * @param defaultSampleRate A sample rate default to be used for all metric methods. Defaults to 1.0
  * @param asynchronous True if you want the client to asynch, false for blocking!
  * @param maxQueueSize Maximum amount of metrics allowed to be queued at a time.
  * @param allowExceptions If false, any `SocketException`s will be swallowed silently
  */
class DogStatsDClient(
  hostname: String = "localhost",
  port: Int = MetricSender.DEFAULT_STATSD_PORT,
  prefix: String = "",
  defaultSampleRate: Double = 1.0,
  asynchronous: Boolean = true,
  maxQueueSize: Option[Int] = None,
  allowExceptions: Boolean = false
) extends Client(
  sender = new UDPSender(hostname = hostname, port = port, allowExceptions = allowExceptions),
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

  /** Emit an event.
   * @param name The title of the event.
   * @param text The text of the event.
   * @param timestamp The timestamp of the event.
   * @param hostname The hostname.
   * @param aggregationKey The aggregation key.
   * @param priority The priority.
   * @param sourceTypeName The source type name.
   * @param alertType The alert type.
   * @param tags The tags!
   */
  def event(
    name: String,
    text: String,
    timestamp: Option[Long] = None,
    hostname: Option[String] = None,
    aggregationKey: Option[String] = None,
    priority: Option[String] = None,
    sourceTypeName: Option[String] = None,
    alertType: Option[String] = None,
    tags: Seq[String] = Seq.empty
  ): Unit = enqueue(
    EventMetric(
      name = name, text = text, timestamp = timestamp, hostname = hostname,
      aggregationKey = aggregationKey, priority = priority, sourceTypeName = sourceTypeName,
      alertType = alertType, tags = tags
    )
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
