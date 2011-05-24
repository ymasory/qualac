package qualac

import org.scalacheck.{ Gen, Prop, Properties }

import qualac.db.DB
import qualac.compile.Scalac

private [qualac] abstract class QMaybeCompiles extends Properties("") {

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
    val postTrialFun = DB.persistPreTrial(progText, shouldCompile)
    //compilation
    val (hasWarnings, hasErrors, infos) = Scalac.compile(progText)
    //post-compile db access
    postTrialFun(hasWarnings, hasErrors, infos)

    if (shouldCompile) hasWarnings == false && hasErrors == false
    else hasErrors
  }
}

abstract class QCompiles extends {
  override val shouldCompile = true
} with QMaybeCompiles

abstract class QNotCompiles extends {
  override val shouldCompile = false
} with QMaybeCompiles
