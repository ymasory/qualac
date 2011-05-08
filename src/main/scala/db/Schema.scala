package qualac.db

import org.joda.time.DateTime

/** Stores sql create statements for the db tables. */
private[db] object Schema {

  /**
   * Sql create statements for the db tables.
   *
   * All are CREATE IF NOT EXISTS.
   */
  val tables = List (

"""
CREATE TABLE IF NOT EXISTS run (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  time_started TIMESTAMP NOT NULL
)
""",

"""
CREATE TABLE IF NOT EXISTS trial (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  run_id BIGINT NOT NULL,
  expected TEXT NOT NULL,
  FOREIGN KEY (run_id) REFERENCES run(id)
)
""",

"""
CREATE TABLE IF NOT EXISTS env (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  run_id BIGINT NOT NULL,
  scala_version TEXT NOT NULL,
  scala_version_string TEXT NOT NULL,
  scala_version_message TEXT NOT NULL,
  java_classpath TEXT NOT NULL,
  java_vendor TEXT NOT NULL,
  java_version TEXT NOT NULL,
  java_vm_info TEXT NOT NULL,
  java_vm_name TEXT NOT NULL,
  java_vm_vendor TEXT NOT NULL,
  java_vm_version TEXT NOT NULL,
  os TEXT NOT NULL,
  source_encoding TEXT NOT NULL,
  FOREIGN KEY (run_id) REFERENCES run(id)
)
""",

"""
CREATE TABLE IF NOT EXISTS outcome (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  run_id BIGINT NOT NULL,
  message TEXT,
  stacktrace TEXT,
  FOREIGN KEY (run_id) REFERENCES run(id)
)
"""
  )
}
