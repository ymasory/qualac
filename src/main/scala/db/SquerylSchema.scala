/**
 * Copyright (c) Yuvi Masory, 2011
 *
 * Available under the Qualac License, see /LICENSE.
 */ 
package qualac.db

import java.sql.Timestamp

import org.squeryl.PrimitiveTypeMode._
import org.squeryl.annotations.Column

object SquerylSchema extends org.squeryl.Schema {

  val run = table[RunTable]
  val preCompile = table[PreCompileTable]("precompile")
  val postCompile = table[PostCompileTable]("postcompile")
  val compileMessage = table[CompileMessageTable]("compilemessage")
  val env = table[EnvTable]
  val outcome = table[OutcomeTable]
  val javaProp = table[JavaPropTable]("javaprop")
  val runtimeProp = table[RuntimePropTable]("runtimeprop")
  val config = table[ConfigTable]
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

import YesNo.YesNo
import Severity.Severity

class RunTable(val id: Long,
               @Column("time_started")
               val timeStarted: Timestamp)

class PreCompileTable(val id: Long,
                      @Column("run_id")
                      val runId: Long,
                      @Column("program_text")
                      val programText: String,
                      @Column("errors_expected")
                      val errorsExpected: YesNo,
                      @Column("warningsExpected")
                      val warningsExpected: YesNo,
                      @Column("time_started")
                      val timeStarted: Timestamp)

class PostCompileTable(val id: Long,
                       @Column("precomp_id")
                       val precompId: Long,
                       val warnings: YesNo,
                       val errors: YesNo,
                       @Column("time_ended")
                       val timeEnded: Timestamp)

class CompileMessageTable(val id: Long,
                          @Column("precomp_id")
                          val precompId: Long,
                          val severity: Severity,
                          val message: String,
                          val line: Int,
                          val col: Int,
                          val point: Int)

class EnvTable(val id: Long,
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
               val hostname: String)

class OutcomeTable(val id: Long,
                   @Column("run_id")
                   val runId: Long,
                   @Column("class")
                   val clazz: String,
                   val cause: String,
                   val message: String,
                   @Column("stacktrace")
                   val stackTrace: String,
                   @Column("time_ended")
                   val timeEnded: Timestamp)

class JavaPropTable(val id: Long,
                    @Column("run_id")
                    val runId: Long,
                    @Column("jkey")
                    val jKey: String,
                    @Column("jvalue")
                    val jValue: String)

class RuntimePropTable(val id: Long,
                       @Column("run_id")
                       val runId: Long,
                       @Column("total_memory")
                       val totalMemory: Long,
                       @Column("free_memory")
                       val freeMemory: Long,
                       @Column("max_memory")
                       val maxMemory: Long,
                       @Column("processors")
                       val processors: Int)

class ConfigTable(val id: Long,
                  @Column("run_id")
                  val runId: Long,
                  @Column("ukey")
                  val uKey: String,
                  @Column("uvalue")
                  val uValue: String)