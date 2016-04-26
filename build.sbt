organization := "com.github.gphat"

name := "censorinus"

scalaVersion := "2.11.7"
crossScalaVersions := Seq("2.10.6", "2.11.8")

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature", "-Yrangepos")

resolvers ++= Seq("snapshots", "releases").map(Resolver.sonatypeRepo)

libraryDependencies += "org.scalactic" %% "scalactic" % "2.2.6" % "test"
libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.6" % "test"

Publish.settings

scalastyleFailOnError := true
