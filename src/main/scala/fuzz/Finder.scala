/**
 * Copyright (c) Yuvi Masory, 2011
 *
 * Available under the Qualac License, see /LICENSE.
 */ 
package qualac.fuzz

import java.io.File

import org.clapper.classutil._
import org.scalacheck.Properties

import scala.annotation.tailrec
import scala.util.matching.Regex

import qualac.common.Env

object Finder {

  val sbtClassDir =
    new File(Env.curDir, "target/scala_" + Env.scalaVersion + "/classes/")
  val libDir =
    new File(Env.curDir, "lib_managed/scala_" + Env.scalaVersion + "/compile/")

  def discoverPropsMatching(Re: Regex,
                            ancestor: String ="org.scalacheck.Properties" ):
  List[Properties] = {
    val jarFinder = ClassFinder()
    val sbtClasses = {
      if (sbtClassDir.exists && libDir.exists) {
        val path = sbtClassDir :: libDir.listFiles().toList
        val sbtFinder = ClassFinder(path)
        sbtFinder.getClasses
      }
      else Nil
    }
    val classes = jarFinder.getClasses ++ sbtClasses
    val classMap = ClassFinder classInfoMap classes
    val allProps =
      ClassFinder.concreteSubclasses(ancestor, classMap)
    val myProps = allProps.filter(_.name.startsWith("qualac.")).toList
    val matchingProps = myProps flatMap { info: ClassInfo =>
      info.name match {
        case Re(n) => Some(info)
        case _     => None
      }
    }
    matchingProps.map { info =>
      val name = info.name
      val clazz = Class.forName(name)
      clazz.getField("MODULE$").get(null).asInstanceOf[Properties]
    }
  }
}
