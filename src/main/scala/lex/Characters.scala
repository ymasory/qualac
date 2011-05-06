package qualac.lex

import org.scalacheck._

import LexImplicits.bmpToCodeUnit

/**
 * ScalaCheck generators for characters and other lexical constituents.
 *
 * @author Yuvi Masory
 * @specSec 1.0
 * @undefined How are we to encode these Unicode code points? The spec doesn't
 * specify an encoding. "UTF-16 code units" should be specified.
 * @undefined Lexical Translats are not specified. See JLS 3.2.
 */
object Characters {

  /**
   * Generate a UTF-16 code unit, UAR.
   *
   * @spec Scala programs are written using the Unicode Basic Multilingual
   * Plane (BMP) character set; Unicode supplementary characters are not
   * presently supported.
   *
   * @undefined Just what is a "supplementary character?" This appears to be
   * borrowed from JLS which defines it as  as code points above
   * U+FFFFUnicode. If Scala isn't supprting supplementary characters I guess
   * that means Unicode code units in the high-surrogates range and
   * low-surrogates range are either banned or treated as code points. JLS
   * says text is just UTF-16 code units, not code points, which I guess
   * means they're treated as though they were code points.
   */
  lazy val codeUnit: Gen[CodeUnit] = Gen choose ("U+0000", "U+FFFF")

  /**
   * Generate a Unicode supplementary character, UAR.
   * 
   */
   lazy val supChar: Gen[CodeUnit] = null

  /**
   * Generate a literal character, UAR.
   * 
   * @spec literal characters ‘c’ refer to the ASCII fragment \u0000-\u007F.
   */
  lazy val literalChar: Gen[CodeUnit] = Gen choose ("U+0000", "U+007F")


  /**
   * Generate a unicode escape sequence, UAR.
   * 
   * @spec In Scala mode, Unicode escapes are replaced by the corresponding
   * Unicode character with the given hexadecimal code.
   * UnicodeEscape ::= \{\\}u{u} hexDigit hexDigit hexDigit hexDigit
   *
   * @correction The EBNF syntax for UnicodeEscape is wrong. It should read
   * \*u{u} hexDigit hexDigit hexDigit hexDigit [remove the *]
   */
  lazy val unicodeEscapeSeq: Gen[List[CodeUnit]] = null

  /**
   * Generate a hex character, UAR.
   * 
   * @spec hexDigit ::= ‘0’ | · · · | ‘9’ | ‘A’ | · · · | ‘F’ | ‘a’ | · · · |
   * ‘f’ |
   */
  lazy val hexDigitChar: Gen[CodeUnit] = {
    val hexUpperLetterChar: Gen[CodeUnit] = Gen choose ("U+0041", "U+005A")
    val hexLowerLetterChar: Gen[CodeUnit] = Gen choose ("U+0061", "U+007A")
    digitChar | hexLowerLetterChar | hexUpperLetterChar
  }

  /**
   * Generate a valid character, UAR.
   * 
   * @spec To construct tokens, characters are distinguished according to the
   * following classes
   */
  lazy val anyChar: Gen[CodeUnit] =
    whitespaceChar | letterChar | digitChar | parenChar |
    delimiterChar | operatorChar

  /**
   * Generate a whitespace character, UAR.
   *
   * @spec 1. Whitespace characters. \u0020 | \u0009 | \u000D | \u000A
   * @correction Finish sentence with period.
   */
  lazy val whitespaceChar: Gen[CodeUnit] = Gen oneOf (
    List("U+0020", "U+0009", "U+000D", "U+000A")
  )

  /**
   * Generate a letter, UAR.
   * 
   * @spec 2. Letters, which include ...
   *
   * @correction There should be spaces before the parens indicating the
   * Unicode groups.
   * @correction Finish sentence with period.
   */
  lazy val letterChar: Gen[CodeUnit] =
    lowercaseLetterChar | uppercaseLetterChar | titlecaseLetterChar |
    otherLetterChar | letterNumeralChar

  /**
   * Generate a lowercase letter character, UAR.
   *
   * @spec lower case letter (Ll)
   */
  lazy val lowercaseLetterChar: Gen[CodeUnit] = null

  /**
   * Generate a uppercase letter character, UAR.
   *
   * @spec upper case letter (Lu) ... and the two characters \u0024 ‘$’ and
   * \u005F ‘_’, which both count as upper case letters
   */
  lazy val uppercaseLetterChar: Gen[CodeUnit] = null

  /**
   * Generate a title-case letter character, UAR.
   *
   * @spec title-case letters (Lt)
   */
  lazy val titlecaseLetterChar: Gen[CodeUnit] = null

  /**
   * Generate a "other letter" character, UAR.
   *
   * @spec other letters (Lo)
   */
  lazy val otherLetterChar: Gen[CodeUnit] = null

  /**
   * Generate a numeral letter character, UAR.
   *
   * @spec letter numerals(Nl)
   */
  lazy val letterNumeralChar: Gen[CodeUnit] = null

  /**
   * Generate a digit character, UAR.
   * 
   * @spec Digits ‘0’ | . . . | ‘9’
   */
  val digitChar: Gen[CodeUnit] = Gen choose ("U+0030", "U+0039")

  /**
   * Generate a paren character, UAR.
   *
   * @spec Parentheses ‘(’ | ‘)’ | ‘[’ | ‘]’ | ‘{’ | ‘}’
   */
  lazy val parenChar: Gen[CodeUnit] = null

  /**
   * Generate a delimiter character, UAR.
   * 
   * @spec Delimiter characters ‘‘’ | ‘’’ | ‘"’ | ‘.’ | ‘;’ | ‘,’
   */
  lazy val delimiterChar: Gen[CodeUnit] = null

  /**
   * Generate a operator character, UAR.
   * 
   * @spec Operator characters. These consist of ...
   */
  lazy val operatorChar: Gen[CodeUnit] =
    otherPrintableAsciiChar | mathematicalSymbolChar | otherSymbolChar

  /**
   * Generate printable ascii character that isn't generated by
   * `whitespaceChar`, `letterChar`, `digitChar`, `parenChar`, or
   * `delimiterChar`
   *
   * @spec These consist of all printable ASCII characters \u0020-\u007F. which
   * are in none of the sets above
   */
  lazy val otherPrintableAsciiChar: Gen[CodeUnit] = null  
  
  /**
   * Generate a mathematical character, UAR.
   * 
   * @spec mathematical symbols(Sm)
   */
  lazy val mathematicalSymbolChar: Gen[CodeUnit] = null

  /**
   * Generate a "other" symbol character, UAR.
   * 
   * @spec other symbols(So)
   */
  lazy val otherSymbolChar: Gen[CodeUnit] = null
}
