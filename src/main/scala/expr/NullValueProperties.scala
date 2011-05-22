/**
 * Copyright (c) Yuvi Masory, 2011
 *
 * Available under the Qualac License, see /LICENSE.
 */ 
package qualac.stdlib

import org.scalacheck._
import Prop.forAll

import qualac.epfl
import qualac.compile.Scalac

/**
 * @specSec(6.3)
 */
object NullValueProperties extends Properties("6.3 The *Null* Value") {

  /**
   * @spec The null value is of type scala.Null
   */

  /**
   * @spec ... and is thus compatible with every reference type.
   */

  /**
   * @spec It denotes a reference value which refers to a special “null”
   * object. This object implements methods in class scala.AnyRef as follows:
   *  • eq(x ) and ==(x ) return true iff the argument x is also the “null”
   *    object.
   */

  /**
   * @spec • ne(x ) and !=(x ) return true iff the argument x is not also the
   * “null” object.
   */

  /**
   * @spec • isInstanceOf[T ] always returns false.
   */

  /**
   *  • asInstanceOf[T ] returns the “null” object itself if T conforms to
   *    scala.AnyRef, and throws a NullPointerException otherwise.
   */
  property("`null` cannot be cast to type incompatible with `AnyRef`") =
    forAll { i: Int =>
      false
    } : @epfl(4624)
 
  
  /**
   * @spec A reference any other member NullPointerException to be thrown.
   */
  property("`null` does not implement `##`") = forAll { i: Int =>
    Scalac.doesNotCompile("object M { null.## }")
  } : @epfl(4311)
}
