package com.acme.commitviewer.util

import com.typesafe.scalalogging.Logger

trait Logging {
  implicit val logger: Logger = Logger(getClass)
}
