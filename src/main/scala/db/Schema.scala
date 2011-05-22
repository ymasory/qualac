/**
 * Copyright (c) Yuvi Masory, 2011
 *
 * Available under the Qualac License, see /LICENSE.
 */ 
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
  etc_hostname TEXT NOT NULL,
  FOREIGN KEY (run_id) REFERENCES run(id)
)
""",

"""
CREATE TABLE IF NOT EXISTS outcome (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  run_id BIGINT NOT NULL,
  class TEXT,
  cause TEXT,
  message TEXT,
  stacktrace TEXT,
  time_ended TIMESTAMP NOT NULL,
  FOREIGN KEY (run_id) REFERENCES run(id)
)
""",
"""
CREATE TABLE IF NOT EXISTS javaprop (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  run_id BIGINT NOT NULL,
  jkey TEXT NOT NULL,
  jvalue TEXT NOT NULL,
  FOREIGN KEY (run_id) REFERENCES run(id)
)
""",
"""
CREATE TABLE IF NOT EXISTS runtimeprop (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  run_id BIGINT NOT NULL,
  total_memory BIGINT NOT NULL,
  free_memory BIGINT NOT NULL,
  max_memory BIGINT NOT NULL,
  processors INT NOT NULL,
  FOREIGN KEY (run_id) REFERENCES run(id)
)
""",
"""
CREATE TABLE IF NOT EXISTS config (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  run_id BIGINT NOT NULL,
  ukey TEXT NOT NULL,
  uvalue TEXT NOT NULL,
  FOREIGN KEY (run_id) REFERENCES run(id)
)
"""
  )
}
