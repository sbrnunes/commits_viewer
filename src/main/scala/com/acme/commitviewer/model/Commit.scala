package com.acme.commitviewer.model

case class Commit(
  ref: String,
  author_name: Option[String] = None,
  login: Option[String] = None,
  author_email: Option[String] = None,
  author_url: Option[String] = None,
  date: String,
  subject: String
)

case class Page(commits: List[Commit], nextPage: Boolean = false)