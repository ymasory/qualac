package qualac.fuzz

object Main {

  val ProgramName = "qualac"
  
  def main(args: Array[String]) {
    val fuzzRun = new FuzzRun
    fuzzRun fuzz()
  }
}
