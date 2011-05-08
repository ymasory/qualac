package qualac.db

import java.io.File
import java.io.File.{ separator => / }
import java.sql.{ DriverManager, Timestamp }

import qualac.common.Env
import qualac.fuzz.{ FuzzRun, Main }

object DB {


  val con = makeConnection()
  val id: Long = {
    try {
      createTables()
      val id = storeRun()
      storeRunEnvironment()
      println("this is run: " + id)
      id
    }
    catch {
      case t: Throwable => {
        Main.shout("database initialization failed. exiting ...",
                   error=true)
        con.close()
        sys.exit(1)
      }
    }
  }
  

  /**
   * Just to encourage early initialization of the `DB` object, whose
   * initialization code opens the db connection and stores information
   * about this run.
   */
  def init() = {}

  /** Establish connection with the database, returning the `Connection`. */
  private def makeConnection() = {
    val DbDirName = "h2"
    val DbName = Main.ProgramName

    val dbUsername = Main.ProgramName
    val dbPassword = Env.getPassword()
    val dbUrl= "jdbc:h2:" + DbDirName + / + DbName
    println(dbUrl)

    Class.forName("org.h2.Driver")
    DriverManager.getConnection(dbUrl, dbUsername, dbPassword)
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
    val sql =
      "INSERT INTO outcome(run_id, message, stacktrace) VALUES (?, ?, ?)"
    val pstmt = con.prepareStatement(sql)
    pstmt.setLong(1, id)
    error match {
      case None => {
        pstmt.setNull(2, java.sql.Types.CLOB)
        pstmt.setNull(3, java.sql.Types.CLOB)
      }
      case Some(t) => {
        pstmt.setString(2, t.getMessage)
        pstmt.setString(3, t.getStackTrace.mkString("\n"))
      }
    }
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
      con.prepareStatement("INSERT INTO run (time_started) values(?)")
    pstmt.setTimestamp(1, new Timestamp(Env.now().toDate.getTime))
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

