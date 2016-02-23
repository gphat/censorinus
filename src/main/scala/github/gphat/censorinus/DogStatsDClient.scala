package github.gphat.censorinus

import github.gphat.censorinus.dogstatsd.Encoder

import java.util.concurrent._
import scala.util.Random

/** A DStatsD client! You should create one of these and reuse it across
  * your application.
  * @constructor Creates a new client instance
  * @param hostname the host to send metrics to, defaults to localhost
  * @param port the port to send metrics to, defaults to 8125
  * @param prefix A prefix to add to all metric names. A period will be added to the end, resulting in prefix.metricname.
  * @param defaultSampleRate A sample rate default to be used for all metric methods. Defaults to 1.0
  * @param flushInterval How often in milliseconds to flush the local buffer to keep things async. Defaults to 100ms
  * @param asynchronous True if you want the client to asynch, false for blocking!
  * @param floatFormat Allows control of the precision of the double output via strings from [[java.util.Formatter]]. Defaults to "%.8f".
  */
class DogStatsDClient(
  hostname: String = "localhost",
  port: Int = 8125,
  prefix: String = "",
  defaultSampleRate: Double = 1.0,
  flushInterval: Long = 100L,
  asynchronous: Boolean = true,
  floatFormat: String = "%.8f"
) extends Client(
  sender = new UDPSender(hostname = hostname, port = port),
  encoder = Encoder,
  prefix = prefix,
  defaultSampleRate = defaultSampleRate,
  flushInterval = flushInterval,
  asynchronous = asynchronous,
  floatFormat = floatFormat
) {
  /** Emit a counter metric.
    * @param name The name of the metric
    * @param value The value of the metric, or how much to increment by
    * @param sampleRate The rate at which to sample this metric.
    * @param bypassSampler If true, the metric will always be passed through, but the sample rate will be included in the emitted metric. This is useful for when you occasionally do your own sampling.
    */
  def counter(
    name: String,
    value: Double,
    sampleRate: Double = defaultSampleRate,
    tags: Seq[String] = Seq.empty,
    bypassSampler: Boolean = false
  ) = enqueue(
    Metric(name = makeName(name), value = floatFormat.format(value), sampleRate = sampleRate, metricType = "c", tags = tags),
    sampleRate,
    bypassSampler
  )

  /** Emit a decrement metric.
    * @param name The name of the metric
    * @param value The value of the metric, or how much to decrement by. Defaults to -1
    * @param sampleRate The rate at which to sample this metric.
    * @param bypassSampler If true, the metric will always be passed through, but the sample rate will be included in the emitted metric. This is useful for when you occasionally do your own sampling.
    */
  def decrement(
    name: String,
    value: Double = 1,
    sampleRate: Double = defaultSampleRate,
    tags: Seq[String] = Seq.empty,
    bypassSampler: Boolean = false
  ) = enqueue(
    Metric(name = makeName(name), value = floatFormat.format(value), sampleRate = sampleRate, metricType = "c", tags = tags),
    sampleRate,
    bypassSampler
  )

  /** Emit a gauge metric.
    * @param name The name of the metric
    * @param value The value of the metric, or current value of the gauge
    * @param sampleRate The rate at which to sample this metric.
    * @param bypassSampler If true, the metric will always be passed through, but the sample rate will be included in the emitted metric. This is useful for when you occasionally do your own sampling.
    */
  def gauge(
    name: String,
    value: Double,
    sampleRate: Double = defaultSampleRate,
    tags: Seq[String] = Seq.empty,
    bypassSampler: Boolean = false
  ) = enqueue(
    Metric(name = makeName(name), value = floatFormat.format(value), sampleRate = sampleRate, metricType = "g", tags = tags),
    sampleRate,
    bypassSampler
  )

  /** Emit a histogram metric.
    * @param name The name of the metric
    * @param value The value of the metric, or a value to be sampled for the histogram
    * @param sampleRate The rate at which to sample this metric.
    * @param bypassSampler If true, the metric will always be passed through, but the sample rate will be included in the emitted metric. This is useful for when you occasionally do your own sampling.
    */
  def histogram(
    name: String,
    value: Double,
    sampleRate: Double = defaultSampleRate,
    tags: Seq[String] = Seq.empty,
    bypassSampler: Boolean = false) = enqueue(
    Metric(name = makeName(name), value = floatFormat.format(value), sampleRate = sampleRate, metricType = "h", tags = tags),
    sampleRate,
    bypassSampler
  )

  /** Emit an increment metric.
    * @param name The name of the metric
    * @param value The value of the metric, or the amount to increment by. Defaults to 1
    * @param sampleRate The rate at which to sample this metric.
    * @param bypassSampler If true, the metric will always be passed through, but the sample rate will be included in the emitted metric. This is useful for when you occasionally do your own sampling.
    */
  def increment(
    name: String, value: Double = 1,
    sampleRate: Double = defaultSampleRate,
    tags: Seq[String] = Seq.empty,
    bypassSampler: Boolean = false
  ) = enqueue(
    Metric(name = makeName(name), value = floatFormat.format(value), sampleRate = sampleRate, metricType = "c", tags = tags),
    sampleRate,
    bypassSampler
  )

  /** Emit a meter metric.
    * @param name The name of the metric
    * @param value The value of the meter
    * @param sampleRate The rate at which to sample this metric.
    * @param bypassSampler If true, the metric will always be passed through, but the sample rate will be included in the emitted metric. This is useful for when you occasionally do your own sampling.
    */
  def meter(
    name: String,
    value: Double,
    sampleRate: Double = defaultSampleRate,
    tags: Seq[String] = Seq.empty,
    bypassSampler: Boolean = false
  ) = enqueue(
    Metric(name = makeName(name), value = floatFormat.format(value), sampleRate = sampleRate, metricType = "m", tags = tags),
    sampleRate,
    bypassSampler
  )

  /** Emit a set metric.
   * @param name The name of the metric
   * @param value The item to add to the set
   */
  def set(
    name: String,
    value: String,
    tags: Seq[String] = Seq.empty
  ) = enqueue(
    Metric(name = makeName(name), value = value, metricType = "s", tags = tags)
  )

  /** Emit a timer metric.
    * @param name The name of the metric
    * @param value The value of the timer in milliseconds
    * @param sampleRate The rate at which to sample this metric.
    * @param bypassSampler If true, the metric will always be passed through, but the sample rate will be included in the emitted metric. This is useful for when you occasionally do your own sampling.
    */
  def timer(
    name: String,
    milliseconds: Double,
    sampleRate: Double = defaultSampleRate,
    tags: Seq[String] = Seq.empty,
    bypassSampler: Boolean = false
  ) = enqueue(
    Metric(name = makeName(name), value = floatFormat.format(milliseconds), sampleRate = sampleRate, metricType = "ms", tags = tags),
    sampleRate,
    bypassSampler
  )
}
