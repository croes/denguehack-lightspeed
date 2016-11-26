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
package samples.lightspeed.common;

import java.awt.Point;

import com.luciad.model.ILcdModelReference;
import com.luciad.reference.ILcdGeoReference;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.shape2D.TLcdXYPoint;
import com.luciad.shape.shape3D.TLcdXYZPoint;
import com.luciad.transformation.TLcdDefaultModelXYZWorldTransformation;
import com.luciad.util.TLcdOutOfBoundsException;
import com.luciad.util.iso19103.TLcdISO19103Measure;
import com.luciad.util.measure.ALcdMeasureProvider;
import com.luciad.util.measure.ILcdLayerMeasureProviderFactory;
import com.luciad.util.measure.ILcdModelMeasureProviderFactory;
import com.luciad.view.lightspeed.ALspAWTView;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.measure.TLspViewMeasureProvider;

import samples.gxy.common.AMouseLocationComponent;


/**
 * Displays the coordinates and measurements of the location under the mouse pointer on a map.
 */
public class MouseLocationComponent extends AMouseLocationComponent {

  private ALspAWTView fView;
  private TLspViewMeasureProvider fViewMeasureProvider;

  public MouseLocationComponent(ALspAWTView aView,
                                Iterable<ILcdModelMeasureProviderFactory> aMeasureProviderFactories,
                                Iterable<ILcdLayerMeasureProviderFactory> aLayerMeasureProviderFactories) {
    super(aView.getHostComponent());
    fView = aView;
    fViewMeasureProvider = new TLspViewMeasureProvider(aView, aMeasureProviderFactories, aLayerMeasureProviderFactories);
  }

  @Override
  protected TLcdISO19103Measure[] getValues(ILcdPoint aPoint, ILcdModelReference aPointReference) {
    ALcdMeasureProvider.Parameters parameters = ALcdMeasureProvider.Parameters.newBuilder().build();
    return fViewMeasureProvider.retrieveMeasuresAt(aPoint, aPointReference, parameters);
  }

  @Override
  protected double getHeight(ILcdPoint aPoint, ILcdModelReference aPointReference) {
    if (aPointReference instanceof ILcdGeoReference) {
      return fView.getServices().getTerrainSupport().getViewDependentHeightProvider(
          (ILcdGeoReference) aPointReference, true).retrieveHeightAt(aPoint);
    }
    return super.getHeight(aPoint, aPointReference);
  }

  @Override
  protected ILcdPoint getCoordinates(Point aAWTPoint, ILcdModelReference aReference) throws TLcdOutOfBoundsException {
    double scaleX = (double) fView.getWidth() / fView.getOverlayComponent().getWidth();
    double scaleY = (double) fView.getHeight() / fView.getOverlayComponent().getHeight();
    ILcdPoint worldPoint = fView.getServices().getTerrainSupport().getPointOnTerrain(
        new TLcdXYPoint(aAWTPoint.x * scaleX, aAWTPoint.y * scaleY), new TLspContext(null, fView));
    if (worldPoint == null) {
      return null;
    }
    TLcdXYZPoint modelPoint = new TLcdXYZPoint();
    TLcdDefaultModelXYZWorldTransformation transformation = new TLcdDefaultModelXYZWorldTransformation();
    transformation.setXYZWorldReference(fView.getXYZWorldReference());
    transformation.setModelReference(aReference);
    transformation.worldPoint2modelSFCT(worldPoint, modelPoint);
    return modelPoint;
  }
}

