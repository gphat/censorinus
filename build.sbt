organization := "com.github.gphat"

name := "censorinus"

scalaVersion := "2.13.1"
crossScalaVersions := Seq("2.11.12", "2.12.10", "2.13.1")

scalacOptions ++= Seq(
 "-encoding", "UTF-8",
 "-language:_",
 "-unchecked",
 "-deprecation",
 "-feature",
 // "-Xlint",
 "-Ywarn-dead-code",
 "-Ywarn-numeric-widen",
 "-Ywarn-value-discard"
 ) ++ (CrossVersion.partialVersion(scalaVersion.value) match {
   case Some((2, x)) if x <= 12 => Seq(
     "-Yno-adapted-args",
     "-Ywarn-unused-import",
     "-Ypartial-unification",
     "-Xfuture",
     "-Xfatal-warnings",
     "-Xsource:2.13"
   )
   case _ => Seq.empty
 })

resolvers ++= Seq("snapshots", "releases").map(Resolver.sonatypeRepo)

libraryDependencies ++= Seq(
  "org.scalactic" %% "scalactic" % "3.1.0" % Test,
  "org.scalatest" %% "scalatest" % "3.1.0" % Test,
  "org.scalatestplus" %% "scalacheck-1-14" % "3.1.0.0" % Test,
  "org.scalacheck" %% "scalacheck" % "1.14.3" % Test
)

releasePublishArtifactsAction := PgpKeys.publishSigned.value

Publish.settings

scalastyleFailOnError := true
