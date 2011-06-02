import java.io.File

import sbt.{ DefaultProject, Path, ProjectInfo, Level, TestFrameworks }

class Project(info: ProjectInfo) extends DefaultProject(info)
  with ProguardProject {

  if (log.getLevel == Level.Info)
    shout("configuring with Scala v" + vs)

  //java dependencies
  val mail = "javax.mail" % "mail" % "1.4.1"
  val mysqlConnectorJava = "mysql" % "mysql-connector-java" % "5.1.16"
  val jodaTime = "joda-time" % "joda-time" % "1.6.2"
  val jsap = "com.martiansoftware" % "jsap" % "2.1"

  //scala dependencies
  val squeryl = "org.squeryl" %% "squeryl" % "0.9.4"
  val scalacheck = "org.scala-tools.testing" %% "scalacheck" % "1.8"

  //turn down logging a bit
  log.setLevel(Level.Warn)
  log.setTrace(2)

  //files to go in packaged jars
  val extraResources = "README.md" +++ "LICENSE"
  override val mainResources = super.mainResources +++ extraResources

  //program entry point
  override def mainClass: Option[String] = Some("qualac.Main")

  //compiler options
  override def compileOptions = super.compileOptions ++
    compileOptions("-deprecation", "-unchecked")
  override def javaCompileOptions =
    JavaCompileOption("-Xlint:unchecked") :: super.javaCompileOptions.toList

  def shout(msg: String) = {
    val banner = (1 to 80).map(i => "#").mkString
    println(banner)
    println("Quala: " + msg)
    println(banner)
  }

  def vs = crossScalaVersionString


  //some custom run tasks, for my sanity
  val home = Path.userHome + Path.sep.toString
  val qualacConf = home + ".qualac.conf"
  val qualacCondorConf = home + ".qualac-condor.conf"
  val qualacLocalConf = home + ".qualac-local.conf"
  val configFlag = "--config"
  val condorFlag = "--condor"
  lazy val condor = task { args =>
    val nArgs = Array(configFlag, qualacConf, condorFlag, qualacCondorConf)
    super.runAction(nArgs).dependsOn(proguard)
  }
  lazy val mrun = task { args =>
    val nArgs = Array(configFlag, qualacLocalConf)
    super.runAction(nArgs)
  }
  lazy val report = task { args =>
    if (args.length == 0) {
      Console.err.println("need report number")
      System.exit(1)
    }
    val nArgs = Array("--report", args(0), configFlag, qualacConf)
    super.runAction(nArgs)
  }
  lazy val createdb = task { args =>
    val nArgs =
      Array("--create-db", configFlag, qualacConf)
    super.runAction(nArgs)
  }
  lazy val dropdb = task { args =>
    val nArgs = Array("--drop-db", configFlag, qualacConf)
    super.runAction(nArgs)
  }

  //proguard
  override def proguardOptions = List(
    "-dontshrink -dontoptimize -dontobfuscate -dontpreverify -dontnote " +
    "-ignorewarnings",
    proguardKeepAllScala
  )

  val cur =  new File(".").getAbsolutePath
  override def proguardInJars =
    Path.fromFile(scalaLibraryJar) +++
    Path.fromFile(
      new File(cur, "project/boot/scala-" + vs + 
               "/lib/scala-compiler.jar")) +++
    super.proguardInJars
}
