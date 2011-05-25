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
  val path = sbtClassDir :: libDir.listFiles().toList

  def discoverPropsMatching(Re: Regex,
                            ancestor: String ="org.scalacheck.Properties" ):
  List[Properties] = {
    val jarFinder = ClassFinder()
    val sbtFinder = ClassFinder(path)
    val classes = jarFinder.getClasses ++ sbtFinder.getClasses
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
