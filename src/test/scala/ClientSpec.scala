import java.lang.Thread
import java.util.concurrent.{Executors,ScheduledThreadPoolExecutor}
import org.scalatest._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Seconds, Span}
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.Future
import github.gphat.censorinus.{Client,Metric,MetricSender}

class TestSender extends MetricSender {
  val buffer = new ArrayBuffer[Metric]()

  def send(metric: Metric): Unit = {
    buffer.append(metric)
  }

  def getBuffer = buffer
}

class ClientSpec extends FlatSpec with Matchers with ScalaFutures {

  implicit val defaultPatience = PatienceConfig(timeout = Span(10, Seconds), interval = Span(50, Millis))

  "Client" should "deal with gauges" in {
    val client = new Client(sender = new TestSender())

    client.gauge("foobar", 1.0)
    val m = client.getQueue.poll
    m.name should be ("foobar")
    m.value should be (1.0)
    m.metricType should be ("g")
  }

  it should "deal with counters" in {
    val client = new Client(sender = new TestSender())

    client.counter("foobar", 1.0)
    val m = client.getQueue.poll
    m.name should be ("foobar")
    m.value should be (1.0)
    m.metricType should be ("c")
  }

  it should "deal with increments" in {
    val client = new Client(sender = new TestSender())

    client.increment("foobar")
    val m = client.getQueue.poll
    m.name should be ("foobar")
    m.value should be (1.0)
    m.metricType should be ("c")
  }

  it should "deal with decrements" in {
    val client = new Client(sender = new TestSender())

    client.increment("foobar")
    val m = client.getQueue.poll
    m.name should be ("foobar")
    m.value should be (1.0)
    m.metricType should be ("c")
  }

  it should "deal with histograms" in {
    val client = new Client(sender = new TestSender())

    client.histogram("foobar", 1.0)
    val m = client.getQueue.poll
    m.name should be ("foobar")
    m.value should be (1.0)
    m.metricType should be ("h")
  }

  it should "deal with meters" in {
    val client = new Client(sender = new TestSender())

    client.meter("foobar", 1.0)
    val m = client.getQueue.poll
    m.name should be ("foobar")
    m.value should be (1.0)
    m.metricType should be ("m")
  }
}
