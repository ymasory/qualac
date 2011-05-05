package qualac.lex

import org.specs2.mutable._

import LexUtil.utfToInt

class LexUtilTests extends Specification {

  "SpecsExamples" must have size(13)

  "`utfToInt`" should {
    "parse the smallest utf bmp string" in {
      utfToInt("U+0000") must be equalTo(0)
    }
    "parse the largest utf bmp string" in {
      utfToInt("U+FFFF") must be equalTo(0)
    }
    "parse lowercase u" in {
      utfToInt("u+FFFF") must be equalTo(0)
    }
  }
}
