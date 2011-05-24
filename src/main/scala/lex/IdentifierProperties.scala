/**
 * Copyright (c) Yuvi Masory, 2011
 *
 * Available under the Qualac License, see /LICENSE.
 */ 
package qualac.lex

import org.scalacheck._
import Prop.forAll

import qualac.{ QCompiles, QNotCompiles }
import qualac.compile.Scalac

/**
 * @specSec(2)
 */
object MathChars extends {
  override val textGen = Gen.oneOf(List("class X"))
} with QCompiles

object BadProgram extends {
  override val textGen = Gen.oneOf(List("class trait X"))
} with QNotCompiles
