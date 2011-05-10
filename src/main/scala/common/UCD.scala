package qualac.common

import scala.io.Source
import scala.collection.{ mutable => m }
import scala.collection.immutable.SortedMap

import qualac.lex.CodePoint

/**
 * Parses the unicode database (UnicodeData.txt) into a map from unicode
 * classes to list of code points. Horrifically imperative.
 */
private object UCD {

  val uniMap: Map[String, List[CodePoint]] = {
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

  def assertClass(clazz: String, size: Int) {
    val len = uniMap(clazz).length
    assert(len == size,
           "expected " + clazz + " to have " + size +
           " code points, but it had " + len + " points")
  }

  val UnicodeLl: List[CodePoint] = uniMap("Ll")
  val UnicodeLu: List[CodePoint] = uniMap("Lu")
  val UnicodeLt: List[CodePoint] = uniMap("Lt")
  val UnicodeLo: List[CodePoint] = uniMap("Lo")
  val UnicodeNl: List[CodePoint] = uniMap("Nl")
  val UnicodeCs: List[CodePoint] = uniMap("Cs")
  val UnicodeCn: List[CodePoint] = uniMap("Cn")
}
