package qualac.common

import java.io.{ File, StringWriter, PrintWriter }

import scala.tools.nsc.{ Global, Settings }
import scala.tools.nsc.reporters.{ Reporter, StoreReporter }

import scala.util.Properties

import org.scalacheck._

import qualac.common.Env.{ curDir, scalaVersion }

object Scalac {

  type ScalacMessage = StoreReporter#Info
  
  /** Generate a compiler, UAR */
  lazy val compiler: Gen[Global] = null

  /** Generate a compiler settings configuration, UAR */
  lazy val settings: Gen[Settings] = null


  // def compile(fileName: String): List[ScalacMessage] = {
  //   val settings = new Settings 
  //   settings.outputDirs setSingleOutput (curDir + "/target")
  //   settings.classpath.tryToSet()
  //   val reporter = new StoreReporter
  //   val compiler = new Global(settings, reporter)
  //   (new compiler.Run).compile(List(testPrefix + fileName))
  //   reporter.infos.toList
  // }

  // lazy val classPathList = List(
  //   "project/boot/scala-" + scalaVersion + "/lib/scala-compiler.jar" +
  //   ":project/boot/scala-" + scalaVersion + "/lib/scala-library.jar")

  /** Generate a *minimal* classpath list (just scala-library.jar and
   * scala-compiler.jar), uniformly at random) */
  lazy val minimalClassPathList: Gen[String] = null
}


