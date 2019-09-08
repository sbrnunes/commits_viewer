package com.acme.commitviewer.http

import java.net.URL
import java.time.Instant

import akka.http.scaladsl.model.{ContentTypes, StatusCodes}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.acme.commitviewer.cli.GitHubClient
import com.acme.commitviewer.config.Settings
import com.acme.commitviewer.model.{Commit, Page}
import com.acme.commitviewer.util.Error
import com.acme.commitviewer.util.Json4s.JsonFormats._
import com.typesafe.config.ConfigFactory
import de.heikoseeberger.akkahttpjson4s.Json4sSupport._
import org.json4s.jackson.Serialization
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpecLike, Matchers}

import scala.collection.JavaConverters._

class GitRoutesSpec extends FunSpecLike with ScalatestRouteTest with MockFactory with Matchers {

  implicit val serialization = Serialization

  describe("getCommits") {
    val repo = new URL("http://some.test.url/repo")
    val limit = 10
    val offset = 0

    it("returns a list of commits") {
      implicit val settings: Settings = Settings()
      val expected = Page(List(Commit("ref", "name", "email", Instant.now.toEpochMilli.toString, "subject")))
      implicit val client: GitHubClient = mock[GitHubClient]

      (client.listCommits _ ).expects(repo, limit, offset).returning(Right(expected))

      Get(s"/git/commits?repository_url=${repo.toString}&limit=$limit&offset=$offset") ~> GitRoutes() ~> check {
        status should equal(StatusCodes.OK)
        contentType should be(ContentTypes.`application/json`)
        responseAs[Page] should equal(expected)
      }
    }

    it("treats 'limit' as an optional parameter, uses default from settings if missing") {
      implicit val settings: Settings = Settings()
      val expected = Page(List(Commit("ref", "name", "email", Instant.now.toEpochMilli.toString, "subject")))
      implicit val client: GitHubClient = mock[GitHubClient]

      (client.listCommits _ ).expects(repo, limit, offset).returning(Right(expected))

      Get(s"/git/commits?repository_url=${repo.toString}") ~> GitRoutes() ~> check {
        status should equal(StatusCodes.OK)
        contentType should be(ContentTypes.`application/json`)
        responseAs[Page] should equal(expected)
      }
    }

    it("treats 'offset' as an optional parameter, uses 0 as default if missing") {
      implicit val settings: Settings = Settings()
      val expected = Page(List(Commit("ref", "name", "email", Instant.now.toEpochMilli.toString, "subject")))
      implicit val client: GitHubClient = mock[GitHubClient]

      (client.listCommits _ ).expects(repo, limit, offset).returning(Right(expected))

      Get(s"/git/commits?repository_url=${repo.toString}") ~> GitRoutes() ~> check {
        status should equal(StatusCodes.OK)
        contentType should be(ContentTypes.`application/json`)
        responseAs[Page] should equal(expected)
      }
    }

    it("returns a 500 - Internal Server Error if the Github client could not fetch the commits") {
      implicit val settings: Settings = Settings()
      val expected = Error("Some error")
      implicit val client: GitHubClient = mock[GitHubClient]

      (client.listCommits _ ).expects(repo, limit, offset).returning(Left(expected))

      Get(s"/git/commits?repository_url=${repo.toString}&limit=10") ~> GitRoutes() ~> check {
        status should equal(StatusCodes.InternalServerError)
        contentType should be(ContentTypes.`application/json`)
        responseAs[Error] should equal(expected)
      }
    }

    it("returns a 408 - Request Timeout if the requests could not be completed under the configured timeout") {
      val expected = Page(List(Commit("ref", "name", "email", Instant.now.toEpochMilli.toString, "subject")))
      val customConfig = ConfigFactory.parseMap(Map("http.requests.timeout" -> "1 millisecond").asJava)
      implicit val client: GitHubClient = mock[GitHubClient]
      implicit val settings: Settings = new Settings(customConfig.withFallback(ConfigFactory.load()))

      (client.listCommits _ ).expects(repo, limit, offset).onCall { _ =>
        Thread.sleep(100)
        Right(expected)
      }

      Get(s"/git/commits?repository_url=${repo.toString}&limit=10") ~> GitRoutes() ~> check {
        status should equal(StatusCodes.RequestTimeout)
        contentType should be(ContentTypes.`application/json`)
        responseAs[SimpleMessage] should equal(SimpleMessage(
          "Request is taking too long to complete. Please try again in a few moments."))
      }
    }
  }
}
