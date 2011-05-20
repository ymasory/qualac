package qualac.common

import scala.io.Source
import scala.collection.{ mutable => m }
import scala.collection.immutable.SortedMap

import qualac.lex.CodePoint

object UCD {

  private val path = Env.unicodePath
  private val verifier = new UCDVerifier(UCDParser.parse(path))

  val BmpPoints = null
  val BmpChars = null
  val BmpNonChar = null
  val SuppChar = null

  val BmpLl = verifier.verifiedClass("Ll")
  val BmpLu = verifier.verifiedClass("Lu")
  val BmpLt = verifier.verifiedClass("Lt")
  val BmpLo = verifier.verifiedClass("Lo")
  val BmpNl = verifier.verifiedClass("Nl")
  val BmpCs = verifier.verifiedClass("Cs")
  val BmpCn = verifier.verifiedClass("Cn")
  val BmpSo = verifier.verifiedClass("So")
  val BmpSm = verifier.verifiedClass("Sm")
}

private[common] class UCDVerifier(uMap: Map[String, List[CodePoint]]) {

  private case class Count(fourCount: Int, fiveDotOneCount: Int, sixCount: Int)

  private val MaxBmp = 65535
  private val NonCharClasses = Set("Cn", "Cs")

  def verifiedClass(clazz: String): List[CodePoint] = {
    val points = uMap(clazz)
    val expectedTotal = Env.unicodeVersion match {
      case VFour       => vMap(clazz).fourCount
      case VFiveDotOne => vMap(clazz).fiveDotOneCount
      case VSix        => vMap(clazz).sixCount
    }
    val actualTotal = points.length
    assert(expectedTotal == actualTotal,
           "expected " + clazz + " to have " + expectedTotal +
           " code points, but it had " + actualTotal + " points")
    points
  }

  def verifiedBmpClass(clazz: String): List[CodePoint] =
    verifiedClass(clazz).filter(_ <= MaxBmp)

  private val vMap = Map(
    "Cc" -> Count(65, 65, 65),
    "Cn" -> Count(878149, 873883, 865147),
    "Cf" -> Count(137, 139, 140),
    "Co" -> Count(137468, 137468, 137468),
    "Cs" -> Count(2048, 2048, 2048),
    "Ll" -> Count(1415, 1748, 1759),
    "Lm" -> Count(114, 187, 210),
    "Lo" -> Count(87797, 90068, 97084),
    "Lt" -> Count(31, 31, 31),
    "Lu" -> Count(1190, 1421, 1436),
    "Mc" -> Count(139, 236, 287),
    "Me" -> Count(10, 13, 12),
    "Mn" -> Count(792, 1032, 1199),
    "Nd" -> Count(268, 370, 420),
    "Nl" -> Count(53, 214, 224),
    "No" -> Count(291, 349, 456),
    "Pc" -> Count(12, 10, 10),
    "Pd" -> Count(17, 20, 21),
    "Pe" -> Count(64, 71, 71),
    "Pf" -> Count(4, 10, 10),
    "Pi" -> Count(6, 12, 12),
    "Po" -> Count(202, 315, 402),
    "Ps" -> Count(65, 72, 72),
    "Sc" -> Count(36, 41, 47),
    "Sk" -> Count(74, 99, 115),
    "Sm" -> Count(899, 945, 948),
    "So" -> Count(2745, 3225, 4398),
    "Zl" -> Count(1, 1, 1),
    "Zp" -> Count(1, 1, 1),
    "Zs" -> Count(19, 18, 18)
  )
}

/**
 * Parses the unicode database (UnicodeData.txt) into a map from unicode
 * classes to list of code points. Horrifically imperative.
 */
private[common] object UCDParser {

  def parse(path: String): Map[String, List[CodePoint]] = {
    val uniMap = new m.HashMap[String, m.ListBuffer[CodePoint]]()

    val lines =
      Source.fromInputStream(
        getClass.getResourceAsStream(path)).getLines.toList

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

    val partMap = SortedMap[String, List[CodePoint]]() ++ {
      uniMap.keys map { key => (key -> uniMap(key).toList) }
    }

    val allAssigned: Set[CodePoint] =
      uniMap.values.foldLeft(Set[CodePoint]())(_ ++ _)
    val allUnicode: Set[CodePoint] = (0 to 1114111).toSet
    val unassigned: List[CodePoint] =  (allUnicode -- allAssigned).toList

    partMap + ("Cn" -> unassigned)
  }
}
