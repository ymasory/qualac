package qualac.fuzz

import java.io.File

import com.martiansoftware.jsap.{ JSAP, JSAPResult, FlaggedOption }
import com.martiansoftware.jsap.stringparsers.FileStringParser

object Main {

  val ProgramName = "Qualac"
  val (conf, condor) = ("config", "condor")

  var _confFile: Option[File] = _

  lazy val jsap = {
    val jsap = new JSAP()
    val confOption =
      new FlaggedOption(conf).setLongFlag(conf)
        .setStringParser(
          FileStringParser.getParser().setMustBeFile(true).setMustExist(true))
    jsap registerParameter confOption
    val condorOption =
      new FlaggedOption(condor).setLongFlag(condor)
        .setStringParser(
          FileStringParser.getParser().setMustBeFile(true).setMustExist(true))
    jsap registerParameter condorOption
    jsap
  }

  def main(args: Array[String]) {
    val config = jsap.parse(args)
    if (config.success) {
      val condorFile = Option(config.getFile(condor))
      _confFile = Option(config.getFile(conf))
      _confFile match {
        case Some(file) => shout("using configuration file: " + file)
        case none       => shout("using default configuration file")
      }
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
    builder append ("  --conf   /path/to/file.conf" + LF)
    builder append ("  --condor /path/to/file.condor" + LF)
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
