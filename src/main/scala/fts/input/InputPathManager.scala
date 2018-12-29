package fts.input

import java.io.File

object InputPathManager {
  def apply(path : String) = new InputPathManager(path)
}

class InputPathManager(path : String) {
  var files = getListOfFiles();

  def getNextFile() : String = {
    val file = files.head
    files = files.tail
    file.getPath()
  }

  private[this] def getListOfFiles() : List[File] = {
    val d = new File(path)
    if (d.exists && d.isDirectory) {
        d.listFiles.filter(_.isFile).toList
    } else {
        List[File]()
    }
  }
}
