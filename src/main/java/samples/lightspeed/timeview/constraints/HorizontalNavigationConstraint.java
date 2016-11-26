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
package samples.lightspeed.timeview.constraints;

import java.awt.Point;

import com.luciad.shape.shape2D.TLcdXYPoint;
import com.luciad.view.lightspeed.camera.ALspCameraConstraint;
import com.luciad.view.lightspeed.camera.TLspViewXYZWorldTransformation2D;

/**
 * Restricts navigation horizontally.
 * You cannot pan or zoom vertically, and also not rotate.
 */
public class HorizontalNavigationConstraint extends ALspCameraConstraint<TLspViewXYZWorldTransformation2D> {

  @Override
  public void constrain(TLspViewXYZWorldTransformation2D aSource, TLspViewXYZWorldTransformation2D aTarget) {
    TLcdXYPoint worldOrig = aTarget.getWorldOrigin();
    worldOrig.move2D(worldOrig.getX(), 0);
    Point viewOrigin = aTarget.getViewOrigin();
    viewOrigin.setLocation(viewOrigin.getX(), aSource.getHeight() - 40);
    double scaleX = aTarget.getScaleX();
    scaleX = Math.max(2e-6, scaleX);
    aTarget.lookAt(worldOrig, viewOrigin, scaleX, aSource.getScaleY(), 0.0);
  }

}
