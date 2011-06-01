/**
 * Copyright (c) Yuvi Masory, 2011
 *
 * Available under the Qualac License, see /LICENSE.
 */ 
package qualac.db

import java.io.File
import java.io.File.{ separator => / }
import java.sql.Timestamp

import scala.io.Source
import scala.tools.nsc.reporters.{ Reporter, StoreReporter }

import org.squeryl.PrimitiveTypeMode._

import qualac.{ Env, FuzzRun, Main }
import qualac.compile.ScalacMessage

class DB(env: Env) {
  
  def persistCondorRun(totalJobs: Int) = {
    val cr = new CondorRun(env.nowStamp(), totalJobs)
    transaction {
      SquerylSchema.condorRunTable.insert(cr)
    }
    cr.id
  }

  def persistSubmission(runId: Long, time: Timestamp, jobNum: Int,
                        propName: String) = {

    val sub = new CondorSubmission(runId, time, jobNum, propName)
    transaction {
      SquerylSchema.condorSubmissionTable.insert(sub)
    }
    sub.id
  }

  def persistConfigs(runId: Long, map: Map[String, Either[String, Int]]) {
    for (key <- map.keys) {
      val value: String = map(key) match {
        case Left(s) => s
        case Right(i) => i.toString
      }
      if ((key endsWith "_password") == false) {
        val config = new Config(runId, key, value)
        transaction {
          SquerylSchema.configTable.insert(config)
        }
      }
    }
  }

  private def bool2Enum(b: Boolean) = if(b) YesNo.Yes else YesNo.No
  private def severity2Enum(s: Reporter#Severity) = {
    s.id match {
      case 2 => Severity.Error
      case 1 => Severity.Warning
      case 0 => Severity.Info
      case x => sys.error("unkown severity: " + x)
    }
  }


  def persistPrecompile(runId: Long, progText: String, shouldCompile: Boolean):
    (Boolean, Boolean, List[ScalacMessage]) => Unit = {
      
      def insertPrecompile(): Long = {
        val preComp = new PreCompile(runId, progText,
                                     bool2Enum(shouldCompile == false),
                                     YesNo.No, env.nowStamp())
        transaction {
          SquerylSchema.preCompileTable.insert(preComp)
        }
        preComp.id
      }
      val trialId = insertPrecompile()

      (hasWarnings: Boolean, hasErrors: Boolean,
       infos: List[ScalacMessage]) => {
         def persistSummary() {
           val postComp = new PostCompile(trialId, bool2Enum(hasWarnings),
                                          bool2Enum(hasErrors), env.nowStamp)
           transaction {
             SquerylSchema.postCompileTable.insert(postComp)
           }
         }
         def persistInfo(info: ScalacMessage) {
           val cm = new CompileMessage(trialId,
                                       severity2Enum(info.severity),
                                       info.msg, info.pos.line,
                                       info.pos.column, info.pos.point)
           transaction {
             SquerylSchema.compileMessageTable.insert(cm)
           }
         }

         persistSummary()
         for (info <- infos) {
           persistInfo(info)
         }
      }
  }

  def persistExit(runId: Long, error: Option[Throwable]) {
    val stamp = env.nowStamp()
    val outcome = 
      error match {
        case None => new Outcome(runId, None, None, None, None, stamp,
                                 YesNo.No)
        case Some(t) => {
          val clazzVal = {
            val clazz = t.getClass
            if (clazz == null) None else Some(clazz.getName)
          }
          val causeVal = {
            val cause = t.getCause
            if (cause == null) None else Some(cause.toString)
          }
          val traceVal = {
            val trace = t.getStackTrace
            if (trace == null) None else Some(trace.mkString("\n"))
          }
          val msg = Option(t.getMessage)
          new Outcome(runId, clazzVal, causeVal, msg, traceVal, stamp,
                      YesNo.Yes)
      }
    }

    transaction {
      SquerylSchema.outcomeTable.insert(outcome)
    }
  }

  /**
   * Store row in run table for this fuzz run, and return run.id the db
   * generates. Must be called AFTER `createTables()` and BEFORE storing
   * anything else in the db.
   */
  def persistRun() = {

    // val run = new Run(env.nowStamp(), env.condorSubmitId)
    val run = new Run(env.nowStamp(), null)
    transaction {
      SquerylSchema.runTable.insert(run)
    }
    run.id
  }

  def persistJavaProps(id: Long) {
    import scala.collection.JavaConversions._
    val set = System.getProperties.stringPropertyNames.toSet
    for (key <- set) {
      val value = System.getProperty(key)
      val prop = new JavaProp(id, key, value)
      transaction {
        SquerylSchema.javaPropTable.insert(prop)
      }
    }

    val run = Runtime.getRuntime
    val totalMemory = run.totalMemory
    val freeMemory = run.freeMemory
    val maxMemory = run.maxMemory
    val processors = run.availableProcessors
    val prop = new RuntimeProp(id, totalMemory, freeMemory, maxMemory,
                               processors)
    transaction {
      SquerylSchema.runtimePropTable.insert(prop)
    }
  }

  /** Store row in env table for this fuzz run. */
  def persistRunEnvironment(id: Long) {
    val etcHostname = {
      val file = new File("/etc/hostname")
      if (file.exists) Source.fromFile(file).mkString.trim
      else "*unkown*"
    }
    val hostname =
      try {
        java.net.InetAddress.getLocalHost().getHostName()
      }
      catch {
        case _: java.net.UnknownHostException => "UnkownHostException"
      }

    val senv = new SEnv(id, env.scalaVersion, env.scalaVersionString,
                        env.scalaVersionMsg, env.javaClasspath, env.javaVendor,
                        env.javaVmInfo, env.javaVmName, env.javaVmVendor,
                        env.javaVmVersion, env.os, env.sourceEncoding,
                        etcHostname, hostname)

    transaction {
      SquerylSchema.envTable.insert(senv)
    }
  }
}

