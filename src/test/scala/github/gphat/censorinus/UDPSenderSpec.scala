package github.gphat.censorinus

import org.scalatest._
import java.nio.channels.UnresolvedAddressException

class UDPSenderSpec extends FlatSpec with Matchers {

  "UDPSender" should "emit errors" in {
    // Guessing this port won't be used? :)
    val u = new UDPSender(hostname = "127.0.0.1789", port = 8126, allowExceptions = true)
    an [UnresolvedAddressException] should be thrownBy u.send("abc")
  }

  it should "swallow errors" in {
    // Guessing this port won't be used? :)
    val u = new UDPSender(hostname = "fart.example.com", port = 8126, allowExceptions = false)
    u.send("abc")
  }
}
