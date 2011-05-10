package qualac.fuzz

import qualac.db.DB

class FuzzThread(seconds: Int) extends Thread {

  val endMillis = System.currentTimeMillis + (1000 * seconds)

  override def run() {
    while(System.currentTimeMillis < endMillis) {
      qualac.lex.IdentifierProperties.check
      DB.persistTrial()
    }
  }
}
