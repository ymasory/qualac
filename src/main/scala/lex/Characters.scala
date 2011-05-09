package qualac.lex

import org.scalacheck._

import qualac.common.Env
import LexImplicits.bmpHexToCodePoint

/**
 * ScalaCheck generators for characters and other lexical constituents.
 *
 * @author Yuvi Masory
 * @specSec 1.0
 *
 * @undefined How are we to encode these Unicode code points? The spec doesn't
 * specify an encoding. Scalac allows one to specify the encoding. The spec
 * should probably specify that it's left up to the implementation.
 * @undefined Lexical Translations are not specified. See JLS 3.2.
 * @undefined No Unicode version is specified. Without that one cannot be sure
 * which code units belong to which classes. This is difficult to specify
 * with a simple answer because in practice it's contingent on the behavior of
 * the VM (e.g., particular JVM version) hosting scalac. Spec should probably
 * specify that it's left up to the implementation.
 * @undefined What happens if a supplementary character is used?
 */
object Characters {

  /**
   * Generate a Unicode BMP character (not Cs or Cn), UAR.
   *
   * @spec Scala programs are written using the Unicode Basic Multilingual
   * Plane (BMP) character set;
   */
  lazy val bmpChar: Gen[CodePoint] = null

  /**
   * Generate a Unicode non-character (class Cs or Cn) in the BMP, UAR.
   */
  lazy val bmpNonChar: Gen[CodePoint] = null

  /**
   * Generate a Unicode character (not Cs or Cn) outside BMP, UAR.
   * 
   * @spec Unicode supplementary characters are not presently supported.
   */
  lazy val supplementaryChar: Gen[CodePoint] = null

  /**
   * Generate a literal character, UAR.
   * 
   * @spec literal characters ‘c’ refer to the ASCII fragment \u0000-\u007F.
   */
  lazy val literalChar: Gen[CodePoint] = Gen choose ("U+0000", "U+007F")


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
  lazy val unicodeEscapeSeq: Gen[List[CodePoint]] = null

  /**
   * Generate a hex character, UAR.
   * 
   * @spec hexDigit ::= ‘0’ | · · · | ‘9’ | ‘A’ | · · · | ‘F’ | ‘a’ | · · · |
   * ‘f’ |
   */
  lazy val hexDigitChar: Gen[CodePoint] = {
    val hexUpperLetterChar: Gen[CodePoint] = Gen choose ("U+0041", "U+005A")
    val hexLowerLetterChar: Gen[CodePoint] = Gen choose ("U+0061", "U+007A")
    digitChar | hexLowerLetterChar | hexUpperLetterChar
  }

  /**
   * Generate a valid character, UAR.
   * 
   * @spec To construct tokens, characters are distinguished according to the
   * following classes
   */
  lazy val validChar: Gen[CodePoint] =
    whitespaceChar | letterChar | digitChar | parenChar |
    delimiterChar | operatorChar

  /**
   * Generate a whitespace character, UAR.
   *
   * @spec 1. Whitespace characters. \u0020 | \u0009 | \u000D | \u000A
   * @correction Finish sentence with period.
   */
  lazy val whitespaceChar: Gen[CodePoint] = Gen oneOf (
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
  lazy val letterChar: Gen[CodePoint] =
    lowercaseLetterChar | uppercaseLetterChar | titlecaseLetterChar |
    otherLetterChar | letterNumeralChar

  /**
   * Generate a lowercase letter character, UAR.
   *
   * @spec lower case letter (Ll)
   */
  lazy val lowercaseLetterChar: Gen[CodePoint] =
    Gen oneOf Env.UnicodeLl

  /**
   * Generate a uppercase letter character, UAR.
   *
   * @spec upper case letter (Lu) ... and the two characters \u0024 ‘$’ and
   * \u005F ‘_’, which both count as upper case letters
   */
  lazy val uppercaseLetterChar: Gen[CodePoint] =
    Gen oneOf Env.UnicodeLu

  /**
   * Generate a title-case letter character, UAR.
   *
   * @spec title-case letters (Lt)
   */
  lazy val titlecaseLetterChar: Gen[CodePoint] =
    Gen oneOf Env.UnicodeLt

  /**
   * Generate a "other letter" character, UAR.
   *
   * @spec other letters (Lo)
   */
  lazy val otherLetterChar: Gen[CodePoint] =
    Gen oneOf Env.UnicodeLo

  /**
   * Generate a numeral letter character, UAR.
   *
   * @spec letter numerals(Nl)
   */
  lazy val letterNumeralChar: Gen[CodePoint] =
    Gen oneOf Env.UnicodeNl

  /**
   * Generate a digit character, UAR.
   * 
   * @spec Digits ‘0’ | . . . | ‘9’
   */
  val digitChar: Gen[CodePoint] = Gen choose ("U+0030", "U+0039")

  /**
   * Generate a paren character, UAR.
   *
   * @spec Parentheses ‘(’ | ‘)’ | ‘[’ | ‘]’ | ‘{’ | ‘}’
   */
  lazy val parenChar: Gen[CodePoint] =
    Gen oneOf List("U+0028", "U+0029", "U+007B", "U+007D", "U+005B", "U+005D")

  /**
   * Generate a delimiter character, UAR.
   * 
   * @spec Delimiter characters ‘‘’ | ‘’’ | ‘"’ | ‘.’ | ‘;’ | ‘,’
   */
  lazy val delimiterChar: Gen[CodePoint] = null

  /**
   * Generate a operator character, UAR.
   * 
   * @spec Operator characters. These consist of ...
   */
  lazy val operatorChar: Gen[CodePoint] =
    otherPrintableAsciiChar | mathematicalSymbolChar | otherSymbolChar

  /**
   * Generate printable ascii character that isn't generated by
   * `whitespaceChar`, `letterChar`, `digitChar`, `parenChar`, or
   * `delimiterChar`
   *
   * @spec These consist of all printable ASCII characters \u0020-\u007F. which
   * are in none of the sets above
   */
  lazy val otherPrintableAsciiChar: Gen[CodePoint] = null  
  
  /**
   * Generate a mathematical character, UAR.
   * 
   * @spec mathematical symbols(Sm)
   */
  lazy val mathematicalSymbolChar: Gen[CodePoint] = null

  /**
   * Generate a "other" symbol character, UAR.
   * 
   * @spec other symbols(So)
   */
  lazy val otherSymbolChar: Gen[CodePoint] = null
}
