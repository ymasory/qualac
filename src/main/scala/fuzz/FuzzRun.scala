package qualac.fuzz

import java.io.File

import org.scalacheck._

import qualac.common.Env
import qualac.db.DB

class FuzzRun() {

  lazy val db = DB

  def fuzz() = {
    try {
      Main.shout("initializing environment: " + Env)
      Main.shout("initializing database: " + db)
      db.persistConfigs(Env.configMap)
      Main.shout("using " + Env.numThreads + " threads")
      Main.shout("Fuzzing started. Going for " + Env.durationSeconds +
                 " seconds. Down with scalac!")

      val params = Test.Params(workers = Env.numThreads)
      qualac.lex.IdentifierProperties.check(params)

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
