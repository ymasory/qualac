package qualac.fuzz

import java.io.File
import java.lang.reflect.Constructor
import java.net.URL

import org.scalacheck.{ Prop, Properties }

object Reflector {

  val dotChar = '.'
  val slashChar = '/'
  val dot = dotChar.toString
  val slash = slashChar.toString

    //   val clazz = Class.forName(name)
    //   val constructors = clazz.getConstructors().toList
    //   val con: Constructor[PatternDetector] =
    //     clazz.getConstructor(classOf[Global])
    //     .asInstanceOf[Constructor[PatternDetector]]

  def discoverProps(): List[Prop] = {
    val clazzNames = clazzNamesForPackage("qualac")
    for {
      name <- clazzNames
      // if name.contains("$$anonfun$")
      // if name.contains("Properties")
      clazz = Class.forName(name)
    } {
        try {
          val con = clazz.newInstance()
          // clazz.asInstanceOf[Properties]
          println(name)
          clazz.getField("MODULE$").get(null).asInstanceOf[Properties]
          println(con)
          println(clazz.getFields.toList)
          println
        }
        catch {
          case _: InstantiationException =>
          case e =>
        }
      // val clazz = Class.forName(name)
      // val cons = clazz.getConstructors.toList
      // println(name)
      // println(cons)
      // println()
    }
    null
  }

  /**
   * Find the fully qualified names of all the classes under the provied
   * package names, where packages are considered nested.
   */
  def clazzNamesForPackage(packageName: String): List[String] = {
    val name = (slash + packageName) replace (dotChar, slashChar)
    val urlOpt = Option(Reflector.getClass.getResource(name))
    urlOpt match {
      case Some(url) => {
        val directory = new File(url.getFile)
        if (directory.exists()) {
          val (filesNames, dirNames) =
            directory.list().toList.partition(_.endsWith(".class"))

          val clazzNames: List[String] = filesNames map { name =>
              packageName + dot +
              name.substring(0, name.length - 6)
          }
          val recClazzNames: List[String] = dirNames.map{ d =>
            clazzNamesForPackage(packageName + dot + d)
          }.flatten
          
          clazzNames ++ recClazzNames
        }
        else Nil
      }
      case None => Nil
    }
  }
}
