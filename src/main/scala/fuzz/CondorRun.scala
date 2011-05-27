/**
 * Copyright (c) Yuvi Masory, 2011
 *
 * Available under the Qualac License, see /LICENSE.
 */ 
package qualac.fuzz

import java.io.{ File, PrintWriter }
import java.util.regex.Pattern

import org.scalacheck.Prop

import qualac.common.{ Env, ConfParser }
import qualac.db.DB

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

  //just for debugging condor
  val allProps: List[Prop] = {
    val firstProp = Finder.discoverPropsMatching(Env.TestPattern).head
    List.fill(5)(firstProp)
  }
  // val allProps = Finder.discoverPropsMatching(Env.TestPattern)
  val stamp = Env.nowMillis() 

  def fuzz() = {
    Main.shout("really submit " + allProps.length + " jobs? (y/N)")
    readLine() match {
      case "y" | "Y" => condorRun()
      case _ => Main.shout("okay, aborting")
    }
  }

  def condorRun() {
    try {
      val runId = DB.persistCondorRun()
      val condorRoot = new File("condor")
      if (condorRoot.exists == false) condorRoot.mkdirs()
      for ((prop, i) <- allProps.zip(0 until allProps.length)) {
        val id = stamp + "-" + i
        def makeRootFor(num: Int) =
          new File(condorRoot, "condor-" + stamp + "-" + num)
        val propRoot = makeRootFor(i)
        val zeroPropRoot = makeRootFor(0)
        val submit =
          new CondorSubmission(prop, zeroPropRoot, propRoot, id, runId)
        val submitFilePath = submit.writeSubmitFile().getAbsolutePath
        if (i == 0) copy(jarFile, new File(propRoot, "qualac.jar"))
        val (o, e, r) = call(condorSubmitPath, submitFilePath)
        if (r != 0)
          sys.error("non-zero return (" + r + ") to condor submit\n" + e)
        else println("submitted #" + i)
      }
    }
    finally {
      DB.persistExit(None)
    }
  }

  class CondorSubmission(prop: Prop, zeroPropRoot: File, propRoot: File,
                         id: String, runId: Long) {
    
    if (propRoot.exists == false) propRoot.mkdirs()

    private val Job = "job"
    private val name = Main.ProgramName.toLowerCase
    val outDir = new File(Env.outDir, name + "-" + stamp)
    if (outDir.exists == false) {
      if (outDir.mkdirs() == false)
        sys.error("could not create " + outDir)
    }
    private val absPath = propRoot.getAbsolutePath()
    private val prefix = name + "-" + id
    val universe: String = "java"
    val executable: String =
      new File(zeroPropRoot, name + ".jar").getAbsolutePath
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
      def writeKv(k: String, v: String) {
        writer.println(k + " " + ConfParser.Delimiter + " " + v)
      }
      for (k <- Env.configMap.keys) {
        k match {
          case Env.TestPatternKey => {
            val singlePat = "^(" + Pattern.quote(prop.getClass.getName) + ")$"
            writeKv(k, singlePat)
          }
          case _ => writeKv(k, extract(Env.configMap(k)))
        }
      }
      writeKv("condor_id", runId.toString)
      
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
