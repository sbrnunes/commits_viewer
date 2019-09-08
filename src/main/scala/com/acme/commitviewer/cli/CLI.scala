package com.acme.commitviewer.cli

import com.acme.commitviewer.util.{Error, Logging}

import scala.io.Source
import scala.sys.process._
import scala.util.{Failure, Success, Try}

trait CLI extends Logging{

  def exec(commands: String*): Either[Error, List[String]] = {
    var output = List[String]()
    var errors = List[String]()

    val process = Process(Seq("bash", "-c", combine(commands:_*)))
    val io = new ProcessIO(
      _ => (),
      stdout => Source.fromInputStream(stdout).getLines().foreach(line => output ::= line),
      stderr => Source.fromInputStream(stderr).getLines().foreach(line => errors ::= line))

    Try(process.run(io).exitValue()) match {
      case Success(result) if result == 0 =>
        Right(output.reverse)
      case Success(result) if result != 0 =>
        Left(Error(s"Could not execute '${combine(commands:_*)}'", errors))
      case Failure(ex) =>
        Left(Error(ex.getMessage))
    }
  }

  private def combine(commands: String*): String = commands.mkString(" && ")
}

object CLI extends CLI