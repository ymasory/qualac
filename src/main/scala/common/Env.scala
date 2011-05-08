package qualac.common

import java.io.File
import java.util.Calendar
import java.sql.Timestamp

import org.joda.time.DateTime

import scala.io.Source
import scala.util.Properties

import qualac.QualacException

/**
 * Some values from the program's environment.
 * 
 * The most horrible mutable/side-effecty stuff goes here.
 */
object Env {

  /** Get the current time in an immutable joda `DateTime`. */
  def now() = new DateTime()

  /** Get the current time as a sql `Timestamp`. */
  def nowStamp() = new Timestamp(Env.now().toDate.getTime)

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

  val scalaVersionString = Properties.versionString.ensuring(_ != null)
  val scalaVersionMsg = Properties.versionMsg.ensuring(_ != null)
  val javaClasspath = Properties.javaClassPath.ensuring(_ != null)
  val javaVendor = Properties.javaVendor.ensuring(_ != null)
  val javaVersion = Properties.javaVersion.ensuring(_ != null)
  val javaVmInfo = Properties.javaVmInfo.ensuring(_ != null)
  val javaVmName = Properties.javaVmName.ensuring(_ != null)
  val javaVmVendor = Properties.javaVmVendor.ensuring(_ != null)
  val javaVmVersion = Properties.javaVmVersion.ensuring(_ != null)
  val os = Properties.osName.ensuring(_ != null)
  val sourceEncoding = Properties.sourceEncoding.ensuring(_ != null)

  private val map = ConfParser.parse(new File("fuzzing.conf"))
  val numThreads = map("threads") match {
    case Right(i) => i
    case Left(_) => throw QualacException("threads value must be an int")
  }
  val durationSeconds = map("duration_seconds") match {
    case Right(i) => i
    case Left(_) =>
      throw QualacException("duration_seconds value must be an int")
  }
  val timeoutSeconds = map("timeout_seconds") match {
    case Right(i) => i
    case Left(_) =>
      throw QualacException("timeout_seconds value must be an int")
  }
  
  /** Pull the qualac password off the hard disk. */
  def getPassword() = {
    val sep = System getProperty "file.separator"
    val file = new File(Properties.userHome + sep + ".quala")
    Source.fromFile(file).mkString.trim
  }
}

