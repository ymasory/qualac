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
import qualac.db.DB
import qualac.Env

/**
 * @specSec(2)
 */
class MathChars(db: DB, runId: Long, env: Env) extends {
  override val textGen = Gen.oneOf(List("class X"))
} with QCompiles(db, runId, env)

