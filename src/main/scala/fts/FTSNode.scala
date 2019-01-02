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
  implicit val mat = ActorMaterializer()

  var starter : Option[ActorRef] = None

  def receive = {
    case Start =>
      starter = Some(sender())
      logger.debug(s"Start with input path $inputPath")

      logger.debug("Start writer actor")
      val writerActor = context.actorOf(WriterActor.props, "writerActor")

      logger.debug("Start input path actor")
      val inputPathActor = context.actorOf(InputPathActor.props(inputPath), "inputPathActor")
      inputPathActor ! InputPathActor.Start

    case InputPathActor.NoMoreFiles =>
      logger.debug("Input path actor sent no more files")
      self ! InputDone

    case InputDone =>
      logger.debug("Input done terminating actor system")
      context.system.terminate()
  }
}
