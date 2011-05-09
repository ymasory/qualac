package qualac

import scala.tools.nsc.reporters.StoreReporter

/**
 * Home to some type aliases.
 *
 * @author Yuvi Masory
 */
package object compile {
  type ScalacMessage = StoreReporter#Info
}
