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
package samples.tea.lightspeed.contour;

import static com.luciad.view.lightspeed.style.complexstroke.ALspComplexStroke.*;

import java.awt.Color;
import java.awt.Font;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.List;

import com.luciad.contour.TLcdValuedContour;
import com.luciad.model.ILcdModel;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.camera.ALspViewXYZWorldTransformation;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.TLspPaintState;
import com.luciad.view.lightspeed.layer.shape.TLspShapeLayerBuilder;
import com.luciad.view.lightspeed.style.TLspComplexStrokedLineStyle;
import com.luciad.view.lightspeed.style.TLspLineStyle;
import com.luciad.view.lightspeed.style.TLspTextStyle;
import com.luciad.view.lightspeed.style.complexstroke.ALspComplexStroke;
import com.luciad.view.lightspeed.style.styler.ALspStyleCollector;
import com.luciad.view.lightspeed.style.styler.ALspStyleTargetProvider;
import com.luciad.view.lightspeed.style.styler.ALspStyler;
import com.luciad.view.lightspeed.style.styler.ALspToggleStyler;

/**
 * Creates layer for the objects created during contour creation.
 */
class PolylineContourLayerFactory {

  /**
   * The view scale at which we switch between a labeled representation and a
   */
  private static final double SCALE_WITH_LABELS = 0.01;
  /**
   * The label font to use for contours.
   */
  private static final Font CONTOUR_LABEL_FONT = Font.decode("SansSerif-PLAIN-14");

  public PolylineContourLayerFactory() {
  }

  public ILspLayer createLayer(ILcdModel aModel) {
    return TLspShapeLayerBuilder.newBuilder().model(aModel)
                                .label(aModel.getModelDescriptor().getDisplayName())
                                .bodyStyler(TLspPaintState.REGULAR, new ContourStyler())
                                .build();
  }

  /**
   * The styler used for drawing the contours.
   *
   * This styler uses two modes between which it toggles:
   * * A regular mode without labels
   * * A detailed mode with labels
   *
   * The mode is toggled when the view scale reaches a certain value. Also see {@link #SCALE_WITH_LABELS}
   */
  private static class ContourStyler extends ALspToggleStyler {

    private static final ALspComplexStroke LINE_COMPLEX_STROKE = line().length(100).lineColor(Color.DARK_GRAY).build();
    private static final ALspComplexStroke GAP = gap(20);
    private ScaleListener fScaleListener;
    private CameraChangeListener fCameraChangeListener;

    protected ContourStyler() {
      super(new ALspStyler() {
        @Override
        public void style(Collection<?> aObjects, ALspStyleCollector aStyleCollector, final TLspContext aContext) {
          //Create a simple line-style when zoomed out
          aStyleCollector.objects(aObjects)
                         .geometry(new ValuedContourStyleTargetProvider())
                         .style(TLspLineStyle.newBuilder().color(Color.DARK_GRAY).build())
                         .submit();
        }
      }, new ALspStyler() {
        @Override
        public void style(Collection<?> aObjects, ALspStyleCollector aStyleCollector, TLspContext aContext) {
          //Create a complex stroke containing labels when zoomed in
          for (Object object : aObjects) {
            if (object instanceof TLcdValuedContour) {
              aStyleCollector.object(object)
                             .geometry(new ValuedContourStyleTargetProvider())
                             .style(TLspComplexStrokedLineStyle.newBuilder()
                                                               .regular(
                                                                   append(
                                                                       atomic(compose(
                                                                           GAP,
                                                                           text(String.valueOf((int) ((TLcdValuedContour) object).getValue()))
                                                                               .textStyle(TLspTextStyle.newBuilder().font(CONTOUR_LABEL_FONT).textColor(Color.LIGHT_GRAY).haloColor(Color.black).build()).build(),
                                                                           GAP)),
                                                                       LINE_COMPLEX_STROKE))
                                                               .fallback(LINE_COMPLEX_STROKE)
                                                               .build())
                             .submit();
            }
          }
        }
      });
    }

    @Override
    public void style(Collection<?> aObjects, ALspStyleCollector aStyleCollector, TLspContext aContext) {
      if (fCameraChangeListener == null) {
        fCameraChangeListener = new CameraChangeListener();
        aContext.getView().addPropertyChangeListener(fCameraChangeListener);
      }
      if (fScaleListener == null) {
        fScaleListener = new ScaleListener(this, aContext.getView().getViewXYZWorldTransformation().getScale());
        aContext.getView().getViewXYZWorldTransformation().addPropertyChangeListener(fScaleListener);
      }
      super.style(aObjects, aStyleCollector, aContext);
    }

    @Override
    protected boolean isUseSpecialStyler(Object aObject, TLspContext aContext) {
      //We use the contour labeling with complex strokes when the scale is big enough
      return aContext.getView().getViewXYZWorldTransformation().getScale() > SCALE_WITH_LABELS;
    }

    /**
     * A listener that monitors changes in scales, and throws style change events when needed.
     */
    private static class ScaleListener implements PropertyChangeListener {

      private final ALspStyler fStyler;
      double fLastScale;

      public ScaleListener(ALspStyler aStyler, double aInitialScale) {
        fStyler = aStyler;
        fLastScale = aInitialScale;
      }

      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("scaleX")) {
          if ((fLastScale < SCALE_WITH_LABELS && ((double) evt.getNewValue()) > SCALE_WITH_LABELS) ||
              (fLastScale > SCALE_WITH_LABELS && ((double) evt.getNewValue()) < SCALE_WITH_LABELS)) {
            fLastScale = (double) evt.getNewValue();
            fStyler.fireStyleChangeEvent(null, null, null);
          }
        }
      }
    }

    /**
     * A style target provider for {@link TLcdValuedContour} objects.
     */
    private static class ValuedContourStyleTargetProvider extends ALspStyleTargetProvider {
      @Override
      public void getStyleTargetsSFCT(Object aObject, TLspContext aContext, List<Object> aResultSFCT) {
        if (aObject instanceof TLcdValuedContour) {
          aResultSFCT.add(((TLcdValuedContour) aObject).getShape());
        }
      }
    }

    /**
     * Listens to changes in viewXYZWorldTransformation, and updates the scale listener where
     * needed.
     */
    private class CameraChangeListener implements PropertyChangeListener {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("viewXYZWorldTransformation")) {
          if (evt.getOldValue() instanceof ALspViewXYZWorldTransformation) {
            ALspViewXYZWorldTransformation oldValue = ((ALspViewXYZWorldTransformation) evt.getOldValue());
            oldValue.removePropertyChangeListener(fScaleListener);
            fScaleListener = null;
          }
        }
      }
    }
  }

}
