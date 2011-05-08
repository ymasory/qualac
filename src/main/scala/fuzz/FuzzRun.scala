package qualac.fuzz

import qualac.common.Env
import qualac.db.DB

class FuzzRun() {

  def fuzz() = {
    shout("Fuzzing started. Down with scalac!")
    var db: DB.type = null
    try {
      Env.init() //fail fast
      db = DB 
      db.init()  //initialize DB connection
    }
    catch {
      case t1: Throwable => {
        try {
          //persiste the error
          t1.printStackTrace
          shout("successfully persisted exit-causing error")
        }
        catch {
          case t2 => {
            shout(
              "could not persist exit-causing error, printing instead",
              error=true)
            t1.printStackTrace()
            shout(
              "printing error encountered in persisting exit-causing error",
              error=true)
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
    finally {
      if (db != null) db.close()
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
