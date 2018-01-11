name := """test-app"""

version := "1.0"

scalaVersion := "2.12.4"

libraryDependencies ++= Seq(
  "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.6",
  "com.typesafe.akka" %% "akka-http" % "10.0.11",
  "com.typesafe.akka" %% "akka-http-spray-json" % "10.0.11",
  "com.typesafe.akka" %% "akka-testkit" % "2.4.20" % "test",
  "com.typesafe.akka" %% "akka-http-testkit" % "10.0.11" % "test",
  "org.scalatest" %% "scalatest" % "3.0.4" % "test")
