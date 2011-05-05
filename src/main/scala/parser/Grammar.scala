package qualac.parser

object Grammar {

  //upper ::= 'A' | ... | 'Z' | '$' | '_' and Unicode category Lu
  val upper = "A"

  //lower ::= 'a' | ... | 'z' and Unicode category Ll
  val lower = "a"

  //letter ::= upper | lower and Unicode categories Lo, Lt, Nl
  val letter = lower

  //digit ::= '0' | ... | '9'
  val digit = "0"

  //opchar ::= "all other characters in \u0020-007F and Unicode categories
  //Sm, So except parentheses ([]) and periods"
  val opchar = "d"
}
