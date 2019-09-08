package com.acme.commitviewer.http

import java.net.URL
import java.time.Instant

import akka.http.scaladsl.model.{ContentTypes, StatusCodes}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.acme.commitviewer.cli.GitHubClient
import com.acme.commitviewer.config.Settings
import com.acme.commitviewer.model.Commit
import com.acme.commitviewer.util.Error
import com.acme.commitviewer.util.Json4s.JsonFormats._
import de.heikoseeberger.akkahttpjson4s.Json4sSupport._
import org.json4s.jackson.Serialization
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpecLike, Matchers}

class GitRoutesSpec extends FunSpecLike with ScalatestRouteTest with MockFactory with Matchers {

  implicit val serialization = Serialization

  describe("getCommits") {
    implicit val settings: Settings = Settings()
    val repo = new URL("http://some.test.url/repo")
    val limit = 10

    it("returns a list of commits") {
      val expected = List(Commit("ref", "name", "email", Instant.now.toEpochMilli.toString, "subject"))
      implicit val client: GitHubClient = mock[GitHubClient]

      (client.listCommits _ ).expects(repo, limit).returning(Right(expected))

      Get(s"/git/commits?repository_url=${repo.toString}&limit=10") ~> GitRoutes() ~> check {
        status should equal(StatusCodes.OK)
        contentType should be(ContentTypes.`application/json`)
        responseAs[List[Commit]] should equal(expected)
      }
    }

    it("treats 'limit' as an optional parameter, used default from settings if missing") {
      val expected = List(Commit("ref", "name", "email", Instant.now.toEpochMilli.toString, "subject"))
      implicit val client: GitHubClient = mock[GitHubClient]

      (client.listCommits _ ).expects(repo, limit).returning(Right(expected))

      Get(s"/git/commits?repository_url=${repo.toString}") ~> GitRoutes() ~> check {
        status should equal(StatusCodes.OK)
        contentType should be(ContentTypes.`application/json`)
        responseAs[List[Commit]] should equal(expected)
      }
    }

    it("returns a 500 - Internal Server Error if the Github client could not fetch the commits") {
      val expected = Error("Some error")
      implicit val client: GitHubClient = mock[GitHubClient]

      (client.listCommits _ ).expects(repo, limit).returning(Left(expected))

      Get(s"/git/commits?repository_url=${repo.toString}&limit=10") ~> GitRoutes() ~> check {
        status should equal(StatusCodes.InternalServerError)
        contentType should be(ContentTypes.`application/json`)
        responseAs[Error] should equal(expected)
      }
    }
  }
}
