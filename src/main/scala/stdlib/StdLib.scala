package qualac.stdlib

import org.scalacheck._
import Prop.forAll

import qualac.compile.Scalac

object StdLib extends Properties("standard library") {

  property("`Any` does not define ##") = forAll { i: Int =>
    Scalac.doesNotCompile("object M { 1.##  }")
  }

  property("`Unit` does not implement `asInstanceOf`") = forAll { i: Int =>
    Scalac.doesNotCompile("object M { ().asInstanceOf[Unit] }")
  }

  property("`Unit` does not implement `isInstanceOf`") = forAll { i: Int =>
    Scalac.doesNotCompile("object M { ().isInstanceOf[Unit] }")
  }

  property("`null` does not implement `##`") = forAll { i: Int =>
    Scalac.doesNotCompile("object M { null.## }")
  }
}
