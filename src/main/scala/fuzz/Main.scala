package qualac.fuzz

import java.io.File

object Main {

  private var _confFile: Option[File] = _

  def confFile = _confFile
  
  def main(args: Array[String]) {
    _confFile =
      if (args.length > 0) {
        val file = new File(args(0))
        if (file.exists) Some(file)
        else None
      }
      else None
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
