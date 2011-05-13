package qualac.common

import scala.io.Source
import scala.collection.{ mutable => m }
import scala.collection.immutable.SortedMap

import qualac.lex.CodePoint

/**
 * Parses the unicode database (UnicodeData.txt) into a map from unicode
 * classes to list of code points. Horrifically imperative.
 */
object UCD {

  private val uniMap: Map[String, List[CodePoint]] = {
    val uniMap = new m.HashMap[String, m.ListBuffer[CodePoint]]()

    val lines =
      Source.fromInputStream(
        getClass.getResourceAsStream(Env.unicodePath)).getLines.toList

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

  private val MaxBmp = 65535
  private val NonCharClasses = Set("Cn", "Cs")

  private val bmpMap = uniMap map { pair =>
    val (k, v) = pair
    (k -> (v filter { _ <= MaxBmp }))
  }

  private def assertClass(clazz: String, size: Int) {
    val len = uniMap(clazz).length
    assert(len == size,
           "expected " + clazz + " to have " + size +
           " code points, but it had " + len + " points")
  }

  val BmpPoints = bmpMap.values.foldLeft(Nil: List[CodePoint]) { _ ++ _ }
  val BmpChars = bmpMap.foldLeft(Nil: List[CodePoint]) { (acc, pair) =>
    val (k, v) = pair
    if (NonCharClasses contains k) acc
    else acc ++ v
  }
  val BmpNonChar = (BmpPoints.toSet -- BmpChars.toSet).toList
  val SuppChar = uniMap.foldLeft(Nil: List[CodePoint]) { (acc, pair) =>
    val (k, v) = pair
    if (NonCharClasses contains k) acc
    else acc ++ v.filter(_ > MaxBmp)
  }


  val BmpLl = bmpMap("Ll")
  val BmpLu = bmpMap("Lu")
  val BmpLt = bmpMap("Lt")
  val BmpLo = bmpMap("Lo")
  val BmpNl = bmpMap("Nl")
  val BmpCs = bmpMap("Cs")
  val BmpCn = bmpMap("Cn")
  val BmpSo = bmpMap("So")
  val BmpSm = bmpMap("Sm")
}

object UCDVerifier {

  case class Count(fourCout: Int, fiveDotOneCount: Int, sixCount: Int = 0)

  val map = Map(
    "Cc" -> Count(65, 65),
    "Cn" -> Count(873883, 865147),
    "Cf" -> Count(139, 140),
    "Co" -> Count(137468, 137468),
    "Cs" -> Count(2048, 2048),
    "Ll" -> Count(1748, 1759),
    "Lm" -> Count(187, 210),
    "Lo" -> Count(90068, 97084),
    "Lt" -> Count(31, 31),
    "Lu" -> Count(1421, 1436),
    "Mc" -> Count(236, 287),
    "Me" -> Count(13, 12),
    "Mn" -> Count(1032, 1199),
    "Nd" -> Count(370, 420),
    "Nl" -> Count(214, 224),
    "No" -> Count(349, 456),
    "Pc" -> Count(10, 10),
    "Pd" -> Count(20, 21),
    "Pe" -> Count(71, 71),
    "Pf" -> Count(10, 10),
    "Pi" -> Count(12, 12),
    "Po" -> Count(315, 402),
    "Ps" -> Count(72, 72),
    "Sc" -> Count(41, 47),
    "Sk" -> Count(99, 115),
    "Sm" -> Count(945, 948),
    "So" -> Count(3225, 4398),
    "Zl" -> Count(1, 1),
    "Zp" -> Count(1, 1),
    "Zs" -> Count(18, 18)
  )
}
