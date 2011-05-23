package qualac.lex

import org.scalacheck._
import Prop.forAll

import qualac.QCompiles
import qualac.compile.Scalac

/**
 * @author Yuvi Masory
 * @specSec(2)
 */
object MathChars extends {
  override val textGen = Gen.oneOf(List("class X"))
} with QCompiles
