package github.gphat.censorinus

import java.util.concurrent._
import scala.util.Random

/** A Censorinus client! You should create one of these and reuse it across
  * your application.
  * @constructor Creates a new client instance
  * @param sender The MetricSender implementation this client will use
  * @param defaultSampleRate A sample rate default to be used for all metric methods. Defaults to 1.0
  * @param flushInterval How often in milliseconds to flush the local buffer to keep things async. Defaults to 100ms
  * @param asynchronous True if you want the client to asynch, false for blocking!
  */
class Client(
  sender: MetricSender,
  val defaultSampleRate: Double = 1.0,
  flushInterval: Long = 100L,
  asynchronous: Boolean = true
) {

  val queue = new ConcurrentLinkedQueue[Metric]()
  // This is an Option[Executor] to allow for NOT sending things.
  // We'll make an executor if the flushInterval is > -1 and we are
  // running in asynchronous mode then spin up the thread-works
  if(flushInterval < 1) {
    throw new Exception("Please use a flush interval > 1!")
  }
  val executor = if(asynchronous) {
    Some(Executors.newScheduledThreadPool(1, new ThreadFactory {
      override def newThread(r: Runnable): Thread = {
        val t = Executors.defaultThreadFactory.newThread(r)
        t.setDaemon(true)
        t
      }
    }))
  } else {
    None
  }

  executor.map({ ex =>
    val task = new Runnable {
      def run() {
        // When we awake, send everything we've got in the queue!
        while(!queue.isEmpty) {
          sender.send(queue.poll)
        }
      }
    }

    // Check this thing every 100ms
    ex.scheduleAtFixedRate(task, flushInterval, flushInterval, TimeUnit.MILLISECONDS)
  })

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

  /** Explicitly shut down the client and it's underlying bits.
   */
  def shutdown: Unit = executor.map({ ex => ex.shutdown })

  /** Get the queue of unsent metrics, if for some reason you feel the need to
    * do that.
    */
  def getQueue = queue

  private def enqueue(metric: Metric, sampleRate: Double = defaultSampleRate) = {
    if(sampleRate == 1.0 || Random.nextDouble <= sampleRate) {
      if(asynchronous) {
        queue.offer(metric)
      } else {
        sender.send(metric)
      }
    }
  }
}
