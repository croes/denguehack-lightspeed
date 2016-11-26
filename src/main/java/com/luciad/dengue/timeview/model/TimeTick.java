package com.luciad.dengue.timeview.model;

import com.luciad.shape.shape2D.TLcdXYPoint;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class TimeTick extends TLcdXYPoint {

  public static final int PRIO_1 = 1;
  public static final int PRIO_2 = 11;
  public static final int PRIO_3 = 111;
  public static final int PRIO_4 = 1111;

  private static final DateFormat HOUR_FORMAT = new SimpleDateFormat("  HH:mm  ");
  private static final DateFormat DAY_FORMAT = new SimpleDateFormat("  MMM dd  ");
  private static final DateFormat MOTH_FORMAT = new SimpleDateFormat("  MMM  ");
  private static final DateFormat YEAR_FORMAT = new SimpleDateFormat("   yyyy   ");
  private final Calendar time;

  public TimeTick(long aTime) {
    super(aTime, 0);
    time = Calendar.getInstance();
    time.setTime(new Date(aTime));
  }

  @Override
  public String toString() {
    int priority = getPriority();
    return priority <= PRIO_1 ? YEAR_FORMAT.format(time.getTime()) :
           priority <= PRIO_2 ? MOTH_FORMAT.format(time.getTime()).toUpperCase() :
           priority <= PRIO_3 ? DAY_FORMAT.format(time.getTime()) :
           HOUR_FORMAT.format(time.getTime());
  }

  public int getPriority() {
    int hour = time.get(Calendar.HOUR_OF_DAY);
    int day = time.get(Calendar.DAY_OF_MONTH);
    int month = time.get(Calendar.MONTH);
    int minutes = time.get(Calendar.MINUTE);

    if (hour == 0 && day == 1 && month == 0 && minutes == 0) {
      return PRIO_1;
    }
    if (hour == 0 && day == 1 && minutes == 0) {
      return PRIO_2;
    }
    if (hour == 0 && minutes == 0) {
      return PRIO_3;
    }
    return PRIO_4;
  }
}
