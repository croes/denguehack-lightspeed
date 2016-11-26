package com.luciad.dengue.timeview.model;

import com.luciad.shape.ILcdBounds;
import com.luciad.shape.shape2D.TLcdXYBounds;
import com.luciad.shape.shape2D.TLcdXYLine;
import com.luciad.shape.shape2D.TLcdXYPoint;

public final class VerticalTimeLine extends TLcdXYLine {

  public VerticalTimeLine(double aTime) {
    super(new TLcdXYPoint(aTime, -25), new TLcdXYPoint(aTime, 1e10));
  }

  public void setTime(double aTime) {
    move2D(aTime, 0);
  }

  @Override
  public synchronized ILcdBounds getBounds() {
    return new TLcdXYBounds(getStartPoint().getX() - 1, -25, getStartPoint().getX() + 1, 1e10);
  }
}
