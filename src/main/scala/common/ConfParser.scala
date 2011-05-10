package qualac.common

import java.io.File
import java.io.InputStream

import scala.io.Source

object ConfParser {

  def parse(stream: InputStream): Map[String, Either[String, Int]] =
    parse(Source.fromInputStream(stream).mkString)

  private def parse(str: String): Map[String, Either[String, Int]] = {
    val lines = str.lines.toList
    lines.map { _.split("#").head }
    val pairs: List[(String, Either[String, Int])] =
      lines.flatMap { line =>
        val trimmed = line.trim
        val parts = trimmed.split("=").toList
        parts match {
          case List(k, v, _*) => {
            val key: String = k.trim
            val vTrim = v.trim
            val value: Either[String, Int] = try { 
              Right(vTrim.toInt)
            }
            catch {
              case _ => Left(vTrim)
            }
            Some(key -> value)
          }
          case _ => None
        }
      }
    Map(pairs: _*)
  }
}
