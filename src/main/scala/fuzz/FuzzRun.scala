package qualac.fuzz

import org.squeryl.PrimitiveTypeMode._

import qualac.common.Env
import qualac.db.{ Connection, QualacSchema, Run, RunOutcome }
import QualacSchema.{ run, outcome }

class FuzzRun() {

  def fuzz() = {
    shout("Fuzzing started. Down with scalac!")
    try {
      Env.init() //fail fast
      Connection.init()
      transaction {
        QualacSchema.create
      }
      transaction {
        run.insert(Run(0, Env.now()))
      }
    }
    catch {
      case t1: Throwable => {
        try {
          transaction {
            val failure =
              new RunOutcome(0L, 0L, Env.now(),
                             Some(t1.getMessage),
                             Some(t1.getStackTrace.mkString("\n").getBytes))
            outcome.insert(failure)
          }
          shout("successfully persisted exit-causing error")
        }
        catch {
          case t2 => {
            shout(
              "could not persist exit-causing error, printing instead",
              error=true)
            t1.printStackTrace()
            shout("printing error in persisting error message", error=true)
            t2.printStackTrace
            shout(
              "done printing exit-causing errors",
              error=true)
          }
        }
        shout("exiting from error", error=true)
        sys.exit(1)
      }
    }
    shout("No errors encountered. Done fuzzing.")
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

object FuzzRun {
  
  def main(args: Array[String]) {
    val fuzzRun = new FuzzRun
    fuzzRun fuzz()
  }
}
