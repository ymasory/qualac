<img src="https://github.com/downloads/quala/quala/quala_trans.png" alt="Quala logo" align="right" width="150" />
# Qualac #

> Same bugs different day.

Qualac is a quality assurance test suite for the Scala compiler.
We focus on fuzzing and automatic specification-based testing.

# Nightly tests #
To see the results of our nightly tests check out the [quala-nightly](https://groups.google.com/group/quala-nightly/) Google group.

# Code comments #
## Abbreviations ##

* UAR = "uniformly at random"
* CA = "completely arbitrary"
* NE = "non-exhaustive"

## Scaladoc ##

* `@spec` - A direct quote from [the spec](http://www.scala-lang.org/node/212/pdfs).
* `@specSec X` - Testing code for section `X` of the spec.
* `@undefined` - The spec is not clear on this point.
* `@correction` - A correction or suggestion for the spec.
