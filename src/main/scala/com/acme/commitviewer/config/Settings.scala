package com.acme.commitviewer.config

import java.io.File

import com.typesafe.config.{Config, ConfigFactory}

class Settings(config: Config) {
  val cachedReposRoot: File = new File(config.getString("git.cached_repos.dir"))
  val commitsDefaultLimit: Int = config.getInt("git.commits.list.default_limit")
}

object Settings {

  def apply(): Settings = {
    val config = ConfigFactory.load()
    new Settings(config)
  }
}
