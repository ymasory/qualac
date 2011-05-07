package qualac.db

import java.sql.DriverManager

import org.squeryl.{ Session, SessionFactory }
import org.squeryl.adapters.H2Adapter

import qualac.common.Env

object Connection {
  val dbUsername = "qualac"
  val dbPassword = Env.getPassword()
  val dbConn= "jdbc:h2://localhost:3306/qualac"

  def init() {
    Class.forName("com.h2database.jdbc.Driver")
    SessionFactory.concreteFactory = Some(
      () => Session.create(
        DriverManager.getConnection(dbConn,
                                    dbUsername,
                                    dbPassword),
        new H2Adapter))
  }  
}
