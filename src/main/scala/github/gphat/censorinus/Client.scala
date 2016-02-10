package github.gphat.censorinus

import java.util.concurrent._
import scala.util.Random

class Client(
  sender: MetricSender,
  val defaultSampleRate: Double = 1.0,
  flushInterval: Long = 100L
) {

  val queue = new ConcurrentLinkedQueue[Metric]()
  val executor = Executors.newScheduledThreadPool(1)

  val task = new Runnable {
    def run() {
      // When we awake, send everything we've got in the queue!
      while(!queue.isEmpty) {
        sender.send(queue.poll)
      }
    }
  }

  // Check this thing every 100ms
  executor.scheduleAtFixedRate(task, flushInterval, flushInterval, TimeUnit.MILLISECONDS)

  def counter(name: String, value: Double, sampleRate: Double = defaultSampleRate) = enqueue(
    Metric(name = name, value = value, sampleRate = sampleRate, metricType = "c")
  )

  def decrement(name: String, value: Double, sampleRate: Double = defaultSampleRate) = enqueue(
    Metric(name = name, value = value, sampleRate = sampleRate, metricType = "c")
  )

  def gauge(name: String, value: Double, sampleRate: Double = defaultSampleRate) = enqueue(
    Metric(name = name, value = value, sampleRate = sampleRate, metricType = "g")
  )

  def histogram(name: String, value: Double, sampleRate: Double = defaultSampleRate) = enqueue(
    Metric(name = name, value = value, sampleRate = sampleRate, metricType = "c")
  )

  def increment(name: String, value: Double, sampleRate: Double = defaultSampleRate) = enqueue(
    Metric(name = name, value = value, sampleRate = sampleRate, metricType = "c")
  )

  def meter(name: String, value: Double, sampleRate: Double = defaultSampleRate) = enqueue(
    Metric(name = name, value = value, sampleRate = sampleRate, metricType = "ms")
  )

  def time(name: String, value: Double, sampleRate: Double = defaultSampleRate) = enqueue(
    Metric(name = name, value = value, sampleRate = sampleRate, metricType = "t")
  )

  private def enqueue(metric: Metric, sampleRate: Double = defaultSampleRate) = {
    if(sampleRate == 1.0 || Random.nextDouble <= sampleRate) {
      queue.offer(metric)
    }
  }
}
