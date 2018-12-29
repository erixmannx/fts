package fts

import java.io.File
import com.typesafe.config.{Config, ConfigFactory}

object Configuration {
  val configPath = "config"
  val configFile = "application.conf"

  val config = ConfigFactory.parseFile(new File(s"$configPath/$configFile"))

  def getConfig : Config = config
}
