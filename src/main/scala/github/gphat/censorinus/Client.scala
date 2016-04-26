package github.gphat.censorinus

import java.text.DecimalFormat
import java.util.concurrent._
import java.util.logging.Logger

import scala.util.control.NonFatal

/** A Censorinus client! You should create one of these and reuse it across
  * your application.
  *
  * @constructor Creates a new client instance
  * @param encoder The MetricEncoder implementation this client will use
  * @param sender The MetricSender implementation this client will use
  * @param prefix A prefix to add to all metric names. A period will be added to the end, resulting in prefix.metricname.
  * @param defaultSampleRate A sample rate default to be used for all metric methods. Defaults to 1.0
  * @param asynchronous True if you want the client to asynch, false for blocking!
  * @param maxQueueSize Maximum amount of metrics allowed to be queued at a time.
  */
class Client(
  encoder: MetricEncoder,
  sender: MetricSender,
  prefix: String = "",
  val defaultSampleRate: Double = 1.0,
  asynchronous: Boolean = true,
  maxQueueSize: Option[Int] = None
) {
  private[this] val log: Logger = Logger.getLogger(classOf[Client].getName)

  private[censorinus] val queue: LinkedBlockingQueue[Metric] =
    maxQueueSize.map({
      capacity => new LinkedBlockingQueue[Metric](capacity)
    }).getOrElse(
      // Unbounded is kinda dangerous, but sure!
      new LinkedBlockingQueue[Metric]()
    )

  // This is an Option[Executor] to allow for NOT sending things.
  // We'll make an executor if we are running in asynchronous mode then spin up
  // the thread-works.
  private[this] val executor: Option[ExecutorService] = if(asynchronous) {
    Some(Executors.newSingleThreadExecutor(new ThreadFactory {
      override def newThread(r: Runnable): Thread = {
        val t = Executors.defaultThreadFactory.newThread(r)
        t.setDaemon(true)
        t
      }
    }))
  } else {
    None
  }

  // If we are running asynchronously, then kick off our long-running task that
  // repeatedly polls the queue and send the available metrics down the road.
  executor.foreach { ex =>
    val task = new Runnable {
      def tick(): Unit = try {
        Option(queue.take).map({ metric =>
          send(metric)
        })
      } catch {
        case _: InterruptedException => Thread.currentThread.interrupt
        case NonFatal(exception) => {
          log.warning(s"Swallowing exception thrown while sending metric: $exception")
        }
      }

      def run(): Unit = {
        while (!Thread.interrupted) {
          tick
        }
      }
    }

    ex.submit(task)
  }

  /** Explicitly shut down the client and it's underlying bits.
   */
  def shutdown(): Unit = {
    sender.shutdown
    // It's pretty safe to just forcibly shutdown the executor and interrupt
    // the running async task.
    executor.foreach(_.shutdownNow)
  }

  def enqueue(metric: Metric, sampleRate: Double = defaultSampleRate, bypassSampler: Boolean = false): Unit = {
    if(bypassSampler || sampleRate == 1.0 || ThreadLocalRandom.current.nextDouble <= sampleRate) {
      if(asynchronous) {
        // Queue it up! Leave encoding for later so get we back as soon as we can.
        if (!queue.offer(metric)) {
          log.warning("Unable to enqueue metric, queue is full. " +
            "If this is during steady state, consider decreasing the defaultSampleRate, " +
            "but if this periodic, consider increasing the maxQueueSize.")
        }
      } else {
        // Just send it.
        send(metric)
      }
    }
  }

  protected def makeName(name: String): String = {
    if(prefix.isEmpty) {
      name
    } else {
      new StringBuilder(prefix).append(".").append(name).toString
    }
  }

  // Encode and send a metric to something approximating statsd.
  private def send(metric: Metric): Unit = {
    encoder.encode(metric) match {
      case Some(message) =>
        sender.send(message)
      case None =>
        log.warning(s"Unable to send metric: unsupported metric type `${metric}`")
    }
  }
}
