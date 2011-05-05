package qualac

import qualac.common.GMail

object Main {
  
  def main(args: Array[String]) {
    val to = List("quala-nightly@googlegroups.com")
    val subj =
      "[quala-nightly] This is a test of the qualac notification system"
    val body = "beeeeeeeeeeeeeeep"
    // GMail sendMail (to, subj, body)
  }
}
