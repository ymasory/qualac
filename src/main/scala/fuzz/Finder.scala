package qualac.fuzz

import java.io.File

import org.clapper.classutil._
import org.scalacheck.Properties

import scala.util.matching.Regex

import qualac.common.Env

object Finder {

  val sbtClassDir =
    new File(Env.curDir, "target/scala_" + Env.scalaVersion + "/classes/")

  def discoverPropsMatching(Re: Regex) = {
    val jarFinder = ClassFinder()
    val sbtFinder = ClassFinder(List(sbtClassDir))
    val classes = jarFinder.getClasses ++ sbtFinder.getClasses
    val classMap = ClassFinder classInfoMap classes
    val allProps = classMap.keys.flatMap { name: String =>
      name match {
        case Re(_) => {
          val clazz = classMap(name)
          val supClass = clazz.superClassName
          if (supClass == "org.scalacheck.Properties") Some(clazz)
          else None
        }
        case _ => None
      }
    }
    val myProps = allProps.filter(_.name.startsWith("qualac.")).toList
    myProps.map { info =>
      val name = info.name
      val clazz = Class.forName(name)
      clazz.getField("MODULE$").get(null).asInstanceOf[Properties]
    }
  }

  def discoverProps() = discoverPropsMatching(".*".r)
}
