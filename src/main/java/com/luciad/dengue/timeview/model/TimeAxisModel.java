package com.luciad.dengue.timeview.model;

import com.luciad.model.ILcdModel;
import com.luciad.model.TLcd2DBoundsIndexedModel;
import com.luciad.model.TLcdModelDescriptor;
import com.luciad.model.TLcdVectorModel;
import com.luciad.shape.ILcdPolyline;

public class TimeAxisModel {

  private final VerticalTimeLine currentTime;
  private ILcdModel timeTicksModel;
  private ILcdModel currentTimeModel;

  public TimeAxisModel(TimeHeightReference aTimeHeightReference) {
    timeTicksModel = new TLcd2DBoundsIndexedModel(aTimeHeightReference, new TLcdModelDescriptor());
    currentTimeModel = new TLcdVectorModel(aTimeHeightReference);

    currentTime = new VerticalTimeLine(0);
    currentTimeModel.addElement(currentTime, ILcdModel.NO_EVENT);
  }

  public void setRange(long aMin, long aMax) {
    timeTicksModel.removeAllElements(ILcdModel.FIRE_LATER);
    ILcdPolyline mainLine = new HorizontalTimeLine(aMin, aMax);
    timeTicksModel.addElement(mainLine, ILcdModel.FIRE_LATER);

    // add a tick for every hour
    for (long i = aMin; i < aMax; i += 1000 * 60 * 60) {
      timeTicksModel.addElement(new TimeTick(i), ILcdModel.FIRE_LATER);
    }
    timeTicksModel.fireCollectedModelChanges();

    currentTime.setTime(aMin);
  }

  public ILcdModel getTimeTicksModel() {
    return timeTicksModel;
  }

  public ILcdModel getCurrentTimeModel() {
    return currentTimeModel;
  }

  public long getTime() {
    return (long) currentTime.getStartPoint().getX();
  }

  public void setTime(long aTime) {
    currentTime.setTime(aTime);
    currentTimeModel.elementChanged(currentTime, ILcdModel.FIRE_NOW);
  }
}
