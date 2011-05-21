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

  def discoverPropsMatching(Re: Regex): List[Properties] = {
    val jarFinder = ClassFinder()
    val sbtFinder = ClassFinder(List(sbtClassDir))
    val classes = jarFinder.getClasses ++ sbtFinder.getClasses
    val classMap = ClassFinder classInfoMap classes
    // val allProps = classMap.keys.flatMap { name: String =>
    //   name match {
    //     case Re(_) => {
    //       val clazz: ClassInfo = classMap(name)
    //       val supClass = clazz.superClassName
    //       if (supClass == "org.scalacheck.Properties") Some(clazz)
    //       else None
    //     }
    //     case _ => None
    //   }
    // }
    // val myProps = allProps.filter(_.name.startsWith("qualac.")).toList
    // myProps.map { info =>
    //   val name = info.name
    //   val clazz = Class.forName(name)
    //   clazz.getField("MODULE$").get(null).asInstanceOf[Properties]
    // }
    println(concreteSubclasses("org.scalacheck.Properties", classMap).toList)
    sys.exit
    null
  }

  def discoverProps() = discoverPropsMatching(".*".r)

  def concreteSubclasses(ancestor: String, classes: Map[String, ClassInfo]):   
    Iterator[ClassInfo] = {
    @tailrec def classMatches(ancestorClassInfo: ClassInfo,
                              classToCheck: ClassInfo): Boolean = {
      if (classToCheck.name == ancestorClassInfo.name)
        true
      else if ((classToCheck.superClassName == ancestorClassInfo.name) ||
               (classToCheck implements ancestorClassInfo.name))
        true
      else {
        classes.get(classToCheck.superClassName) match {
          case None            => false
          case Some(classInfo) => classMatches(ancestorClassInfo,
                                               classInfo)
        }
      }
    }
    classes.get(ancestor) match {
      case None   => Iterator.empty
      case Some(ci) =>
        classes.values.toIterator.
        filter(_.isConcrete).
        filter(classMatches(ci, _))
    }
  }
}
