package github.gphat

package object censorinus {

  case class Metric(
    name: String,
    value: Double,
    metricType: String,
    sampleRate: Double = 1.0,
    tags: Seq[String] = Seq.empty
  )
}
