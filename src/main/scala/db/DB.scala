package qualac.db

import java.io.File
import java.io.File.{ separator => / }
import java.sql.DriverManager

import qualac.common.Env
import qualac.fuzz.{ FuzzRun, Main }

object DB {


  private lazy val con = {
    val DbDirName = "h2"
    val DbName = Main.ProgramName

    val dbUsername = Main.ProgramName
    val dbPassword = Env.getPassword()
    val dbUrl= "jdbc:h2:" + DbDirName + / + DbName

    Class.forName("org.h2.Driver")
    DriverManager.getConnection(dbUrl, dbUsername, dbPassword)
  }



  /**
   * Initialize DB, creating if needed.
   *
   * It's here to encourage the program to fail fast by calling this
   * function immediately and getting some DB stuff out of the way.
   * */
  def init() = {
    con
    for (table <- Schema.tables) update(table)
    update("INSERT INTO run (date_started) VALUES (2011-01-01)")
  }

  /** Trivial wrapper over JDBC statement stuff. */
  private def update(sql: String) {
    val stmt = con.createStatement()
    stmt.executeUpdate(sql)
  }

  /** Create the tables, if the database doesn't exist. */
  def createIfNeeded() = null

  def close() = con.close()
}

