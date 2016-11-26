package com.luciad.dengue.timeview.model;

import com.luciad.shape.shape2D.TLcdXYLine;
import com.luciad.shape.shape2D.TLcdXYPoint;

public final class HorizontalTimeLine extends TLcdXYLine {

  public HorizontalTimeLine(double aStartTime, double aEndTime) {
    super(new TLcdXYPoint(aStartTime, 0), new TLcdXYPoint(aEndTime, 0));
  }

}
