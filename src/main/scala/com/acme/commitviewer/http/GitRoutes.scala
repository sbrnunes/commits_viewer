package com.acme.commitviewer.http

import java.net.URL

import akka.actor.ActorSystem
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.acme.commitviewer.cli.GitHubClient
import com.acme.commitviewer.config.Settings
import com.acme.commitviewer.util.FutureImplicits._
import com.acme.commitviewer.util.Json4s.JsonFormats._
import de.heikoseeberger.akkahttpjson4s.Json4sSupport._
import org.json4s.jackson.Serialization

import scala.concurrent.{ExecutionContext, Future, TimeoutException, blocking}
import scala.util.{Failure, Success}

object GitRoutes {
  implicit val serialization: Serialization.type = Serialization

  def getCommits(implicit system: ActorSystem, settings: Settings, client: GitHubClient): Route = {
    implicit val ec: ExecutionContext = system.dispatcher
    path("commits") {
      get {
        parameters("repository_url".as[String], "limit" ? settings.commitsDefaultLimit) { (repositoryUrl, limit) =>
          //TODO: validate URL, return 400 - Bad Request if not valid
          onComplete {
            Future {
              blocking(client.listCommits(new URL(repositoryUrl), limit))
            } withTimeout(settings.requestDefaultTimeout)
          } {
            case Success(Right(commits)) =>
              complete(StatusCodes.OK -> commits)
            case Success(Left(error)) =>
              complete(StatusCodes.InternalServerError -> error)
            case Failure(ex: TimeoutException) =>
              complete(StatusCodes.RequestTimeout -> SimpleMessage(
                "Request is taking too long to complete. Please try again in a few moments."))
            case Failure(ex: Throwable) =>
              failWith(ex)
          }
        }
      }
    }
  }

  def apply()(implicit system: ActorSystem, settings: Settings, client: GitHubClient): Route = {
    pathPrefix("git") {
      concat(getCommits)
    }
  }
}
