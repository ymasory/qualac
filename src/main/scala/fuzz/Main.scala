package qualac.fuzz

import java.io.File

import com.martiansoftware.jsap.{ JSAP, JSAPResult, UnflaggedOption }
import com.martiansoftware.jsap.stringparsers.FileStringParser

object Main {

  val ProgramName = "qualac"
  val (conf, condor) = ("config", "condor")

  lazy val jsap = {
    val jsap = new JSAP()
    val confOption =
      new UnflaggedOption(conf)
        .setStringParser(
          FileStringParser.getParser().setMustBeFile(true).setMustExist(true))
    jsap registerParameter confOption
    val condorOption =
      new UnflaggedOption(condor)
        .setStringParser(
          FileStringParser.getParser().setMustBeFile(true).setMustExist(true))
    jsap registerParameter condorOption
    jsap
  }

  private var _confFile: Option[File] = _
  private var _condorFile: Option[File] = _

  def main(args: Array[String]) {
    val config = jsap.parse(args)
    if (config.success) {
      _condorFile = Option(config.getFile(condor))
      _confFile = Option(config.getFile(conf))
      _confFile match {
        case Some(file) => shout("using configuration file: " + file)
        case none       => shout("using default configuration file")
      }
      val fuzzRun = new FuzzRun()
      fuzzRun fuzz()
    }
    else println(usage(config))
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
    val name = if (error) "QUALAC ERROR: " else "Qualac: "
    out.println(name + (if (error) str.toUpperCase else str))
    out.println(banner)
  }

  def confFile = _confFile
  def condorFile = _condorFile
}
