package github.gphat.censorinus

trait MetricSender {

  def send(message: String): Unit

  def shutdown: Unit
}

object MetricSender {
  val DEFAULT_STATSD_PORT: Int = 8125
}
