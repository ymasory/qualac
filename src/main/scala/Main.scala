/**
 * Copyright (c) Yuvi Masory, 2011
 *
 * Available under the Qualac License, see /LICENSE.
 */ 
package qualac

import java.io.File
import java.sql.DriverManager

import com.martiansoftware.jsap.{ JSAP, JSAPResult, FlaggedOption, Switch }
import com.martiansoftware.jsap.stringparsers.{ FileStringParser,
                                                LongStringParser }

import org.squeryl.adapters.MySQLInnoDBAdapter
import org.squeryl.{ Session, SessionFactory }

import qualac.condor.CondorReporterRun
import qualac.db.{ DbCreationRun, DbDropRun }

object Main {

  val ProgramName = "Qualac"
  val ProgramNameLower = ProgramName.toLowerCase()
  val ProgramNameUpper = ProgramName.toUpperCase()

  val (conf, condor, report, createDb, dropDb) =
    ("config", "condor", "report", "create-db", "drop-db")

  lazy val jsap = {
    val jsap = new JSAP()
    val confOption =
      new FlaggedOption(conf).setLongFlag(conf).setRequired(true)
        .setStringParser(
          FileStringParser.getParser().setMustBeFile(true).setMustExist(true))
    jsap registerParameter confOption
    val condorOption =
      new FlaggedOption(condor).setLongFlag(condor)
        .setStringParser(
          FileStringParser.getParser().setMustBeFile(true).setMustExist(true))
    jsap registerParameter condorOption
    val reportOption =
      new FlaggedOption(report).setLongFlag(report)
        .setStringParser(LongStringParser.getParser())
    jsap registerParameter reportOption
    val createOption = new Switch(createDb).setLongFlag(createDb)
    jsap registerParameter createOption
    val dropOption = new Switch(dropDb).setLongFlag(dropDb)
    jsap registerParameter dropOption
    jsap
  }

  def main(args: Array[String]) {
    val jsapResult = jsap.parse(args)
    if (jsapResult.success) {

      val generalConfigFile = jsapResult.getFile(conf)
      val generalConfig = new ConfigFile(generalConfigFile)
      shout("using general configuration file " + generalConfigFile)

      val dbUser = generalConfig.getString("db_username")
      val dbPassword = generalConfig.getString("db_password")
      val dbUrl = generalConfig.getString("db_url")
      val dbCreds = db.DbCreds(dbUrl, dbUser, dbPassword)

      withShout("connecting to database") {
        Class.forName("com.mysql.jdbc.Driver");
        SessionFactory.concreteFactory = Some( () =>
          Session.create(
          DriverManager.getConnection(dbCreds.dbUrl, dbCreds.dbUser,
                                      dbCreds.dbPassword),
          new MySQLInnoDBAdapter))
      }

      val condorId = jsapResult.getLong(report, -1)
      if (condorId >= 0) {
        val recipients =
          generalConfig.getString("recipients").split(",").toList
        val account = generalConfig.getString("gmail_account")
        val name = generalConfig.getString("gmail_name")
        val password = generalConfig.getString("gmail_password")
        val condorReporterRun =
          new CondorReporterRun(recipients, account, name, password, condorId)
        condorReporterRun.run()
      }
      else if (jsapResult getBoolean createDb) {
        withShout("creating tables") {
          new DbCreationRun().run()
        }
      }
      else if (jsapResult getBoolean dropDb) {
        withShout("dropping tables") {
          new DbDropRun().run()
        }
      }
      else {
        val condorConfigFileOpt = Option(jsapResult.getFile(condor))
        condorConfigFileOpt match {
          case Some(condorConfigFile) => {
            withShout("using condor configuration file: " + condorConfigFile) {
              val condorConfig = new ConfigFile(condorConfigFile)
            }
          }
          case None => {
            // val fuzzRun = new FuzzRun()
            // fuzzRun fuzz()
          }
        }
      }
    }
    else Console.err println(usage(jsapResult))
  }

  def usage(jsapResult: JSAPResult) = {
    val LF = "\n"
    val builder = new java.lang.StringBuilder

    builder append LF
    val iter = jsapResult.getErrorMessageIterator()
    while (iter.hasNext()) {
      builder append ("Error: " + iter.next() + LF)
    }
    builder append LF
    builder append ("java -jar " + ProgramNameLower + ".jar ")
    builder append (jsap.getUsage + LF)
    builder toString
  }

  def withShout[A](str: String, error: Boolean = false,
                   suppressOutcome: Boolean = false) (thunk: => A): A = {
    val banner = (1 to 80).map(_ => "-").mkString
    val out = if (error) Console.err else Console.out
    out.println(banner)
    val name =
      if (error) (ProgramNameUpper + " ERROR: ")
      else (ProgramName + ": ")
    val msg = if (error) str.toUpperCase else str
    out.print(name + msg)
    if (suppressOutcome == false) out.print(" ... ")
    val a: A = try {
      val a = thunk
      if (suppressOutcome == false) out.print("done!")
      out.println()
      a
    }
    catch {
      case t: Throwable => {
        if (suppressOutcome == false) out.print("ERROR!")
        out.println()
        throw t
      }
    }
    finally {
      out.println(banner)
    }
    a
  }

  def shout(str: String, error: Boolean = false) =
    withShout(str, error, true) {}
}
