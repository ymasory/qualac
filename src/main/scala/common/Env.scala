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

import qualac.lex.CodePoint
import scala.collection.{ mutable => m }

/**
 * Parses the unicode database (UnicodeData.txt) into a map from unicode
 * classes to list of code points. Horrifically imperative.
 */
private object UCD {

  val unicodeClassMap: Map[String, List[CodePoint]] = {
    val unicodeClassMap = new m.HashMap[String, m.ListBuffer[CodePoint]]()

    val lines =
      Source.fromInputStream(
        getClass.getResourceAsStream("/UnicodeData-6.0.0.txt")).getLines.toList

    val pairs: List[(Int, String, String)] = lines map { line =>
      line.split(";").toList match {
        case List(hex, name, clazz, _*) =>
          (Integer.parseInt(hex, 16), name, clazz)
        case _ =>
          throw new RuntimeException("unexpected Unicode data: " + line)
      }
    }

    def addToMap(clazz: String, code: CodePoint) {
      if (unicodeClassMap contains clazz) unicodeClassMap(clazz) += code
      else unicodeClassMap(clazz) = m.ListBuffer[CodePoint](code)
    }

    for (i <- 0 until pairs.length) {
      val (code, name, clazz) = pairs(i)
      if ((name endsWith ", Last>") == false) {
        if (name endsWith ", First>") {
          val (code2, name2, clazz2) = pairs(i + 1)
          assert(clazz == clazz2, clazz + " does not class match " + clazz2)
          for (j <- code to code2) addToMap(clazz, j)
        }
        else addToMap(clazz, code)
      }
    }

    Map[String, List[CodePoint]]() ++ {
      unicodeClassMap.keys map { key => (key -> unicodeClassMap(key).toList) }
    }
  }

  for (key <- unicodeClassMap.keys) {
    println(key + " " + unicodeClassMap(key).length)
  }

  val UnicodeLl: List[CodePoint] = unicodeClassMap("Ll").toList
  val UnicodeLu: List[CodePoint] = unicodeClassMap("Lu").toList
  val UnicodeLt: List[CodePoint] = unicodeClassMap("Lt").toList
  val UnicodeLo: List[CodePoint] = unicodeClassMap("Lo").toList
  val UnicodeNl: List[CodePoint] = unicodeClassMap("Nl").toList
  val UnicodeCs: List[CodePoint] = unicodeClassMap("Cs").toList
  val UnicodeCn: List[CodePoint] = List[Int]()


}
