import sbt._

object Dependencies {
  lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.0.8" % Test

  lazy val scalaMock = "org.scalamock" %% "scalamock" % "4.4.0" % Test

  lazy val logback = "ch.qos.logback" % "logback-classic" % "1.2.3"

  lazy val typesafeConfig = "com.typesafe" % "config" % "1.3.4"

  lazy val typesafeLogging = "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2"

  lazy val json4s = "org.json4s" %% "json4s-jackson" % "3.6.7"
}
