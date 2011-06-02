/**
 * Copyright (c) Yuvi Masory, 2011
 *
 * Available under the Qualac License, see /LICENSE.
 */ 
package qualac.db

import org.squeryl.PrimitiveTypeMode._

class DbCreationRun() {

  def run() {
    transaction {
      SquerylSchema.create
    }
  }
}

class DbDropRun() {

  def run() {
    transaction {
      SquerylSchema.drop
    }
  }
}
