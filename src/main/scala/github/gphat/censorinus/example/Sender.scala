package github.gphat.censorinus.example

import github.gphat.censorinus.Client
import github.gphat.censorinus.statsd.UDPSender

import java.util.UUID
import scala.util.Random

object Sender {

  def main(args: Array[String]): Unit = {
    println(args)
    val client = new Client(sender = new UDPSender(), asynchronous=false)

    client.gauge("foo.bar.baz_gauge", 10.0)
    client.increment("foo.bar.baz_counter", 1)
    client.histogram("foo.bar.baz_histo", Random.nextDouble)
    client.timer("foo.bar.baz_timing", Random.nextDouble)
    client.set("foo.bar.baz_set", UUID.randomUUID.toString)

    client.shutdown
  }
}
