package qualac.fuzz

import qualac.db.DB
import qualac.compile.Scalac
import qualac.common.Env

import java.io.File

class FuzzThread(seconds: Int, threadNo: Int) extends Thread {

  /**
   * Decide which file the thread with number `threadNo` will use
   * for output.
   */
  val dir = new File(Env.outDir, "thread-" + threadNo)
  if (dir.exists) dir.delete()
  dir.mkdir()

  val endMillis = System.currentTimeMillis + (1000 * seconds)

  override def run() {
    while(System.currentTimeMillis < endMillis) {
      // qualac.lex.IdentifierProperties.check
      qualac.compile.Scalac.compile("class X", dir)
      DB.persistTrial()
    }
  }
}
