import com.typesafe.sbt.SbtStartScript

import SbtStartScript.StartScriptKeys._

name := "plusonenews"

version := "0.1.0"

scalaVersion := "2.10.3"

seq(SbtStartScript.startScriptForClassesSettings: _*)

seq(Revolver.settings: _*)

scalacOptions ++= Seq(
  "-unchecked",
  "-deprecation",
  "-Xlint",
  "-Ywarn-dead-code",
  "-language:_",
  "-target:jvm-1.7",
  "-encoding", "UTF-8",
  "-feature"
)

parallelExecution in Test := false

resolvers ++= Seq(
  "spray" at "http://repo.spray.io/",
  "Spray Nightlies" at "http://nightlies.spray.io/",
  "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"
)

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.2.3",
  "com.typesafe.akka" %% "akka-slf4j" % "2.2.3",
  "io.spray" % "spray-can" % "1.2-20130712",
  "io.spray" % "spray-client" % "1.2-20130712",
  "io.spray" % "spray-routing" % "1.2-20130712",
  "io.spray" %% "spray-json" % "1.2.5",
  "com.typesafe.slick" %% "slick" % "2.0.0",
  "mysql" % "mysql-connector-java" % "5.1.28",
  "org.slf4j" % "slf4j-api" % "1.7.5",
  "org.slf4j" % "slf4j-simple" % "1.7.5",
  "joda-time" % "joda-time" % "2.3",
  "org.joda" % "joda-convert" % "1.5",
  "com.github.tototoshi" %% "slick-joda-mapper" % "1.0.0",
  "org.specs2" %% "specs2" % "2.2.2" % "test",
  "io.spray" % "spray-testkit" % "1.2.0" % "test",
  "com.typesafe.akka" %% "akka-testkit" % "2.2.3" % "test"
)
