package com.acme.commitviewer.git

import java.io.File
import java.net.URL
import java.time.Instant

import com.acme.commitviewer.model.Commit
import com.acme.commitviewer.util.{Json4s, MD5}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpecLike, Matchers}

class GitCLISpec extends FunSpecLike with MockFactory with Matchers {
  val repo = new URL("http://some.repo.test/repo")
  val cachedReposRoot = new File(".")
  val cachedRepo = new File(cachedReposRoot, MD5.digest(repo.toString))

  describe("clone") {

    it("should use the CLI to clone the given repository") {
      implicit val cli = mock[CLI]
      (cli.exec _ )
        .expects(Seq(s"git clone ${repo.toString} ${cachedRepo.getAbsolutePath}"))
        .returning(Right(List.empty))

      val git = new GitCLI
      git.clone(repo, cachedRepo) should equal(Right(List.empty))
    }

    it("should propagate any errors from the CLI") {
      val expected = new Exception("Some error")

      implicit val cli = mock[CLI]
      (cli.exec _ )
        .expects(Seq(s"git clone ${repo.toString} ${cachedRepo.getAbsolutePath}"))
        .returning(Left(expected))

      val git = new GitCLI
      git.clone(repo, cachedRepo) should equal(Left(expected))
    }

    //TODO: ADD TEST FOR EXCEPTIONS
  }

  describe("pull") {

    it("should use the CLI to update the given repository") {
      implicit val cli = mock[CLI]
      (cli.exec _ )
        .expects(Seq(s"cd ${cachedRepo.getAbsolutePath}", "git pull"))
        .returning(Right(List.empty))

      val git = new GitCLI
      git.pull(repo, cachedRepo) should equal(Right(List.empty))
    }

    it("should propagate any errors from the CLI") {
      val expected = new Exception("Some error")

      implicit val cli = mock[CLI]
      (cli.exec _ )
        .expects(Seq(s"cd ${cachedRepo.getAbsolutePath}", "git pull"))
        .returning(Left(expected))

      val git = new GitCLI
      git.pull(repo, cachedRepo) should equal(Left(expected))
    }

    //TODO: ADD TEST FOR EXCEPTIONS
  }

  describe("log") {
    val limit = 10
    val now = Instant.now.toEpochMilli.toString

    it("should use the CLI to fetch the commits from the given repository") {
      val expected = Commit("ref", "name", "email", now, "subject")

      implicit val cli = mock[CLI]
      (cli.exec _ )
        .expects(Seq(
          s"cd ${cachedRepo.getAbsolutePath}",
          s"""git log -n $limit --pretty=format:'${GitCLI.CommitFormat}'"""))
        .returning(Right(List(s"""{ "ref":"${expected.ref}","author_name":"${expected.authorName}","author_email":"${expected.authorEmail}","date":"$now","subject":"${expected.subject}" }""")))

      val git = new GitCLI
      git.log(repo, cachedRepo, limit) should equal(Right(List(expected)))
    }

    it("should propagate any errors from the CLI") {
      val expected = new Exception("Some error")

      implicit val cli = mock[CLI]
      (cli.exec _ )
        .expects(Seq(
          s"cd ${cachedRepo.getAbsolutePath}",
          s"""git log -n $limit --pretty=format:'${GitCLI.CommitFormat}'"""))
        .returning(Left(expected))

      val git = new GitCLI
      git.log(repo, cachedRepo, limit) should equal(Left(expected))
    }

    //TODO: ADD TEST FOR EXCEPTIONS
  }
}
