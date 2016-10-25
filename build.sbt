organization := "com.github.gphat"

name := "censorinus"

scalaVersion := "2.11.8"
crossScalaVersions := Seq("2.10.6", "2.11.8")

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")

resolvers ++= Seq("snapshots", "releases").map(Resolver.sonatypeRepo)

libraryDependencies += "org.scalactic" %% "scalactic" % "2.2.6" % "test"
libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.6" % "test"

releasePublishArtifactsAction := PgpKeys.publishSigned.value

Publish.settings

scalastyleFailOnError := true
