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

import qualac.db.{ CondorReporter, DbCreationRun, DbDropRun }

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

      val generalConfig = new ConfigFile(jsapResult.getFile(conf))
      val dbUser = generalConfig.getString("db_username")
      val dbPassword = generalConfig.getString("db_password")
      val dbUrl = generalConfig.getString("db_url")
      val dbCreds = db.DbCreds(dbUrl, dbUser, dbPassword)

      shout("connecting to database")
      Class.forName("com.mysql.jdbc.Driver");
      SessionFactory.concreteFactory = Some( () =>
        Session.create(
        DriverManager.getConnection(dbCreds.dbUrl, dbCreds.dbUser,
                                    dbCreds.dbPassword),
        new MySQLInnoDBAdapter))
      shout("... done")

      val condorId = jsapResult.getLong(report, -1)
      if (condorId >= 0) {
      //   val reporter = new CondorReporter(env)
      //   shout("generating and mailing report")
      //   reporter.mailReport(condorId)
      }
      else if (jsapResult getBoolean createDb) {
        shout("creating tables")
        new DbCreationRun().run()
        shout("... done")
      }
      else if (jsapResult getBoolean dropDb) {
        shout("dropping tables")
        new DbDropRun().run()
        shout("... done")
      }
      else {
      //   val condorFile = Option(jsapResult.getFile(condor))
      //   shout("using configuration file: " + confFile)
      //   condorFile match {
      //     case Some(file) => {
      //       val condRun = new CondorRun(file, env)
      //       condRun fuzz()
      //     }
      //     case None => {
      //       val fuzzRun = new FuzzRun()
      //       fuzzRun fuzz()
      //     }
      //   }
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


  def shout(str: String, error: Boolean = false) {
    val banner = (1 to 80).map(_ => "#").mkString
    val out = if (error) Console.err else Console.out
    out.println(banner)
    val name =
      if (error) (ProgramNameUpper + " ERROR: ")
      else (ProgramName + ": ")
    out.println(name + (if (error) str.toUpperCase else str))
    out.println(banner)
  }
}
