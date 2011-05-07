package qualac.db

import java.util.Date

import org.squeryl.Schema

object QualacSchema extends Schema {
  val run = table[Run]
  val outcome = table[RunOutcome]
  val trial = table[Trial]
}

case class Run(val id: Long,
               val dateStarted: Date
             )

case class RunOutcome(val id: Long,
                      val runId: Long,
                      val dateFinished: Date,
                      val stackTrace: Option[String],
                      val exceptionMessage: Option[String]
                    )


case class RunEnvironment(val id: Long,
                          val scalaVersion: String,
                          val javaVersion: String,
                          val hostName: String)
                          

case class Trial(val id: Long,
                 val runId: Long,
                 val programText: String,
                 val expectedResult: Int,
                 val actualResult: Int
               )
