package com.acme.commitviewer.cli

import java.net.URL
import java.time.Instant

import com.acme.commitviewer.model.Commit
import com.acme.commitviewer.util.{Error, MD5}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{BeforeAndAfterEach, Matchers, Outcome, fixture}

import scala.reflect.io.Directory

class GitCLISpec extends fixture.FunSpecLike with MockFactory with Matchers with BeforeAndAfterEach {
  val repo: URL = new URL("http://some.repo.test/repo")
  val cachedReposRoot: Directory = Directory("target/cached_repos")
  val cachedRepoDir: Directory = cachedReposRoot / Directory(MD5.digest(repo.toString))

  type FixtureParam = CLI

  def withFixture(test: OneArgTest): Outcome = {
    val cli: CLI = mock[CLI]
    test(cli)
  }

  def withExistingCachedRepo(test: Directory => Any) {
    cachedRepoDir.createDirectory()
    test(cachedRepoDir)
  }

  def withMissingCachedRepo(test: Directory => Any) {
    test(cachedRepoDir)
  }

  def withExistingGitDirectory(test: (Directory) => Any) {
    val directoryGit: Directory = cachedRepoDir / Directory(".git")
    directoryGit.createDirectory(force = true)
    test(directoryGit)
  }

  def withMissingGitDirectory(test: (Directory) => Any) {
    cachedRepoDir.createDirectory()
    test(cachedRepoDir)
  }

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    cachedReposRoot.createDirectory()
  }

  override protected def afterEach(): Unit = {
    super.afterEach()
    cachedReposRoot.deleteRecursively()
  }

  describe("clone") {

    it("should use the CLI to clone the given repository") { implicit cli: CLI =>
      withMissingCachedRepo { cachedRepo =>
        (cli.exec _ )
          .expects(Seq(s"git clone ${repo.toString} ${cachedRepo.path}"))
          .returning(Right(List.empty))

        val git = new GitCLI
        git.clone(repo, cachedRepo) should equal(Right(List.empty))
      }
    }

    it("should propagate any errors from the CLI") { implicit cli: CLI =>
      withMissingCachedRepo { cachedRepo =>
        val expected = Error("Some error")
        (cli.exec _ )
          .expects(Seq(s"git clone ${repo.toString} ${cachedRepo.path}"))
          .returning(Left(expected))

        val git = new GitCLI
        git.clone(repo, cachedRepo) should equal(Left(expected))
      }
    }
  }

  describe("pull") {

    it("should use the CLI to update the given repository") { implicit cli: CLI =>
      withExistingGitDirectory { directoryGit =>
        val cachedRepo = directoryGit.parent
        (cli.exec _ )
          .expects(Seq(s"cd ${cachedRepo}", "git reset --hard origin/master", "git pull"))
          .returning(Right(List.empty))

        val git = new GitCLI
        git.pull(repo, cachedRepo) should equal(Right(List.empty))
      }
    }

    it("should fail when the cached repo does not exist") { implicit cli: CLI =>
      withMissingCachedRepo { cachedRepo =>
        val expected = Left(Error(s"Directory ${cachedRepo.path} is not a valid Git repo."))
        (cli.exec _ )
          .expects(Seq(s"cd ${cachedRepo.path}", "git reset --hard origin/master", "git pull"))
          .never()

        val git = new GitCLI
        git.pull(repo, cachedRepo) should equal(expected)
      }
    }

    it("should fail when the cached repo is empty (no .git directory)") { implicit cli: CLI =>
      withMissingGitDirectory { cachedRepo =>
        val expected = Left(Error(s"Directory ${cachedRepo.path} is not a valid Git repo."))
        (cli.exec _ )
          .expects(Seq(s"cd ${cachedRepo.path}", "git reset --hard origin/master", "git pull"))
          .never()

        val git = new GitCLI
        git.pull(repo, cachedRepo) should equal(expected)
      }
    }

    it("should propagate any errors from the CLI") { implicit cli: CLI =>
      withExistingGitDirectory { directoryGit =>
        val cachedRepo = directoryGit.parent
        val expected = Error("Some error")
        (cli.exec _ )
          .expects(Seq(s"cd ${cachedRepo.path}", "git reset --hard origin/master", "git pull"))
          .returning(Left(expected))

        val git = new GitCLI
        git.pull(repo, cachedRepo) should equal(Left(expected))
      }
    }
  }

  describe("log") {
    val limit = 10
    val offset = 0
    val now = Instant.now.toEpochMilli.toString

    it("should use the CLI to fetch the commits from the given repository") { implicit cli: CLI =>
      withExistingGitDirectory { directoryGit =>
        val cachedRepo = directoryGit.parent

        val expected = Commit(
          "ref",
          author_name = Some("name"),
          author_email = Some("email"),
          date = now,
          subject = "subject"
        )

        (cli.exec _ )
          .expects(Seq(
            s"cd ${cachedRepo.path}",
            s"""git log --max-count=$limit --skip=$offset --pretty=format:'${GitCLI.CommitFormat}'"""))
          .returning(Right(List(
            s"""{ "ref":"ref","author_name":"name","author_email":"email","date":"$now","subject":"subject" }""")))

        val git = new GitCLI
        git.log(repo, cachedRepo, limit, offset) should equal(Right(List(expected)))
      }
    }

    it("should fail when the cached repo does not exist") { implicit cli: CLI =>
      withMissingCachedRepo { cachedRepo =>
        val expected = Left(Error(s"Directory ${cachedRepo.path} is not a valid Git repo."))

        (cli.exec _ )
          .expects(Seq(
            s"cd ${cachedRepo.path}",
            s"""git log --max-count=$limit --skip=$offset --pretty=format:'${GitCLI.CommitFormat}'"""))
          .never()

        val git = new GitCLI
        git.log(repo, cachedRepo, limit, offset) should equal(expected)
      }
    }

    it("should fail when the cached repo is empty") { implicit cli: CLI =>
      withMissingGitDirectory { cachedRepo =>
        val expected = Left(Error(s"Directory ${cachedRepo.path} is not a valid Git repo."))

        (cli.exec _)
          .expects(Seq(
            s"cd ${cachedRepo.path}",
            s"""git log --max-count=$limit --skip=$offset --pretty=format:'${GitCLI.CommitFormat}'"""))
          .never()

        val git = new GitCLI
        git.log(repo, cachedRepo, limit, offset) should equal(expected)
      }
    }

    it("should propagate any errors from the CLI") { implicit cli: CLI =>
      withExistingGitDirectory { directoryGit =>
        val cachedRepo = directoryGit.parent
        val expected = Error("Some error")

        (cli.exec _ )
          .expects(Seq(
            s"cd ${cachedRepo.path}",
            s"""git log --max-count=$limit --skip=$offset --pretty=format:'${GitCLI.CommitFormat}'"""))
          .returning(Left(expected))

        val git = new GitCLI
        git.log(repo, cachedRepo, limit, offset) should equal(Left(expected))
      }
    }
  }
}
