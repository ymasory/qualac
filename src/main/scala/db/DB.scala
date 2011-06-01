/**
 * Copyright (c) Yuvi Masory, 2011
 *
 * Available under the Qualac License, see /LICENSE.
 */ 
package qualac.db

import java.io.File
import java.io.File.{ separator => / }
import java.sql.{ Statement, Timestamp }
import java.sql.Types.{ BIGINT, CLOB }

import scala.io.Source
import scala.tools.nsc.reporters.{ Reporter, StoreReporter }

import qualac.common.Env
import qualac.compile.ScalacMessage
import qualac.fuzz.{ FuzzRun, Main }

import SquerylSchema._

object CondorDB {

  val con: java.sql.Connection = null

  def persistCondorRun(totalJobs: Int) = {
    val cr = new CondorRun(Env.nowStamp(), totalJobs)
    condorRunTable.insert(cr)
    cr.id
  }

  def persistSubmission(runId: Long, time: Timestamp, jobNum: Int,
                        propName: String) = {

    val sub = new CondorSubmission(runId, time, jobNum, propName)
    condorSubmissionTable.insert(sub)
    sub.id
  }
}

object DB {

  val con: java.sql.Connection = null

  val id: Long = {
    try {
      val id = persistRun()
      persistRunEnvironment(id)
      persistJavaProps(id)
      id
    }
    catch {
      case t: Throwable => {
        Main.shout("database initialization failed. exiting ...",
                   error=true)
        con.close()
        t.printStackTrace()
        sys.exit(1)
      }
    }
  }
  
  def persistConfigs(map: Map[String, Either[String, Int]]) {
    for (key <- map.keys) {
      val value: String = map(key) match {
        case Left(s) => s
        case Right(i) => i.toString
      }
      if ((key endsWith "_password") == false) {
        val config = new Config(id, key, value)
        configTable.insert(config)
      }
    }
  }

  private def bool2EnumString(b: Boolean) = if(b) "yes" else "no"
  private def severity2EnumString(s: Reporter#Severity) = {
    s.id match {
      case 2 => "error"
      case 1 => "warning"
      case 0 => "info"
      case i => sys.error("unkown severity: " + i)
    }
  }

  def persistPrecompile(progText: String, shouldCompile: Boolean):
    (Boolean, Boolean, List[ScalacMessage]) => Unit = {
      
      def insertPrecompile(): Long = {
        val sql =
          """|INSERT INTO precompile(run_id, program_text, errors_expected,
             |                       warnings_expected, time_started)
             |VALUES(?, ?, ?, ?, ?)""".stripMargin

        val pstmt = con.prepareStatement(sql,
                                         Statement.RETURN_GENERATED_KEYS)
        pstmt.setLong(1, id)
        pstmt.setString(2, progText)
        pstmt.setString(3, bool2EnumString(shouldCompile == false)) 
        pstmt.setString(4, "no")
        pstmt.setTimestamp(5, Env.nowStamp())
        pstmt.executeUpdate()

        val rs = pstmt.getGeneratedKeys()
        val trialId = if (rs.next()) rs.getLong(1)
                      else sys.error("could not get generated key")

        pstmt.close()
        trialId
      }
      val trialId = insertPrecompile()

      (hasWarnings: Boolean, hasErrors: Boolean,
       infos: List[ScalacMessage]) => {
         def persistSummary() {
           val sql =
"""
INSERT INTO postcompile(precompile_id, warnings, errors, time_ended)
VALUES(?, ?, ?, ?)
"""
           val pstmt = con.prepareStatement(sql)
           pstmt.setLong(1, trialId)
           pstmt.setString(2, bool2EnumString(hasWarnings))
           pstmt.setString(3, bool2EnumString(hasErrors))
           pstmt.setTimestamp(4, Env.nowStamp)
           pstmt.executeUpdate()
           pstmt.close()
         }
         def persistInfo(info: ScalacMessage) {
           val sql = (
"""
INSERT INTO compile_message(precompile_id, severity, message, line, col, point)
VALUES(?, ?, ?, ?, ?, ?)
"""
           )
           val pstmt = con.prepareStatement(sql)
           pstmt.setLong(1, trialId)
           pstmt.setString(2, severity2EnumString(info.severity))
           pstmt.setString(3, info.msg)
           pstmt.setInt(4, info.pos.line)
           pstmt.setInt(5, info.pos.column)
           pstmt.setInt(6, info.pos.point)
           pstmt.executeUpdate()
           pstmt.close()
         }

         persistSummary()
         for (info <- infos) {
           persistInfo(info)
         }
      }
  }

  def persistExit(error: Option[Throwable]) {
    val sql = (
      "INSERT INTO outcome(" +
      "  run_id, class, cause, message, stacktrace, time_ended, problem) " +
      "VALUES (?, ?, ?, ?, ?, ?, ?)"
    )
    val pstmt = con.prepareStatement(sql)
    pstmt.setLong(1, id)
    error match {
      case None => {
        pstmt.setNull(2, CLOB)
        pstmt.setNull(3, CLOB)
        pstmt.setNull(4, CLOB)
        pstmt.setNull(5, CLOB)
        pstmt.setString(7, "no")
      }
      case Some(t) => {
        val clazz = t.getClass
        if (clazz == null) pstmt.setNull(2, CLOB)
        else pstmt.setString(2, clazz.getName)

        val cause = t.getCause
        if (cause == null) pstmt.setNull(3, CLOB)
        else pstmt.setString(3, cause.toString)

        pstmt.setString(4, t.getMessage)

        val trace = t.getStackTrace
        if (trace == null) pstmt.setNull(5, CLOB)
        else pstmt.setString(5, trace.mkString("\n"))
        pstmt.setString(7, "yes")
      }
    }
    pstmt.setTimestamp(6, Env.nowStamp())
    pstmt.executeUpdate()
    pstmt.close()
  }

  /**
   * Store row in run table for this fuzz run, and return run.id the db
   * generates. Must be called AFTER `createTables()` and BEFORE storing
   * anything else in the db.
   */
  private def persistRun() = {

    // val run = new Run(Env.nowStamp(), Env.condorSubmitId)
    val run = new Run(Env.nowStamp(), null)
    runTable.insert(run)
    run.id
  }

  private def persistJavaProps(id: Long) {
    import scala.collection.JavaConversions._
    val set = System.getProperties.stringPropertyNames.toSet
    for (key <- set) {
      val value = System.getProperty(key)
      val prop = new JavaProp(id, key, value)
      SquerylSchema.javaPropTable.insert(prop)
    }

    val run = Runtime.getRuntime
    val totalMemory = run.totalMemory
    val freeMemory = run.freeMemory
    val maxMemory = run.maxMemory
    val processors = run.availableProcessors
    val prop = new RuntimeProp(id, totalMemory, freeMemory, maxMemory,
                               processors)
    SquerylSchema.runtimePropTable.insert(prop)
  }

  /** Store row in env table for this fuzz run. */
  private def persistRunEnvironment(id: Long) {
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

    val env = new SEnv(id, Env.scalaVersion, Env.scalaVersionString,
                       Env.scalaVersionMsg, Env.javaClasspath, Env.javaVendor,
                       Env.javaVmInfo, Env.javaVmName, Env.javaVmVendor,
                       Env.javaVmVersion, Env.os, Env.sourceEncoding,
                       etcHostname, hostname)

    SquerylSchema.envTable.insert(env)
  }
}

