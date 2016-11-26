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
package samples.lucy.drawing.custompainting;

import java.beans.PropertyChangeEvent;
import java.util.Collection;
import java.util.List;

import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.lucy.addons.drawing.format.TLcySLDDomainObject;
import com.luciad.lucy.addons.drawing.lightspeed.ALcyLspDomainObjectSupplier;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.shape.ILcdCircle;
import com.luciad.shape.ILcdShape;
import com.luciad.shape.shape2D.ILcd2DEditablePoint;
import com.luciad.util.ALcdWeakPropertyChangeListener;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.editor.ILspEditor;
import com.luciad.view.lightspeed.geometry.discretization.ILspShapeDiscretizer;
import com.luciad.view.lightspeed.layer.TLspPaintRepresentation;
import com.luciad.view.lightspeed.layer.TLspPaintRepresentationState;
import com.luciad.view.lightspeed.layer.TLspPaintState;
import com.luciad.view.lightspeed.painter.label.style.ALspLabelTextProviderStyle;
import com.luciad.view.lightspeed.style.styler.ALspLabelStyleCollector;
import com.luciad.view.lightspeed.style.styler.ALspLabelStyler;
import com.luciad.view.lightspeed.style.styler.ALspStyleTargetProvider;
import com.luciad.view.lightspeed.style.styler.ILspStyler;

/**
 * Wraps a shape supplier to add a custom paint representation displaying a circle's radius as a label.
 */
public class LspCircleSupplierWrapper extends ALcyLspDomainObjectSupplier {

  private static final TLspPaintRepresentation CIRCLE_LABEL =
      TLspPaintRepresentation.getInstance("CIRCLE_LABEL", 327);

  private ILcyLucyEnv fLucyEnv;
  private ALcyLspDomainObjectSupplier fDelegate;

  public LspCircleSupplierWrapper(ILcyLucyEnv aLucyEnv, ALcyLspDomainObjectSupplier aDelegate) {
    super(aDelegate.getDomainObjectSupplier());
    fLucyEnv = aLucyEnv;
    fDelegate = aDelegate;
  }

  @Override
  public TLspPaintRepresentation[] getLabelPaintRepresentations() {
    return new TLspPaintRepresentation[]{TLspPaintRepresentation.LABEL, CIRCLE_LABEL};
  }

  @Override
  public ILspStyler createShapeStyler(TLspPaintRepresentationState aPaintRepresentationState) {
    TLspPaintRepresentationState creationLabel =
        TLspPaintRepresentationState.getInstance(CIRCLE_LABEL, TLspPaintState.EDITED);
    if (aPaintRepresentationState.equals(creationLabel)) {
      ALspLabelStyler styler = new CircleRadiusLabelStyler(fLucyEnv);
      fLucyEnv.addPropertyChangeListener(new DistanceFormatStylerInvalidator(fLucyEnv, styler));
      return styler;
    } else {
      return fDelegate.createShapeStyler(aPaintRepresentationState);
    }
  }

  @Override
  public ILspShapeDiscretizer createShapeDiscretizer(TLspPaintRepresentation aPaintRepresentation) {
    return fDelegate.createShapeDiscretizer(aPaintRepresentation);
  }

  @Override
  public ILspEditor createShapeEditor(TLspPaintRepresentation aPaintRepresentation) {
    return fDelegate.createShapeEditor(aPaintRepresentation);
  }

  private static class CircleRadiusLabelStyler extends ALspLabelStyler {

    private ALspLabelTextProviderStyle fTextProvider;
    private ALspStyleTargetProvider fGeometryProvider;

    public CircleRadiusLabelStyler(final ILcyLucyEnv aLucyEnv) {
      fTextProvider = new ALspLabelTextProviderStyle() {
        @Override
        public String[] getText(Object aDomainObject, Object aSubLabelID, TLspContext aContext) {
          ILcdShape shape = ((TLcySLDDomainObject) aDomainObject).getDelegateShape();
          ILcdCircle circle = (ILcdCircle) shape;
          String distance = aLucyEnv.getDefaultDistanceFormat().format(circle.getRadius());

          String centerPoint = aLucyEnv.getDefaultLonLatPointFormat().format(circle.getCenter());
          return new String[]{distance, centerPoint};
        }
      };
      fGeometryProvider = new ALspStyleTargetProvider() {
        @Override
        public void getStyleTargetsSFCT(Object aObject, TLspContext aContext, List<Object> aResultSFCT) {
          ILcdShape shape = ((TLcySLDDomainObject) aObject).getDelegateShape();
          ILcdCircle circle = (ILcdCircle) shape;
          ILcd2DEditablePoint p1 = circle.getCenter().cloneAs2DEditablePoint();
          ILcd2DEditablePoint p2 = aContext.getModelReference().makeModelPoint().cloneAs2DEditablePoint();
          if (aContext.getModelReference() instanceof TLcdGeodeticReference) {
            ((TLcdGeodeticReference) aContext.getModelReference()).getGeodeticDatum().getEllipsoid().
                geodesicPointSFCT(p1, circle.getRadius(), 90, p2);
            aResultSFCT.add(p2);
          } else {
            p2.move2D(p1);
            p2.translate2D(circle.getRadius(), 0);
            aResultSFCT.add(p2);
          }
        }
      };
    }

    @Override
    public void style(Collection<?> aObjects, ALspLabelStyleCollector aStyleCollector, TLspContext aContext) {
      aStyleCollector
          .objects(aObjects)
          .label(0)
          .geometry(fGeometryProvider)
          .style(fTextProvider)
          .submit();
    }

  }

  private static class DistanceFormatStylerInvalidator extends ALcdWeakPropertyChangeListener<ALspLabelStyler> {

    private final ILcyLucyEnv fLucyEnv;

    public DistanceFormatStylerInvalidator(ILcyLucyEnv aLucyEnv, ALspLabelStyler aStyler) {
      super(aStyler);
      fLucyEnv = aLucyEnv;
    }

    @Override
    protected void propertyChangeImpl(ALspLabelStyler aStyler, PropertyChangeEvent aPropertyChangeEvent) {
      if ("defaultDistanceFormat".equals(aPropertyChangeEvent.getPropertyName())) {
        aStyler.fireStyleChangeEvent();
      }
    }
  }

}
