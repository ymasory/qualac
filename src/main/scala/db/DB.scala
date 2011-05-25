/**
 * Copyright (c) Yuvi Masory, 2011
 *
 * Available under the Qualac License, see /LICENSE.
 */ 
package qualac.db

import java.io.File
import java.io.File.{ separator => / }
import java.sql.{ DriverManager, Statement }
import java.sql.Types.CLOB

import scala.io.Source
import scala.tools.nsc.reporters.{ Reporter, StoreReporter }

import qualac.common.Env
import qualac.compile.ScalacMessage
import qualac.fuzz.{ FuzzRun, Main }

object DB {


  val con = makeConnection()
  val id: Long = {
    try {
      createTables()
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
  
  /** Establish connection with the database, returning the `Connection`. */
  private def makeConnection() = {
    val url = Env.dbUrl
    Class.forName("com.mysql.jdbc.Driver")
    DriverManager.getConnection(Env.dbUrl, Env.dbUsername, Env.dbPassword)
  }

  def persistConfigs(map: Map[String, Either[String, Int]]) {
    for (key <- map.keys) {
      val value: String = map(key) match {
        case Left(s) => s
        case Right(i) => i.toString
      }
      if ((key endsWith "_password") == false) {
        val sql = "INSERT INTO config(run_id, ukey, uvalue) VALUES(?, ?, ?)"
        val pstmt = con.prepareStatement(sql)
        pstmt.setLong(1, id)
        pstmt.setString(2, key)
        pstmt.setString(3, value)
        pstmt.executeUpdate()
        pstmt.close()
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
             "INSERT INTO postcompile(precomp_id, warnings, errors, time_ended) VALUES(?, ?, ?, ?)"
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
INSERT INTO compilemessage(precomp_id, severity, message, line, col, point)
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
      "  run_id, class, cause, message, stacktrace, time_ended) " +
      "VALUES (?, ?, ?, ?, ?, ?)"
    )
    val pstmt = con.prepareStatement(sql)
    pstmt.setLong(1, id)
    error match {
      case None => {
        pstmt.setNull(2, CLOB)
        pstmt.setNull(3, CLOB)
        pstmt.setNull(4, CLOB)
        pstmt.setNull(5, CLOB)
      }
      case Some(t) => {
        val clazz = t.getClass
        if (clazz == null) pstmt.setNull(2, CLOB)
        else pstmt.setString(2, clazz.toString)

        val cause = t.getCause
        if (cause == null) pstmt.setNull(3, CLOB)
        else pstmt.setString(3, cause.toString)

        pstmt.setString(4, t.getMessage)

        val trace = t.getStackTrace
        if (trace == null) pstmt.setNull(5, CLOB)
        else pstmt.setString(5, trace.mkString("\n"))
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
    val stmt =
      con.prepareStatement("INSERT INTO run (time_started) VALUES(?)",
                           Statement.RETURN_GENERATED_KEYS)
    stmt.setTimestamp(1, Env.nowStamp())
    stmt.executeUpdate()
    val rs = stmt.getGeneratedKeys()
    val id = if (rs.next()) rs.getLong(1)
             else sys.error("could not get generated key")
    rs.close()
    stmt.close()
    id
  }

  /** Create the db tables, if they don't exist yet. */
  private def createTables() {
    for (table <- Schema.tables) {
      val stmt = con.createStatement()
      stmt.executeUpdate(table)
      stmt.close()
    }
  }

  private def persistJavaProps(id: Long) {
    import scala.collection.JavaConversions._
    val set = System.getProperties.stringPropertyNames.toSet
    for (key <- set) {
      val value = System.getProperty(key)
      val sql = "INSERT INTO javaprop(run_id, jkey, jvalue) VALUES(?, ?, ?)"
      val pstmt = con.prepareStatement(sql)
      pstmt.setLong(1, id)
      pstmt.setString(2, key)
      pstmt.setString(3, value)
      pstmt.executeUpdate()
      pstmt.close()
    }

    val run = Runtime.getRuntime
    val totalMemory = run.totalMemory
    val freeMemory = run.freeMemory
    val maxMemory = run.maxMemory
    val processors = run.availableProcessors
    val sql =
"""
INSERT INTO runtimeprop(run_id, total_memory, free_memory, max_memory,
                        processors)
VALUES(?, ?, ?, ?, ?)
"""
    val pstmt = con.prepareStatement(sql)
    pstmt.setLong(1, id)
    pstmt.setLong(2, totalMemory)
    pstmt.setLong(3, freeMemory)
    pstmt.setLong(4, maxMemory)
    pstmt.setInt(5, processors)
    pstmt.executeUpdate()
    pstmt.close()

  }

  /** Store row in env table for this fuzz run. */
  private def persistRunEnvironment(id: Long) {
    val etcHostname = {
      val file = new File("/etc/hostname")
      if (file.exists) Source.fromFile(file).mkString.trim
      else "*unkown*"
    }
    val hostname = java.net.InetAddress.getLocalHost().getHostName()
    val sql =
      """|INSERT INTO
         |  env(run_id, scala_version, scala_version_string,
         |  scala_version_message, java_classpath, java_vendor,
         |  java_version, java_vm_info, java_vm_name, java_vm_vendor,
         |  java_vm_version, os, source_encoding, etc_hostname, hostname)
         |VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)""".stripMargin
    val pstmt = con.prepareStatement(sql)
    pstmt.setLong(1, id)
    pstmt.setString(2, Env.scalaVersion)
    pstmt.setString(3, Env.scalaVersionString)
    pstmt.setString(4, Env.scalaVersionMsg)
    pstmt.setString(5, Env.javaClasspath)
    pstmt.setString(6, Env.javaVendor)
    pstmt.setString(7, Env.javaVersion)
    pstmt.setString(8, Env.javaVmInfo)
    pstmt.setString(9, Env.javaVmName)
    pstmt.setString(10, Env.javaVmVendor)
    pstmt.setString(11, Env.javaVmVersion)
    pstmt.setString(12, Env.os)
    pstmt.setString(13, Env.sourceEncoding)
    pstmt.setString(14, etcHostname)
    pstmt.setString(15, hostname)
    pstmt.executeUpdate()
    pstmt.close()
  }

  def close() = con.close()
}

