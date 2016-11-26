package com.luciad.dengue.weather;

import com.luciad.format.raster.TLcdArcInfoASCIIGridModelDecoder;
import com.luciad.imaging.ALcdBandMeasurementSemantics;
import com.luciad.imaging.ALcdImage;
import com.luciad.imaging.TLcdBandMeasurementSemanticsBuilder;
import com.luciad.imaging.operator.TLcdSemanticsOp;
import com.luciad.model.*;
import com.luciad.util.concurrent.TLcdLockUtil;
import com.luciad.util.iso19103.ILcdISO19103UnitOfMeasure;
import com.luciad.dengue.util.DateUtils;
import com.luciad.dengue.util.HasAImage;
import com.luciad.dengue.util.LRUCache;
import com.luciad.dengue.util.TimeBaseModel;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.util.Arrays;
import java.util.Collections;

import static com.luciad.dengue.util.DateUtils.eachMonthBetweenYears;

/**
 * @author Thomas De Bodt
 */
class MonthlyWeatherModel extends TLcdVectorModel implements TimeBaseModel {
  private final String fPattern;
  private final ILcdModelDecoder fDelegate = new TLcdArcInfoASCIIGridModelDecoder();
  private final long[] fTimeInstances;
  private final HasAImage fHasAImage = new HasAImage();
  private final ILcdISO19103UnitOfMeasure fUnitOfMeasure;

  private final LRUCache<Long, ALcdImage> fImageCache;

  private long fTime = -1;
  private long fDataTime = -1;

  MonthlyWeatherModel(ILcdModelReference aModelReference, ILcdModelDescriptor aModelDescriptor,
                      String aPattern, int aFirstYear, int aLastYear,
                      ILcdISO19103UnitOfMeasure aUnitOfMeasure) {
    super(aModelReference, aModelDescriptor);
    fPattern = aPattern;
    fTimeInstances = eachMonthBetweenYears(aFirstYear, aLastYear);
    fUnitOfMeasure = aUnitOfMeasure;
    fImageCache = new LRUCache<>(16);

    addElement(fHasAImage, NO_EVENT);
    setTime(fTimeInstances[0]);
  }

  @Override
  public void setTime(long aTime) {
    // Find out the change
    int instanceIdx;
    try(TLcdLockUtil.Lock lock = TLcdLockUtil.writeLock(this)) {
      instanceIdx = Arrays.binarySearch(fTimeInstances, aTime);
      if(instanceIdx < 0) instanceIdx = ~instanceIdx;
      instanceIdx = Math.max(0, Math.min(fTimeInstances.length - 1, instanceIdx));
      if(fTimeInstances[instanceIdx] == fDataTime) {
        // no change
        fTime = aTime;
        return;
      }
    }
    // Load data
    ALcdImage newImage = getImage(fTimeInstances[instanceIdx]);
    // Update model
    try(TLcdLockUtil.Lock lock = TLcdLockUtil.writeLock(this)) {
      fTime = aTime;
      if(fTimeInstances[instanceIdx] != fDataTime) {
        fDataTime = fTimeInstances[instanceIdx];
        fHasAImage.setImage(newImage);
        elementChanged(fHasAImage, FIRE_LATER);
      }
    }
    fireCollectedModelChanges();
  }

  @Override
  public long getTime() {
    return fTime;
  }

  @Override
  public long getDataTime() {
    return fDataTime;
  }

  private ALcdImage getImage(long aTime) {
    synchronized(fImageCache) {
      ALcdImage img = fImageCache.get(aTime);
      if(img != null) {
        return img;
      }
    }
    ALcdImage img = createImage(aTime);
    synchronized(fImageCache) {
      fImageCache.put(aTime, img);
    }
    return img;
  }

  private ALcdImage createImage(long aTime) {
    ZonedDateTime dateTime = DateUtils.toDate(aTime);
    String sourceName = fPattern
        .replace("{yyyy}", Integer.toString(dateTime.get(ChronoField.YEAR)))
        .replace("{m}", Integer.toString(dateTime.get(ChronoField.MONTH_OF_YEAR)));
    System.out.println("Loading " + sourceName);
    ILcdModel model = null;
    try {
      model = fDelegate.decode(sourceName);
    } catch(IOException aE) {
      System.err.println("Could not decode " + sourceName + ": " + aE);
    }
    ALcdImage img = model != null ? (ALcdImage)model.elements().nextElement() : null;
    if(img == null) {
      return null;
    }
    ALcdBandMeasurementSemantics inputSemantics = (ALcdBandMeasurementSemantics)img.getConfiguration().getSemantics().get(0);
    if(!inputSemantics.getUnitOfMeasure().equals(fUnitOfMeasure)) {
      ALcdBandMeasurementSemantics fixedSemantics = TLcdBandMeasurementSemanticsBuilder
          .newBuilder()
          .all(inputSemantics)
          .unitOfMeasure(fUnitOfMeasure)
          .build();
      img = TLcdSemanticsOp.semantics(img, Collections.singletonList(fixedSemantics));
    }
    return img;
  }

}
