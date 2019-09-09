package com.acme.commitviewer.http

import java.net.URL

import akka.actor.ActorSystem
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import cats.syntax.either._
import com.acme.commitviewer.cli.{GitHubApi, GitHubCLI}
import com.acme.commitviewer.config.Settings
import com.acme.commitviewer.util.FutureImplicits._
import com.acme.commitviewer.util.Json4s.JsonFormats._
import com.acme.commitviewer.util.Logging
import de.heikoseeberger.akkahttpjson4s.Json4sSupport._
import org.json4s.jackson.Serialization

import scala.concurrent.{ExecutionContext, Future, TimeoutException, blocking}
import scala.util.{Failure, Success}

object GitRoutes extends Logging {
  implicit val serialization: Serialization.type = Serialization

  def getCommits(implicit system: ActorSystem, settings: Settings, api: GitHubApi, cli: GitHubCLI): Route = {
    implicit val ec: ExecutionContext = system.dispatcher
    path("commits") {
      get {
        parameters("repository_url".as[String], "limit" ? settings.commitsDefaultLimit, "offset" ? 0) {
          (repositoryUrl, limit, offset) =>
            onComplete {
              Future {
                blocking {
                  val repo = new URL(repositoryUrl) //TODO: Validate the URL and return a 400
                  api.listCommits(repo, limit, offset)
                    .recoverWith {
                      case error =>
                        logger.error(s"Could not fetch commits using the GitHub API: ${error.format}")
                        logger.info("Attempting fallback using the CLI...")
                        cli.listCommits(repo, limit, offset)
                    }
                }
              } withTimeout (settings.requestDefaultTimeout)
            } {
              case Success(Right(page)) =>
                complete(StatusCodes.OK -> page)
              case Success(Left(error)) =>
                complete(StatusCodes.InternalServerError -> error)
              case Failure(ex: TimeoutException) =>
                complete(StatusCodes.RequestTimeout -> SimpleMessage(
                  "Request is taking too long to complete. Please try again in a few moments."))
              case Failure(ex: Throwable) => failWith(ex)
            }
        }
      }
    }
  }

  def apply()(implicit system: ActorSystem, settings: Settings, api: GitHubApi, cli: GitHubCLI): Route = {
    pathPrefix("git") {
      concat(getCommits)
    }
  }
}
