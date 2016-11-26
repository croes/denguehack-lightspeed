package com.luciad.dengue.timeview.constraints;

import com.luciad.shape.shape2D.TLcdXYPoint;
import com.luciad.view.lightspeed.camera.ALspCameraConstraint;
import com.luciad.view.lightspeed.camera.TLspViewXYZWorldTransformation2D;

import java.awt.*;

/**
 * Restricts panning to only pan horizontally.
 */
public class HorizontalPanningConstraint extends ALspCameraConstraint<TLspViewXYZWorldTransformation2D> {

  @Override
  public void constrain(TLspViewXYZWorldTransformation2D aSource, TLspViewXYZWorldTransformation2D aTarget) {
    TLcdXYPoint worldOrig = aTarget.getWorldOrigin();
    worldOrig.move2D(worldOrig.getX(), 0);
    Point viewOrigin = aTarget.getViewOrigin();
    viewOrigin.setLocation(viewOrigin.getX(), aSource.getHeight() - 40);
    double scaleX = aTarget.getScaleX();
//    scaleX = Math.max(2e-6, scaleX);
    aTarget.lookAt(worldOrig, viewOrigin, scaleX, 1.0, 0.0);
  }

}
