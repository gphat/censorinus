package github.gphat.censorinus

import github.gphat.censorinus.dogstatsd.Encoder

import java.util.concurrent._
import scala.util.Random

/** A DStatsD client! You should create one of these and reuse it across
  * your application.
  * @constructor Creates a new client instance
  * @param hostname the host to send metrics to, defaults to localhost
  * @param port the port to send metrics to, defaults to 8125
  * @param defaultSampleRate A sample rate default to be used for all metric methods. Defaults to 1.0
  * @param flushInterval How often in milliseconds to flush the local buffer to keep things async. Defaults to 100ms
  * @param asynchronous True if you want the client to asynch, false for blocking!
  */
class DogStatsDClient(
  hostname: String = "localhost",
  port: Int = 8125,
  defaultSampleRate: Double = 1.0,
  flushInterval: Long = 100L,
  asynchronous: Boolean = true
) extends Client(
  sender = new UDPSender(hostname = hostname, port = port),
  encoder = Encoder,
  defaultSampleRate = defaultSampleRate,
  flushInterval = flushInterval,
  asynchronous = asynchronous
)
