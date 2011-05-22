package qualac.fuzz

import java.io.File

import org.scalacheck._

import qualac.QProp
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
      val allProps =
        Finder.discoverPropsMatching(Env.TestPattern, "qualac.QProp")
      Main.shout("found " + allProps.length + " properties to test")
      Main.shout("Fuzzing started. Going for " + Env.durationSeconds +
                 " seconds. Down with scalac!")

      for (prop <- allProps) {
        val params = Test.Params(
          minSuccessfulTests = Env.minSuccessfulTests,
          maxDiscardedTests = Env.maxDiscardedTests,
          workers = Env.numThreads,
          testCallback = new QCallback
        )
        prop.check(params)
      }

      DB.persistExit(None)
    }
    catch {
      case t1: Throwable => {
        try {
          DB.persistExit(Some(t1))
          t1.printStackTrace()
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
