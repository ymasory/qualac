/**
 * Copyright (c) Yuvi Masory, 2011
 *
 * Available under the Qualac License, see /LICENSE.
 */ 
package qualac.fuzz

import java.io.{ File, PrintWriter }
import java.util.regex.Pattern

import qualac.common.{ Env, ConfParser }

import org.scalacheck.Prop

class CondorRun(conf: File) {

  val map = ConfParser.parse(conf)

  val condorSubmitPath: String = {
    val binDir = new File(ConfParser.getConfigString("bin_loc", map))
    assert(binDir.exists, "bin_loc " + binDir + " does not exist")
    val condorSubmit = new File(binDir, "condor_submit")
    // assert(condorSubmit.exists, condorSubmit + " does not exist")
    condorSubmit.getAbsolutePath
  }
  val jarFile: File = {
    val jarPath = ConfParser.getConfigString("jar_loc", map)
    val jarFile = new File(jarPath)
    assert(
      jarFile.exists, jarFile + " does not exist. did you run " +
      "sbt proguard task?")
    jarFile
  }

  // val allProps: List[Prop] = List.fill(5) {
  //   Finder.discoverPropsMatching(Env.TestPattern).head
  // }
  val allProps = Finder.discoverPropsMatching(Env.TestPattern)
  val stamp = Env.nowMillis() 

  def fuzz() = {
    Main.shout("really submit " + allProps.length + " jobs? (y/N)")
    readLine() match {
      case "y" | "Y" =>
      case _ => { Main.shout("okay, aborting"); sys.exit }
    }
    val condorRoot = new File("condor")
    if (condorRoot.exists == false) condorRoot.mkdirs()
    for ((prop, i) <- allProps.zip(0 until allProps.length)) {
      val id = stamp + "-" + i
      val propRoot = new File(condorRoot, "condor-" + id)
      val submit = new CondorSubmission(prop, propRoot, id)
      val submitFilePath = submit.writeSubmitFile().getAbsolutePath
      copy(jarFile, new File(propRoot, "qualac.jar"))
      val (o, e, r) = call(condorSubmitPath, submitFilePath)
      if (r != 0) {
        Console.err.println(
          "non-zero return (" + r + ") to condor submit\n" + e)
        sys.exit(1)
      }
    }
  }

  class CondorSubmission(prop: Prop, propRoot: File, id: String) {
    
    if (propRoot.exists == false) propRoot.mkdirs()

    private val Job = "job"
    private val name = Main.ProgramName.toLowerCase
    val outDir = new File(Env.outDir, name + "-" + stamp)
    if (outDir.exists == false) {
      if (outDir.mkdirs() == false) sys.error("could not create " + outDir)
    }
    private val absPath = propRoot.getAbsolutePath()
    private val prefix = name + "-" + id
    val universe: String = "java"
    val executable: String =
      new File(propRoot, name + ".jar").getAbsolutePath
    val jarFiles: String = executable
    val customConfigFile: File = new File(propRoot, "qualac.conf")
    writeCustomConfig(customConfigFile)
    val mainFile: String =
      "qualac.fuzz.Main --config " + customConfigFile.getAbsolutePath
    val error: String = new File(outDir, Job + ".error").getAbsolutePath
    val output: String = new File(outDir, Job + ".output").getAbsolutePath
    val log: String = new File(outDir, Job + ".log").getAbsolutePath

    val fileString = {
      val buf = new StringBuffer
      val LF = "\n"
      def addLine(prop: String, value: String) {
        buf append (prop + " = " + value + LF)
      }
      addLine("universe", universe)
      addLine("executable", executable)
      addLine("jar_files", jarFiles)
      addLine("arguments", mainFile)
      addLine("error", error)
      addLine("output", output)
      addLine("log", log)
      for (k <- map.keys if k.startsWith("_")) {
        val v: String = map(k) match {
          case Left(s) => s
          case Right(i) => i.toString
        }
        addLine (k.substring(1), v)
      }
      buf append ("queue" + LF)
      buf.toString
    }

    def writeCustomConfig(file: File) {
      val writer = new PrintWriter(file)
      def extract(e: Either[String, Int]) = {
        e match {
          case Left(s) => s
          case Right(i) => i.toString
        }
      }
      for (k <- Env.configMap.keys) {
        def writeKv(k: String, v: String) {
          writer.println(k + " " + ConfParser.Delimiter + " " + v)
        }
        k match {
          case Env.TestPatternKey => {
            val singlePat = "^(" + Pattern.quote(prop.getClass.getName) + ")$"
            writeKv(k, singlePat)
          }
          case _ => writeKv(k, extract(Env.configMap(k)))
        }
      }
      writer.flush()
      writer.close()
    }

    def writeSubmitFile(): File = {
      val file = new File(propRoot, "condor.submit")
      val writer = new PrintWriter(file)
      writer.println(fileString)
      writer.flush()
      writer.close()
      file
    }
  }

  def copy(from: File, to: File) {
    import java.io.{ FileInputStream, FileOutputStream }
    val srcChannel = new FileInputStream(from).getChannel()
    val dstChannel = new FileOutputStream(to).getChannel()
    dstChannel transferFrom (srcChannel, 0, srcChannel.size())
    srcChannel.close()
    dstChannel.close()
  }

  def call(args: String*): (String, String, Int) = {
    import scala.io.Source

    val runTime = Runtime.getRuntime
    val process = runTime.exec(args.toArray)
    val outSource = Source.fromInputStream(process.getInputStream)
    val errSource = Source.fromInputStream(process.getErrorStream)
    process.waitFor
    val stdout = outSource.mkString
    val stderr = errSource.mkString
    val ret = process.exitValue
    (stdout, stderr, ret)
  }
}
