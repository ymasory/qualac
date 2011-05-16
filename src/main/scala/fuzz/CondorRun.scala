package qualac.fuzz

import java.io.{ File, PrintWriter }

import qualac.common.{ Env, ConfParser }

class CondorRun(conf: File) {

  val map = ConfParser.parse(conf)

  val condorSubmitPath: String = {
    val binDir = new File(ConfParser.getConfigString("bin_loc", map))
    assert(binDir.exists, "bin_loc " + binDir + " does not exist")
    val condorSubmit = new File(binDir, "condor_submit")
    assert(condorSubmit.exists, condorSubmit + " does not exist")
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

  val allProps = Reflector.discoverProps()
  val stamp = Env.now() 

  def fuzz() = {
    Main.shout("really submit " + allProps.length + " jobs? (y/N)")
    readLine() match {
      case "y" | "Y" =>
      case _ => { Main.shout("okay, aborting"); sys.exit }
    }
    val condorRoot = new File("condor")
    if (condorRoot.exists == false) condorRoot.mkdir()
    for ((prop, i) <- allProps.zip(0 until allProps.length)) {
      val id = stamp + "-" + i
      val propRoot = new File(condorRoot, "condor-" + id)
      val submit = new CondorSubmission(propRoot, id)
      val submitFilePath = submit.writeSubmitFile().getAbsolutePath
      copy(jarFile, new File(propRoot, "qualac.jar"))
      Runtime.getRuntime.exec(Array(condorSubmitPath, submitFilePath))
    }
  }

  class CondorSubmission(propRoot: File, id: String) {
    
    if (propRoot.exists == false) propRoot.mkdir()

    private val absPath = propRoot.getAbsolutePath()
    private val prefix = "qualac-" + id
    val universe: String = "java"
    val executable: String =
      new File(propRoot, prefix + ".jar").getAbsolutePath
    val jarFiles: String = executable
    val mainFile: String = "qualac.fuzz.Main"
    val error: String = prefix + ".error"
    val output: String = prefix + ".output"
    val log: String = prefix + ".log"

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
}
