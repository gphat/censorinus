package github.gphat.censorinus.example

import github.gphat.censorinus.DogStatsDClient

object Benchmark {

  def main(args: Array[String]): Unit = {
    val client = new DogStatsDClient(asynchronous=true, defaultSampleRate = 1, maxQueueSize = Some(10000))

    val iterations = 10
    var tot = 0L
    Range(start = 0, end = iterations - 1, step = 1).foreach({ h =>
      tot += time {
        Range(start = 0, end = 10050, step = 1).foreach({ h =>
          client.increment("foo.bar.baz_counter", 1, tags = Seq("gorch:flurb"))
        })
        while(client.queue.size > 0) {
          // Nothing
        }
      }
    })
    println("Elapsed time: " + tot + "ns, " + (tot / iterations) + "ns average")
    client.shutdown
  }

  def time[R](block: => Unit): Long = {
    val t0 = System.nanoTime()
    block
    val t1 = System.nanoTime()
    t1 - t0
  }
}
