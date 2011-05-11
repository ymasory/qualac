package qualac.db

import java.io.File
import java.io.File.{ separator => / }
import java.sql.{ DriverManager }
import java.sql.Types.CLOB

import qualac.common.Env
import qualac.fuzz.{ FuzzRun, Main }

object DB {


  val con = makeConnection()
  val id: Long = {
    try {
      createTables()
      val id = storeRun()
      storeRunEnvironment()
      storeJavaProps()
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
    if (url contains "jdbc:h2:")
      Class.forName("org.h2.Driver")
    else if (url contains "jdbc:mysql:")
      Class.forName("com.mysql.jdbc.Driver")
    else
      throw qualac.QualacException(
        "I don't know what driver to load for " + url + ". You must " +
        "modify the code to use this database.")
                                
    DriverManager.getConnection(Env.dbUrl, Env.dbUsername, Env.dbPassword)
  }

  def persistTrial() {
    val sql = "INSERT INTO trial(run_id, expected) VALUES(?, ?)"
    val pstmt = con.prepareStatement(sql)
    pstmt.setLong(1, id)
    pstmt.setString(2, "durf")
    pstmt.executeUpdate()
    pstmt.close()
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
  private def storeRun() = {
    val pstmt =
      con.prepareStatement("INSERT INTO run (time_started) VALUES(?)")
    pstmt.setTimestamp(1, Env.nowStamp())
    pstmt.executeUpdate()
    pstmt.close()

    val stmt = con.createStatement()
    val res = stmt.executeQuery(
      "SELECT id FROM run ORDER BY id DESC LIMIT 1")
    res.next()
    val id = res.getLong("id")
    res.close()
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

  private def storeJavaProps() {
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
  }

  /** Store row in env table for this fuzz run. */
  private def storeRunEnvironment() {
    val sql =
      """|INSERT INTO
         |  env(run_id, scala_version, scala_version_string,
         |  scala_version_message, java_classpath, java_vendor,
         |  java_version, java_vm_info, java_vm_name, java_vm_vendor,
         |  java_vm_version, os, source_encoding)
         |VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)""".stripMargin
    val pstmt = con.prepareStatement(sql)
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
    pstmt.close()
  }

  def close() = con.close()
}

