package qualac.lex

import scala.io.Source

/** Utilities for the lexical fuzzer. */
object LexUtils {

   object UnicodeClasses {

    lazy val lines =
      Source.fromInputStream(
        getClass.getResourceAsStream("UnicodeData.txt")).getLines

    lazy val pairs: Iterator[(String, String)] = lines map { line =>
      line.split(";").toList match {
        case List(hex, _, clazz, _*) => (hex, clazz)
        case _ =>
          throw new RuntimeException("unexpected Unicode data: " + line)
      }
    }

    def filterClass(clazz: String) = {
      val filteredPairs = pairs filter { _._2 == clazz }
      val hexes = filteredPairs map { _._1 }
      val ints = hexes map { Integer parseInt (_, 16) }
      ints.toList
    }

    lazy val UnicodeLl: List[CodePoint] = filterClass("Ll")
    lazy val UnicodeLu: List[CodePoint]  = filterClass("Lu")
    lazy val UnicodeLt: List[CodePoint]  = filterClass("Lt")
    lazy val UnicodeLo: List[CodePoint]  = filterClass("Lo")
    lazy val UnicodeNl: List[CodePoint]  = filterClass("Nl")
  }
}
