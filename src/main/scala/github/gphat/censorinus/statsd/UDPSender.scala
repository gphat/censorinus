package github.gphat.censorinus.statsd

import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.DatagramChannel
import java.nio.charset.StandardCharsets

import github.gphat.censorinus.{Metric,MetricSender}

class UDPSender(hostname: String = "localhost", port: Int = 8125) extends MetricSender {

  val clientSocket = DatagramChannel.open();
  clientSocket.connect(new InetSocketAddress(hostname, port));

  def send(metric: Metric): Unit = {
    val message = Encoder.encode(metric)
    clientSocket.write(ByteBuffer.wrap(message.getBytes(StandardCharsets.UTF_8)))
  }
}
