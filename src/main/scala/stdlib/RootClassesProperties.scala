/**
 * Copyright (c) Yuvi Masory, 2011
 *
 * Available under the Qualac License, see /LICENSE.
 */ 
package qualac.stdlib

import org.scalacheck._
import Prop.forAll

import qualac.compile.Scalac

object RootClassesProperties extends Properties("12.1 Root Classes") {
  
  val literals = List("?", "?", "1", "1L", "1F", "1D", "'c'", "\"str\"")
  val litGen = Gen.oneOf(literals)
}
