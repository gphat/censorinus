package github.gphat.censorinus.example

import github.gphat.censorinus.DogStatsDClient

import java.util.UUID
import scala.util.Random

object DogStatsD {

  def main(args: Array[String]): Unit = {
    val client = new DogStatsDClient(asynchronous=false)

    client.gauge("foo.bar.baz_gauge", 10.0, tags = Seq("gorch:flurb"))
    client.increment("foo.bar.baz_counter", 1, tags = Seq("gorch:flurb"))
    client.histogram("foo.bar.baz_histo", Random.nextDouble, tags = Seq("gorch:flurb"))
    client.timer("foo.bar.baz_timing", Random.nextDouble, tags = Seq("gorch:flurb"))
    client.set("foo.bar.baz_set", UUID.randomUUID.toString, tags = Seq("gorch:flurb"))

    client.event(
      name = "I am an event",
      text = "And here are my contents",
      timestamp = Some(System.currentTimeMillis / 1000),
      hostname = Some("farts"),
      aggregationKey = Some("abc-1234"),
      priority = Some(DogStatsDClient.EVENT_PRIORITY_LOW),
      sourceTypeName = Some("nagios"),
      alertType = Some(DogStatsDClient.EVENT_ALERT_TYPE_ERROR),
      tags = Seq("a:b")
    )

    client.serviceCheck(
      name = "foo.bar.gorch",
      status = DogStatsDClient.SERVICE_CHECK_OK,
      timestamp = Some(System.currentTimeMillis / 1000),
      hostname = Some("Farts"),
      message = Some("GOT 'IM"),
      tags = Seq("a:b")
    )

    client.shutdown
  }
}
