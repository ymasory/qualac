package qualac.fuzz

import org.squeryl.PrimitiveTypeMode._

import qualac.common.Env
import qualac.db.{ Connection, QualacSchema, Run, RunOutcome }
import QualacSchema.{ run, outcome }

class FuzzRun() {

  def fuzz() = {
    shout("Fuzzing started. Down with scalac!")
    try {
      Connection.init()
      transaction {
        QualacSchema.create
      }
      transaction {
        run.insert(Run(0, Env.now()))
      }
    }
    catch {
      case t: Throwable => {
        try {
          transaction {
            val failure = RunOutcome(0L,
                                     0L,
                                     Env.now(),
                                     Some(t.getStackTrace.mkString("\n")),
                                     Some(t.getMessage))
            outcome.insert(failure)
          }
          shout("successfully persisted exit-causing error")
        }
        catch {
          case t => {
            shout(
              "could not persist exit-causing error, printing instead",
              error=true)
            t.printStackTrace()
            shout(
              "done printing exit-causing error",
              error=true)
          }
        }
        shout("exiting from error", error=true)
        throw t
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
