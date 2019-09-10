package com.acme.commitviewer.cli

import java.net.URL

import com.acme.commitviewer.config.Settings
import com.acme.commitviewer.util.Logging
import github4s.Github
import org.rogach.scallop.ScallopConf

import scala.reflect.io.Directory

object CommitViewer extends App with Logging {

  class Conf(arguments: Seq[String]) extends ScallopConf(arguments) {
    val repo = opt[String]("repo", required = true)
    val limit = opt[Int]("limit")
    val page = opt[Int]("page", default = Some(1))
    verify()
  }

  def cleanCachedRepos(dir: Directory): Unit = {
    if(dir.exists) {
      logger.info(s"Deleting ${dir.path}")
      dir.deleteRecursively()
    }
  }

  override def main(args: Array[String]): Unit = {
    val conf = new Conf(args)

    val settings = Settings()

    implicit val cli = CLI
    implicit val git = GitCLI()
    implicit val api = Github(settings.githubApiToken)

    val repositoryUrl: URL = new URL(conf.repo())
    val cliClient = GitHubCLI(settings.cachedReposRoot)
    val apiClient = GitHubApi()

    val limit = conf.limit.toOption.getOrElse(settings.commitsDefaultLimit)
    val page = conf.page()

    apiClient.listCommits(repositoryUrl, limit, page) match {
      case Right(page) => page.commits.foreach(println)
      case Left(ex) =>
        logger.error("Could not list commits for the given repository", ex)
        cliClient.listCommits(repositoryUrl, limit, page) match {
          case Right(page) => page.commits.foreach(println)
          case Left(ex) => logger.error("Could not list commits for the given repository", ex)
        }
    }

    sys.addShutdownHook {
      // Cleaning up resources left by this exercise.
      if(settings.cleanResourcesOnShutdown) {
        cleanCachedRepos(settings.cachedReposRoot)
      }
    }
  }
}
