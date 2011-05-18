package qualac.fuzz

import org.clapper.classutil._
import org.scalacheck.Properties

object Finder {

  def discoverTests(): List[Properties] = {
    val finder = ClassFinder()
    val classes = finder.getClasses
    val classMap = ClassFinder classInfoMap classes
    val allProps =
      ClassFinder.concreteSubclasses("org.scalacheck.Properties", classMap)
    val myProps = allProps.filter(_.name.startsWith("qualac.")).toList
    myProps.map { info =>
      val name = info.name
      val clazz = Class.forName(name)
      clazz.getField("MODULE$").get(null).asInstanceOf[Properties]
    }
  }
}
