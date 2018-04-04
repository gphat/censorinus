organization := "com.github.gphat"

name := "censorinus"

scalaVersion := "2.12.4"
crossScalaVersions := Seq("2.11.11", "2.12.2")

scalacOptions ++= Seq(
 "-encoding", "UTF-8",
 "-language:_",
 "-unchecked",
 "-deprecation",
 "-feature",
 "-Xfatal-warnings",
 // "-Xlint",
 "-Yno-adapted-args",
 "-Ywarn-dead-code",
 "-Ywarn-numeric-widen",
 "-Ywarn-value-discard",
 "-Xfuture",
 "-Ywarn-unused-import",
 "-Ypartial-unification"
)

resolvers ++= Seq("snapshots", "releases").map(Resolver.sonatypeRepo)

libraryDependencies += "org.scalactic" %% "scalactic" % "3.0.3" % "test"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.3" % "test"
libraryDependencies += "org.scalacheck" %% "scalacheck" % "1.13.4" % "test"

releasePublishArtifactsAction := PgpKeys.publishSigned.value

Publish.settings

scalastyleFailOnError := true
