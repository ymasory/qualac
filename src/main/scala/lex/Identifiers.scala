package qualac.lex

/**
 * ScalaCheck generators for identifiers.
 *
 * @author Yuvi Masory
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

  private lazy val reservedAlphas =
    List("abstract", "case", "catch", "class", "def", "do", "else", "extends",
         "false", "final", "finally", "for", "forSome", "if", "implicit",
         "import", "lazy", "match", "new", "null", "object", "override",
         "package", "private", "protected", "return", "sealed",
         "super", "this", "throw", "trait", "try", "true", "type", "val",
         "var", "while", "with", "yield")

  private lazy val reservedNonAlphas =
    List("_", ":", "=", "=>", "<-", "<:", "<%", ">:", "#", "@", "⇒", "←")

  /**
   * @spec Example 1.1.1 Here are examples of identifiers
   */
  lazy val identifierExamples =
    List("x", "Object", "maxIndex", "p2p", "empty_?", "+", "`yield`",
         "αρ τη", "_y", "dot_product_*", "__system", "_MAX_LEN_")

  /** @spec Example 1.1.2 Backquote-enclosed strings are a solution when one
   * needs to access Java identifiers that are reserved words in Scala.
   * For instance, the statement Thread.yield() is illegal, since yield is a
   * reserved word in Scala. However, here’s a work-around:
   */
  lazy val legalYieldStatement = "Thread.`yield`()"
}
