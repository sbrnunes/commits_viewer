package com.acme.commitviewer.util

case class Error(msg: String, details: List[String] = List.empty) {
  def format: String = s"$msg. ${details.mkString(",")}"
}
