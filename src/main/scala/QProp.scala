package qualac

import org.scalacheck.{ Gen, Prop, Properties }

import qualac.compile.Scalac

private [qualac] abstract class QMaybeCompiles extends Properties("") {

  val textGen: Gen[String] 
  val compiles: Boolean
  val desc: String = {
    val name = getClass.getName
    val deObjName = if (name endsWith "$") name.substring(0, name.length - 1)
                    else name
    // deObjName.split("\\.").last
    deObjName
  }

  property(desc) = Prop.forAll(textGen) { s =>
    //pre-compile db access
    val (hasWarnings, hasErrors, infos) = Scalac.compile(s)
    //post-compile db access
    if (compiles) hasWarnings == false && hasErrors == false
    else hasErrors
  }
}

abstract class QCompiles extends {
  override val compiles = true
} with QMaybeCompiles

abstract class QNotCompiles extends {
  override val compiles = false
} with QMaybeCompiles
