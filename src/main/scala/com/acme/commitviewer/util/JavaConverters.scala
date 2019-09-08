package com.acme.commitviewer.util

import java.time

import scala.concurrent.duration.{Duration, FiniteDuration}

object JavaConverters {

  implicit class ScalaDurationConverter(duration: time.Duration) {
    val asScala: FiniteDuration = Duration.fromNanos(duration.toNanos)
  }
}
