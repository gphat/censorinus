package github.gphat.censorinus

import java.nio.ByteBuffer
import java.net.InetSocketAddress
import java.nio.channels.DatagramChannel
import java.nio.charset.StandardCharsets
import scala.util.{ Try, Success, Failure }

class UDPSender(
  hostname: String = "localhost",
  port: Int = MetricSender.DEFAULT_STATSD_PORT,
  allowExceptions: Boolean = false
) extends MetricSender {

  lazy val clientSocket = DatagramChannel.open.connect(new InetSocketAddress(hostname, port))

  def send(message: String): Unit =
    Try {
      clientSocket.write(ByteBuffer.wrap(message.getBytes(StandardCharsets.UTF_8)))
    } match {
      case Failure(ex) =>
        if( allowExceptions) 
          throw ex
      case Success(_) =>
    }


  def shutdown: Unit = clientSocket.close
}
