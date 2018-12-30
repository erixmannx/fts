package fts

import scala.concurrent._

import com.typesafe.scalalogging._

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer

import akka.stream._
import akka.stream.scaladsl._
import akka.util.ByteString

import fts.input._
import fts.output._

object FTS extends LazyLogging {
  def main (args : Array[String]) : Unit = {

    val config = Configuration.getConfig()
    val inputPath = config.getString("fts.input.path")

    logger.debug(s"input path: $inputPath")

    implicit val ec = ExecutionContext.global
    implicit val sys = ActorSystem("system")
    implicit val mat = ActorMaterializer()

    val inputPathActor = sys.actorOf(InputPathActor.props(inputPath), "inputPathActor")
    val writerActor = sys.actorOf(WriterActor.props(), "writerActor")

    import scala.concurrent.duration.Duration

/*
    for {
      res <- printFut 
    } yield {
      actorSystem.terminate
    }

    Await.ready(printFut, Duration.Inf)
*/
  }
}
