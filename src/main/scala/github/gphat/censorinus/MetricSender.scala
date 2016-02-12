package github.gphat.censorinus

trait MetricSender {

  def send(message: String): Unit
}
