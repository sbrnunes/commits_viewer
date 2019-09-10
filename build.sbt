import Dependencies._

ThisBuild / scalaVersion     := "2.12.8"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.acme"
ThisBuild / organizationName := "acme"

lazy val root = (project in file("."))
  .settings(
    name := "commits_viewer",
    test in assembly := {},
    assemblyOutputPath in assembly := baseDirectory.value / "dist" / "commits-viewer.jar",
    libraryDependencies ++= Seq(
      scalaTest,
      scalaMock,
      logback,
      typesafeConfig,
      typesafeLogging,
      json4s,
      akkaHttp,
      akkaHttpTestKit,
      akkaTestKit,
      akkaStream,
      akkaHttpJson4s,
      github4s,
      cats,
      scallop
    )
  )
