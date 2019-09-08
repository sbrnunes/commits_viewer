package com.acme.commitviewer.cli

import java.net.URL

import com.acme.commitviewer.model.Commit
import com.acme.commitviewer.util.{Error, LocalRepo, Logging, MD5}

import scala.reflect.io.Directory

trait GitHubClient {
  def listCommits(repo: URL, limit: Int): Either[Error, List[Commit]]
}

class GitHubCLI(cachedReposRoot: Directory)(implicit git: GitCLI) extends GitHubClient with Logging {

  def listCommits(repo: URL, limit: Int): Either[Error, List[Commit]] = {
    val cachedRepo = LocalRepo.cachedRepoFor(repo, cachedReposRoot)

    for {
      _ <- git.clone(repo, cachedRepo)
      _ <- git.pull(repo, cachedRepo)
      commits <- git.log(repo, cachedRepo, limit)
    } yield commits
  }
}

object GitHubCLI {
  def apply(cachedReposRoot: Directory)(implicit git: GitCLI): GitHubClient = {
    new GitHubCLI(cachedReposRoot)
  }
}
