/**
 * Copyright (c) Yuvi Masory, 2011
 *
 * Available under the Qualac License, see /LICENSE.
 */ 
package qualac.lex

object LexUtil {

  implicit def codesToString(lst: List[CodePoint]) =
    new String(lst.toArray, 0, lst.length)

  implicit def codeToString(c: CodePoint) = codesToString(List(c))
}
