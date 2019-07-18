organization := "com.github.gphat"

name := "censorinus"

scalaVersion := "2.12.8"
crossScalaVersions := Seq("2.11.12", "2.12.8")

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

libraryDependencies ++= Seq(
  "org.scalactic" %% "scalactic" % "3.0.5",
  "org.scalatest" %% "scalatest" % "3.0.5",
  "org.scalacheck" %% "scalacheck" % "1.13.5"
).map(_ % Test)
