package fts.input

import akka.actor._
import com.typesafe.scalalogging.slf4j.LazyLogging

object InputPathActor {
  def props(path : String) = Props(new InputPathActor(path))

  case object Start
  case object StartFile
  case object GetNextChunk

  case object NoMoreFiles
  case class Error(message : String)
}

class InputPathActor(path : String) extends Actor with LazyLogging {
  import InputPathActor._

  var inputPathManager : Option[InputPathManager] = None

  def receive = {
    case Start => 
      logger.debug(s"Start $path")
      inputPathManager = Some(InputPathManager(path))

    case StartFile => 
      inputPathManager match {
        case Some(ipm) => 
          val file = ipm.getNextFile()
          logger.debug(s"StartFile $file")

        case None => 
          val message = "call Start first"
          logger.error(message)
          sender() ! Error(message)
      }

    case GetNextChunk =>
      logger.debug("GetNextChunk")
  }
}
