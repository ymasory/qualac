package qualac.common

import java.io.File

import scala.io.Source

object ConfParser {

  def parse(file: File): Map[String, Int] = parse(Source.fromFile(file).mkString)

  def parse(str: String): Map[String, Int] = {
    val lines = str.lines.toList
    lines.map { _.split("#").head }
    val pairs = lines.flatMap { line =>
      val trimmed = line.trim
      val parts = trimmed.split("=").toList
      parts match {
        case List(k, v, _*) => {
          try { Some(k.trim -> v.trim.toInt) }
          catch { case _ => None }
        }
        case _              => None
      }
    }
    Map(pairs: _*)
  }
}
