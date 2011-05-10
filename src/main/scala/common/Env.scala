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
import scala.collection.immutable.SortedMap

/**
 * Parses the unicode database (UnicodeData.txt) into a map from unicode
 * classes to list of code points. Horrifically imperative.
 */
private object UCD {

  val uniMap: Map[String, List[CodePoint]] = {
    val uniMap = new m.HashMap[String, m.ListBuffer[CodePoint]]()

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
      if (uniMap contains clazz) uniMap(clazz) += code
      else uniMap(clazz) = m.ListBuffer[CodePoint](code)
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

    SortedMap[String, List[CodePoint]]() ++ {
      uniMap.keys map { key => (key -> uniMap(key).toList) }
    }
  }

  def assertClass(clazz: String, size: Int) {
    val len = uniMap(clazz).length
    assert(len == size,
           "expected " + clazz + " to have " + size +
           " code points, but it had " + len + " points")
  }

  assertClass("Cc", 65)
  assertClass("Cf", 140)
  assertClass("Co", 137468)
  assertClass("Cs", 2048)
  assertClass("Ll", 1759)
  assertClass("Lm", 210)
  assertClass("Lo", 97084)
  assertClass("Lt", 31)
  assertClass("Lu", 1436)
  assertClass("Mc", 287)
  assertClass("Me", 12)
  assertClass("Mn", 1199)
  assertClass("Nd", 420)
  assertClass("Nl", 224)
  assertClass("No", 456)
  assertClass("Pc", 10)
  assertClass("Pd", 21)
  assertClass("Pe", 71)
  assertClass("Pf", 10)
  assertClass("Pi", 12)
  assertClass("Po", 402)
  assertClass("Ps", 72)
  assertClass("Sc", 47)
  assertClass("Sk", 115)
  assertClass("Sm", 948)
  assertClass("So", 4398)
  assertClass("Zl", 1)
  assertClass("Zp", 1)
  assertClass("Zs", 18)
  

  val UnicodeLl: List[CodePoint] = uniMap("Ll")
  val UnicodeLu: List[CodePoint] = uniMap("Lu")
  val UnicodeLt: List[CodePoint] = uniMap("Lt")
  val UnicodeLo: List[CodePoint] = uniMap("Lo")
  val UnicodeNl: List[CodePoint] = uniMap("Nl")
  val UnicodeCs: List[CodePoint] = uniMap("Cs")
  val UnicodeCn: List[CodePoint] = List[Int]()


}
