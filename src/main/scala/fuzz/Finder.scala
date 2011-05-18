package qualac.fuzz

import org.clapper.classutil._
import org.scalacheck.Prop

object Finder {

  def discoverTests(): List[Prop] = {
    val finder = ClassFinder()
    val classes = finder.getClasses
    val classMap = ClassFinder classInfoMap classes
    val allProps =
      ClassFinder.concreteSubclasses("org.scalacheck.Prop", classMap)
    val myProps = allProps.filter(_.name.startsWith("qualac.")).toList
    myProps.map { info =>
      val name = info.name
      val adjName =
        if (name endsWith "$") name.substring(0, name.length - 1)
        else name
      println("instantiate " + adjName)
      Class.forName(adjName).newInstance.asInstanceOf[Prop]
    }
  }
}
