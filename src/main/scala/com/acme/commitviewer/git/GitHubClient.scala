package com.acme.commitviewer.git

import java.io.File
import java.net.URL

import com.acme.commitviewer.model.Commit
import com.acme.commitviewer.util.{Logging, MD5}

trait GitHubClient {
  def listCommits(limit: Int): Either[Throwable, List[Commit]]
}

class GitHubCLI(repo: URL, cachedRepo: File)(implicit git: GitCLI) extends GitHubClient with Logging {

  def listCommits(limit: Int): Either[Throwable, List[Commit]] = {
    for {
      _ <- git.clone(repo, cachedRepo)
      _ <- git.pull(repo, cachedRepo)
      commits <- git.log(repo, cachedRepo, limit)
    } yield commits
  }
}

object GitHubCLI {
  def apply(repo: URL, cachedRepos: File)(implicit git: GitCLI): GitHubClient = {
    val cachedRepo: File = new File(cachedRepos, MD5.digest(repo.toString))
    new GitHubCLI(repo, cachedRepo)
  }
}
