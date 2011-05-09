package qualac.compile

import java.io.{ File, StringWriter, PrintWriter }

import scala.tools.nsc.{ Global, Settings }
import scala.tools.nsc.reporters.{ Reporter, StoreReporter }

import scala.util.Properties

import org.scalacheck._

import qualac.common.Env.{ curDir, scalaVersion }

/**
 * ScalaCheck generators for compiler instances.
 *
 * @author Yuvi Masory
 */
class Scalac() {

  lazy val tmpDir = null

  /** Generate a compiler instance, UAR. */
  lazy val compiler: Gen[Global] = {
    val compiler = new Global(null, null)
    compiler
  }

  /** Generate a compiler settings configuration, NE/CA */
  lazy val settings: Gen[Settings] = {
    val settings = new Settings 
    settings.outputDirs setSingleOutput (curDir + "/target")
    settings.classpath.tryToSet(null)
    settings
  }

  /** Generate a classpath list. NE */
  lazy val classPathList: Gen[String] = minimalClassPathList

  /**
   * Generate a *minimal* classpath list (just scala-library.jar and
   * scala-compiler.jar), UAR).
   */
  lazy val minimalClassPathList: Gen[String] = {
    val comp = "project/boot/scala-" + scalaVersion + "/lib/scala-compiler.jar"
    val lib = "project/boot/scala-" + scalaVersion + "/lib/scala-library.jar"
    val semi = ":"
    Gen oneOf List(comp + semi + lib, lib + semi + comp)
  }

  /** Generate a `StoreReporter` */
  def reporter = new StoreReporter

  def compile(text: String): List[ScalacMessage] = {
    // (new compiler.Run).compile(List(testPrefix + fileName))
    // reporter.infos.toList
    null
  }
}
