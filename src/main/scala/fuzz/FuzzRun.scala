package qualac.fuzz

import qualac.common.Env
import qualac.db.DB

class FuzzRun() {

  lazy val db = DB

  def fuzz() = {
    try {
      Main.shout("initializing database: " + db)
      Main.shout("initializing environment: " + Env)
      val thread = new FuzzThread(Env.durationSeconds)
      Main.shout("Fuzzing started. Down with scalac!")
      thread.start()
      thread.join()
      DB.persistExit(None)
    }
    catch {
      case t1: Throwable => {
        try {
          DB.persistExit(Some(t1))
          Main.shout("successfully persisted exit-causing error", error=true)
        }
        catch {
          case t2 => {
            Main.shout(
              "could not persist exit-causing error, printing instead",
              error=true)
            t1.printStackTrace()
            Main.shout(
              "printing error encountered in persisting exit-causing error",
              error=true)
            t2.printStackTrace
            Main.shout(
              "done printing exit-causing errors",
              error=true)
          }
        }
        Main.shout("exiting from error", error=true)
        sys.exit(1)
      }
    }
    finally {
      if (db != null) db.close()
    }
    Main.shout("No errors encountered. Done fuzzing.")
  }
}
