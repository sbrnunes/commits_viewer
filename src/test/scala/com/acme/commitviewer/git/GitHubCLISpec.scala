package com.acme.commitviewer.git

import java.io.File
import java.net.URL
import java.time.Instant

import com.acme.commitviewer.model.Commit
import com.acme.commitviewer.util.{Error, MD5}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpecLike, Matchers}

import scala.reflect.io.Directory

class GitHubCLISpec extends FunSpecLike with MockFactory with Matchers {

  describe("listCommits") {
    val repo = new URL("http://some.repo.test/repo")
    val cachedReposRoot = Directory("cached_repos")
    val cachedRepo = cachedReposRoot / Directory(MD5.digest(repo.toString))
    val limit = 10

    it("should use the Git CLI to setup the cached repo and fetch the list of commits") {
      val expected = List(Commit("ref", "name", "email", Instant.now.toEpochMilli.toString, "subject"))

      implicit val git = mock[GitCLI]
      (git.clone _ ).expects(repo, cachedRepo).returning(Right(true))
      (git.pull _ ).expects(repo, cachedRepo).returning(Right(true))
      (git.log _ ).expects(repo, cachedRepo, limit).returning(Right(expected))

      val client = GitHubCLI(repo, cachedReposRoot)
      client.listCommits(limit) should equal(Right(expected))
    }

    it("should fail if the repo could not be cloned") {
      val expected = Left(Error("Some error"))

      implicit val git: GitCLI = mock[GitCLI]
      (git.clone _ ).expects(repo, cachedRepo).returning(expected)
      (git.pull _ ).expects(*, *).never()
      (git.log _ ).expects(*, *, *).never()

      val client = GitHubCLI(repo, cachedReposRoot)
      client.listCommits(limit) should equal(expected)
    }

    it("should fail if the repo could not be updated") {
      val expected = Left(Error("Some error"))

      implicit val git: GitCLI = mock[GitCLI]
      (git.clone _ ).expects(repo, cachedRepo).returning(Right(true))
      (git.pull _ ).expects(repo, cachedRepo).returning(expected)
      (git.log _ ).expects(*, *, *).never()

      val client = GitHubCLI(repo, cachedReposRoot)
      client.listCommits(limit) should equal(expected)
    }

    it("should fail if the list of commits could not be retrieved") {
      val expected = Left(Error("Some error"))

      implicit val git: GitCLI = mock[GitCLI]
      (git.clone _ ).expects(repo, cachedRepo).returning(Right(true))
      (git.pull _ ).expects(repo, cachedRepo).returning(Right(true))
      (git.log _ ).expects(repo, cachedRepo, limit).returning(expected)

      val client = GitHubCLI(repo, cachedReposRoot)
      client.listCommits(limit) should equal(expected)
    }
  }
}
