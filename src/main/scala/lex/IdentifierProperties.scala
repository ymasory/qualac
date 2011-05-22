package qualac.lex

import org.scalacheck._
import Prop.forAll

import qualac.QProp
import qualac.compile.Scalac

/**
 * @author Yuvi Masory
 * @specSec(2)
 */
object MathChars extends QProp {

  override val textGen = Gen.oneOf(List("class X"))
}
