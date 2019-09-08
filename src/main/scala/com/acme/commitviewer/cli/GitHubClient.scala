package com.acme.commitviewer.cli

import java.net.URL

import com.acme.commitviewer.model.{Commit, Page}
import com.acme.commitviewer.util.{Error, LocalRepo, Logging, MD5}

import scala.reflect.io.Directory

trait GitHubClient {
  def listCommits(repo: URL, limit: Int, skip: Int): Either[Error, Page]
}

class GitHubCLI(cachedReposRoot: Directory)(implicit git: GitCLI) extends GitHubClient with Logging {

  def listCommits(repo: URL, limit: Int, offset: Int): Either[Error, Page] = {
    val cachedRepo = LocalRepo.cachedRepoFor(repo, cachedReposRoot)

    for {
      _ <- git.clone(repo, cachedRepo)
      _ <- git.pull(repo, cachedRepo)
      commits <- git.log(repo, cachedRepo, limit, offset)
    } yield {
      Page(commits, nextOffset = offset + commits.size)
    }
  }
}

object GitHubCLI {
  def apply(cachedReposRoot: Directory)(implicit git: GitCLI): GitHubClient = {
    new GitHubCLI(cachedReposRoot)
  }
}
