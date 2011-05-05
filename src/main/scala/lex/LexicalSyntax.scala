package qualac.lex

import LexUtil.utfToInt

import org.scalacheck._

/**
 * ScalaCheck generators for tokens and their lexical constituents.
 *
 * UAR = "uniformly at random"
 *
 * @author Yuvi Masory
 * @specSec 1.0
 */
object LexicalSyntax {

  /**
   * Generate a UTF BMP character, UAR.
   *
   * @spec Scala programs are written using the Unicode Basic Multilingual
   * Plane (BMP) character set;
   */
  val bmpChar: Gen[Int] =
    Gen choose (utfToInt("U+0000"), utfToInt("U+FFFF"))

  /**
   * Generate a Unicode supplementary character, UAR.
   * 
   * @spec Unicode supplementary characters are not presently supported.
   */
  // val supChar: Gen[Int] = Gen choose List(0)

  /**
   * Generate a literal character, UAR.
   * 
   * @spec [...] literal characters ‘c’ refer to the ASCII fragment
   * \u0000-\u007F.
   */
  // val literalChar: Gen[Int] =
  //   Gen chooseOne (utfToInt("U+0000"), utfToInt("U+007F"))

  /**
   * Generate a valid character, UAR.
   * 
   * @spec To construct tokens, characters are distinguished according to the
   * following classes
   */
  val anyChar: Gen[Int] = null

  /**
   * Generate a whitespace character, UAR.
   *
   * @spec 1. Whitespace characters. \u0020 | \u0009 | \u000D | \u000A
   */
  val whitespaceChar: Gen[Int] = Gen oneOf (
    List("U+0020", "U+0009", "U+000D", "U+000A") map { utfToInt(_) }
  )

  /**
   * Generate a letter, UAR.
   * 
   * @ spec 2. Letters, which include lower case letters(Ll), upper case
   * letters(Lu), title-case letters(Lt), other letters(Lo), letter
   * numerals(Nl) and the two characters \u0024 ‘$’ and \u005F ‘_’, which
   * both count as upper case letters */
  val letter: Gen[Int] =
    lowercaseLetter | uppercaseLetter | titlecaseLetter | otherLetter |
    letterNumeral

  val lowercaseLetter: Gen[Int] = Gen oneOf List(0)
  val uppercaseLetter: Gen[Int] = Gen oneOf List(0)
  val titlecaseLetter: Gen[Int] = Gen oneOf List(0)
  val otherLetter: Gen[Int] = Gen oneOf List(0)
  val letterNumeral: Gen[Int] = Gen oneOf List(0)

  val digits: Gen[Int] = Gen oneOf List(0)
}
