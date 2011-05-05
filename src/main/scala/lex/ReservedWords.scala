package qualac.lex

object ReservedWords {

  /**
   * List of all reserved words according to Wampler & Payne
   * 
   * http://programming-scala.labs.oreilly.com/ch02.html#reserved-words-table
   */
  val reservedWords = reservedAlphas ++ reservedNonAlphas

  val reservedAlphas =
    List("abstract", "case", "catch", "class", "def", "do", "else", "extends",
         "false", "final", "finally", "for", "forSome", "if", "implicit",
         "import", "lazy", "match", "new", "null", "object", "override",
         "package", "private", "protected", "requires", "return", "sealed",
         "super", "this", "throw", "trait", "try", "true", "type", "val",
         "var", "while", "with", "yield")

  val reservedNonAlphas =
    List("_", ":", "=", "=>", "<-", "<:", "<%", ">:", "#", "@", "⇒", "←")
}
