package github.gphat.censorinus

import java.text.DecimalFormat
import java.util.concurrent._
import scala.util.Random

/** A Censorinus client! You should create one of these and reuse it across
  * your application.
  * @constructor Creates a new client instance
  * @param encoder The MetricEncoder implementation this client will use
  * @param sender The MetricSender implementation this client will use
  * @param prefix A prefix to add to all metric names. A period will be added to the end, resulting in prefix.metricname.
  * @param defaultSampleRate A sample rate default to be used for all metric methods. Defaults to 1.0
  * @param flushInterval How often in milliseconds to flush the local buffer to keep things async. Defaults to 100ms
  * @param asynchronous True if you want the client to asynch, false for blocking!
  * @param floatFormat Allows control of the precision of the double output via strings from [[java.util.Formatter]]. Defaults to "%.8f".
  */
class Client(
  encoder: MetricEncoder,
  sender: MetricSender,
  prefix: String = "",
  val defaultSampleRate: Double = 1.0,
  flushInterval: Long = 100L,
  asynchronous: Boolean = true,
  floatFormat: String = "%.8f"
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
          sender.send(encoder.encode(queue.poll))
        }
      }
    }

    // Check this thing every 100ms
    ex.scheduleAtFixedRate(task, flushInterval, flushInterval, TimeUnit.MILLISECONDS)
  })

  /** Explicitly shut down the client and it's underlying bits.
   */
  def shutdown: Unit = {
    sender.shutdown
    executor.map({ ex => ex.shutdown })
  }

  /** Get the queue of unsent metrics, if for some reason you feel the need to
    * do that.
    */
  def getQueue = queue

  def enqueue(metric: Metric, sampleRate: Double = defaultSampleRate) = {
    if(sampleRate == 1.0 || Random.nextDouble <= sampleRate) {
      if(asynchronous) {
        // Queue it up! Leave encoding for later so we back as soon as we can.
        queue.offer(metric)
      } else {
        // Just send it.
        sender.send(encoder.encode(metric))
      }
    }
  }

  def makeName(name: String): String = {
    if(prefix.isEmpty) {
      name
    } else {
      s"${prefix}.${name}"
    }
  }
}
