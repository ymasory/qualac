import java.io.File

import sbt.{ DefaultProject, Path, ProjectInfo, Level, TestFrameworks }

class Project(info: ProjectInfo) extends DefaultProject(info)
  with ProguardProject {

  if (log.getLevel == Level.Info)
    shout("configuring with Scala v" + vs)

  //java dependencies
  val mail = "javax.mail" % "mail" % "1.4.1"
  val mysqlConnectorJava = "mysql" % "mysql-connector-java" % "5.1.16"
  val junit = "junit" % "junit" % "4.8.2"
  val junitInterface = "com.novocode" % "junit-interface" % "0.6"
  val jodaTime = "joda-time" % "joda-time" % "1.6.2"
  val jsap = "com.martiansoftware" % "jsap" % "2.1"

  //scala dependencies
  val scalacheck = "org.scala-tools.testing" %% "scalacheck" % "1.8"
  val classutil = "org.clapper" %% "classutil" % "0.3.6"

  //junit
  override def testOptions = 
    super.testOptions ++ 
    Seq(TestArgument(TestFrameworks.JUnit, "-q", "-v"))
  
  //turn down logging a bit
  log.setLevel(Level.Warn)
  log.setTrace(2)

  //files to go in packaged jars
  val extraResources = "README.md" +++ "LICENSE"
  override val mainResources = super.mainResources +++ extraResources

  //program entry point
  override def mainClass: Option[String] = Some("qualac.fuzz.Main")

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
  lazy val condor = task { args =>
    val nArgs =
      Array("--config",
            Path.userHome + Path.sep.toString + ".qualac-condor.conf",
            "--condor",
            "src/main/resources/sample.condor.conf")
    super.runAction(nArgs).dependsOn(proguard)
  }
  lazy val mrun = task { args =>
    val nArgs =
      Array("--config",
            Path.userHome + Path.sep.toString + ".qualac-local.conf")
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
