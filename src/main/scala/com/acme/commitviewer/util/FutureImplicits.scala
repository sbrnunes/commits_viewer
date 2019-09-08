package com.acme.commitviewer.util

import akka.actor.ActorSystem
import akka.pattern.after

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{ExecutionContextExecutor, Future, TimeoutException}

object FutureImplicits {

  implicit class RichFuture[T](future: Future[T]) {

    def withTimeout(timeout: FiniteDuration)(implicit system: ActorSystem): Future[T] = {
      implicit val ec: ExecutionContextExecutor = system.dispatcher
      val failed = Future.failed(new TimeoutException("Future timed out"))
      val delayedFuture = after(timeout, system.scheduler)(failed)
      Future.firstCompletedOf(Seq(future, delayedFuture))
    }
  }
}
