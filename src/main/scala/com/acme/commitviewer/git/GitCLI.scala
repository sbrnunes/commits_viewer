package com.acme.commitviewer.git

import java.net.URL

import com.acme.commitviewer.model.Commit
import com.acme.commitviewer.util.{Json4s, Logging}

import scala.reflect.io.{Directory, File}

class GitCLI(implicit cli: CLI) extends Logging {

  def clone(repo: URL, cachedRepo: Directory): Either[Throwable, _] = {
    if(!cachedRepo.exists || cachedRepo.isEmpty) {
      logger.info(s"Cloning repo: ${repo.toString} into ${cachedRepo.path}")
      cli.exec(s"git clone ${repo.toString} ${cachedRepo.path}")
    } else {
      val gitDir = cachedRepo / Directory(".git")
      if(gitDir.exists) {
        logger.info(s"Repo ${repo.toString} already exists in ${cachedRepo.path}. Skipping...")
        Right()
      } else {
        Left(new Exception(s"Non empty directory already exists in ${cachedRepo.path} and is not a Git repository."))
      }
    }
  }

  def pull(repo: URL, cachedRepo: Directory): Either[Throwable, _] = {
    if(!cachedRepo.exists || cachedRepo.isEmpty) {
      Left(new Exception(s"Directory ${cachedRepo.path} does not exist or it is empty."))
    } else {
      val gitDir = cachedRepo / Directory(".git")
      if(gitDir.exists) {
        logger.info(s"Updating ${cachedRepo.path}")
        cli.exec(
          s"cd ${cachedRepo.path}",
          "git pull")
      } else {
        Left(new Exception(s"Directory ${cachedRepo.path} is not a Git repository."))
      }
    }
  }

  def log(repo: URL, cachedRepo: Directory, limit: Int): Either[Throwable, List[Commit]] = {
    if(!cachedRepo.exists || cachedRepo.isEmpty) {
      Left(new Exception(s"Directory ${cachedRepo.path} does not exist or it is empty."))
    } else {
      val gitDir = cachedRepo / Directory(".git")
      if(gitDir.exists) {
        logger.info(s"Listing commits for: ${cachedRepo.path}")
        cli.exec(
          s"cd ${cachedRepo.path}",
          s"""git log -n $limit --pretty=format:'${GitCLI.CommitFormat}'"""
        ) map { output =>
          output.map(Json4s.fromJson[Commit])
        }
      } else {
        Left(new Exception(s"Directory ${cachedRepo.path} is not a Git repository."))
      }
    }
  }
}

object GitCLI {
  val CommitFormat = """{ "ref":"%H","author_name":"%an","author_email":"%ae","date":"%at","subject":"%s" }"""

  def apply(implicit cli: CLI): GitCLI = new GitCLI
}