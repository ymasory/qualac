package qualac.stdlib

import org.scalacheck._
import Prop.forAll

import qualac.compile.Scalac

object StdLib extends Properties("standard library") {

  property("`Any` does not define ##") = forAll(Gen choose (1, 9)) { i =>
    Scalac.doesCompile("object M { 1.##  }") == false
  }
}
