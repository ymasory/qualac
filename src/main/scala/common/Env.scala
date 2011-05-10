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

  /** Scala property. */
  val scalaVersionString = Properties.versionString.ensuring(_ != null)
  /** Scala property. */
  val scalaVersionMsg = Properties.versionMsg.ensuring(_ != null)
  /** Scala property. */
  val javaClasspath = Properties.javaClassPath.ensuring(_ != null)
  /** Scala property. */
  val javaVendor = Properties.javaVendor.ensuring(_ != null)
  /** Scala property. */
  val javaVersion = Properties.javaVersion.ensuring(_ != null)
  /** Scala property. */
  val javaVmInfo = Properties.javaVmInfo.ensuring(_ != null)
  /** Scala property. */
  val javaVmName = Properties.javaVmName.ensuring(_ != null)
  /** Scala property. */
  val javaVmVendor = Properties.javaVmVendor.ensuring(_ != null)
  /** Scala property. */
  val javaVmVersion = Properties.javaVmVersion.ensuring(_ != null)
  /** Scala property. */
  val os = Properties.osName.ensuring(_ != null)
  /** Scala property. */
  val sourceEncoding = Properties.sourceEncoding.ensuring(_ != null)
  /** Java property. */
  val classPathSep = System.getProperty("path.separator").ensuring {
    _ != null
  }
  /** Java property. */
  val sep = System.getProperty("file.separator").ensuring {
    _ != null
  }

  private val map =
    ConfParser.parse(getClass.getResourceAsStream("/fuzzing.conf"))

  /** Pull a value from the config file that's supposed to be a `String`. */
  private def getConfigInt(key: String) = {
    map(key) match {
      case Right(i) => i
      case Left(_) => throw QualacException(key + " value must be an int")
    }
  }

  /** Pull a value from the config file that's supposed to be an `Int`. */
  private def getConfigString(key: String) = {
    map(key) match {
      case Right(_) => throw QualacException(key + " value must be an int")
      case Left(s) => s
    }
  }

  /** fuzzing.conf property. */
  val numThreads = getConfigInt("threads")
  /** fuzzing.conf property. */
  val durationSeconds = getConfigInt("duration_seconds")
  /** fuzzing.conf property. */
  val timeoutSeconds = getConfigInt("timeout_seconds")
  /** fuzzing.conf property. */
  val dbUsername = getConfigString("db_username")
  /** fuzzing.conf property. */
  val dbUrl = getConfigString("db_url")
  /** fuzzing.conf property. */
  val dbPassword = getConfigString("db_password")
  
  /** Pull the qualac password off the hard disk. */
  def getPassword() = {
    val sep = System getProperty "file.separator"
    val file = new File(Properties.userHome + sep + ".quala")
    Source.fromFile(file).mkString.trim
  }

  /** The Unicode class Ll, in code points. */
  val UnicodeLl = UCD.UnicodeLl
  /** The Unicode class Lu, in code points. */
  val UnicodeLu = UCD.UnicodeLu
  /** The Unicode class Lt, in code points. */
  val UnicodeLt = UCD.UnicodeLt
  /** The Unicode class Lo, in code points. */
  val UnicodeLo = UCD.UnicodeLo
  /** The Unicode class Nl, in code points. */
  val UnicodeNl = UCD.UnicodeNl
  /** The Unicode class Cs, in code points. */
  val UnicodeCs = UCD.UnicodeCs
  /** The Unicode class Cn, in code points. */
  val UnicodeCn = UCD.UnicodeCn

}

private object UCD {

  import qualac.lex.CodePoint

  def filterClass(clazz: String) = {
    val lines =
      Source.fromInputStream(
        getClass.getResourceAsStream("/UnicodeData-6.0.0.txt")).getLines

    val pairs: Iterator[(String, String)] = lines map { line =>
      line.split(";").toList match {
        case List(hex, _, clazz, _*) => (hex, clazz)
        case _ =>
          throw new RuntimeException("unexpected Unicode data: " + line)
      }
    }

    val filteredPairs = pairs filter { _._2 == clazz }
    val hexes = filteredPairs map { _._1 }
    val ints = hexes map { Integer parseInt (_, 16) }
    ints.toList
  }

  val UnicodeLl: List[CodePoint] = filterClass("Ll")
  val UnicodeLu: List[CodePoint] = filterClass("Lu")
  val UnicodeLt: List[CodePoint] = filterClass("Lt")
  val UnicodeLo: List[CodePoint] = filterClass("Lo")
  val UnicodeNl: List[CodePoint] = filterClass("Nl")
  val UnicodeCs: List[CodePoint] = filterClass("Cs")
  val UnicodeCn: List[CodePoint] = filterClass("Cn")

}
