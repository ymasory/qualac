import sbt._

class Project(info: ProjectInfo) extends DefaultProject(info)
  with ProguardProject {

  if (log.getLevel == Level.Info)
    shout("configuring with Scala v" + vs)

  //java dependencies
  val mail = "javax.mail" % "mail" % "1.4.1"
  val h2 = "com.h2database" % "h2" % "1.3.154"
  val mysqlConnectorJava = "mysql" % "mysql-connector-java" % "5.1.16"
  val junit = "junit" % "junit" % "4.8.2"
  val junitInterface = "com.novocode" % "junit-interface" % "0.6"
  val jodaTime = "joda-time" % "joda-time" % "1.6.2"
  val jsap = "com.martiansoftware" % "jsap" % "2.1"

  //scalacheck is the only Scala non-test dependency
  val scalacheck = "org.scala-tools.testing" % "scalacheck_2.9.0.RC3" % "1.8"

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

  //proguard
  override def proguardOptions = List(
    "-keepclasseswithmembers " +
    "public class * { public static void main(java.lang.String[]); }",
    proguardKeepAllScala
  )
  override def proguardInJars =
    Path.fromFile(scalaLibraryJar) +++ super.proguardInJars
}
