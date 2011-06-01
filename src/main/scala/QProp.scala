/**
 * Copyright (c) Yuvi Masory, 2011
 *
 * Available under the Qualac License, see /LICENSE.
 */ 
package qualac

import org.scalacheck.{ Gen, Prop, Properties }

import qualac.db.DB
import qualac.compile.Scalac

private [qualac] abstract class QMaybeCompiles(db: DB, runId: Long, env: Env) extends Properties("") {

  val textGen: Gen[String] 
  val shouldCompile: Boolean
  val desc: String = {
    val name = getClass.getName
    val deObjName = if (name endsWith "$") name.substring(0, name.length - 1)
                    else name
    deObjName
  }

  property(desc) = Prop.forAll(textGen) { progText =>
    //pre-compile db access
    val postcompileFun = db.persistPrecompile(runId, progText, shouldCompile)
    //compilation
    val scalac = new Scalac(env)
    val (hasWarnings, hasErrors, infos) = scalac.compile(progText)
    //post-compile db access
    postcompileFun(hasWarnings, hasErrors, infos)

    if (shouldCompile) hasWarnings == false && hasErrors == false
    else hasErrors
  }
}

abstract class QCompiles(db: DB, runId: Long, env: Env) extends {
  override val shouldCompile = true
} with QMaybeCompiles(db, runId, env)

abstract class QNotCompiles(db: DB, runId: Long, env: Env) extends {
  override val shouldCompile = false
} with QMaybeCompiles(db, runId, env)
