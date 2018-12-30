package fts.input

import scala.concurrent._

import akka.actor._
import akka.stream._
import akka.stream.scaladsl._
import akka.util.ByteString

import com.typesafe.scalalogging._

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

  implicit val sys = context.system
  implicit val mat = ActorMaterializer()(context)

  var inputPathManager : Option[InputPathManager] = None

  def receive = {
    case Start => 
      logger.debug(s"Start $path")
      inputPathManager = Some(InputPathManager(path))

    case StartFile => 
      inputPathManager match {
        case Some(ipm) => 
          val file = ipm.getNextFile()
          val to = sender()

          logger.debug(s"StartFile $file")

          import java.nio.file.Path
          import java.nio.file.Paths

          val path = Paths.get(file)
          val source = FileIO.fromPath(path, chunkSize = 8192)

          val runnable : RunnableGraph[Future[IOResult]] = source.to(Sink.foreach((bs : ByteString) => {
            to ! bs
          }))
          runnable.run()

        case None => 
          val message = "call Start first"
          logger.error(message)
          sender() ! Error(message)
      }

    case GetNextChunk =>
      logger.debug("GetNextChunk")
  }
}
