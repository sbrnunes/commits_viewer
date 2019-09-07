package com.acme.commitviewer.git

import java.io.File
import java.net.URL

import com.acme.commitviewer.model.Commit
import com.acme.commitviewer.util.{Json4s, Logging}

class GitCLI(implicit cli: CLI) extends Logging {

  def clone(repo: URL, cachedRepo: File): Either[Throwable, _] = {
    logger.info(s"Cloning repo: ${repo.toString} into ${cachedRepo.getAbsolutePath}")
    cli.exec(s"git clone ${repo.toString} ${cachedRepo.getAbsolutePath}")
  }

  def pull(repo: URL, cachedRepo: File): Either[Throwable, _] = {
    logger.info(s"Updating ${cachedRepo.getAbsolutePath}")
    cli.exec(
      s"cd ${cachedRepo.getAbsolutePath}",
      "git pull"
    )
  }

  def log(repo: URL, cachedRepo: File, limit: Int): Either[Throwable, List[Commit]] = {
    logger.info(s"Listing commits for: ${cachedRepo.getAbsolutePath}")
    cli.exec(
      s"cd ${cachedRepo.getAbsolutePath}",
      s"""git log -n $limit --pretty=format:'${GitCLI.CommitFormat}'"""
    ) map { output =>
      output.map(Json4s.fromJson[Commit])
    }
  }
}

object GitCLI {
  val CommitFormat = """{ "ref":"%H","author_name":"%an","author_email":"%ae","date":"%at","subject":"%s" }"""

  def apply(implicit cli: CLI): GitCLI = new GitCLI
}