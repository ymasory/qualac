/**
 * Copyright (c) Yuvi Masory, 2011
 *
 * Available under the Qualac License, see /LICENSE.
 */ 
package qualac

import scala.tools.nsc.reporters.StoreReporter

/**
 * Home to some type aliases.
 */
package object compile {
  type ScalacMessage = StoreReporter#Info
}
