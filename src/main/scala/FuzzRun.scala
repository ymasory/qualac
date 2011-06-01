/**
 * Copyright (c) Yuvi Masory, 2011
 *
 * Available under the Qualac License, see /LICENSE.
 */ 
package qualac

import java.io.File

import org.scalacheck._

import qualac.db.DB

class FuzzRun() {

  def fuzz() = {
    try {
      Main.shout("initializing environment")
      val env = new Env()
      Main.shout("... done")

      Main.shout("begin initializing database")
      val db = new DB(env)
      val runId = db.persistRun()
      db.persistRunEnvironment(runId)
      db.persistJavaProps(runId)
      Main.shout("... done")

      db.persistConfigs(runId, env.configMap)
      Main.shout("using " + env.numThreads + " threads")
      val finder = new Finder(env)
      val allProps = finder.loadProperties()
      Main.shout("found " + allProps.length + " properties to test")
      Main.shout("Fuzzing started. Going for " + env.durationSeconds +
                 " seconds. Down with scalac!")

      for (prop <- allProps) {
        val params = Test.Params(
          minSuccessfulTests = env.minSuccessfulTests,
          maxDiscardedTests = env.maxDiscardedTests,
          workers = env.numThreads
          // testCallback = new QCallback
        )
        prop.check(params)
      }

      db.persistExit(runId, None)
    }
    catch {
      case t1: Throwable => {
        try {
          // db.persistExit(Some(t1))
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
    Main.shout("No errors encountered. Done fuzzing.")
  }
}
