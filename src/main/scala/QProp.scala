package qualac

import org.scalacheck.{ Gen, Prop, Properties }

import qualac.compile.Scalac

abstract class QProp extends Properties("") {

  val compiles: Boolean = true
  val desc: String = getClass.getName
  val textGen: Gen[String] 

  property(desc) = Prop.forAll(textGen) { s =>
    //pre-compile db access
    val (hasWarnings, hasErrors, infos) = Scalac.compile(s)
    //post-compile db access
    if (compiles) hasWarnings == false && hasErrors == false
    else hasErrors
  }
}

