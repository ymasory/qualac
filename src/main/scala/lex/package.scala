package qualac

package object lex {
  
  /**
   * A UTF-16 Code Unit.
   * 
   * These can be either code points identifying a character of the Unicode
   * Character Set, or part of a multi-code unit sequence that identifies
   * a supplementary character.
   */
  type CodeUnit = Int
}
