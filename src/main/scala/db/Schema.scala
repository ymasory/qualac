package qualac.db

import org.joda.time.DateTime

private[db] object Schema {

  val tables = List (

"""
CREATE TABLE IF NOT EXISTS run (
  id INT AUTO_INCREMENT PRIMARY KEY,
  time_started TIMESTAMP NOT NULL
)
""",

"""
CREATE TABLE IF NOT EXISTS trial (
  id INT AUTO_INCREMENT PRIMARY KEY,
  run_id INT NOT NULL,
  FOREIGN KEY (run_id) REFERENCES run(id)
)
""",

"""
CREATE TABLE IF NOT EXISTS env (
  id INT AUTO_INCREMENT PRIMARY KEY,
  run_id INT NOT NULL,
  FOREIGN KEY (run_id) REFERENCES run(id)
)
"""
  )
}

case class Run(id: Long, dateStarted: DateTime)

case class RunOutcome(id: Long, runId: Long, dateFinished: DateTime,
                      exceptionMessage: Option[String],
                      stackTrace: Option[String])

case class RunEnvironment(id: Long, scalaVersion: String, javaVersion: String,
                          hostName: String)

case class Trial(id: Long, runId: Long, programText: Array[Byte],
                 expectedResult: Int, actualResult: Int)
