import sbt._
import sbt.Keys._

object Publish {
  lazy val defaultRepo = Some(Resolver.file("file", new File("/Users/gphat/src/mvn-repo/releases")))

  def getPublishTo(snapshot: Boolean) = {
    val providedRepo =
      if(snapshot) {
        val url = sys.props.get("publish.snapshots.url")
        url.map("snapshots" at _)
      } else {
        val url = sys.props.get("publish.releases.url")
        url.map("releases" at _)
      }

    providedRepo orElse defaultRepo
  }

  lazy val settings = Seq(
    homepage := Some(url("http://github.com/gphat/censorinus")),
    publishMavenStyle := true,
    publishTo := getPublishTo(isSnapshot.value),
    publishArtifact in Test := false,
    pomIncludeRepository := Function.const(false),
    pomExtra := (
      <scm>
        <url>git@github.com:gphat/censorinus.git</url>
        <connection>scm:git:git@github.com:gphat/censorinus.git</connection>
      </scm>
      <developers>
        <developer>
          <name>Cory Watson</name>
          <email>github@onemogin.com</email>
          <organization>Cory Industries Ltd Inc</organization>
          <organizationUrl>https://onemogin.com</organizationUrl>
        </developer>
      </developers>)
  )

  lazy val skip = Seq(
    publishTo := getPublishTo(isSnapshot.value),
    publish := (),
    publishLocal := (),
    publishArtifact := false
  )
}
