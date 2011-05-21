package qualac.lex

/**
 * Implicit conversions for the lexical fuzzer.
 *
 * @author Yuvi Masory
 */
object LexImplicits {

  /**
   * Convert UTF hex descriptions of a BMP code point like "UTF+008F" into
   * an `CodePoint`
   */
  implicit def bmpHexToCodePoint(str: String): CodePoint = {
    val Utf = """[uU]\+([A-F\d]{4})""".r
    str match {
      case Utf(numStr) => Integer parseInt (numStr, 16)
      case _           => throw UtfStringFormatException(str)
    }
  }


  /** `String` wrapper with utility methods .*/
  case class RicherString(str: String) {

    /** Convert this string to its code points. MUST BE ASCII. */
    def codePoints: List[CodePoint] = {
      str.foreach { c =>
        assert(c.toInt < 128, str + " contains non-ascii")
      }
      (0 until str.length).map{ i => str.codePointAt(i) }.toList
    }
  }

  /** Convert a `String` to a `RicherString`. */
  implicit def richify(str: String): RicherString = RicherString(str)

  /**
   * Custom exception indicating a UTF string (ideally like "UTF+008F") is
   * malformed.
   */
  case class UtfStringFormatException(malformedUtfStr: String)
    extends RuntimeException(malformedUtfStr)
}
