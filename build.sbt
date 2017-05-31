organization := "com.github.gphat"

name := "censorinus"

scalaVersion := "2.12.2"
crossScalaVersions := Seq("2.10.6", "2.11.11", "2.12.2")

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")

resolvers ++= Seq("snapshots", "releases").map(Resolver.sonatypeRepo)

libraryDependencies += "org.scalactic" %% "scalactic" % "3.0.3" % Test
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.3" % Test

releasePublishArtifactsAction := PgpKeys.publishSigned.value

Publish.settings

scalastyleFailOnError := true
