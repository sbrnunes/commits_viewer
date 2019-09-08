package com.acme.commitviewer.http

import java.net.URL

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.acme.commitviewer.cli.GitHubClient
import com.acme.commitviewer.config.Settings
import com.acme.commitviewer.util.Json4s.JsonFormats._
import de.heikoseeberger.akkahttpjson4s.Json4sSupport._
import org.json4s.jackson.Serialization

import scala.concurrent.{ExecutionContext, Future, blocking}

object GitRoutes {
  implicit val serialization: Serialization.type = Serialization

  def getCommits(implicit ec: ExecutionContext, settings: Settings, client: GitHubClient): Route = {
    path("commits") {
      get {
        parameters("repository_url".as[String], "limit" ? settings.commitsDefaultLimit) { (repositoryUrl, limit) =>
          //TODO: validate URL, return 400 - Bad Request if not validz
          onSuccess(Future(blocking(client.listCommits(new URL(repositoryUrl), limit)))) {
            case Right(commits) => complete(StatusCodes.OK -> commits)
            case Left(error) => complete(StatusCodes.InternalServerError -> error)
          }
        }
      }
    }
  }

  def apply()(implicit ec: ExecutionContext, settings: Settings, client: GitHubClient): Route = {
    pathPrefix("git") {
      concat(getCommits)
    }
  }
}
