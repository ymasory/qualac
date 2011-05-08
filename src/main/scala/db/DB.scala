package qualac.db

import java.io.File
import java.io.File.{ separator => / }
import java.sql.DriverManager

import org.springframework.jdbc.datasource.DriverManagerDataSource

import qualac.common.Env
import qualac.fuzz.{ FuzzRun, Main }

object DB {

  /**
   * Does nothing.
   *
   * It's here just to encourage the program to fail fast by calling this
   * function immediately and getting the DB connection out of the way.
   * */
  def init() = { con }

  private lazy val con = {
    val DbDirName = "h2"
    val DbName = Main.ProgramName

    val dbUsername = Main.ProgramName
    val dbPassword = Env.getPassword()
    val dbUrl= "jdbc:h2:" + DbDirName + / + DbName

    Class.forName("org.h2.Driver")
    DriverManager.getConnection(dbUrl, dbUsername, dbPassword)
  }

  val stmt = con.createStatement()
  // stmt.executeUpdate(Schema.runTable)

  /** Create the tables, if the database doesn't exist. */
  def createIfNeeded() = null

  def close() = con.close()
}

