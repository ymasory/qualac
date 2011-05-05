package qualac.common

import java.io.{ File, StringWriter, PrintWriter }

import scala.tools.nsc.{ Global, Settings }
import scala.tools.nsc.reporters.{ Reporter, StoreReporter }

import scala.util.Properties

object Scalac {
  
  type ScalacMessage = StoreReporter#Info 

  val curDir = (new java.io.File(".")).getCanonicalPath
  val testPrefix = curDir + "/check/"

  lazy val optVersion: Option[String] = {
    val dev = Properties.developmentVersion
    val rel = Properties.releaseVersion
    if (rel.isDefined) rel
    else if (dev.isDefined) dev
    else None
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
