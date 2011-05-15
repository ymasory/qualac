package qualac.fuzz

import java.io.File
import java.lang.reflect.Constructor
import java.net.URL

import org.scalacheck.{ Prop, Properties }

import qualac.common.Env.TestPattern

object Reflector {

  val dotChar = '.'
  val slashChar = '/'
  val dot = dotChar.toString
  val slash = slashChar.toString

  /** Return a list of all `Prop`s reflectively discovered and constructed. */
  def discoverProps(): List[Properties] = {
    val clazzNames = clazzNamesForPackage("qualac")
    val props = clazzNames.flatMap { name =>
      try {
        name match {
          case TestPattern(_) => {
            val clazz: Class[_] = Class.forName(name)
            if (clazz.getSuperclass.getName == "org.scalacheck.Properties") {
              val prop =
                clazz.getField("MODULE$").get(null).asInstanceOf[Properties]
              Some(prop)
            }
            else None
          }
          case _ => None
        }
      }
      catch {
        case e => {e.printStackTrace() ; None}
      }
    }
    props
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
          val (fileNames, dirNames) =
            directory.list().toList.partition(_.endsWith(".class"))

          val clazzNames: List[String] = fileNames map { name =>
              packageName + dot +
              name.substring(0, name.length - 6)
          }
          val recClazzNames: List[String] = dirNames.map{ d =>
            clazzNamesForPackage(packageName + dot + d)
          }.flatten
          
          (clazzNames ++ recClazzNames).sorted
        }
        else Nil
      }
      case None => Nil
    }
  }
}
