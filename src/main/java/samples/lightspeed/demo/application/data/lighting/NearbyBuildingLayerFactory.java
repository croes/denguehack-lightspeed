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
package samples.lightspeed.demo.application.data.lighting;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import com.luciad.geometry.cartesian.TLcdCartesian;
import com.luciad.model.ILcdModel;
import com.luciad.shape.ILcdBounded;
import com.luciad.shape.ILcdBounds;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.shape3D.ILcd3DEditablePoint;
import com.luciad.shape.shape3D.TLcdXYZPoint;
import com.luciad.transformation.TLcdDefaultModelXYZWorldTransformation;
import com.luciad.util.ALcdDynamicFilter;
import com.luciad.util.TLcdOutOfBoundsException;
import com.luciad.util.collections.TLcdWeakIdentityHashMap;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.camera.ALspViewXYZWorldTransformation;
import com.luciad.view.lightspeed.camera.TLspViewXYZWorldTransformation3D;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.TLspLayer;
import com.luciad.view.lightspeed.layer.TLspPaintRepresentationState;
import com.luciad.view.lightspeed.style.ALspStyle;
import com.luciad.view.lightspeed.style.TLspFillStyle;
import com.luciad.view.lightspeed.style.TLspLineStyle;
import com.luciad.view.lightspeed.style.styler.ALspStyleCollector;
import com.luciad.view.lightspeed.style.styler.ALspStyleTargetProvider;
import com.luciad.view.lightspeed.style.styler.ALspStyler;

/**
 * Buildings layer factory that only shows nearby buildings to ensure decent performance.
 */
public class NearbyBuildingLayerFactory extends BuildingLayerFactory {

  private double fStartFadeDist;
  private double fEndFadeDist;

  @Override
  public void configure(Properties aProperties) {
    super.configure(aProperties);
    fStartFadeDist = Double.parseDouble(aProperties.getProperty("startFadeDistance", "1000.0"));
    fEndFadeDist = Double.parseDouble(aProperties.getProperty("endFadeDistance", "2000.0"));
  }

  @Override
  public Collection<ILspLayer> createLayers(ILcdModel aModel) {
    Collection<ILspLayer> layers = super.createLayers(aModel);
    for (ILspLayer layer : layers) {
      TLspLayer tLspLayer = (TLspLayer) layer;
      if (fEndFadeDist > 0) {
        tLspLayer.setFilter(new DistanceFilter(tLspLayer, fEndFadeDist));
        tLspLayer.setStyler(TLspPaintRepresentationState.REGULAR_BODY, new NearbyBuildingLayerStyler(createStyles(), createStyleTargetProvider(), fStartFadeDist, fEndFadeDist));
      }
    }
    return layers;
  }

  private static double getSqrDistance(Object aObject, ILspView aView, ILspLayer aLayer) {
    ALspViewXYZWorldTransformation v2w = aView.getViewXYZWorldTransformation();
    if (v2w instanceof TLspViewXYZWorldTransformation3D) {
      ILcdPoint eye = ((TLspViewXYZWorldTransformation3D) v2w).getEyePoint();
      ILcdBounds bounds = ((ILcdBounded) aObject).getBounds();
      ILcd3DEditablePoint pt = bounds.getLocation().cloneAs3DEditablePoint();
      pt.translate3D(bounds.getWidth() / 2, bounds.getHeight() / 2, bounds.getDepth() / 2);
      TLcdDefaultModelXYZWorldTransformation m2w = new TLcdDefaultModelXYZWorldTransformation();
      m2w.setModelReference(aLayer.getModel().getModelReference());
      m2w.setXYZWorldReference(aView.getXYZWorldReference());
      TLcdXYZPoint wp = new TLcdXYZPoint();
      double sqrDist = Double.POSITIVE_INFINITY;
      try {
        m2w.modelPoint2worldSFCT(pt, wp);
        sqrDist = TLcdCartesian.squaredDistance3D(eye, wp);
      } catch (TLcdOutOfBoundsException ignored) {
      }
      return sqrDist;
    } else {
      return 0.0;
    }
  }

  private static class DistanceFilter extends ALcdDynamicFilter {
    private final TLspLayer fLayer;
    private double fMaxSqrDist;

    public DistanceFilter(TLspLayer aLayer, double aMaxDist) {
      fLayer = aLayer;
      fMaxSqrDist = aMaxDist * aMaxDist;
    }

    @Override
    public boolean accept(Object aObject) {
      ILspView view = fLayer.getCurrentViews().iterator().next();
      double sqrDist = getSqrDistance(aObject, view, fLayer);
      return sqrDist < fMaxSqrDist;
    }

  }

  private class NearbyBuildingLayerStyler extends ALspStyler {
    private final TLcdWeakIdentityHashMap<ILspView, ALspViewXYZWorldTransformation> fViewToTransformation = new TLcdWeakIdentityHashMap<ILspView, ALspViewXYZWorldTransformation>();
    private final ScaleChangeListener fScaleChangeListener = new ScaleChangeListener();

    private final List<ALspStyle> fStyles;
    private final ALspStyleTargetProvider fStyleTargetProvider;

    private final double fStartFadeDist;
    private final double fEndFadeDist;
    private final double fStartFadeDistSqr;
    private final double fEndFadeDistSqr;

    public NearbyBuildingLayerStyler(List<ALspStyle> aStyles, ALspStyleTargetProvider aStyleTargetProvider, double aStartFadeDist, double aEndFadeDist) {
      fStyles = aStyles;
      fStyleTargetProvider = aStyleTargetProvider;
      fEndFadeDist = aEndFadeDist;
      fStartFadeDist = aStartFadeDist;
      fEndFadeDistSqr = aEndFadeDist * aEndFadeDist;
      fStartFadeDistSqr = aStartFadeDist * aStartFadeDist;
    }

    @Override
    public void style(Collection<?> aObjects, ALspStyleCollector aStyleCollector, TLspContext aContext) {
      ILspView view = aContext.getView();
      ALspViewXYZWorldTransformation oldTransf = fViewToTransformation.get(view);
      ALspViewXYZWorldTransformation newTransf = view.getViewXYZWorldTransformation();
      if (oldTransf != newTransf) {
        if (oldTransf != null) {
          oldTransf.removePropertyChangeListener(fScaleChangeListener);
        }
        fViewToTransformation.put(view, newTransf);
        if (newTransf != null) {
          newTransf.addPropertyChangeListener(fScaleChangeListener);
        }
      }

      for (Object object : aObjects) {
        double sqrDistance = getSqrDistance(object, aContext.getView(), aContext.getLayer());
        if (sqrDistance >= fEndFadeDistSqr) {
          aStyleCollector.object(object).hide().submit();
        } else if (sqrDistance <= fStartFadeDistSqr) {
          aStyleCollector.object(object).geometry(fStyleTargetProvider).styles(fStyles).submit();
        } else {
          float opacity = (float) (1.0 - (Math.sqrt(sqrDistance) - fStartFadeDist) / (fEndFadeDist - fStartFadeDist));
          aStyleCollector.object(object).geometry(fStyleTargetProvider).styles(getStyles(opacity)).submit();
        }
      }
    }

    private ArrayList<ALspStyle> getStyles(float aOpacity) {
      ArrayList<ALspStyle> styles = new ArrayList<ALspStyle>(fStyles.size());
      for (int i = 0; i < fStyles.size(); i++) {
        ALspStyle style = fStyles.get(i);
        if (style instanceof TLspLineStyle) {
          style = ((TLspLineStyle) style).asBuilder().opacity(aOpacity).build();
        } else if (style instanceof TLspFillStyle) {
          style = ((TLspFillStyle) style).asBuilder().opacity(aOpacity).build();
        }
        styles.add(style);
      }
      return styles;
    }

    private class ScaleChangeListener implements PropertyChangeListener {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        if ("eyePoint".equalsIgnoreCase(evt.getPropertyName())) {
          fireStyleChangeEvent();
        }
      }
    }
  }
}
