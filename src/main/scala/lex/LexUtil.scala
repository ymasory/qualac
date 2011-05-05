package qualac.lex

/** Utilities for the lexical fuzzer */
object LexUtil {

  /** Convert UTF hex descriptions like "UTF+008F" into an `Int` */
  def utfToInt(str: String) = {
    val Utf = """[uU]\+([A-F\d]{4})""".r
    str match {
      case Utf(numStr) => Integer parseInt (numStr, 16)
      case _           => throw UtfStringFormatException(str)
    }
  }

}

/**
 * Custom exception indicating a UTF string (ideally like "UTF+008F") is
 * malformed.
 */
case class UtfStringFormatException(malformedUtfStr: String)
  extends RuntimeException(malformedUtfStr)
