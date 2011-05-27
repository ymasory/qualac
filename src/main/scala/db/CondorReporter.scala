/**
 * Copyright (c) Yuvi Masory, 2011
 *
 * Available under the Qualac License, see /LICENSE.
 */ 
package qualac.db

import java.sql.DriverManager

import org.squeryl.{ Session, SessionFactory }
import org.squeryl.adapters.MySQLAdapter
import org.squeryl.PrimitiveTypeMode._

import qualac.common.{ ConfParser, Env, GMail }

object CondorReporter {

  Class.forName("com.mysql.jdbc.Driver")
  SessionFactory.concreteFactory = Some(
    () => Session.create(
    DriverManager.getConnection(Env.dbUrl,
                                Env.dbUsername,
                                Env.dbPassword),
    new MySQLAdapter))


  val password = ConfParser.getConfigString("gmail_password", Env.configMap)
  val recipients =
    ConfParser.getConfigString("recipients", Env.configMap).split(",").toList
  val account = ConfParser.getConfigString("gmail_account", Env.configMap)
  val name = ConfParser.getConfigString("gmail_name", Env.configMap)

  def mailReport() = {
    val lastId = lastCondorRunId()
    val (subject, report) = 
      if (lastId < 0) ("error generating report",
                       lastId + " is not a valid id")
      else new Report(lastId).generateReport()
    GMail.sendMail(recipients, subject, report, account, name, password)
  }

  def lastCondorRunId(): Long = {
    transaction {
      from(SquerylSchema.condorRun) ( r =>
        compute(nvl(max(r.id), -1))
      )
    }
  }
}

class Report(condorId: Long) {
  import SquerylSchema._

  def generateReport() = {
    ("subject", "report")
  }
}
