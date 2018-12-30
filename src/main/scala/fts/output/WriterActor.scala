package fts.output

import com.typesafe.scalalogging._

import akka.actor._
import akka.util.ByteString

object WriterActor {
  def props() = Props()

  case class Print(byteString : ByteString)
}

class WriterActor() extends Actor with LazyLogging {
  import WriterActor._

  def receive = {
    case Print(byteString) =>
      logger.debug(byteString.utf8String)
  }
}
