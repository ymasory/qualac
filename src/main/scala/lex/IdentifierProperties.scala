package qualac.lex

import org.scalacheck._
import Prop.forAll

import qualac.compile.Scalac

object IdentifierProperties extends Properties("Identifiers") {

  val bmpNonChar = Characters.bmpNonChar

  property("any BMP char can be an identifier") =
    forAll(Characters.bmpChar) { c =>
      val text = LexUtil.codeToString(c)
      val (errors, warnings, _) =
        Scalac.compile(text, new java.io.File("out"))
      (errors == false && warnings == false)
    }
}
