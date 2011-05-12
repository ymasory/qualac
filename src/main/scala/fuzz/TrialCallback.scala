package qualac.fuzz

import org.scalacheck._
import org.scalacheck.Test._

class QCallback extends TestCallback {

  // override def chain(callback: TestCallback): TestCallback =  { null }

  override def onPropEval(name: String, threadIdx: Int, succeeded: Int,
                          discarded: Int) {}

  override def onTestResult(name: String, result: Result) {}
}
