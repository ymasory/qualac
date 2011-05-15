package qualac.lex

import org.scalacheck._
import Prop.forAll

import qualac.compile.Scalac

object IdentifierProperties extends Properties("Identifiers") {

  property("any letter char can be an identifier") =
    forAll(Characters.letterChar) { c =>
      val char = LexUtil.codeToString(c)
      val text = "object Foo { def " + char + " = 0 }"
      val (errors, warnings, _) =
        Scalac.compile(text)
      (errors == false && warnings == false)
    }
}
