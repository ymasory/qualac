package qualac.db

import java.util.Date

import org.squeryl.Schema

import qualac.common.Env

object QualacSchema extends Schema {
  val run = table[Run]
  val outcome = table[RunOutcome]
  val trial = table[Trial]
}

case class Run(val id: Long,
               val dateStarted: Date
             )

class RunOutcome(val id: Long,
                 val runId: Long,
                 val dateFinished: Date,
                 val exceptionMessage: Option[String],
                 val stackTrace: Option[Array[Byte]]
               ) {

  def this(id: Long, runId: Long, dateFinished: Date, exceptionMessage: String,
           stackTrace: String) = {
    this(id, runId, dateFinished, Some(exceptionMessage),
         Some(stackTrace.getBytes))
  }

  def this() = this(0L, 0L, Env.now(), None, None)
}


case class RunEnvironment(val id: Long,
                          val scalaVersion: String,
                          val javaVersion: String,
                          val hostName: String)
                          

case class Trial(val id: Long,
                 val runId: Long,
                 val programText: Array[Byte],
                 val expectedResult: Int,
                 val actualResult: Int
               )
