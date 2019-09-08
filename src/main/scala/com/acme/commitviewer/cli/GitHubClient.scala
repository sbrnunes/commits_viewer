package com.acme.commitviewer.cli

import java.net.URL

import com.acme.commitviewer.model.Commit
import com.acme.commitviewer.util.{Error, Logging, MD5}

import scala.reflect.io.Directory

trait GitHubClient {
  def listCommits(limit: Int): Either[Error, List[Commit]]
}

class GitHubCLI(repo: URL, cachedRepo: Directory)(implicit git: GitCLI) extends GitHubClient with Logging {

  def listCommits(limit: Int): Either[Error, List[Commit]] = {
    for {
      _ <- git.clone(repo, cachedRepo)
      _ <- git.pull(repo, cachedRepo)
      commits <- git.log(repo, cachedRepo, limit)
    } yield commits
  }
}

object GitHubCLI {
  def apply(repo: URL, cachedRepos: Directory)(implicit git: GitCLI): GitHubClient = {
    val cachedRepo = cachedRepos / Directory(MD5.digest(repo.toString))
    new GitHubCLI(repo, cachedRepo)
  }
}
