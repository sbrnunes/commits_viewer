package com.acme.commitviewer.cli

import java.net.URL

import com.acme.commitviewer.model.{Commit, Page}
import com.acme.commitviewer.util.{Error, LocalRepo, Logging}
import github4s.Github
import github4s.Github._
import github4s.free.domain.Pagination
import github4s.jvm.Implicits._
import scalaj.http.HttpResponse

import scala.reflect.io.Directory

trait GitHubClient {
  def listCommits(repo: URL, limit: Int, page: Int): Either[Error, Page]
}

class GitHubCLI(cachedReposRoot: Directory)(implicit git: GitCLI) extends GitHubClient with Logging {

  def listCommits(repo: URL, limit: Int, page: Int): Either[Error, Page] = {
    val cachedRepo = LocalRepo.cachedRepoFor(repo, cachedReposRoot)

    for {
      _ <- git.clone(repo, cachedRepo)
      _ <- git.pull(repo, cachedRepo)
      commits <- git.log(repo, cachedRepo, limit, limit * (page - 1))
    } yield {
      Page(commits, nextPage = commits.size >= limit)
    }
  }
}

object GitHubCLI {
  def apply(cachedReposRoot: Directory)(implicit git: GitCLI): GitHubCLI = {
    new GitHubCLI(cachedReposRoot)
  }
}

class GitHubApi(implicit api: Github) extends GitHubClient with Logging {

  def listCommits(repo: URL, limit: Int, page: Int): Either[Error, Page] = {
    GitHubApi.extractOwnerAndRepo(repo) match {
      case Some((owner, repo)) =>
        val listCommits = api.repos.listCommits(owner, repo, pagination = Some(GitHubApi.pageFrom(limit, page)))
        listCommits.exec[cats.Id, HttpResponse[String]]() match {
          case Left(e) =>
            Left(Error(e.getMessage))
          case Right(r) =>
            val commits = r.result.map { commit =>
              Commit(
                ref = commit.sha,
                author_email = None,
                author_url = commit.author_url,
                login = commit.login,
                date = commit.date,
                subject = commit.message)
            }
            Right(Page(commits, nextPage = commits.size >= limit))
        }
      case None =>
        Left(Error("Could not extract Owner and Repo from URL"))
    }
  }
}

object GitHubApi {

  def extractOwnerAndRepo(repo: URL): Option[(String, String)] = {
    repo.getPath.stripPrefix("/").stripSuffix(".git").split("/").toList match {
      case owner :: repo :: Nil => Some((owner, repo))
      case _ => None
    }
  }

  def pageFrom(limit: Int, page: Int): Pagination = {
    Pagination(page = page, per_page = limit)
  }

  def apply()(implicit api: Github): GitHubApi = {
    new GitHubApi
  }
}
