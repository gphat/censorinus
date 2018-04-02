package github.gphat.censorinus

import java.nio.ByteBuffer

trait MetricSender {
  def send(message: ByteBuffer): Unit

  def shutdown: Unit
}

object MetricSender {
  val DEFAULT_STATSD_PORT: Int = 8125
}
