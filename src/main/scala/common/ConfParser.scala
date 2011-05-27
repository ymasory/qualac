/**
 * Copyright (c) Yuvi Masory, 2011
 *
 * Available under the Qualac License, see /LICENSE.
 */ 
package qualac.common

import java.io.File
import java.io.InputStream

import scala.io.Source

import qualac.QualacException

object ConfParser {
  
  val Delimiter = "="

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
  def getConfigInt(key: String, map: Map[String, Either[String, Int]]) = {
    map(key) match {
      case Right(i) => i
      case Left(_) => throw QualacException(key + " value must be an int")
    }
  }

  /** Pull a value from the config file that's supposed to be an `Int`. */
  def getConfigString(key: String, map: Map[String, Either[String, Int]]) = {
    map(key) match {
      case Right(_) => throw QualacException(key + " value must be an int")
      case Left(s) => s
    }
  }
}
