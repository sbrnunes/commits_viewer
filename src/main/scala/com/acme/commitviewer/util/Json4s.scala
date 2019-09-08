package com.acme.commitviewer.util

import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.json4s.jackson.Serialization.{read, write}

object Json4s {
  object JsonFormats {
    implicit val formats: Formats = DefaultFormats
  }

  def fromJson[T: Manifest](str: String)(implicit formats: Formats = JsonFormats.formats): T = {
    parse(str).camelizeKeys.extract[T]
  }

  def toJson(obj: AnyRef)(implicit formats: Formats = JsonFormats.formats): String = write(obj)
}
