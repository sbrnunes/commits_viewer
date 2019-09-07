package com.acme.commitviewer.model

case class Commit(ref: String, authorName: String, authorEmail: String, date: String, subject: String)
