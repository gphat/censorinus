package github.gphat.censorinus.statsd

import java.net.InetSocketAddress
import java.nio.channels.DatagramChannel

import github.gphat.censorinus.{Metric,MetricSender}

class StatsDUDPSender(hostname: String, port: Int) extends MetricSender {

  val clientSocket = DatagramChannel.open();
  clientSocket.connect(new InetSocketAddress(hostname, port));

  def send(metric: Metric): Unit = {
    println("foo")
  }
}
