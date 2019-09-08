package com.acme.commitviewer.git

import java.net.URL
import java.time.Instant

import com.acme.commitviewer.model.Commit
import com.acme.commitviewer.util.MD5
import org.scalamock.scalatest.MockFactory
import org.scalatest.{BeforeAndAfterEach, FunSpecLike, Matchers}

import scala.reflect.io.Directory

class GitCLISpec extends FunSpecLike with MockFactory with Matchers with BeforeAndAfterEach {
  val repo = new URL("http://some.repo.test/repo")
  val cachedReposRoot = Directory("cached_repos")
  val cachedRepo = cachedReposRoot / Directory(MD5.digest(repo.toString))
  val cachedRepoGit = cachedReposRoot / Directory(MD5.digest(repo.toString)) / Directory(".git")

  override protected def beforeEach(): Unit = {
    super.beforeEach()
//    cachedReposRoot.createDirectory()
//    cachedRepo.createDirectory()
    cachedRepoGit.createDirectory()
  }

  override protected def afterEach(): Unit = {
    super.afterEach()
    cachedReposRoot.deleteRecursively()
  }

  describe("clone") {

    it("should use the CLI to clone the given repository") {
      implicit val cli = mock[CLI]
      (cli.exec _ )
        .expects(Seq(s"git clone ${repo.toString} ${cachedRepo.path}"))
        .returning(Right(List.empty))

      val git = new GitCLI
      git.clone(repo, cachedRepo) should equal(Right(List.empty))
    }

    it("should propagate any errors from the CLI") {
      val expected = new Exception("Some error")

      implicit val cli = mock[CLI]
      (cli.exec _ )
        .expects(Seq(s"git clone ${repo.toString} ${cachedRepo.path}"))
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
        .expects(Seq(s"cd ${cachedRepo.path}", "git pull"))
        .returning(Right(List.empty))

      val git = new GitCLI
      git.pull(repo, cachedRepo) should equal(Right(List.empty))
    }

    it("should fail when the cached repo does not exist") {
      implicit val cli = mock[CLI]
      (cli.exec _ )
        .expects(Seq(s"cd ${cachedRepo.path}", "git pull"))
        .returning(Right(List.empty))

      val git = new GitCLI
      git.pull(repo, cachedRepo) should equal(Left())
    }

    it("should propagate any errors from the CLI") {
      val expected = new Exception("Some error")

      implicit val cli = mock[CLI]
      (cli.exec _ )
        .expects(Seq(s"cd ${cachedRepo.path}", "git pull"))
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
          s"cd ${cachedRepo.path}",
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
          s"cd ${cachedRepo.path}",
          s"""git log -n $limit --pretty=format:'${GitCLI.CommitFormat}'"""))
        .returning(Left(expected))

      val git = new GitCLI
      git.log(repo, cachedRepo, limit) should equal(Left(expected))
    }

    //TODO: ADD TEST FOR EXCEPTIONS
  }
}
