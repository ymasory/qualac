package qualac.fuzz

import java.io.{ File, PrintWriter }

import qualac.common.{ Env, ConfParser }

class CondorRun(conf: File) {

  val map = ConfParser.parse(conf)

  val condorSubmit: File = {
    val binDir = new File(ConfParser.getConfigString("bin_loc", map))
    assert(binDir.exists, "bin_loc " + binDir + " does not exist")
    val condorSubmit = new File(binDir, "condor_submit")
    assert(binDir.exists, condorSubmit + " does not exist")
    condorSubmit
  }
   val jarFile: File = {
    val jarPath = ConfParser.getConfigString("jar_loc", map)
    val jarFile = new File(jarPath)
    assert(jarFile.exists, jarFile + " does not exist")
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
      propRoot.mkdir()
      val writer = new PrintWriter(new File(propRoot, "condor.submit"))
      writer.println(makeSubmitString(id))
      writer.flush()
      writer.close()
    }
  }

  def makeSubmitString(id: String) = {
    val LF = "\n"
    val buf = new StringBuffer
    buf append ("universe = java" + LF)
    buf append ("executable = qualac-" + id + ".jar" + LF)
    buf append ("jar_files = qualac-" + id + ".jar" + LF)
    buf append ("arguments = qualac.fuzz.Main" + LF)
    buf append ("error = qualac-" + id + ".error" + LF)
    buf append ("output = qualac-" + id + ".output" + LF)
    buf append ("log = qualac-" + id + ".log" + LF)
    buf append ("queue" + LF)

    for (k <- map.keys if k.startsWith("_")) {
      val v: String = map(k) match {
        case Left(s) => s
        case Right(i) => i.toString
      }
      buf append (k.substring(1) + " = " + v + LF)
    }

    buf.toString
  }
}
