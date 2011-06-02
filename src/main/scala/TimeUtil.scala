/**
 * Copyright (c) Yuvi Masory, 2011
 *
 * Available under the Qualac License, see /LICENSE.
 */ 
package qualac

import java.sql.Timestamp

import org.joda.time.{ DateTime, Period }
import org.joda.time.format.{ DateTimeFormat, PeriodFormatterBuilder }


object TimeUtil {

  /** Current time in an immutable joda `DateTime`. */
  def now() = new DateTime()

  /** Current time as a sql `Timestamp`. */
  def nowStamp() = new Timestamp(0L)

  /** Current time in milliseconds. */
  def nowMillis() = System.currentTimeMillis

  implicit def toRichDateTime(d: DateTime) = new RichDateTime(d)
  implicit def toRichPeriod(p: Period) = new RichPeriod(p)
}

class RichDateTime(d: DateTime) {

  implicit def conciseRepr = {
    val fmt = DateTimeFormat.forPattern("EEE YYYY-MMMM-dd, hh:mma")
    fmt.print(d)
  }

  implicit def fullRepr = TimeUtil.toRichDateTime(d).dateRepr + " at " +
                          TimeUtil.toRichDateTime(d).timeRepr

  implicit def timeRepr = {
    val timeFmt = DateTimeFormat.forPattern("hh:mma")
    timeFmt.print(d)
  }

  implicit def dateRepr = {
    val dayFmt = DateTimeFormat.forPattern("EEE MMMM dd, YYYY")
    dayFmt.print(d)
  }
}

class RichPeriod(p: Period) {

  implicit def periodRepr = {
    val fmt =
      new PeriodFormatterBuilder()
        .printZeroAlways()
        .appendHours()
        .appendSuffix(" hours")
        .appendSeparator(", ")
        .appendMinutes()
        .appendSuffix(" minutes")
        .appendSeparator(", ")
        .toFormatter()
    fmt.print(p)
  }
}
