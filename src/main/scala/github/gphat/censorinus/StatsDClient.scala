package github.gphat.censorinus

import github.gphat.censorinus.statsd.Encoder

import java.text.DecimalFormat
import java.util.concurrent._
import scala.util.Random

/** A StatsD client! You should create one of these and reuse it across
  * your application.
  * @constructor Creates a new client instance
  * @param hostname the host to send metrics to, defaults to localhost
  * @param port the port to send metrics to, defaults to 8125
  * @param prefix A prefix to add to all metric names. A period will be added to the end, resulting in prefix.metricname.
  * @param defaultSampleRate A sample rate default to be used for all metric methods. Defaults to 1.0
  * @param asynchronous True if you want the client to asynch, false for blocking!
  * @param floatFormat Allows control of the precision of the double output via strings from [[java.util.Formatter]]. Defaults to "%.8f".
  */
class StatsDClient(
  hostname: String = "localhost",
  port: Int = MetricSender.DEFAULT_STATSD_PORT,
  prefix: String = "",
  defaultSampleRate: Double = 1.0,
  asynchronous: Boolean = true
) extends Client(
  sender = new UDPSender(hostname = hostname, port = port),
  encoder = Encoder,
  prefix = prefix,
  defaultSampleRate = defaultSampleRate,
  asynchronous = asynchronous
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
    bypassSampler: Boolean = false
  ): Unit = enqueue(
    CounterMetric(name = makeName(name), value = value, sampleRate = sampleRate),
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
    bypassSampler: Boolean = false
  ): Unit = enqueue(
    CounterMetric(name = makeName(name), value = value, sampleRate = sampleRate),
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
    bypassSampler: Boolean = false
  ): Unit = enqueue(
    GaugeMetric(name = makeName(name), value = value),
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
    name: String,
    value: Double = 1,
    sampleRate: Double = defaultSampleRate,
    bypassSampler: Boolean = false
  ): Unit = enqueue(
    CounterMetric(name = makeName(name), value = value, sampleRate = sampleRate),
    sampleRate,
    bypassSampler
  )

  /** Emit a meter metric.
    * @param name The name of the metric
    * @param value The value of the meter
    * @param sampleRate The rate at which to sample this metric.
    * @param bypassSampler If true, the metric will always be passed through, but the sample rate will be included in the emitted metric.
    *                      This is useful for when you occasionally do your own sampling.
    */
  def meter(
    name: String,
    value: Double,
    sampleRate: Double = defaultSampleRate,
    bypassSampler: Boolean = false
  ): Unit = enqueue(
    MeterMetric(name = makeName(name), value = value),
    sampleRate,
    bypassSampler
  )

  /** Emit e a set metric.
   * @param name The name of the metric
   * @param value The item to add to the set
   */
  def set(name: String, value: String): Unit = enqueue(
    SetMetric(name = makeName(name), value = value)
  )

  /** Emit a timer metric.
    * @param name The name of the metric
    * @param value The value of the timer in milliseconds
    * @param sampleRate The rate at which to sample this metric.
    * @param bypassSampler If true, the metric will always be passed through, but the sample rate will be included in the emitted metric.
    *                      This is useful for when you occasionally do your own sampling.
    */
  def timer(name: String,
    milliseconds: Double,
    sampleRate: Double = defaultSampleRate,
    bypassSampler: Boolean = false
  ): Unit = enqueue(
    TimerMetric(name = makeName(name), value = milliseconds, sampleRate = sampleRate),
    sampleRate,
    bypassSampler
  )
}
