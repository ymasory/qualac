package qualac.stdlib

import org.scalacheck._
import Prop.forAll

import qualac.epfl
import qualac.compile.Scalac


/**
 * @author Yuvi Masory
 * @specSec(12.2)
 */
object ValueClasses extends Properties("12.2 Value Classes") {

  property("`Unit` does not implement `asInstanceOf`") = forAll { i: Int =>
    Scalac.doesNotCompile("object M { ().asInstanceOf[Unit] }")
  } : @epfl(4623)

  property("`Unit` does not implement `isInstanceOf`") = forAll { i: Int =>
    Scalac.doesNotCompile("object M { ().isInstanceOf[Unit] }")
  } : @epfl(4623)

  property("`Unit` does not implement `##`") = forAll { i: Int =>
    Scalac.doesNotCompile("object M { ().## }")
  } : @epfl(4623)
}
