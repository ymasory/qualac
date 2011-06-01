/**
 * Copyright (c) Yuvi Masory, 2011
 *
 * Available under the Qualac License, see /LICENSE.
 */ 
package qualac

import java.io.File

import scala.io.Source

class ConfigFile(file: File) {

  val Delimiter = "="

  val map = parse(Source.fromFile(file).mkString)
  
  private def parse(str: String): Map[String, Either[String, Int]] = {
    val lines = str.lines.toList
    val commentlessLines = lines.map { _.split("#").head }
    val pairs: List[(String, Either[String, Int])] =
      commentlessLines.flatMap { line =>
        val trimmed = line.trim
        val parts = trimmed.split(Delimiter).toList
        parts match {
          case k :: t => {
            val key: String = k.trim
            if (key.isEmpty) None
            else {
              val v = t match {
                case v :: vt => t.mkString(Delimiter)
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
          }
          case _ => None
        }
      }
    Map(pairs: _*)
  }

  /** Pull a value from the config file that's supposed to be an `Int`. */
  def getInt(key: String) = {
    map(key) match {
      case Right(i) => i
      case Left(_) => throw QualacException(key + " value must be an int")
    }
  }

  /** Pull a value from the config file that's supposed to be an `Int`. */
  def getString(key: String) = {
    map(key) match {
      case Right(_) => throw QualacException(key + " value must be an int")
      case Left(s) => s
    }
  }
}
