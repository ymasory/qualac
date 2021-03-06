/**
 * Copyright (c) Yuvi Masory, 2011
 *
 * Available under the Qualac License, see /LICENSE.
 */ 
package qualac.compile

import java.io.{ File, StringWriter, PrintWriter }

import scala.tools.nsc.{ Global, Settings }
import scala.tools.nsc.reporters.{ Reporter, StoreReporter }

import scala.util.Properties

import org.scalacheck._

import qualac.common.Env

/**
 * ScalaCheck generators for compiler instances.
 */
object Scalac {

  lazy val tmpDir = null

  /** Generate a compiler instance, UAR. */
  lazy val compiler: Gen[Global] = {
    val compiler = new Global(null, null)
    compiler
  }

  /** Generate a compiler settings configuration, NE/CA */
  lazy val settings: Gen[Settings] = {
    // val settings = new Settings 
    // settings.outputDirs setSingleOutput (curDir + "/target")
    // settings.classpath.tryToSet(null)
    // settings
    null
  }

  /** Generate a classpath list. NE */
  lazy val classPathList: Gen[String] = minimalClassPathList

  /**
   * Generate a *minimal* classpath list (just scala-library.jar and
   * scala-compiler.jar), UAR).
   */
  lazy val minimalClassPathList: Gen[String] = {
    val comp =
      "project/boot/scala-" + Env.scalaVersion + "/lib/scala-compiler.jar"
    val lib =
      "project/boot/scala-" + Env.scalaVersion + "/lib/scala-library.jar"
    Gen oneOf List(comp + Env.classPathSep + lib,
                   lib + Env.classPathSep + comp)
  }

  /** Generate a `StoreReporter` */
  def reporter = new StoreReporter

  def doesNotCompile(text: String) = (! doesCompile(text))

  def doesCompile(text: String) = {
    val (_, errors, _) = compile(text)
    errors == false
  }

  def compile(text: String) = doCompile(text, None)
  def parse(text: String) = doCompile(text, Some("parser"))

  private def doCompile(text: String, stopAfter: Option[String] = None) = {
    import java.io.{ BufferedWriter, FileWriter }

    val settings = new Settings
    stopAfter match {
      case Some(phase) => settings.stopAfter.value = List(phase)
      case None => 
    }
    val threadDir =
      new File(Env.outDir, "thread-" + Thread.currentThread.getId)
    if (threadDir.exists == false) threadDir.mkdirs()
    val file = new File(threadDir, "temp.scala")
    val writer = new BufferedWriter(new FileWriter(file))
    writer.write(text)
    writer.close()
    val classPathList = List(
      "project/boot/scala-" + Env.scalaVersion + "/lib/scala-compiler.jar" +
      ":project/boot/scala-" + Env.scalaVersion + "/lib/scala-library.jar")
    settings.outputDirs setSingleOutput (threadDir.getPath)
    settings.classpath.tryToSet(classPathList)
    val reporter = new StoreReporter
    val compiler = new Global(settings, reporter)
    (new compiler.Run).compile(List(file.getAbsolutePath))
    (reporter.hasWarnings, reporter.hasErrors, reporter.infos.toList)
  }
}
