package com.acme.commitviewer.http

import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.http.javadsl.server.RouteResult
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.RouteResult.{Complete, Rejected}
import akka.http.scaladsl.server.directives.DebuggingDirectives._
import akka.http.scaladsl.server.directives.{DebuggingDirectives, LoggingMagnet}
import akka.stream.ActorMaterializer
import com.acme.commitviewer.cli.{CLI, GitCLI}
import com.acme.commitviewer.config.Settings
import com.acme.commitviewer.util.Logging

import scala.concurrent.ExecutionContextExecutor
import scala.util.{Failure, Success}

object CommitViewer extends App with Logging{
  implicit val system: ActorSystem = ActorSystem("commit_viewer")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher
  implicit val cli: CLI.type = CLI
  implicit val git: GitCLI = GitCLI(cli)
  implicit val settings: Settings = Settings()

  def accessLogger(logger: LoggingAdapter)(request: HttpRequest)(result: RouteResult): Unit = {
    result match {
      case Complete(response) =>
        logger.info(s"Completed ${request.method.value} ${request.uri} ${response.status}")
      case Rejected(rejections) =>
        logger.info(s"Rejected ${request.method.value} ${request.uri} ${rejections.mkString(",")}")
    }
  }

  val routes: Seq[Route] = Seq(
    DiagnosticRoutes()
  ) map { route =>
    logRequestResult(LoggingMagnet(accessLogger))(route)
  }

  Http().bindAndHandle(concat(routes:_*), "127.0.0.1", 8888).onComplete {
    case Success(_) =>
      logger.info(s"Server listening at 127.0.0.1:8888")
    case Failure(ex) =>
      logger.error("Server failed to start!", ex)
  }
}
