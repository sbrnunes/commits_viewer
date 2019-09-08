package com.acme.commitviewer.http

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.acme.commitviewer.cli.GitCLI
import com.acme.commitviewer.config.Settings

import scala.concurrent.ExecutionContext

object DiagnosticRoutes {

  def ping(implicit ec: ExecutionContext): Route = {
    path("ping") {
      get {
        complete(StatusCodes.OK -> "Pong")
      }
    }
  }

  def apply()(implicit ec: ExecutionContext, settings: Settings, git: GitCLI): Route = {
    ping
  }
}
