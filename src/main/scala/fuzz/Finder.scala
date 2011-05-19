package qualac.fuzz

import java.io.File

import org.clapper.classutil._
import org.scalacheck.Properties

import qualac.common.Env

object Finder {

  val sbtClassDir =
    new File(Env.curDir, "target/scala_" + Env.scalaVersion + "/classes/")

  def discoverProps() = {    
    // val jarFinder = ClassFinder()
    // val sbtFinder = ClassFinder(List(sbtClassDir))
    // val classes = jarFinder.getClasses ++ sbtFinder.getClasses
    val classes = ClassFinder(List(sbtClassDir)).getClasses
    val clazz = classes.toList.head
    val classMap = ClassFinder classInfoMap classes
    val allProps = classMap.keys.flatMap { name: String =>
      println(name)
      Some(classMap(name))
    }
    val myProps = allProps.filter(_.name.startsWith("qualac.")).toList
    myProps.map { info =>
      val name = info.name
      val clazz = Class.forName(name)
      clazz.getField("MODULE$").get(null).asInstanceOf[Properties]
    }
  }
}
