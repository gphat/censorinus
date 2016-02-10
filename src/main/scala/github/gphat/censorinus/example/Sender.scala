package github.gphat.censorinus.example

import github.gphat.censorinus.Client
import github.gphat.censorinus.statsd.UDPSender

object Sender {

  def main(args: Array[String]): Unit = {
    val client = new Client(sender = new UDPSender())
    1.to(160000).foreach({ _ =>
      client.gauge("foo.bar.baz", 10.0)
    })
    Thread.sleep(2000)
    client.shutdown
  }
}
