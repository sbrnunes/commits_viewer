package com.acme.commitviewer.cli

import java.net.URL
import java.time.Instant

import com.acme.commitviewer.model.{Commit, Page}
import com.acme.commitviewer.util.{Error, MD5}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{Matchers, Outcome, fixture}

import scala.reflect.io.Directory

class GitHubCLISpec extends fixture.FunSpecLike with MockFactory with Matchers {
  val repo = new URL("http://some.repo.test/repo")
  val cachedReposRoot = Directory("cached_repos")
  val cachedRepo = cachedReposRoot / Directory(MD5.digest(repo.toString))
  val limit = 10
  val offset = 0

  type FixtureParam = GitCLI

  def withFixture(test: OneArgTest): Outcome = {
    val git: GitCLI = mock[GitCLI]
    test(git)
  }

  describe("listCommits") {

    it("should use the Git CLI to setup the cached repo and fetch the list of commits") { implicit git: GitCLI =>
      val expected = List(
        Commit(
          "ref",
          author_name = Some("name"),
          author_email = Some("email"),
          date = Instant.now.toEpochMilli.toString,
          subject = "subject"))

      (git.clone _ ).expects(repo, cachedRepo).returning(Right(true))
      (git.pull _ ).expects(repo, cachedRepo).returning(Right(true))
      (git.log _ ).expects(repo, cachedRepo, limit, offset).returning(Right(expected))

      val client = GitHubCLI(cachedReposRoot)
      client.listCommits(repo, limit, offset) should equal(Right(Page(expected, nextOffset = 1)))
    }

    it("should fail if the repo could not be cloned") { implicit git: GitCLI =>
      val expected = Left(Error("Some error"))

      (git.clone _ ).expects(repo, cachedRepo).returning(expected)
      (git.pull _ ).expects(*, *).never()
      (git.log _ ).expects(*, *, *, *).never()

      val client = GitHubCLI(cachedReposRoot)
      client.listCommits(repo, limit, offset) should equal(expected)
    }

    it("should fail if the repo could not be updated") { implicit git: GitCLI =>
      val expected = Left(Error("Some error"))

      (git.clone _ ).expects(repo, cachedRepo).returning(Right(true))
      (git.pull _ ).expects(repo, cachedRepo).returning(expected)
      (git.log _ ).expects(*, *, *, *).never()

      val client = GitHubCLI(cachedReposRoot)
      client.listCommits(repo, limit, offset) should equal(expected)
    }

    it("should fail if the list of commits could not be retrieved") { implicit git: GitCLI =>
      val expected = Left(Error("Some error"))

      (git.clone _ ).expects(repo, cachedRepo).returning(Right(true))
      (git.pull _ ).expects(repo, cachedRepo).returning(Right(true))
      (git.log _ ).expects(repo, cachedRepo, limit, offset).returning(expected)

      val client = GitHubCLI(cachedReposRoot)
      client.listCommits(repo, limit, offset) should equal(expected)
    }
  }
}
