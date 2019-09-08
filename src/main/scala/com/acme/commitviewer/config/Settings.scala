package com.acme.commitviewer.config

import java.io.File

import com.acme.commitviewer.util.JavaConverters._
import com.typesafe.config.{Config, ConfigFactory}

import scala.concurrent.duration.FiniteDuration
import scala.reflect.io.Directory

class Settings(config: Config) {
  val cleanResourcesOnShutdown: Boolean = config.getBoolean("jvm.clean_resources_on_shutdown")
  val cachedReposRoot: Directory = Directory(new File(config.getString("git.cached_repos.dir")))
  val commitsDefaultLimit: Int = config.getInt("git.commits.list.default_limit")
  val requestDefaultTimeout: FiniteDuration = config.getDuration("http.requests.timeout").asScala
}

object Settings {

  def apply(): Settings = {
    val config = ConfigFactory.load()
    new Settings(config)
  }
}
