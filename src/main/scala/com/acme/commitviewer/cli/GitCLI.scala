package com.acme.commitviewer.cli

import java.net.URL

import com.acme.commitviewer.model.Commit
import com.acme.commitviewer.util.{Error, Json4s, Logging}
import com.acme.commitviewer.util.LocalRepo._

import scala.reflect.io.Directory

class GitCLI(implicit cli: CLI) extends Logging {
  import GitCLI._

  def clone(repo: URL, cachedRepo: Directory): Either[Error, _] = {
    if(canCloneTo(cachedRepo)) {
      logger.info(s"Cloning repo: ${repo.toString} into ${cachedRepo.path}")
      cli.exec(s"git clone ${repo.toString} ${cachedRepo.path}")
    } else {
      if(existsCachedRepo(cachedRepo)) {
        logger.info(s"Repo ${repo.toString} already exists in ${cachedRepo.path}. Skipping...")
        Right()
      } else {
        Left(Error(s"Non empty directory already exists in ${cachedRepo.path} and is not a Git repository."))
      }
    }
  }

  def pull(repo: URL, cachedRepo: Directory): Either[Error, _] = {
    if(!existsCachedRepo(cachedRepo)) {
      Left(Error(s"Directory ${cachedRepo.path} is not a valid Git repo."))
    } else {
      logger.info(s"Updating ${cachedRepo.path}")
      cli.exec(
        s"cd ${cachedRepo.path}",
        "git reset --hard origin/master",
        "git pull"
      )
    }
  }

  def log(repo: URL, cachedRepo: Directory, limit: Int, offset: Int): Either[Error, List[Commit]] = {
    if(!existsCachedRepo(cachedRepo)) {
      Left(Error(s"Directory ${cachedRepo.path} is not a valid Git repo."))
    } else {
      logger.info(s"Listing commits for: ${cachedRepo.path}")
      cli.exec(
        s"cd ${cachedRepo.path}",
        s"""git log --max-count=$limit --skip=$offset --pretty=format:'${GitCLI.CommitFormat}'"""
      ) map { output =>
        output.map(Json4s.fromJson[Commit])
      }
    }
  }
}

object GitCLI {
  //TODO: breaks if the subject contains quotes, need to find a workaround
  val CommitFormat = """{ "ref":"%H","author_name":"%an","author_email":"%ae","date":"%at","subject":"%s" }"""

  def apply()(implicit cli: CLI): GitCLI = new GitCLI
}