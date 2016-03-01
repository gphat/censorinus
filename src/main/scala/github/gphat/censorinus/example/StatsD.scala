package github.gphat.censorinus.example

import github.gphat.censorinus.StatsDClient

import java.util.UUID
import scala.util.Random

object StatsD {

  def main(args: Array[String]): Unit = {
    val client = new StatsDClient(asynchronous=false)

    client.gauge("foo.bar.baz_gauge", 10.0)
    client.increment("foo.bar.baz_counter", 1)
    client.timer("foo.bar.baz_timing", Random.nextDouble)
    client.set("foo.bar.baz_set", UUID.randomUUID.toString)

    client.shutdown
  }
}
