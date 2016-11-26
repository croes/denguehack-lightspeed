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
package samples.lightspeed.demo.application.data.flyover;

import java.io.IOException;

import com.luciad.format.shp.TLcdSHPModelDecoder;
import com.luciad.geodesy.TLcdEllipsoid;
import com.luciad.geometry.cartesian.TLcdCartesian;
import com.luciad.model.ILcdModel;
import com.luciad.reference.ILcdGeoReference;
import com.luciad.shape.ILcdCurve;
import com.luciad.shape.ILcdShapeList;
import com.luciad.shape.shape3D.TLcdLonLatHeightPoint;
import com.luciad.shape.shape3D.TLcdXYZPoint;
import com.luciad.transformation.TLcdDefaultModelXYZWorldTransformation;
import com.luciad.view.animation.ALcdAnimation;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.camera.TLspViewXYZWorldTransformation3D;

public class FlyOverAnimation extends ALcdAnimation {

  private ILspView fView;

  private ILcdCurve fPolyline;
  private ILcdModel fModel;
  private TLcdDefaultModelXYZWorldTransformation m2w;

  public FlyOverAnimation(ILspView aView) {
    super(120.0, aView);
    fView = aView;

    TLcdSHPModelDecoder decoder = new TLcdSHPModelDecoder();
    try {
      fModel = decoder.decode("Data/internal.data/LA_streets/mulhollanddrive.shp");
      m2w = new TLcdDefaultModelXYZWorldTransformation();
      m2w.setModelReference(fModel.getModelReference());
      m2w.setXYZWorldReference(fView.getXYZWorldReference());
      ILcdShapeList list = (ILcdShapeList) fModel.elements().nextElement();
      fPolyline = (ILcdCurve) list.getShape(0);

//      fPolyline = new TLcdLonLatCircularArcBy3Points(new TLcdLonLatPoint(-118.33161755,34.11765283),
//                                                     new TLcdLonLatPoint(-118.40823373,34.13078703),
//                                                     new TLcdLonLatPoint(-118.47937733,34.12750348), ((ILcdGeodeticReference)fModel.getModelReference()).getGeodeticDatum().getEllipsoid());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public ILspView getView() {
    return fView;
  }

  @Override
  public void start() {
    smoothRadians = 0;
  }

  private double smoothRadians = 0;

  @Override
  protected void setTimeImpl(double aTime) {
    // TODO: support 2D
    final TLspViewXYZWorldTransformation3D v2w = (TLspViewXYZWorldTransformation3D) getView()
        .getViewXYZWorldTransformation();
    final TLcdEllipsoid ellipsoid = new TLcdEllipsoid();

    TLcdXYZPoint refProjected = new TLcdXYZPoint();
    final double dist = TLcdCartesian.distance3D(v2w.getEyePoint(), v2w.getReferencePoint());
    double durationInSeconds = getDuration();

    TLcdLonLatHeightPoint ref = new TLcdLonLatHeightPoint();
    fPolyline.computePointSFCT(Math.min(1.0, aTime / durationInSeconds), ref);
    TLcdLonLatHeightPoint ref2 = new TLcdLonLatHeightPoint();
    fPolyline.computePointSFCT(Math.min(1.0, aTime / durationInSeconds + 0.001), ref2);
    double radians = ellipsoid.forwardAzimuth2D(ref, ref2);
    if (Math.abs(radians + 2 * Math.PI - smoothRadians) < Math
        .abs(radians - smoothRadians)) {
      radians += 2 * Math.PI;
    } else if (Math.abs(radians - 2 * Math.PI - smoothRadians) < Math
        .abs(radians - smoothRadians)) {
      radians -= 2 * Math.PI;
    }
    smoothRadians = smoothRadians == 0 ? radians : 0.99 * smoothRadians + 0.01 * radians;
    try {
      double elevation = getView().getServices().getTerrainSupport().getViewDependentHeightProvider((ILcdGeoReference) fModel.getModelReference(), false).retrieveHeightAt(ref);
      if (Double.isNaN(elevation)) {
        elevation = 0;
      }
      ref.translate3D(0, 0, elevation);
      TLcdXYZPoint wp = new TLcdXYZPoint();
      m2w.modelPoint2worldSFCT(ref, wp);

      v2w.lookAt(wp, dist, Math.toDegrees(smoothRadians), v2w.getPitch(), v2w.getRoll());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
