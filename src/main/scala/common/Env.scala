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

  def nowMillis() = System.currentTimeMillis

  /** Current directory the program is executing in. */
  val curDir = (new File(".")).getCanonicalFile

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

  /** Map of the parsed config file. */
  val configMap =  qualac.fuzz.Main.confFile match {
      case Some(file) =>
        ConfParser.parse(file)
      case None =>
        ConfParser.parse(getClass.getResourceAsStream("/default.conf"))
  }
  
  val OutDirKey = "out_dir"
  /** Config file property */
  val outDir = {
    val dir = ConfParser.getConfigString(OutDirKey, configMap)
    new File(dir).getCanonicalFile
  }.ensuring(f => f.exists && f.isDirectory,
    "output_dir does not exist. Check your config file.")

  /** config file property */
  val numThreads = {
    val num = ConfParser.getConfigInt("threads", configMap)
    if (num <= 0) Runtime.getRuntime.availableProcessors
    else num
  }
  /** config file property */
  val durationSeconds = ConfParser.getConfigInt("duration_seconds", configMap)
  /** config file property */
  val timeoutSeconds = ConfParser.getConfigInt("timeout_seconds", configMap)
  /** config file property */
  val dbUsername = ConfParser.getConfigString("db_username", configMap)
  /** config file property */
  val dbUrl = ConfParser.getConfigString("db_url", configMap)
  /** config file property */
  val dbDriver = ConfParser.getConfigString("db_driver", configMap)
  /** config file property */
  val dbPassword = ConfParser.getConfigString("db_password", configMap)
  /** config file property */
  val gmailAccount = ConfParser.getConfigString("gmail_account", configMap)
  /** config file property */
  val gmailPassword = ConfParser.getConfigString("gmail_password", configMap)

  val TestPatternKey = "test_pattern"
  /** config file property */
  val TestPattern = ConfParser.getConfigString(TestPatternKey, configMap).r
  /** config file property */
  val recipients =
    ConfParser.getConfigString("recipients", configMap).split(",").toList
  /** config file property */
  val maxDiscardedTests =
    ConfParser.getConfigInt("max_discarded_tests", configMap)
  /** config file property */
  val minSuccessfulTests =
    ConfParser.getConfigInt("min_successful_tests", configMap)
  


  val unicodeVersion =
    if (javaVersion startsWith "1.5") "4.0.0"
    else if (javaVersion startsWith "1.6") "4.0.0"
    else if (javaVersion startsWith "1.7") "5.1.0"
    else throw QualacException("unkown java version " + javaVersion)

  val unicodePath = "/UnicodeData-" + Env.unicodeVersion + ".txt"
  getClass.getResourceAsStream(unicodePath).ensuring(_ != null)
}

