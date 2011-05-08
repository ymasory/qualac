package qualac.common

import java.io.File
import java.util.Calendar

import scala.io.Source

import scala.util.Properties

import qualac.QualacException

/**
 * Some values from the program's environment.
 * 
 * The most horrible mutable/side-effecty stuff goes here.
 */
object Env {

  /** Does nothing.
   *
   * It's here just to encourage the program to fail fast by calling this
   * function immediately and getting much of the i/o out of the way. */
  def init() = {}

  /** Get the current time in a mutable `java.util.Date` */
  def now() = Calendar.getInstance().getTime

  /** Current directory the program is executing in. */
  val curDir = (new java.io.File(".")).getCanonicalPath

  /** Running Scala version, like 2.8.0 or 2.9.0.RC3. */
  val scalaVersion: String = {
    Properties.releaseVersion match {
      case Some(v) => v
      case None    => Properties.developmentVersion match {
        case Some(v) => v
        case None => throw QualacException("cannot determine Scala version")
      }
    }
  }

  private val map = ConfParser.parse(new File("fuzzing.conf"))
  val numThreads = map("threads")
  val durationSeconds = map("duration_seconds")
  val timeoutSeconds = map("timeout_seconds")
  
  /** Pull the qualac password off the hard disk. */
  def getPassword() = {
    val sep = System getProperty "file.separator"
    val file = new File(Properties.userHome + sep + ".quala")
    Source.fromFile(file).mkString.trim
  }
}

