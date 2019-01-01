package fts

import com.typesafe.scalalogging._

import scala.concurrent._

import akka.actor._
import akka.stream.ActorMaterializer

import fts.input._
import fts.output._

object FTSNodeActor {
  def props(inputPath : String) = Props(new FTSNodeActor(inputPath))

  case object Start
  case object InputDone
}

class FTSNodeActor(inputPath : String) extends Actor with LazyLogging {
  import FTSNodeActor._

  implicit val ec = ExecutionContext.global
  implicit val sys = ActorSystem("system")
  implicit val mat = ActorMaterializer()

  def receive = {
    case Start =>
      logger.debug(s"Start with input path $inputPath")

      logger.debug("Start input path actor")
      val inputPathActor = sys.actorOf(InputPathActor.props(inputPath), "inputPathActor")
      inputPathActor ! InputPathActor.Start

      logger.debug("Start writer actor")
      val writerActor = sys.actorOf(WriterActor.props, "writerActor")

    case InputDone =>
      sys.terminate
  }
}
