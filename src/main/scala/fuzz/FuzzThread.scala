package qualac.fuzz

class FuzzThread(seconds: Int) extends Thread {

  val endMillis = System.currentTimeMillis + (1000 * seconds)

  override def run() {
    while(System.currentTimeMillis < endMillis) {
      
    }
  }
}
