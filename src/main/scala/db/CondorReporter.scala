/**
 * Copyright (c) Yuvi Masory, 2011
 *
 * Available under the Qualac License, see /LICENSE.
 */ 
package qualac.db

import org.squeryl.{ Session, SessionFactory }
import org.squeryl.adapters.MySQLAdapter

import qualac.common.{ ConfParser, Env, GMail }

object CondorReporter {

  Class.forName("com.mysql.jdbc.Driver")
  SessionFactory.concreteFactory = Some(
    () => Session.create(
    DriverManager.getConnection(Env.dbUrl,
                                Env.dbUser,
                                Env.dbPassword),
    new MySQLAdapter))


  val password = ConfParser.getConfigString("gmail_password", Env.configMap)
  val recipients =
    ConfParser.getConfigString("recipients", Env.configMap).split(",").toList
  val account = ConfParser.getConfigString("gmail_account", Env.configMap)
  val name = ConfParser.getConfigString("gmail_name", Env.configMap)

  def mailReport() = {
    val (subject, report) = generateReport()
    GMail.sendMail(recipients, subject, report, account, name, password)
  }

  import SquerylSchema._

  def generateReport() = {
    numPreCompilations()
    ("subject", "report")
  }

  def numPreCompilations() = {
    from(preCompliations) ( s =>
      select(s)
    )
  }
}
