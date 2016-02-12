package github.gphat

package object censorinus {

  case class Metric(
    name: String,
    value: String,
    metricType: String,
    sampleRate: Double = 1.0,
    tags: Seq[String] = Seq.empty
  )
}
