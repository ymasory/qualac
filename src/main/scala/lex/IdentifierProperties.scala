package qualac.lex

import org.scalacheck._
import Prop.forAll

import qualac.compile.Scalac

object IdentifierProperties extends Properties("Identifiers") {

  val bmpNonChar = Characters.bmpNonChar

  property("BMP non-chars cannot be an identifier") =
    forAll(Characters.bmpChar) { c =>
      val text = LexUtil.codeToString(c)
      Scalac.compiles("class X")
    }
}
