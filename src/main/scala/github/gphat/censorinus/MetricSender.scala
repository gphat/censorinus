package github.gphat.censorinus

trait MetricSender {

  def send(metric: Metric): Unit
}
