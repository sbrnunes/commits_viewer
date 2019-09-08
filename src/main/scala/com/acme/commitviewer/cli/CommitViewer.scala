package com.acme.commitviewer.cli

import java.net.URL

import com.acme.commitviewer.config.Settings
import com.acme.commitviewer.util.Logging

import scala.reflect.io.Directory
import scala.util.Try

object CommitViewer extends App with Logging {

  def cleanCachedRepos(dir: Directory): Unit = {
    if(dir.exists) {
      logger.info(s"Deleting ${dir.path}")
      dir.deleteRecursively()
    }
  }

  override def main(args: Array[String]): Unit = {
    //args(0) -> repo url
    //args(1) -> limit

    implicit val cli = CLI
    implicit val git = GitCLI(cli)

    val settings = Settings()

    //TODO: extracts args in a more reliable way and handle failures
    val repositoryUrl: URL = new URL(args(0))
    val client = GitHubCLI(settings.cachedReposRoot)
    val limit = Try(args(1).toInt).toOption.getOrElse(settings.commitsDefaultLimit)

    client.listCommits(repositoryUrl, limit) match {
      case Right(commits) => commits.foreach(println)
      case Left(ex) => logger.error("Could not list commits for the given repository", ex)
    }

    sys.addShutdownHook {
      // Cleaning up resources left by this exercise.
      if(settings.cleanResourcesOnShutdown) {
        cleanCachedRepos(settings.cachedReposRoot)
      }
    }
  }
}
