package qualac.db

private[db] object Schema {

  val runTable =
"""
CREATE TABLE run (
  id INT AUTO_INCREMENT PRIMARY KEY,
  date_started DATE NOT NULL
)
"""

  val trialTable =
"""
CREATE TABLE trial (
  id INT AUTO_INCREMENT PRIMARY KEY,
  run_id INT NOT NULL,
  FOREIGN KEY (run_id) REFERENCES run(id)
)
"""

  val envTable =
"""
CREATE TABLE env (
  id INT AUTO_INCREMENT PRIMARY KEY,
  run_id INT NOT NULL,
  FOREIGN KEY (run_id) REFERENCES run(id)
)
"""
}
