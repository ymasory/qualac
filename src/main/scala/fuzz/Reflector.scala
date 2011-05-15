package qualac.fuzz

import java.io.File
import java.net.URL

import java.lang.reflect.Constructor

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
