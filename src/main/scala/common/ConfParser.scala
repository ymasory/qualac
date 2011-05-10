package qualac.common

import java.io.File
import java.io.InputStream

import scala.io.Source

object ConfParser {

  def parse(file: File): Map[String, Either[String, Int]] =
    parse(Source.fromFile(file).mkString)

  def parse(stream: InputStream): Map[String, Either[String, Int]] =
    parse(Source.fromInputStream(stream).mkString)

  private def parse(str: String): Map[String, Either[String, Int]] = {
    val lines = str.lines.toList
    val commentlessLines = lines.map { _.split("#").head }
    val pairs: List[(String, Either[String, Int])] =
      commentlessLines.flatMap { line =>
        val trimmed = line.trim
        val parts = trimmed.split("=").toList
        parts match {
          case k :: t => {
            val key: String = k.trim
            val v = t match {
                case v :: _ => v
                case Nil    => ""
            }
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
