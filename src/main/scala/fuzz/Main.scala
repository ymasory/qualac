package qualac.fuzz

object Main {

  val ProgramName = "qualac"
  
  def main(args: Array[String]) {
    val fuzzRun = new FuzzRun
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
