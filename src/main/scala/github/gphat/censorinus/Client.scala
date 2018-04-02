package github.gphat.censorinus

import java.nio.{ByteBuffer, CharBuffer}
import java.nio.charset.{CharsetEncoder, CoderResult, StandardCharsets}
import java.util.ArrayList
import java.util.concurrent._
import java.util.concurrent.atomic.AtomicLong
import java.util.logging.Logger

import scala.util.control.NonFatal
import scala.collection.JavaConverters._

/** A Censorinus client! You should create one of these and reuse it across
  * your application.
  *
  * If `maxBatchSize` is defined, metrics will be batched before being sent to
  * `sender`. The `maxBatchSize` param controls the maximum size of the
  * `ByteBuffer`s sent to `sender`. This should generally be smaller than the
  * maximum allowable size of UDP packets on the system.
  *
  * @constructor Creates a new client instance
  * @param encoder The MetricEncoder implementation this client will use
  * @param sender The MetricSender implementation this client will use
  * @param prefix A prefix to add to all metric names. A period will be added to the end, resulting in prefix.metricname.
  * @param defaultSampleRate A sample rate default to be used for all metric methods. Defaults to 1.0
  * @param asynchronous True if you want the client to asynch, false for blocking!
  * @param maxQueueSize Maximum amount of metrics allowed to be queued at a time.
  * @param maxBatchSize maximum size of byte buffer supplied to `sender`
  */
class Client(
  encoder: MetricEncoder,
  sender: MetricSender,
  prefix: String = "",
  val defaultSampleRate: Double = 1.0,
  asynchronous: Boolean = true,
  maxQueueSize: Option[Int] = None,
  consecutiveDropWarnThreshold: Long = 1000,
  val consecutiveDroppedMetrics: AtomicLong = new AtomicLong(0),
  val maxBatchSize: Option[Int] = None
) {
  private[this] val log: Logger = Logger.getLogger(classOf[Client].getName)

  private[this] val batcher: Client.Batcher = maxBatchSize match {
    case Some(bufSize) if maxQueueSize.isDefined =>
      Client.Batched(bufSize)
    case Some(bufSize) =>
      log.warning(s"maxBatchSize=$bufSize ignored, because maxQueueSize is None")
      Client.Unbatched
    case None =>
      Client.Unbatched
  }

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
  // repeatedly polls the queue and sends the available metrics down the road.
  executor.foreach { ex =>
    val task = new Runnable {
      val metrics = new ArrayList[Metric]()
      def tick(): Unit = try {
        metrics.clear()
        // Start with `take()` since it'll block on an empty queue. If this
        // succeeds, then drain any remaining metrics into our metrics list.
        val head = queue.take()
        if (head != null) {
          metrics.add(head)
          // Drain remaining metrics into the list.
          queue.drainTo(metrics)
          send(metrics.iterator.asScala)
        } else {
          ()
        }
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
    if(bypassSampler || sampleRate >= 1.0 || ThreadLocalRandom.current.nextDouble <= sampleRate) {
      if(asynchronous) {
        // Queue it up! Leave encoding for later so get we back as soon as we can.
        if (!queue.offer(metric)) {
          val dropped = consecutiveDroppedMetrics.incrementAndGet
          if (dropped == 1 || (dropped % consecutiveDropWarnThreshold) == 0) {
            log.warning("Queue is full. Metric was dropped. " +
              "Consider decreasing the defaultSampleRate or increasing the maxQueueSize."
            )
          }
        }
      } else {
        consecutiveDroppedMetrics.set(0)
        // Just send it.
        send(Iterator.single(metric))
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
  private def send(metrics: Iterator[Metric]): Unit = {
    val lines = metrics.flatMap { metric =>
      encoder.encode(metric) match {
        case Some(line) => Iterator.single(line)
        case None =>
          log.warning(s"Unable to send metric: unsupported metric type `${metric}`")
          Iterator.empty
      }
    }

    batcher.batch(lines) { message =>
      sender.send(message)
    }
  }
}

object Client {

  /**
   * Batches metrics encoded with a [[MetricEncoder]] into byte buffers to be
   * sent with a [[MetricSender]].
   */
  sealed trait Batcher {

    /**
     * Iterate over all lines, batching metrics as possible, and sending them
     * to `f`. The `ByteBuffer` provided to `f` is only owned by the caller for
     * the duration of `f`. Any use of the `ByteBuffer` outside of this will
     * result in undefined and, likely, confusing/incorrect behaviour. This
     * will completely exhaust `line`.
     *
     * @param lines the encoded metrics to batch
     * @param f the caller-supplied function to send the batched metrics to
     */
    def batch(lines: Iterator[String])(f: ByteBuffer => Unit): Unit = {
      val buffered = lines.buffered
      while (buffered.hasNext) {
        batch1(buffered)(f)
      }
    }

    // Call `f` exactly once with a batched message.
    protected def batch1(lines: BufferedIterator[String])(f: ByteBuffer => Unit): Unit
  }

  final object Unbatched extends Batcher {
    protected def batch1(lines: BufferedIterator[String])(f: ByteBuffer => Unit): Unit = {
      val buf = StandardCharsets.UTF_8
        .newEncoder()
        .encode(CharBuffer.wrap(lines.next))
      f(buf)
    }
  }

  final case class Batched(bufferSize: Int) extends Batcher {
    private[this] val buffer: ByteBuffer = ByteBuffer.allocate(bufferSize)
    private[this] val encoder: CharsetEncoder = StandardCharsets.UTF_8.newEncoder()

    protected def batch1(lines: BufferedIterator[String])(f: ByteBuffer => Unit): Unit =
      synchronized {
        buffer.clear()
        encoder.reset()

        // We always try to send the first metric. If the line is too long,
        // then it just gets truncated.
        encoder.encode(CharBuffer.wrap(lines.next), buffer, false)

        // Keep adding metrics in a loop until no more fit.
        var cont = true
        while (cont && buffer.remaining() > 1 && lines.hasNext) {
          buffer.mark()
          buffer.put('\n'.toByte)
          encoder.encode(CharBuffer.wrap(lines.head), buffer, false) match {
            case CoderResult.OVERFLOW =>
              // On overflow, reset the buffer back to the last metric.
              buffer.reset()
              cont = false
            case CoderResult.UNDERFLOW =>
              lines.next
          }
        }

        // Let the encoder know we're at the end of the input. UTF-8 won't dump
        // any special characters or anything.
        encoder.encode(CharBuffer.wrap(""), buffer, true)

        // Make the buffer available to read and send it to the caller.
        buffer.flip()
        f(buffer)
      }
  }
}
