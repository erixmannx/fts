package fts

import scala.concurrent._
import scala.concurrent.duration._

import com.typesafe.scalalogging._

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer

import akka.pattern.ask
import akka.stream._
import akka.stream.scaladsl._
import akka.util.ByteString
import akka.util.Timeout

object FTS extends LazyLogging {
  def main (args : Array[String]) : Unit = {

    val config = Configuration.getConfig()
    val inputPath = config.getString("fts.input.path")

    logger.debug(s"start FTSNode")

    implicit val ec = ExecutionContext.global
    implicit val sys = ActorSystem("system")
    implicit val mat = ActorMaterializer()

    implicit val timeout = Timeout(5 seconds)

    val ftsNodeActor = sys.actorOf(FTSNodeActor.props(inputPath), "FTSNodeActor")

    ftsNodeActor ? FTSNodeActor.Start
  }
}
