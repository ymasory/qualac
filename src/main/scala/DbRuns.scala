/**
 * Copyright (c) Yuvi Masory, 2011
 *
 * Available under the Qualac License, see /LICENSE.
 */ 
package qualac

import org.squeryl.PrimitiveTypeMode._

import qualac.db.SquerylSchema

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
