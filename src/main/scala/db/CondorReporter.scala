/**
 * Copyright (c) Yuvi Masory, 2011
 *
 * Available under the Qualac License, see /LICENSE.
 */ 
package qualac.db

import java.sql.{ DriverManager, Timestamp }

import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

import org.squeryl.{ Session, SessionFactory }
import org.squeryl.adapters.MySQLAdapter
import org.squeryl.PrimitiveTypeMode._

import scala.xml.Text

import qualac.fuzz.Main
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

  def mailReport(id: Long) = {
    val (subject, report) = new Report(id).generateReport()
    println("subject: " + subject)
    println("body: " + report)
    // GMail.sendMail(recipients, subject, report, account, name, password,
    //                mimeType = "text/html")
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

  val q = new Querier(condorId)

  def generateReport() = {
    (makeSubject(), makeBody())
  }

  private def makeBody() = {
    val header =
"""
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
"""
    val xml = {
<html>
  <head>
  </head>
  <body>
    <h1>Qualac Report for Condor Run #{condorId}</h1>
    <h2 style="color:red">Properties failed</h2>
    <h2>Systems information</h2>
      {makeTimeParagraph()}
      {makePerformanceParagraph()}
      {makeExitsParagraph()}
    <h2 style="color:green">Properties passed</h2>
  </body>
</html>
    }

    header + xml.toString
  }

  private def dateRepr(d: DateTime) = {
    val dayFmt = DateTimeFormat.forPattern("EEE MMMM dd, YYYY")
    val timeFmt = DateTimeFormat.forPattern("hh:mma")
    dayFmt.print(d) + " at " + timeFmt.print(d)
  }

  private def makeTimeParagraph() = {
    <p>
      This Condor run began on {dateRepr(q.timeStarted())} and ended
      approximately {dateRepr(q.timeEnded())}.
    </p>
  }

  private def makePerformanceParagraph() = {
    <p>
      {q.numHosts()} hosts performed {q.numCompilations()} compilations.
      {q.jobsSubmitted()} Condor jobs were submitted, of which
      {q.jobsStarted()} actually started.
      A peak rate of {q.peakRate()} compilations was reached.
    </p>
  }

  private def makeExitsParagraph() = {
    val errLines =
      for ((k, v) <- q.errorMap()) yield {
        Text("There were " + k + " graceful JVM exits due to " + v)
      }
    val numCrashes = q.numCrashes()
    val crashLine =
      if (numCrashes <= 1) Text("There were no non-graceful JVM exits.")
      else
        Text("There were " + numCrashes + " non-graceful JVM exits (crashes).")
    <p>{errLines ++ crashLine}</p>
  }

  private def makeSubject() = {
    val dateTime = Env.now()
    val fmt = DateTimeFormat.forPattern("EEE YYYY-MMMM-dd, hh:mma")
    val datePart = fmt.print(dateTime)
    val tag = "[" + Main.ProgramName + "]"
    val passedString = "passed " + q.numPropsPassed()
    val falsifiedString = {
      val numFalsified = q.numPropsFalsified()
      (if (numFalsified == 0) "falsified " else "FALSIFIED") + numFalsified
    }
    val testPart = falsifiedString + ", " + passedString
    val condorPart = "Condor Run #" + condorId
    tag + " " + List(condorPart, testPart, datePart).mkString(" | ")
  }
}

class Querier(condorId: Long) {
  import SquerylSchema._

  def timeStarted(): DateTime = timeEndedStarted(false)
  def timeEnded(): DateTime = timeEndedStarted(true)

  def timeEndedStarted(isMax: Boolean): DateTime = {
    /*
     SELECT MAX(time_ended)
     FROM outcome o
       INNER JOIN run r ON o.run_id = r.id
       INNER JOIN condor_submission cs ON r.condor_submission_id = cs.id
     WHERE cs.condor_run_id = condorId;
     */
    // val timeEnded: Timestamp =
    //   transaction {
    //     Session.currentSession.setLogger( (s: String) => println(s) )
    //     from(outcome, run, submission) ( (o, r, cs) =>
    //       where (
    //         (o.runId === r.id) and
    //         (r.condorSubmissionId === cs.id)
    //       )
    //       compute(min(o.timeEnded))
    //     )
    //   }
    // new DateTime(timeEnded.getTime)
    new DateTime
  }

  def numHosts(): Long = {
    /*
     SELECT COUNT(DISTINCT hostname)
     FROM env e
       INNER JOIN run r ON e.run_id = r.id
       INNER JOIN condor_submission cs ON r.condor_submission_id = cs.id
     WHERE cs.condor_run_id = condorId;
     */
    transaction {
      from(env, run, submission) ( (e, r, cs) =>
        where (
          (e.runId === r.id) and
          (r.condorSubmissionId === cs.id)
        )
        compute(countDistinct(e.hostname))
      )
    }
  }

  def jobsSubmitted(): Int = {
    /*
     SELECT total_jobs
     FROM condor_run
     WHERE condor_run.id = condorId
     */
    -1
  }

  def jobsStarted(): Int = {
    /*
     SELECT COUNT(*)
     FROM run r
       INNER JOIN condor_submission cs on r.condor_submission_id = cs.id
     WHERE cs.condor_run_id = condorId;
     */
    -1
  }

  def numCompilations(): Long = {
    /*
     SELECT COUNT(*)
     FROM postcompile post
       INNER JOIN precompile pre ON post.precompile_id = pre.id
       INNER JOIN run r ON pre.run_id = r.id
       INNER JOIN condor_submission cs on r.condor_submission_id = cs.id
     WHERE cs.condor_run_id = condorId;
     */
    -1L
  }

  def numCrashes(): Long = {
    /*
     -- this yields suspicious results --
     SELECT COUNT(*)
     FROM run r
       INNER JOIN condor_submission cs on r.condor_submission_id = cs.id
     WHERE
       r.id NOT IN (SELECT o.run_id FROM outcome o) AND
       cs.condor_run_id = condorId;
     */
    -1L
  }

  def peakRate(): Int = -1
  def errorMap(): Map[Option[String], Int] =
    scala.collection.immutable.HashMap.empty
  def numPropsPassed(): Long = -1L
  def numPropsFalsified(): Long = -1L
} 
