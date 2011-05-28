/**
 * Copyright (c) Yuvi Masory, 2011
 *
 * Available under the Qualac License, see /LICENSE.
 */ 
package qualac.db

/** Stores sql create statements for the db tables. */
private[db] object ManualSchema {

  /**
   * Sql create statements for the db tables.
   *
   * All are CREATE IF NOT EXISTS.
   */
  val tables = List (
//a batch run of condor jobs
"""
CREATE TABLE IF NOT EXISTS condor_run (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  time_started TIMESTAMP NOT NULL,
  total_jobs INT NOT NULL
)
ENGINE=InnoDB
""",
//the act of running condor_submit
"""
CREATE TABLE IF NOT EXISTS condor_submission (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  condor_run_id BIGINT,
  time_started TIMESTAMP NOT NULL,
  job_num INT NOT NULL,
  prop_name TEXT NOT NULL,
  FOREIGN KEY (condor_run_id) REFERENCES condor_run(id)
)
ENGINE=InnoDB
""",
//information on the entire run of the fuzzing program
"""
CREATE TABLE IF NOT EXISTS run (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  time_started TIMESTAMP NOT NULL,
  condor_run_id BIGINT,
  FOREIGN KEY (condor_run_id) REFERENCES condor_run(id)
)
ENGINE=InnoDB
""",
//information on a particular program that the fuzzer generated and compiled
"""
CREATE TABLE IF NOT EXISTS precompile (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  run_id BIGINT NOT NULL,
  program_text TEXT NOT NULL,
  errors_expected ENUM('yes', 'no') NOT NULL,
  warnings_expected ENUM('yes', 'no') NOT NULL,
  time_started TIMESTAMP NOT NULL,
  FOREIGN KEY (run_id) REFERENCES run(id)
)
ENGINE=InnoDB
""",
//whether a trial's compilation was successful or not
"""
CREATE TABLE IF NOT EXISTS postcompile (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  precomp_id BIGINT NOT NULL,
  warnings ENUM('yes', 'no') NOT NULL,
  errors ENUM('yes', 'no') NOT NULL,
  time_ended TIMESTAMP NOT NULL,
  FOREIGN KEY (precomp_id) REFERENCES precompile(id)
)
ENGINE=InnoDB
""",
//the scalac output from the compilation of a particular program the fuzzer
//generated
"""
CREATE TABLE IF NOT EXISTS compile_message (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  precomp_id BIGINT NOT NULL,
  severity ENUM('info', 'warning', 'error') NOT NULL,
  message TEXT NOT NULL,
  line INT NOT NULL,
  col INT NOT NULL,
  point INT NOT NULL,
  FOREIGN KEY (precomp_id) REFERENCES precompile(id)
)
ENGINE=InnoDB
""",
//some environment values, mostly from scala.util.Properties

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
  hostname TEXT NOT NULL,
  FOREIGN KEY (run_id) REFERENCES run(id)
)
ENGINE=InnoDB
""",
//the outcome of the entire run of the fuzzing program

"""
CREATE TABLE IF NOT EXISTS outcome (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  run_id BIGINT NOT NULL,
  class TEXT,
  cause TEXT,
  message TEXT,
  stacktrace TEXT,
  time_ended TIMESTAMP NOT NULL,
  problem ENUM('yes', 'no') NOT NULL,
  FOREIGN KEY (run_id) REFERENCES run(id)
)
ENGINE=InnoDB
""",
//key-values from java.lang.System.getProperties 
"""
CREATE TABLE IF NOT EXISTS java_prop (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  run_id BIGINT NOT NULL,
  jkey TEXT NOT NULL,
  jvalue TEXT NOT NULL,
  FOREIGN KEY (run_id) REFERENCES run(id)
)
ENGINE=InnoDB
""",
//some values from java.lang.Runtime
"""
CREATE TABLE IF NOT EXISTS runtime_prop (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  run_id BIGINT NOT NULL,
  total_memory BIGINT NOT NULL,
  free_memory BIGINT NOT NULL,
  max_memory BIGINT NOT NULL,
  processors INT NOT NULL,
  FOREIGN KEY (run_id) REFERENCES run(id)
)
ENGINE=InnoDB
""",
//key-values from the user config file used by the fuzzing program
"""
CREATE TABLE IF NOT EXISTS config (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  run_id BIGINT NOT NULL,
  ukey TEXT NOT NULL,
  uvalue TEXT NOT NULL,
  FOREIGN KEY (run_id) REFERENCES run(id)
)
ENGINE=InnoDB
"""
  )

  val postUpdates = Nil
}
