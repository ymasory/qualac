import sbt._

class Project(info: ProjectInfo) extends DefaultProject(info)
  with AkkaProject {

  if (log.getLevel == Level.Info)
    shout("configuring with Scala v" + vs)

  //dependencies from built-in repos
  val scalaToolsSnapshots = (
    "Scala-Tools Maven2 Snapshots Repository" at
    "http://scala-tools.org/repo-snapshots"
  )
  val junit = "junit" % "junit" % "4.8.2"
  val junitInterface = "com.novocode" % "junit-interface" % "0.6"
  val scalacheck = "org.scala-tools.testing" % "scalacheck_2.9.0.RC2" % "1.8"
  val specs2 = "org.specs2" % "specs2_2.9.0.RC2" % "1.2"
  val scalatest = "org.scalatest" %% "scalatest" % "1.4.RC3"
  val mail = "javax.mail" % "mail" % "1.4.1"
  val squeryl = "org.squeryl" % "squeryl_2.8.1" % "0.9.4-RC6"
  val h2 = "com.h2database" % "h2" % "1.3.154"
  val akkaTypedActor = akkaModule("typed-actor")
  val akkaRemote = akkaModule("remote")
  

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
}
