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
package samples.encoder.kml22.converter;

import static com.luciad.shape.ILcdTimeBounds.Boundedness.BOUNDED;

import com.luciad.format.kml22.model.TLcdKML22DataTypes;
import com.luciad.format.kml22.model.time.TLcdKML22AbstractTimePrimitive;
import com.luciad.format.kml22.model.time.TLcdKML22TimeSpan;
import com.luciad.format.kml22.model.time.TLcdKML22TimeStamp;
import com.luciad.shape.ILcdTimeBounds;

/**
 * Utility class to buid {@link TLcdKML22AbstractTimePrimitive} from an {@link ILcdTimeBounds}.
 */
public final class KML22TimeUtil {

  private KML22TimeUtil() {
  }

  /**
   * Create a new instance of <code>TLcdKML22AbstractTimePrimitive</code> from the given time bounds.
   *
   * @param aTimeBounds a time bounds.
   * @return a new TLcdKML22AbstractTimePrimitive
   */
  public static TLcdKML22AbstractTimePrimitive createTimePrimitive(ILcdTimeBounds aTimeBounds) {
    if (aTimeBounds == null) {
      return new TLcdKML22TimeSpan(TLcdKML22DataTypes.TimeSpanType);
    }
    Long beginTime = null;
    Long endTime = null;
    if (aTimeBounds.getBeginTimeBoundedness() == BOUNDED) {
      beginTime = aTimeBounds.getBeginTime();
    }
    if (aTimeBounds.getEndTimeBoundedness() == BOUNDED) {
      endTime = aTimeBounds.getEndTime();
    }
    if (beginTime != null && endTime != null && beginTime.compareTo(endTime) != 0) {
      TLcdKML22TimeSpan timeSpan = new TLcdKML22TimeSpan(TLcdKML22DataTypes.TimeSpanType);
      timeSpan.setBeginTime(beginTime);
      timeSpan.setEndTime(endTime);
      return timeSpan;
    }
    TLcdKML22TimeStamp timeStamp = new TLcdKML22TimeStamp(TLcdKML22DataTypes.TimeStampType);
    if (beginTime != null) {
      timeStamp.setBeginTime(beginTime);
    }
    if (endTime != null) {
      timeStamp.setEndTime(endTime);
    }
    return timeStamp;
  }

}
