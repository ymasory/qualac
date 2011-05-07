package qualac.db

import java.io.File
import java.sql.DriverManager

import org.squeryl.{ Session, SessionFactory }
import org.squeryl.adapters.H2Adapter

import qualac.common.Env
import qualac.fuzz.FuzzRun

object Connection {

  val DbDirName = "h2"
  val DbName = FuzzRun.ProgramName

  val dbUsername = FuzzRun.ProgramName
  val dbPassword = Env.getPassword()
  val dbConn= "jdbc:h2:" + DbDirName + File.separator + DbName

  def init() {
    Class.forName("org.h2.Driver")
    SessionFactory.concreteFactory = Some(
      () => Session.create(
        DriverManager.getConnection(dbConn,
                                    dbUsername,
                                    dbPassword),
        new H2Adapter))
  }  
}
