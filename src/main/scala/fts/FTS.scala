package fts

import scala.concurrent._

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer

import akka.stream._
import akka.stream.scaladsl._
import akka.util.ByteString

object FTS {
  def main (args : Array[String]) : Unit = {

    val filePath = args(0)

    println(s"file path: $filePath")

    implicit val ec = ExecutionContext.global
    implicit val actorSystem = ActorSystem("system")
    implicit val materializer = ActorMaterializer()

    val fileStream = getFileStream(filePath)
    val printFut = printSourceContents(fileStream)

    import scala.concurrent.duration.Duration

    for {
      res <- printFut 
    } yield {
      actorSystem.terminate
    }

    Await.ready(printFut, Duration.Inf)
  }

  def getFileStream(filePath : String) : Source[ByteString, Future[IOResult]] = {
    import java.nio.file.Path
    import java.nio.file.Paths

    val path = Paths.get(filePath)
    FileIO.fromPath(path, chunkSize = 8192)
  }

  def printSourceContents(source : Source[ByteString, Future[IOResult]])(implicit mat : ActorMaterializer) : Future[IOResult] = {
    val runnable : RunnableGraph[Future[IOResult]] = source.to(Sink.foreach((bs : ByteString) => println(bs.utf8String)))
    runnable.run()
  }
}
