package qualac.lex

import org.scalacheck._
import Prop.forAll

import qualac.compile.Scalac

object IdentifierProperties extends Properties("Identifiers") {

  val bmpNonChar = Characters.bmpNonChar

  property("scalac speed test") = forAll { (i: Int) => 
    true
    // Scalac.compiles("class X")
  }

  // property("BMP non-chars cannot be an identifier") =
  //   forAll(Characters.bmpChar) { c =>
  //     println("chose: " + c)
  //     val text = LexUtil.codeToString(c)
  //     Scalac.compiles("class X")
  //     true
  //   }
}
