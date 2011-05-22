/**
 * Copyright (c) Yuvi Masory, 2011
 *
 * Available under the Qualac License, see /LICENSE.
 */ 
package qualac.lex

import LexImplicits.toQString

/**
 * ScalaCheck generators for identifiers.
 *
 * @specSec 1.1
 */
object Identifiers {

  /**
   * List of all reserved words.
   * 
   * @spec The following names are reserved words instead of being members of
   * the syntactic class id of lexical identifiers.
   */
  lazy val reservedWords = reservedAlphas ++ reservedNonAlphas

  private lazy val reservedAlphas = {
    val lst =
      List("abstract", "case", "catch", "class", "def", "do", "else",
           "extends", "false", "final", "finally", "for", "forSome", "if",
           "implicit", "import", "lazy", "match", "new", "null", "object",
           "override", "package", "private", "protected", "return", "sealed",
           "super", "this", "throw", "trait", "try", "true", "type", "val",
           "var", "while", "with", "yield")
    lst.map(_.codePoints)
  }

  private lazy val reservedNonAlphas = {
    val lst =
      List("_", ":", "=", "=>", "<-", "<:", "<%", ">:", "#", "@", "⇒", "←")
    lst.map(_.codePoints)
  }
  

  /**
   * @spec Example 1.1.1 Here are examples of identifiers
   */
  lazy val identifierExamples = {
    val lst =
      List("x", "Object", "maxIndex", "p2p", "empty_?", "+", "`yield`",
           "αρ τη", "_y", "dot_product_*", "__system", "_MAX_LEN_")
    lst.map(_.codePoints)
  }

  /** @spec Example 1.1.2 Backquote-enclosed strings are a solution when one
   * needs to access Java identifiers that are reserved words in Scala.
   * For instance, the statement Thread.yield() is illegal, since yield is a
   * reserved word in Scala. However, here’s a work-around:
   */
  lazy val legalYieldStatement = "Thread.`yield`()".codePoints
}
