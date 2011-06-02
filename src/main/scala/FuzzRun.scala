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

  val runId = -1L
  val db: DB = null

  def fuzz() = {
    try {
      val env = Main.withShout("initializing environment") { new Env(null) }
      val (_, _) = Main.withShout("begin initializing database") {
        val db = new DB(env)
        val runId = db.persistRun()
        db.persistRunEnvironment(runId)
        db.persistJavaProps(runId)
        (db, runId)
      }

      db.persistConfigs(runId, null)
      Main.withShout("using " + env.numThreads + " threads") {}
      val finder = new Finder(env)
      val allProps = finder.loadProperties()
      Main.withShout("found " + allProps.length + " properties to test") {}
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
          Main.withShout("persisting exit-causing error", error = true) {
            db.persistExit(runId, Some(t1))
            t1.printStackTrace()
          }
        }
        catch {
          case t2 => {
            Main.withShout(
              "could not persist exit-causing error, printing instead",
              error=true) {
                t1.printStackTrace()
            }
            Main.withShout(
              "printing error encountered in persisting exit-causing error",
              error=true) {
                t2.printStackTrace
            }
          }
        }
        Main.shout("exiting from error", error=true)
        sys.exit(1)
      }
    }
  }
}
