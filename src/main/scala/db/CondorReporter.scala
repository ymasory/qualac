/**
 * Copyright (c) Yuvi Masory, 2011
 *
 * Available under the Qualac License, see /LICENSE.
 */ 
package qualac.db

import java.sql.{ DriverManager, Timestamp }

import org.joda.time.DateTime

import org.squeryl.{ Session, SessionFactory }
import org.squeryl.adapters.MySQLAdapter
import org.squeryl.PrimitiveTypeMode._

import scala.xml.Text

import qualac.{ ConfParser, Env, GMail }
import SquerylSchema._

class CondorReporter(env: Env) {

  val password = ConfParser.getConfigString("gmail_password", env.configMap)
  val recipients =
    ConfParser.getConfigString("recipients", env.configMap).split(",").toList
  val account = ConfParser.getConfigString("gmail_account", env.configMap)
  val name = ConfParser.getConfigString("gmail_name", env.configMap)

  def mailReport(id: Long) = {
    val (subject, report) = new Report(env, id).generateReport()
    println("subject: " + subject)
    println("body: " + report)
    GMail.sendMail(recipients, subject, report, account, name, password,
                   mimeType = "text/html")
  }

  def lastCondorRunId(): Long = {
    transaction {
      from(condorRunTable) ( r =>
        compute(nvl(max(r.id), -1))
      )
    }
  }
}

object DateFmt {

  import org.joda.time.Period
  import org.joda.time.format.{ DateTimeFormat, PeriodFormatterBuilder }

  def conciseRepr(d: DateTime) = {
    val fmt = DateTimeFormat.forPattern("EEE YYYY-MMMM-dd, hh:mma")
    fmt.print(d)
  }

  def fullRepr(d: DateTime) = dateRepr(d) + " at " + timeRepr(d)

  def timeRepr(d: DateTime) = {
    val timeFmt = DateTimeFormat.forPattern("hh:mma")
    timeFmt.print(d)
  }

  def dateRepr(d: DateTime) = {
    val dayFmt = DateTimeFormat.forPattern("EEE MMMM dd, YYYY")
    dayFmt.print(d)
  }

  def periodRepr(p: Period) = {
    val fmt =
      new PeriodFormatterBuilder()
        .printZeroAlways()
        .appendHours()
        .appendSuffix(" hours")
        .appendSeparator(", ")
        .appendMinutes()
        .appendSuffix(" minutes")
        .appendSeparator(", ")
        .toFormatter()
    fmt.print(p)
  }
}

class Report(env: Env, condorId: Long) {

  import org.joda.time.Duration

  val q = new Querier(condorId)

  def generateReport() = {
    (makeSubject(), makeBody())
  }

//scalatest green EE5566
//scalatest red 55EE66

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

  private def makeTimeParagraph() = {
    val started = q.timeStarted()
    val ended = q.timeEnded()
    val period = (new Duration(started.getMillis, ended.getMillis)).toPeriod()
    <p>
      This Condor run began on {DateFmt.fullRepr(started)} and ended
      approximately {DateFmt.fullRepr(ended)}, making a total of
      {DateFmt.periodRepr(period)}.
    </p>
  }

  private def makePerformanceParagraph() = {
    val (comPerSec, when) = q.peakRate()
    val threadsNode = {
      val numThreads = q.numThreads()
      if (numThreads == 0) { <tt>Runtime.availableProcessors</tt> }
      else Text(numThreads.toString)
    }
    <p>
      During that time {q.numHosts()} hosts performed {q.numCompilations()}
      compilations. Each JVM used {threadsNode} threads.
      A peak rate of {comPerSec} compilations per minute was reached at
      {DateFmt.timeRepr(when)}.
    </p>
  }

  private def makeExitsParagraph() = {
    val errLines =
      for ((errOpt, count) <- q.errorMap()) yield {
        errOpt match {
          case Some(err) =>
            Text("There were " + count  + " graceful exits due to " +
                 err + ".")
          case None => Text("There were " + count +
                            " graceful exits from unkown causes.")
        }
      }
    <p>
      {q.jobsSubmitted()} Condor jobs were submitted, of which
      {q.jobsStarted()} actually started.
      Of those, {q.numRecordedExits()} recorded their exits.
      {errLines.mkString(" ")}
    </p>

  }

  private def makeSubject() = {
    val dateTime = env.now()
    val datePart = DateFmt.conciseRepr(dateTime)
    val passedString = "passed " + q.numPropsPassed()
    val falsifiedString = {
      val numFalsified = q.numPropsFalsified()
      (if (numFalsified == 0) "falsified " else "FALSIFIED ") + numFalsified
    }
    val testPart = falsifiedString + ", " + passedString
    val condorPart = "Condor Run #" + condorId
    List(condorPart, testPart, datePart).mkString(" | ")
  }
}

class Querier(condorId: Long) {
  import SquerylSchema._

  val printSql = {
    () => Session.currentSession.setLogger( (s: String) => println(s) )
  }

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
    val timeEnded: Timestamp =
      transaction {
        from(outcomeTable, runTable, condorSubmissionTable) ( (o, r, cs) =>
          where (
            (o.runId === r.id) and
            (r.condorSubmissionId === cs.id) and
            (cs.condorRunId === condorId)
          )
          select(o.timeEnded)
          orderBy(if(isMax) { o.timeEnded desc } else { o.timeEnded asc })
        ).head
      }
    new DateTime(timeEnded.getTime)
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
      from(envTable, runTable, condorSubmissionTable) ( (e, r, cs) =>
        where (
          (e.runId === r.id) and
          (r.condorSubmissionId === cs.id) and
          (cs.condorRunId === condorId)
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
    transaction {
      from(condorRunTable) ( c =>
        where(c.id === condorId)
        select(c.totalJobs)
      ).single
    }
  }

  def jobsStarted(): Long = {
    /*
     SELECT COUNT(*)
     FROM run r
       INNER JOIN condor_submission cs on r.condor_submission_id = cs.id
     WHERE cs.condor_run_id = condorId;
     */
    transaction {
      from(runTable, condorSubmissionTable) ( (r, cs) =>
        where (
          (cs.id === r.condorSubmissionId) and
          (cs.condorRunId === condorId)
        )
        compute(count())
      )
    }
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
    transaction {
      from(postCompileTable, preCompileTable,
           runTable, condorSubmissionTable)( (post, pre, r, cs) =>

        where (
          (pre.id === post.precompileId) and
          (pre.runId === r.id) and
          (cs.id === r.condorSubmissionId) and
          (cs.condorRunId === condorId)
        )
        compute(count())
      )
    }
  }

  def numRecordedExits(): Long = {
    /*
     SELECT COUNT(*)
     FROM
       outcome o
       INNER JOIN run r ON r.id = o.run_id
       INNER JOIN condor_submission cs on r.condor_submission_id = cs.id
     WHERE
       cs.condor_run_id = condorId;
     */
    transaction {
      from(outcomeTable, runTable, condorSubmissionTable) ( (o, r, cs) =>
        where (
          (o.runId === r.id) and
          (cs.id === r.condorSubmissionId) and
          (cs.condorRunId === condorId)
        )
        compute(count())
      )
    }
  }

  def peakRate(): (Long, DateTime) = {
    var cur = timeStarted()
    val end = timeEnded()
    var best = 0L
    var bestTime = cur
    while (cur isBefore end) {
      val next = cur.plusMinutes(1)
      val rate = compilationRate(cur, next)
      println(rate, cur)
      if (best < rate) {
        best = rate
        bestTime = cur
      }
      cur = next
    }
    (best, bestTime)
  }

  def compilationRate(start: DateTime, end: DateTime): Long = {
    /*
     SELECT COUNT(*)
     FROM
       postcompile post
       INNER JOIN precompile pre ON pre.id = post.precompile_id
       INNER JOIN run r ON pre.run_id = r.id
       INNER JOIN condor_submission cs ON r.condor_submission_id = cs.id
     WHERE
       cs.condor_run_id = condorId AND
       post.time_ended BETWEEN start AND end
     */
    transaction {
      from(postCompileTable, preCompileTable,
           runTable, condorSubmissionTable) ( (post, pre, r, cs) =>

        where (
          (post.precompileId === pre.id) and
          (pre.runId === r.id) and
          (r.condorSubmissionId === cs.id) and
          (cs.condorRunId === condorId) and
          (pre.timeStarted lt new Timestamp(end.getMillis)) and
          (pre.timeStarted gt new Timestamp(start.getMillis))
        )
        compute(count())
      )
    }
  }

  def numThreads(): Int = {
    /*
     SELECT DISTINCT uvalue
     FROM
       config c
       INNER JOIN run r ON c.run_id = r.id
       INNER JOIN condor_submission cs ON r.condor_submission_id = cs.id
     WHERE
       ukey = 'threads' AND
       cs.condor_run_id = condorId
     */
    transaction {
      printSql()
      from(configTable, runTable, condorSubmissionTable) ( (c, r, cs) =>
        where (
          (c.runId === r.id) and
          (r.condorSubmissionId === cs.id) and
          (cs.condorRunId === condorId) and
          (c.uKey === "threads")
        )
        select(c.uValue)
      ).distinct.single.toInt
    }
  }

  def errorMap(): Map[Option[String], Int] = {
    import scala.collection.immutable.HashMap
    HashMap.empty
  }

  def numPropsPassed(): Long = {
    /*
     SELECT DISTINCT uvalue
     FROM
       config c
       INNER JOIN run r ON c.run_id = r.id
       INNER JOIN condor_submission cs ON r.condor_submission_id = cs.id
     WHERE
       EXISTS (
         SELECT *
         FROM
           precompile pre
           INNER JOIN postcompile post ON post.precompile_id = pre.id
           INNER JOIN run r2 ON pre.run_id = r2.id
           INNER JOIN condor_submission cs2 ON r2.condor_submission_id = cs2.id
         WHERE post.errors = pre.errors_expected
       ) AND
       ukey = 'pattern_classes' AND
       cs.condor_run_id = condorId;
     */
    -1L
  }

  def numPropsFalsified(): Long = -1L
} 
 
