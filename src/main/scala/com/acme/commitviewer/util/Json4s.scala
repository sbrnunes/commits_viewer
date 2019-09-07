package com.acme.commitviewer.util

import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.json4s.jackson.Serialization.{read, write}

object Json4s {
  implicit val formats = DefaultFormats

  def fromJson[T: Manifest](str: String): T = parse(str).camelizeKeys.extract[T]

  def toJson(obj: AnyRef): String = write(obj)
}
