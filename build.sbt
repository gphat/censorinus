organization := "com.github.gphat"

name := "censorinus"

scalaVersion := "2.12.5"
crossScalaVersions := Seq("2.11.12", "2.12.5")

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

libraryDependencies += "org.scalactic" %% "scalactic" % "3.0.5" % Test
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.5" % Test
libraryDependencies += "org.scalacheck" %% "scalacheck" % "1.13.5" % Test

releasePublishArtifactsAction := PgpKeys.publishSigned.value

Publish.settings

scalastyleFailOnError := true
