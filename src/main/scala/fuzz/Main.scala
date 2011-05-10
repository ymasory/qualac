package qualac.fuzz

import java.io.File

object Main {

  private var _confFile: Option[File] = _

  def confFile = _confFile
  
  def main(args: Array[String]) {
    _confFile = args.toList match {
      case List(path, _*) => {
        val file = new File(path)
        if (file.exists) Some(file)
        else throw new qualac.QualacException("could not find file: " + file)
      }
      case Nil => None
    }
    _confFile match {
      case Some(file) => shout("using configuration file " + file)
      case None       => shout("using default configuration file")
    }
    val fuzzRun = new FuzzRun()
    fuzzRun fuzz()
  }

  def shout(str: String, error: Boolean = false) {
    val banner = (1 to 80).map(_ => "#").mkString
    val out = if (error) Console.err else Console.out
    out.println(banner)
    val name = if (error) "QUALAC ERROR: " else "Qualac: "
    out.println(name + (if (error) str.toUpperCase else str))
    out.println(banner)
  }
}
