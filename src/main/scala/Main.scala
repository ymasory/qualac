/**
 * Copyright (c) Yuvi Masory, 2011
 *
 * Available under the Qualac License, see /LICENSE.
 */ 
package qualac

import java.io.File

import com.martiansoftware.jsap.{ JSAP, JSAPResult, FlaggedOption, Switch }
import com.martiansoftware.jsap.stringparsers.{ FileStringParser,
                                                LongStringParser }

import qualac.db.{ CondorReporter, TableMaker }

object Main {

  val ProgramName = "Qualac"
  val (conf, condor, report, tables) =
    ("config", "condor", "report", "tables")

  private var _confFile: File = _

  lazy val jsap = {
    val jsap = new JSAP()
    val confOption =
      new FlaggedOption(conf).setLongFlag(conf).setRequired(true)
        .setStringParser(
          FileStringParser.getParser().setMustBeFile(true).setMustExist(true))
    jsap registerParameter confOption
    val condorOption =
      new FlaggedOption(condor).setLongFlag(condor)
        .setStringParser(
          FileStringParser.getParser().setMustBeFile(true).setMustExist(true))
    jsap registerParameter condorOption
    val reportOption =
      new FlaggedOption(report).setLongFlag(report)
        .setStringParser(LongStringParser.getParser())
    jsap registerParameter reportOption
    val tableOption = new Switch(tables).setLongFlag(tables)
    jsap registerParameter tableOption
    jsap
  }

  def main(args: Array[String]) {
    val config = jsap.parse(args)
    if (config.success) {
      _confFile = config.getFile(conf)
      val condorId = config.getLong(report, -1)
      if (condorId >= 0) {
        shout("generating and mailing report")
        CondorReporter.mailReport(condorId)
      }
      else if (config.getBoolean(tables)) {
        shout("creating MySQL tables")
        TableMaker.makeTables()
      }
      else {
        val condorFile = Option(config.getFile(condor))
        shout("using configuration file: " + _confFile)
        condorFile match {
          case Some(file) => {
            val condRun = new CondorRun(file)
            condRun fuzz()
          }
          case None => {
            val fuzzRun = new FuzzRun()
            fuzzRun fuzz()
          }
        }
      }
    }
    else Console.err println(usage(config))
  }

  def usage(config: JSAPResult) = {
    val LF = "\n"
    val builder = new java.lang.StringBuilder

    builder append LF
    val iter = config.getErrorMessageIterator()
    while (iter.hasNext()) {
      builder append ("Error: " + iter.next() + LF)
    }
    builder append LF
    builder append "run "
    builder append (jsap.getUsage + LF)
    builder append LF
    builder append ("Options:" + LF)
    builder append ("  --tables" + LF)
    builder append ("  --conf   /path/to/file.conf" + LF)
    builder append ("  --condor /path/to/file.condor" + LF)
    builder append ("  --report" + LF)
    builder append LF
    builder append ("Examples:" + LF)
    builder append ("  run" + LF)
    builder append ("  run --conf /home/yuvi/.qualac.conf" + LF)
    builder append LF
    builder toString
  }


  def shout(str: String, error: Boolean = false) {
    val banner = (1 to 80).map(_ => "#").mkString
    val out = if (error) Console.err else Console.out
    out.println(banner)
    val name =
      if (error) (ProgramName.toUpperCase + " ERROR: ")
      else (ProgramName.capitalize + ": ")
    out.println(name + (if (error) str.toUpperCase else str))
    out.println(banner)
  }

  def confFile = _confFile
}
