package github.gphat.censorinus

import java.net.InetSocketAddress
import java.nio.channels.DatagramChannel

class StatsDUDPSender(hostname: String, port: Int) extends MetricSender {

  val clientSocket = DatagramChannel.open();
  clientSocket.connect(new InetSocketAddress(hostname, port));

  def send(metric: Metric): Unit = {
    println("foo")
  }
}
