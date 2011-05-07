package qualac

import scala.util.Properties

object Env {

  val curDir = (new java.io.File(".")).getCanonicalPath

  lazy val scalaVersion: String = {
    Properties.releaseVersion match {
      case Some(v) => v
      case None    => Properties.developmentVersion match {
        case Some(v) => v
        case None => throw QualacException("cannot determine Scala version")
      }
    }
  }
}

