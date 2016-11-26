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
package samples.gxy.common;

import java.awt.Component;
import java.awt.Point;

import com.luciad.model.ILcdModelReference;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.shape2D.TLcdXYPoint;
import com.luciad.shape.shape3D.TLcdXYZPoint;
import com.luciad.transformation.TLcdDefaultModelXYWorldTransformation;
import com.luciad.util.TLcdOutOfBoundsException;
import com.luciad.util.iso19103.TLcdISO19103Measure;
import com.luciad.util.measure.ALcdMeasureProvider;
import com.luciad.util.measure.ILcdLayerMeasureProviderFactory;
import com.luciad.util.measure.ILcdModelMeasureProviderFactory;
import com.luciad.view.gxy.ILcdGXYView;
import com.luciad.view.gxy.measure.TLcdGXYViewMeasureProvider;


/**
 * Displays the coordinates and measurements of the location under the mouse pointer on a map.
 */
public class MouseLocationComponent extends AMouseLocationComponent {

  private ILcdGXYView fView;
  private TLcdGXYViewMeasureProvider fViewMeasureProvider;
  private CachedMeasures fCachedMeasures = null;

  public MouseLocationComponent(ILcdGXYView aView,
                                Iterable<ILcdModelMeasureProviderFactory> aModelMeasureProviderFactories,
                                Iterable<ILcdLayerMeasureProviderFactory> aLayerMeasureProviderFactories) {
    super((Component) aView);
    fView = aView;
    fViewMeasureProvider = new TLcdGXYViewMeasureProvider(aView, aModelMeasureProviderFactories, aLayerMeasureProviderFactories);
  }

  @Override
  protected ILcdPoint getCoordinates(Point aAWTPoint, ILcdModelReference aReference) throws TLcdOutOfBoundsException {
    TLcdXYPoint worldPoint = new TLcdXYPoint();
    TLcdXYZPoint modelPoint = new TLcdXYZPoint();

    fView.getGXYViewXYWorldTransformation().viewAWTPoint2worldSFCT(aAWTPoint, worldPoint);
    TLcdDefaultModelXYWorldTransformation transformation = new TLcdDefaultModelXYWorldTransformation(
        aReference,
        fView.getXYWorldReference()
    );
    transformation.worldPoint2modelSFCT(worldPoint, modelPoint);
    return modelPoint;
  }


  @Override
  protected TLcdISO19103Measure[] getValues(final ILcdPoint aPoint, final ILcdModelReference aPointReference) {
    // measures are retrieved asynchronously
    if (fCachedMeasures == null || !fCachedMeasures.isValid(aPoint, aPointReference)) {
      ALcdMeasureProvider.Parameters parameters = ALcdMeasureProvider.Parameters.newBuilder().build();
      fViewMeasureProvider.retrieveMeasuresAt(aPoint, aPointReference, parameters, new TLcdGXYViewMeasureProvider.Callback() {
        @Override
        public void measuresRetrieved(TLcdISO19103Measure[] aMeasures) {
          fCachedMeasures = new CachedMeasures(aMeasures, aPointReference, aPoint);
          refreshContent();
        }
      });
    }
    return fCachedMeasures != null ? fCachedMeasures.fMeasures : new TLcdISO19103Measure[0];
  }

  private class CachedMeasures {
    private TLcdISO19103Measure[] fMeasures = new TLcdISO19103Measure[0];
    private ILcdModelReference fReference;
    private ILcdPoint fPoint;

    CachedMeasures(TLcdISO19103Measure[] aMeasures, ILcdModelReference aReference, ILcdPoint aPoint) {
      fMeasures = aMeasures;
      fReference = aReference;
      fPoint = aPoint;
    }

    boolean isValid(ILcdPoint aPoint, ILcdModelReference aModelReference) {
      if (fReference != null ? !fReference.equals(aModelReference) : aModelReference != null) {
        return false;
      }
      return fPoint != null ? fPoint.equals(aPoint) : aPoint == null;
    }

    public TLcdISO19103Measure[] getMeasures() {
      return fMeasures;
    }
  }
}

