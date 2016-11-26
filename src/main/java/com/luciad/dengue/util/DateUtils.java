package com.luciad.dengue.util;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

/**
 * @author Thomas De Bodt
 */
public class DateUtils {
  public static ZonedDateTime toDate(long aTime) {
    return Instant.ofEpochMilli(aTime).atZone(ZoneOffset.UTC);
  }

  public static ZonedDateTime date(int year, int month) {
    return ZonedDateTime.of(year, month, 1, 0, 0, 0, 0, ZoneOffset.UTC);
  }
  public static ZonedDateTime date(int year, int month, int day) {
    return ZonedDateTime.of(year, month, day, 0, 0, 0, 0, ZoneOffset.UTC);
  }

  public static long dateToMillis(ZonedDateTime aDate) {
    return aDate.toEpochSecond() * 1000L;
  }

  public static long[] eachMonthBetweenYears(int aFirstYear, int aLastYear) {
    long[] times = new long[(aLastYear - aFirstYear + 1) * 12];
    int i = 0;
    for(int y = aFirstYear; y <= aLastYear; y++) {
      for(int m = 1; m <= 12; m++) {
        times[i++] = dateToMillis(date(y, m));
      }
    }
    return times;
  }
}
