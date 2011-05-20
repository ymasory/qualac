package qualac.lex

import org.scalacheck._
import Prop.forAll

import qualac.compile.Scalac

/**
 * @author Yuvi Masory
 * @specSec(2)
 */
object IdentifierProperties extends
  Properties("2 Identifiers, Names, and Scopes") {

  property("any mathematical char can be an identifier") =
    forAll(Characters.mathematicalSymbolChar) { cp =>
      val char = LexUtil.codeToString(cp)
      val text = "object Foo { def " + char + " = 0 }"
      Scalac.doesCompile(text)
    }
}
