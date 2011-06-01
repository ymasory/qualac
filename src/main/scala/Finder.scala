/**
 * Copyright (c) Yuvi Masory, 2011
 *
 * Available under the Qualac License, see /LICENSE.
 */ 
package qualac

import org.scalacheck.Properties

class Finder(env: Env) {

  def loadProperties(): List[Properties] = {
    val myProps = env.patternClasses
    myProps.map { name =>
      val clazz = Class.forName(name)
      clazz.getField("MODULE$").get(null).asInstanceOf[Properties]
    }
  }
}
