package github.gphat.censorinus

import github.gphat.censorinus.statsd.Encoder

import java.util.concurrent._
import scala.util.Random

/** A StatsD client! You should create one of these and reuse it across
  * your application.
  * @constructor Creates a new client instance
  * @param hostname the host to send metrics to, defaults to localhost
  * @param port the port to send metrics to, defaults to 8125
  * @param defaultSampleRate A sample rate default to be used for all metric methods. Defaults to 1.0
  * @param flushInterval How often in milliseconds to flush the local buffer to keep things async. Defaults to 100ms
  * @param asynchronous True if you want the client to asynch, false for blocking!
  */
class StatsDClient(
  hostname: String = "localhost",
  port: Int = 8125,
  defaultSampleRate: Double = 1.0,
  flushInterval: Long = 100L,
  asynchronous: Boolean = true
) extends Client(
  sender = new UDPSender(hostname = hostname, port = port),
  encoder = Encoder,
  defaultSampleRate = defaultSampleRate,
  flushInterval = flushInterval,
  asynchronous = asynchronous
) {

  /** Emit a counter metric.
    * @param name The name of the metric
    * @param value The value of the metric, or how much to increment by
    * @param sampleRate The rate at which to sample this metric.
    */
  def counter(name: String, value: Double, sampleRate: Double = defaultSampleRate) = enqueue(
    Metric(name = name, value = value.toString, sampleRate = sampleRate, metricType = "c")
  )

  /** Emit a decrement metric.
    * @param name The name of the metric
    * @param value The value of the metric, or how much to decrement by. Defaults to -1
    * @param sampleRate The rate at which to sample this metric.
    */
  def decrement(name: String, value: Double = 1, sampleRate: Double = defaultSampleRate) = enqueue(
    Metric(name = name, value = value.toString, sampleRate = sampleRate, metricType = "c")
  )

  /** Emit a gauge metric.
    * @param name The name of the metric
    * @param value The value of the metric, or current value of the gauge
    * @param sampleRate The rate at which to sample this metric.
    */
  def gauge(name: String, value: Double, sampleRate: Double = defaultSampleRate) = enqueue(
    Metric(name = name, value = value.toString, sampleRate = sampleRate, metricType = "g")
  )

  /** Emit a histogram metric.
    * @param name The name of the metric
    * @param value The value of the metric, or a value to be sampled for the histogram
    * @param sampleRate The rate at which to sample this metric.
    */
  def histogram(name: String, value: Double, sampleRate: Double = defaultSampleRate) = enqueue(
    Metric(name = name, value = value.toString, sampleRate = sampleRate, metricType = "h")
  )

  /** Emit an increment metric.
    * @param name The name of the metric
    * @param value The value of the metric, or the amount to increment by. Defaults to 1
    * @param sampleRate The rate at which to sample this metric.
    */
  def increment(name: String, value: Double = 1, sampleRate: Double = defaultSampleRate) = enqueue(
    Metric(name = name, value = value.toString, sampleRate = sampleRate, metricType = "c")
  )

  /** Emit a meter metric.
    * @param name The name of the metric
    * @param value The value of the meter
    * @param sampleRate The rate at which to sample this metric.
    */
  def meter(name: String, value: Double, sampleRate: Double = defaultSampleRate) = enqueue(
    Metric(name = name, value = value.toString, sampleRate = sampleRate, metricType = "m")
  )

  /** Emit e a set metric.
   * @param name The name of the metric
   * @param value The item to add to the set
   * @param sampleRate The rate at which to sample the metric
   */
  def set(name: String, value: String) = enqueue(
    Metric(name = name, value = value, metricType = "s")
  )

  /** Emit a timer metric.
    * @param name The name of the metric
    * @param value The value of the timer in milliseconds
    * @param sampleRate The rate at which to sample this metric.
    */
  def timer(name: String, milliseconds: Double, sampleRate: Double = defaultSampleRate) = enqueue(
    Metric(name = name, value = milliseconds.toString, sampleRate = sampleRate, metricType = "ms")
  )
}
