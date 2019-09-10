import sbt._

object Dependencies {
  lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.0.8" % Test

  lazy val scalaMock = "org.scalamock" %% "scalamock" % "4.4.0" % Test

  lazy val logback = "ch.qos.logback" % "logback-classic" % "1.2.3"

  lazy val typesafeConfig = "com.typesafe" % "config" % "1.3.4"

  lazy val typesafeLogging = "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2"

  lazy val json4s = "org.json4s" %% "json4s-jackson" % "3.6.7"

  lazy val akkaHttp = "com.typesafe.akka" %% "akka-http"   % "10.1.9"

  lazy val akkaHttpTestKit = "com.typesafe.akka" %% "akka-http-testkit"   % "10.1.9" % Test

  lazy val akkaStream = "com.typesafe.akka" %% "akka-stream" % "2.5.23"

  lazy val akkaTestKit = "com.typesafe.akka" %% "akka-testkit" % "2.5.23" % Test

  lazy val akkaHttpJson4s = "de.heikoseeberger" %% "akka-http-json4s" % "1.27.0"

  lazy val github4s = "com.47deg" %% "github4s" % "0.20.1"

  lazy val cats = "org.typelevel" %% "cats-core" % "2.0.0-RC1"

  lazy val scallop = "org.rogach" %% "scallop" % "3.3.1"
}
