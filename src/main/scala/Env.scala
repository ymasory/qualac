/**
 * Copyright (c) Yuvi Masory, 2011
 *
 * Available under the Qualac License, see /LICENSE.
 */ 
package qualac

import java.io.File
import java.sql.Timestamp

import org.joda.time.DateTime

import scala.io.Source
import scala.util.Properties

/**
 * Some values from the program's environment.
 * 
 * The most horrible mutable/side-effecty stuff goes here.
 */
class Env(confFile: File) {

  val generalConfig = new ConfigFile(confFile)

  /** Get the current time in an immutable joda `DateTime`. */
  def now() = new DateTime()

  /** Get the current time as a sql `Timestamp`. */
  def nowStamp() = new Timestamp(0L)

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

  val OutDirKey = "output_root"
  /** Config file property */
  val outDir = {
    val dirName = generalConfig.getString(OutDirKey)
    val dir = {
      val name = Main.ProgramNameLower + "-" + nowMillis()
      new File(dirName, name).getCanonicalFile
    }
    if (dir.exists == false) {
      if (dir.mkdirs() == false)
        sys.error("could not create output directory: " + dir)
    }
    dir
  }

  /** config file property */
  val numThreads = {
    val num = generalConfig.getInt("threads")
    if (num <= 0) Runtime.getRuntime.availableProcessors
    else num
  }
  /** config file property */
  val durationSeconds = generalConfig.getInt("duration_seconds")
  /** config file property */
  val timeoutSeconds = generalConfig.getInt("timeout_seconds")
  /** config file property */
  val dbUsername = generalConfig.getString("db_username")
  /** config file property */
  val dbUrl = generalConfig.getString("db_url")
  /** config file property */
  val dbPassword = generalConfig.getString("db_password")
  /** config file property */
  val dbName = generalConfig.getString("db_name")

  /** config file property */
  val maxDiscardedTests =
    generalConfig.getInt("max_discarded_tests")
  /** config file property */
  val minSuccessfulTests =
    generalConfig.getInt("min_successful_tests")
  val PatternClassesKey = "pattern_classes"
  val patternClasses: List[String] = {
    val classes = generalConfig.getString(PatternClassesKey)
    classes.split(",").toList.map(_.trim)
  }


  /** whether this run was generated Condor mode */
  val condorSubmitId = {
    try {
      Some(generalConfig.getInt("condor_submission"))
    }
    catch {
      case _ => None
    }
  }


  val unicodeVersion =
    if (javaVersion startsWith "1.5") VFour
    else if (javaVersion startsWith "1.6") VFour
    else if (javaVersion startsWith "1.7") VFiveDotOne
    else throw QualacException("unkown java version " + javaVersion)

  val unicodePath = "/UnicodeData-" + unicodeVersion.value + ".txt"
  getClass.getResourceAsStream(unicodePath).ensuring(_ != null)
}

abstract class UnicodeVersion(val value: String)
case object VFour extends UnicodeVersion("4.0.0")
case object VFiveDotOne extends UnicodeVersion("5.1.0")
case object VSix extends UnicodeVersion("6.0.0")
