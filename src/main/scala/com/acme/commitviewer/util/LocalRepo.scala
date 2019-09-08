package com.acme.commitviewer.util

import java.net.URL

import scala.reflect.io.Directory

object LocalRepo {

  val cachedRepoFor: (URL, Directory) => Directory = (repo: URL, cachedReposRoot: Directory) => {
    cachedReposRoot / Directory(MD5.digest(repo.toString))
  }

  val canCloneTo: Directory => Boolean = (dir: Directory) => !dir.exists || dir.isEmpty

  val existsCachedRepo: Directory => Boolean = (dir: Directory) => {
    val gitDir = dir / Directory(".git")
    dir.exists && gitDir.exists
  }
}
