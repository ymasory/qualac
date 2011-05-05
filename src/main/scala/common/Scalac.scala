package qualac.common

import java.io.{ File, StringWriter, PrintWriter }

import scala.tools.nsc.{ Global, Settings }
import scala.tools.nsc.reporters.{ Reporter, StoreReporter }

object Scalac {
  
  type ScalacMessage = StoreReporter#Info 

  val curDir = (new java.io.File(".")).getCanonicalPath
  val testPrefix = curDir + "/check/"

  lazy val optVersion: Option[String] = {
    val RCVersion = """version (\d+\.\d+\.\d+\.RC\d+).*""".r
    val SimpleVersion = """version (\d+\.\d+\.\d+).*""".r
    util.Properties.versionString match {
      case RCVersion(version)     => Some(version)
      case SimpleVersion(version) => Some(version)
      case _ => {
        Console.err println ("could not detect scala version")
        None
      }
    }
  }

  def compile(fileName: String): List[ScalacMessage] = {
    val scalaVersion = optVersion match {
      case Some(v) => v
      case None    => throw new QualacException("no Scala version detected")
    }
    val settings = new Settings 
    settings.outputDirs setSingleOutput (curDir + "/target")
    settings.classpath.tryToSet(List(
      "project/boot/scala-" + scalaVersion + "/lib/scala-compiler.jar" +
      ":project/boot/scala-" + scalaVersion + "/lib/scala-library.jar"))
    val reporter = new StoreReporter
    val compiler = new Global(settings, reporter)
    (new compiler.Run).compile(List(testPrefix + fileName))
    reporter.infos.toList
  }
}

case class QualacException(msg: String) extends RuntimeException(msg)
