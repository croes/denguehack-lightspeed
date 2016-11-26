package com.luciad.dengue.timeview.model;

import com.luciad.model.ILcdModelReference;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.shape2D.ILcd2DEditableBounds;
import com.luciad.shape.shape2D.TLcdXYBounds;
import com.luciad.shape.shape2D.TLcdXYPoint;
import com.luciad.view.ILcdXYWorldReference;
import com.luciad.view.ILcdXYZWorldReference;

import java.util.Properties;

public class TimeHeightReference implements ILcdModelReference, ILcdXYZWorldReference, ILcdXYWorldReference {

  private final ILcd2DEditableBounds bounds;

  public TimeHeightReference() {
    this(new TLcdXYBounds());
  }

  private TimeHeightReference(ILcd2DEditableBounds aBounds) {
    bounds = aBounds;
  }

  public void updateBounds(double aMin, double aMax) {
    bounds.move2D(aMin, 0);
    bounds.setWidth(aMax - aMin);
    bounds.setHeight(100000);
  }

  @Override
  public boolean isBoundsAvailable() {
    return true;
  }

  @Override
  public ILcd2DEditableBounds get2DEditableBounds() {
    return bounds.cloneAs2DEditableBounds();
  }

  @Override
  public void loadProperties(String aPrefix, Properties aProperties) throws IllegalArgumentException {
  }

  @Override
  public void writePropertiesSFCT(String aPrefix, Properties aPropertiesSFCT) throws IllegalArgumentException {
  }

  @Override
  public ILcdPoint makeModelPoint() {
    return new TLcdXYPoint();
  }

  @Override
  public Object clone() {
    return new TimeHeightReference(bounds.cloneAs2DEditableBounds());
  }
}
