package fts.input

import scala.concurrent._
import scala.concurrent.duration._

import akka.actor._
import akka.stream._
import akka.stream.scaladsl._
import akka.util.ByteString
import akka.util.Timeout

import com.typesafe.scalalogging._

import fts.output._

object InputPathActor {
  def props(inputPath : String) = Props(new InputPathActor(inputPath))

  //accepts
  case object Start
  case object StartFile
  case object GetNextChunk

  //response
  case object NoMoreFiles
  case class FileChunk(bs : ByteString)
  case class Error(message : String)
}

class InputPathActor(inputPath : String) extends Actor with LazyLogging {
  import InputPathActor._

  implicit val sys = context.system
  implicit val mat = ActorMaterializer()(context)
  implicit val ec = ExecutionContext.global

  implicit val timeout = Timeout(5 seconds)

  val writer = Await.result(sys.actorSelection("user/FTSNodeActor/writerActor").resolveOne(), Duration.Inf)
  logger.debug(s"Got writer $writer")

  var parent : Option[ActorRef] = None;

  var inputPathManager : Option[InputPathManager] = None

  def receive = {
    case Start => 
      logger.debug(s"Start with inputPath $inputPath")
      inputPathManager = Some(InputPathManager(inputPath))

      parent = Some(sender())
      self ! StartFile

    case StartFile => 
      inputPathManager match {
        case Some(ipm) => 
          val fileOption = ipm.getNextFile()

          fileOption match {
            case Some(file) =>
              logger.debug(s"StartFile $file")

              import java.nio.file.Path
              import java.nio.file.Paths

              val path = Paths.get(file)
              val source = FileIO.fromPath(path, chunkSize = 8192)

              val runnable : RunnableGraph[Future[IOResult]] = source.to(Sink.foreach((bs : ByteString) => {
                writer ! WriterActor.Print(bs)
              }))
          
              for {
                f <- runnable.run()
              } yield {
                self ! StartFile
              }

            case None =>
              logger.debug("No more files")
              parent.get ! NoMoreFiles
          }

        case None => 
          val message = "call Start first"
          logger.error(message)
          sender() ! Error(message)
      }

    case GetNextChunk =>
      logger.debug("GetNextChunk")
  }
}
