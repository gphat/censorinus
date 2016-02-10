import java.lang.Thread
import java.util.concurrent.{Executors,ScheduledThreadPoolExecutor}
import org.scalatest._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Seconds, Span}
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.Future
import github.gphat.censorinus.{Client,Metric,MetricSender}

class TestSender extends MetricSender {
  val metrics = new ArrayBuffer[Metric]()

  def send(metric: Metric): Unit = {
    metrics.append(metric)
  }

  def getMetrics = metrics
}

class ClientSpec extends FlatSpec with Matchers with ScalaFutures {

  implicit val defaultPatience = PatienceConfig(timeout = Span(10, Seconds), interval = Span(50, Millis))

  "Client" should "do normal metric things" in {
    val sender = new TestSender()
    val client = new Client(sender)

    client.gauge("foobar", 1.0)
    // Pause to let things catch up
    Thread.sleep(200)
    val m = sender.getMetrics(0)
    m.name should be ("foobar")
    m.value should be (1.0)
  }

  // it should "silently handle candidate failures" in {
  //   val ex = new Experiment[String](name = Some("better_string"), control = slowOK, candidate = fastFail)
  //   whenReady(ex.perform)({ res =>
  //     res should be("OK")
  //   })
  // }
}
