package qualac.lex

/** Implicit conversions for the lexical fuzzer. */
object LexImplicits {

  /**
   * Convert UTF hex descriptions of a BMP character like "UTF+008F" into
   * an `CodePoint`
   */
  implicit def bmpToCodePoint(str: String): CodePoint = {
    val Utf = """[uU]\+([A-F\d]{4})""".r
    str match {
      case Utf(numStr) => Integer parseInt (numStr, 16)
      case _           => throw UtfStringFormatException(str)
    }
  }

  /**
   * Custom exception indicating a UTF string (ideally like "UTF+008F") is
   * malformed.
   */
  case class UtfStringFormatException(malformedUtfStr: String)
    extends RuntimeException(malformedUtfStr)
}
