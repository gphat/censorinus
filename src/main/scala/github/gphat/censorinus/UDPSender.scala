package github.gphat.censorinus

import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.DatagramChannel
import java.nio.charset.StandardCharsets

class UDPSender(hostname: String = "localhost", port: Int = MetricSender.DEFAULT_STATSD_PORT) extends MetricSender {

  lazy val clientSocket = DatagramChannel.open.connect(new InetSocketAddress(hostname, port))

  def send(message: String): Unit = {
    clientSocket.write(ByteBuffer.wrap(message.getBytes(StandardCharsets.UTF_8)))
  }

  def shutdown: Unit = clientSocket.close
}
