/**
 * Copyright (c) Yuvi Masory, 2011
 *
 * Available under the Qualac License, see /LICENSE.
 */ 
package qualac.fuzz

import org.scalacheck.Properties

import qualac.common.Env

object Finder {

  def loadProperties(): List[Properties] = {
    val myProps = Env.patternClasses
    myProps.map { name =>
      val clazz = Class.forName(name)
      clazz.getField("MODULE$").get(null).asInstanceOf[Properties]
    }
  }
}
