package fts

import scala.concurrent._

import com.typesafe.scalalogging._

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer

import akka.stream._
import akka.stream.scaladsl._
import akka.util.ByteString

object FTS extends LazyLogging {
  def main (args : Array[String]) : Unit = {

    val config = Configuration.getConfig()
    val inputPath = config.getString("fts.input.path")

    logger.debug(s"start FTSNode")

    implicit val ec = ExecutionContext.global
    implicit val sys = ActorSystem("system")
    implicit val mat = ActorMaterializer()

    val ftsNodeActor = sys.actorOf(FTSNodeActor.props(inputPath), "FTSNodeActor")

    ftsNodeActor ! FTSNodeActor.Start
  }
}
