/*
 *
 * Copyright (c) 1999-2016 Luciad All Rights Reserved.
 *
 * Luciad grants you ("Licensee") a non-exclusive, royalty free, license to use,
 * modify and redistribute this software in source and binary code form,
 * provided that i) this copyright notice and license appear on all copies of
 * the software; and ii) Licensee does not utilize the software in a manner
 * which is disparaging to Luciad.
 *
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING ANY
 * IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NON-INFRINGEMENT, ARE HEREBY EXCLUDED. LUCIAD AND ITS LICENSORS SHALL NOT BE
 * LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING
 * OR DISTRIBUTING THE SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL LUCIAD OR ITS
 * LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT,
 * INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
 * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF
 * OR INABILITY TO USE SOFTWARE, EVEN IF LUCIAD HAS BEEN ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGES.
 */
package samples.lightspeed.timeview.model;

import java.util.Calendar;
import java.util.Date;

import com.luciad.model.ILcdModel;
import com.luciad.model.TLcdVectorModel;
import com.luciad.shape.shape2D.TLcdXYPoint;
import com.luciad.shape.shape2D.TLcdXYPolyline;
import com.luciad.util.concurrent.TLcdLockUtil;

/**
 * Model that contains the time axis elements:
 * <ul>
 *   <li>{@link TimeLine}: the horizontal line, representing the valid {@link #setValidRange time range}.</li>
 *   <li>{@link CurrentTime}: the vertical line indicating {@link #getTime current time}.</li>
 *   <li>{@link TimeTick}: ticks along the time line.</li>
 * </ul>
 */
public class TimeAxisModel extends TLcdVectorModel {

  private final TLcdXYPolyline fCurrentTime = new CurrentTime();

  public TimeAxisModel(TimeReference aTimeReference) {
    super(aTimeReference);
    fCurrentTime.insert2DPoint(0, 0, 0);
    fCurrentTime.insert2DPoint(1, 0, 0);
  }

  public void setValidRange(long aTimeMin, long aTimeMax, double aYMin, double aYMax) {
    try (TLcdLockUtil.Lock autoUnlock = TLcdLockUtil.writeLock(this)) {
      removeAllElements(FIRE_LATER);

      // Vertical current time marker
      fCurrentTime.move2DPoint(0, 0, aYMin);
      fCurrentTime.move2DPoint(1, 0, aYMax);
      addElement(fCurrentTime, FIRE_LATER);

      // Horizontal time axis line
      TLcdXYPolyline timeLine = new TimeLine();
      timeLine.insert2DPoint(0, aTimeMin, 0);
      timeLine.insert2DPoint(1, aTimeMax, 0);
      addElement(timeLine, FIRE_LATER);

      // add a tick for every hour
      long granularity = 1000 * 60 * 60;
      for (long i = aTimeMin; i <= aTimeMax; i += granularity) {
        addElement(new TimeTick(i), FIRE_LATER);
      }

      setTime((aTimeMin + aTimeMax) / 2);
    } finally {
      fireCollectedModelChanges();
    }
  }

  public void setTime(long aTime) {
    try (TLcdLockUtil.Lock autoUnlock = TLcdLockUtil.writeLock(this)) {
      fCurrentTime.move2D(aTime, 0);
      elementChanged(fCurrentTime, ILcdModel.FIRE_LATER);
    } finally {
      fireCollectedModelChanges();
    }
  }

  public long getTime() {
    return (long) (fCurrentTime.getStartPoint().getX());
  }

  public static class TimeTick extends TLcdXYPoint {

    public enum Granularity {
      YEAR, MONTH, DAY, HOUR, MINUTE, SECOND;

      private static Granularity getGranularity(long timeMS) {
        Calendar time = Calendar.getInstance();
        time.setTime(new Date(timeMS));
        int hour = time.get(Calendar.HOUR_OF_DAY);
        int day = time.get(Calendar.DAY_OF_MONTH);
        int month = time.get(Calendar.MONTH);
        int minutes = time.get(Calendar.MINUTE);
        int seconds = time.get(Calendar.SECOND);

        if (month == 0 && day == 1 && hour == 0 && minutes == 0 && seconds == 0) {
          return Granularity.YEAR;
        }

        if (day == 1 && hour == 0 && minutes == 0 && seconds == 0) {
          return Granularity.MONTH;
        }

        if (hour == 0 && minutes == 0 && seconds == 0) {
          return Granularity.DAY;
        }

        if (minutes == 0 && seconds == 0) {
          return Granularity.HOUR;
        }

        if (seconds == 0) {
          return Granularity.MINUTE;
        }

        return Granularity.SECOND;
      }
    }

    private final long fTime;
    private final Granularity fGranularity;

    public TimeTick(long time) {
      super(time, 0);
      fTime = time;
      fGranularity = Granularity.getGranularity(time);
    }

    public long getTime() {
      return fTime;
    }

    public Granularity getGranularity() {
      return fGranularity;
    }
  }

  public static class TimeLine extends TLcdXYPolyline {
  }

  public static class CurrentTime extends TLcdXYPolyline {
  }
}
