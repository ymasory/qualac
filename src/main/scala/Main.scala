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

import qualac.db.{ CondorReporter, DbMaker }

object Main {

  val ProgramName = "Qualac"
  val ProgramNameLower = ProgramName.toLowerCase()
  val ProgramNameUpper = ProgramName.toUpperCase()

  val (conf, condor, report, createDb) =
    ("config", "condor", "report", "create-db")

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
    val tableOption = new Switch(createDb).setLongFlag(createDb)
    jsap registerParameter tableOption
    jsap
  }

  def main(args: Array[String]) {
    val config = jsap.parse(args)
    if (config.success) {
      val confFile = config.getFile(conf)
      val env = new Env(confFile)
      val condorId = config.getLong(report, -1)
      if (condorId >= 0) {
        val reporter = new CondorReporter(env)
        shout("generating and mailing report")
        reporter.mailReport(condorId)
      }
      else if (config.getBoolean(createDb)) {
        shout("creating MySQL tables")
        new DbMaker(env).createDb()
      }
      else {
        val condorFile = Option(config.getFile(condor))
        shout("using configuration file: " + confFile)
        condorFile match {
          case Some(file) => {
            val condRun = new CondorRun(file, env)
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
    builder append ("java -jar " + ProgramNameLower + ".jar ")
    builder append (jsap.getUsage + LF)
    builder toString
  }


  def shout(str: String, error: Boolean = false) {
    val banner = (1 to 80).map(_ => "#").mkString
    val out = if (error) Console.err else Console.out
    out.println(banner)
    val name =
      if (error) (ProgramNameUpper + " ERROR: ")
      else (ProgramName + ": ")
    out.println(name + (if (error) str.toUpperCase else str))
    out.println(banner)
  }
}
