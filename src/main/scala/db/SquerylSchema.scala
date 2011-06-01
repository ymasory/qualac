/**
 * Copyright (c) Yuvi Masory, 2011
 *
 * Available under the Qualac License, see /LICENSE.
 */ 
package qualac.db

import java.sql.Timestamp

import org.squeryl.KeyedEntity
import org.squeryl.PrimitiveTypeMode._
import org.squeryl.annotations.Column

object SquerylSchema extends org.squeryl.Schema {

  val condorRun      = table[CondorRunTable]("condor_run")
  val run            = table[RunTable]("run")
  val preCompile     = table[PreCompileTable]("precompile")
  val postCompile    = table[PostCompileTable]("postcompile")
  val compileMessage = table[CompileMessageTable]("compile_message")
  val env            = table[EnvTable]("env")
  val outcome        = table[OutcomeTable]("outcome")
  val javaProp       = table[JavaPropTable]("java_prop")
  val runtimeProp    = table[RuntimePropTable]("runtime_prop")
  val config         = table[ConfigTable]("config")
  val submission     = table[CondorSubmission]("condor_submission")
}

object YesNo extends Enumeration {

  type YesNo = Value
  val Yes = Value(1, "yes")
  val No = Value(2, "no")
}

object Severity extends Enumeration {

  type Severity = Value
  val Info = Value(1, "info")
  val Warning = Value(2, "warning")
  val Error = Value(3, "error")
}

object Stamp {
  import java.sql.Timestamp
  import java.util.Calendar

  val stamp: Timestamp = new Timestamp(Calendar.getInstance.getTimeInMillis)
}

import YesNo.YesNo
import Severity.Severity
import Stamp.stamp

class CondorRunTable(
  @Column("time_started")
  val timeStarted: Timestamp,
  @Column("total_jobs")
  val totalJobs: Int) extends KeyedEntity[Long] {

  val id: Long = -1L
  def this() = this(stamp, 0)
}

class CondorSubmission(
  @Column("condor_run_id")
  val condorRunId: Long,
  @Column("time_started")
  val timeStarted: Timestamp,
  @Column("job_num")
  val jobNum: Int,
  @Column("prop_name")
  val propName: String) extends KeyedEntity[Long] {

  val id: Long = -1L
  def this() = this(0L, stamp, 0, "")
}

class RunTable(
  @Column("time_started")
  val timeStarted: Timestamp,
  @Column("condor_submission_id")
  val condorSubmissionId: Option[Long]) extends KeyedEntity[Long] {

  val id: Long = -1L
  def this() = this(stamp, Some(0L))
}

class PreCompileTable(
  @Column("run_id")
  val runId: Long,
  @Column("program_text")
  val programText: String,
  @Column("errors_expected")
  val errorsExpected: YesNo,
  @Column("warningsExpected")
  val warningsExpected: YesNo,
  @Column("time_started")
  val timeStarted: Timestamp) extends KeyedEntity[Long] {

  val id: Long = -1L
  def this() = this(0L, "", YesNo.No, YesNo.No, stamp)
}

class PostCompileTable(
  @Column("precompile_id")
  val precompileId: Long,
  val warnings: YesNo,
  val errors: YesNo,
  @Column("time_ended")
  val timeEnded: Timestamp) extends KeyedEntity[Long] {

  val id: Long = -1L
  def this() = this(0L, YesNo.No, YesNo.No, stamp)

}

class CompileMessageTable(
  @Column("precomp_id")
  val precompId: Long,
  val severity: Severity,
  val message: String,
  val line: Int,
  val col: Int,
  val point: Int) extends KeyedEntity[Long] {

  val id: Long = -1L
  def this() = this(0L, Severity.Error, "", 0, 0, 0)
}

class EnvTable(
  @Column("run_id")
  val runId: Long,
  @Column("scala_version")
  val scalaVersion: String,
  @Column("scala_version_string")
  val scalaVersionString: String,
  @Column("scala_version_message")
  val scalaVersionMessage: String,
  @Column("java_classpath")
  val javaClasspath: String,
  @Column("java_vendor")
  val javaVendor: String,
  @Column("java_vm_info")
  val javaVmInfo: String,
  @Column("java_vm_name")
  val javaVmName: String,
  @Column("java_vm_vendor")
  val javaVmVendor: String,
  @Column("java_vm_version")
  val javaVmVersion: String,
  @Column("os")
  val os: String,
  @Column("source_encoding")
  val sourceEncoding: String,
  @Column("etc_hostname")
  val etcHostname: String,
  @Column("hostname")
  val hostname: String) extends KeyedEntity[Long] {

  val id: Long = -1L
  def this() = this(0, "", "", "", "", "", "", "", "", "", "", "", "", "")
}


class OutcomeTable(
  @Column("run_id")
  val runId: Long,
  @Column("class")
  val clazz: Option[String],
  val cause: Option[String],
  val message: Option[String],
  @Column("stacktrace")
  val stackTrace: Option[String],
  @Column("time_ended")
  val timeEnded: Timestamp,
  val problem: YesNo) extends KeyedEntity[Long] {

  def id: Long = -1L
  def this() = this(0L, None, None, None, None, stamp, YesNo.No)
}


class JavaPropTable(
  @Column("run_id")
  val runId: Long,
  @Column("jkey")
  val jKey: String,
  @Column("jvalue")
  val jValue: String) extends KeyedEntity[Long] {

  val id: Long = -1L
  def this() = this(0L, "", "")
}


class RuntimePropTable(
  @Column("run_id")
  val runId: Long,
  @Column("total_memory")
  val totalMemory: Long,
  @Column("free_memory")
  val freeMemory: Long,
  @Column("max_memory")
  val maxMemory: Long,
  @Column("processors")
  val processors: Int) extends KeyedEntity[Long] {

  val id: Long = -1L
  def this() = this(0L, 0L, 0L, 0L, 0)
}


class ConfigTable(
  @Column("run_id")
  val runId: Long,
  @Column("ukey")
  val uKey: String,
  @Column("uvalue")
  val uValue: String) extends KeyedEntity[Long] {

  val id: Long = -1L
  def this() = this(0L, "", "")
}
