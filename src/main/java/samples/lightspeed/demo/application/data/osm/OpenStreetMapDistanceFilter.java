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
package samples.lightspeed.demo.application.data.osm;

import com.luciad.geometry.cartesian.TLcdCartesian;
import com.luciad.shape.ILcdBounded;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.ILcdShape;
import com.luciad.shape.shape3D.TLcdXYZPoint;
import com.luciad.transformation.ILcdModelXYZWorldTransformation;
import com.luciad.util.ALcdDynamicFilter;
import com.luciad.util.TLcdOutOfBoundsException;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.camera.TLspViewXYZWorldTransformation3D;
import com.luciad.view.lightspeed.layer.ILspLayer;

class OpenStreetMapDistanceFilter extends ALcdDynamicFilter {

  private final double fSqrDistanceThreshold;

  private ILspLayer fLayer;
  private ILspView fView;
  private ILcdModelXYZWorldTransformation fTransfo;
  private TLcdXYZPoint fWorldPoint = new TLcdXYZPoint(); // Assume single-threaded access

  public OpenStreetMapDistanceFilter(double aDistanceThreshold, ILspLayer aLayer, ILspView aView) {
    fLayer = aLayer;
    fView = aView;
    fSqrDistanceThreshold = aDistanceThreshold * aDistanceThreshold;
  }

  public ILspLayer getLayer() {
    return fLayer;
  }

  public ILspView getView() {
    return fView;
  }

  @Override
  public boolean accept(Object aObject) {
    if (fView.getViewType() == ILspView.ViewType.VIEW_2D) {
      return true;
    }

    // Only filter in 3D
    if (aObject instanceof ILcdShape) {
      ILcdPoint focusPoint = ((ILcdBounded) aObject).getBounds().getFocusPoint();
      setTransfo();
      try {
        fTransfo.modelPoint2worldSFCT(focusPoint, fWorldPoint);
        ILcdPoint eyePoint = ((TLspViewXYZWorldTransformation3D) fView.getViewXYZWorldTransformation()).getEyePoint();
        double sqrDistance = TLcdCartesian.squaredDistance3D(eyePoint, fWorldPoint);
        return sqrDistance < fSqrDistanceThreshold;
      } catch (TLcdOutOfBoundsException e) {
        return false;
      }

    }
    return true;
  }

  private void setTransfo() {
    if (fTransfo == null || fTransfo.getXYZWorldReference() != fView.getXYZWorldReference()) {
      fTransfo = fLayer.getModelXYZWorldTransformation(fView);
    }
  }
}
