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
package samples.lightspeed.demo.application.data.airspaces;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import com.luciad.format.arinc.model.aerodrome.TLcdARINCAerodromeModelDescriptor;
import com.luciad.format.arinc.model.airspace.TLcdARINCControlledAirspaceModelDescriptor;
import com.luciad.format.arinc.model.procedure.TLcdARINCProcedureTrajectoryModelDescriptor;
import com.luciad.model.ILcdModel;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.layer.ALspSingleLayerFactory;
import com.luciad.view.lightspeed.layer.ILspInteractivePaintableLayer;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.TLspPaintRepresentation;
import com.luciad.view.lightspeed.layer.TLspPaintState;
import com.luciad.view.lightspeed.layer.shape.TLspShapeLayerBuilder;
import com.luciad.view.lightspeed.painter.label.TLspLabelPainter;
import com.luciad.view.lightspeed.style.styler.ALspStyleCollector;
import com.luciad.view.lightspeed.style.styler.ILspStyler;
import com.luciad.view.lightspeed.style.styler.TLspStyler;

import samples.lightspeed.customization.style.highlighting.AnimatedHighlightStyler;
import samples.lightspeed.customization.style.highlighting.HighlightController;

/**
 * Layer factory that creates layers for air spaces. <p> The layer factory keeps track of all the
 * views in the demo application to add mouse listeners to their associated swing components. These
 * mouse listeners then cause the air spaces to change color on mouse over.
 */
public class AirspaceLayerFactory extends ALspSingleLayerFactory {

  // We store a reference to the highlight controllers to be able to register the styles as highlight listeners.
  private Collection<HighlightController> fHighlightControllers = new ArrayList<HighlightController>();

  /**
   * Constructs a new layer factory. Use {@link #addHighlightController} before using this layer factory.
   */
  public AirspaceLayerFactory() {
  }

  /**
   * Register a new highlight controller with this layer factory. At least one should be
   * registered before using this factory.
   *
   * @param aHighlightController a highlight controller.
   */
  public void addHighlightController(HighlightController aHighlightController) {
    fHighlightControllers.add(aHighlightController);
  }

  public boolean canCreateLayers(ILcdModel aModel) {
    return aModel.getModelDescriptor() instanceof TLcdARINCProcedureTrajectoryModelDescriptor ||
           aModel.getModelDescriptor() instanceof TLcdARINCAerodromeModelDescriptor ||
           aModel.getModelDescriptor() instanceof TLcdARINCControlledAirspaceModelDescriptor;
  }

  public ILspLayer createLayer(ILcdModel aModel) {
    if (canCreateLayers(aModel)) {
      return createArincLayer(aModel);
    } else {
      throw new IllegalArgumentException("Cannot create layer for given model [" + aModel + "], " +
                                         "reason: model is not an ARINC model.");
    }
  }

  private ILspLayer createArincLayer(ILcdModel aModel) {
    if (fHighlightControllers.isEmpty()) {
      throw new IllegalArgumentException("Please set the view on this layer factory before creating layers.");
    }

    AAnimatedHighlightStyler airspaceHighlightStyler = new AirspaceBodyStyler();
    AnimatedHighlightStyler airspaceLabelHighlightStyler = new AnimatedLabelHighlightStyler();

    TLspLabelPainter labelPainter = new TLspLabelPainter();
    labelPainter.setOverlayLabels(true);
    labelPainter.setStyler(TLspPaintState.REGULAR, airspaceLabelHighlightStyler);
    labelPainter.setStyler(TLspPaintState.SELECTED, airspaceLabelHighlightStyler);

    TLspShapeLayerBuilder layerBuilder = TLspShapeLayerBuilder.newBuilder();
    layerBuilder.model(aModel)
                .selectable(true)
                .bodyEditable(false)
                .bodyStyler(TLspPaintState.REGULAR, airspaceHighlightStyler)
                .bodyStyler(TLspPaintState.SELECTED, airspaceHighlightStyler)
                .labelPainter(labelPainter);
    ILspInteractivePaintableLayer layer = layerBuilder.build();

    layer.setVisible(TLspPaintRepresentation.LABEL, true);

    for (HighlightController highlightController : fHighlightControllers) {
      highlightController.addHighlightListener(airspaceHighlightStyler);
      highlightController.addHighlightListener(airspaceLabelHighlightStyler);
    }

    // Return a default layer which is selectable (this is important,
    // or mouse motion listener will not work correctly)
    return layer;
  }

  private static class AnimatedLabelHighlightStyler extends AnimatedHighlightStyler {

    @Override
    public void style(Collection<?> aObjects, ALspStyleCollector aStyleCollector, TLspContext aContext) {
      for (Object object : aObjects) {
        Integer alpha = getCurrentAlpha(object);
        ILspStyler styler = fStylerIndex.get(alpha);
        if (styler == null) {
          styler = createStyler(((double) alpha) / 255.0f);
          fStylerIndex.put(alpha, styler);
        }
        styler.style(Collections.singleton(object), aStyleCollector, aContext);
      }
    }

    private ILspStyler createStyler(double aAlpha) {
      if (aAlpha > 0.1) {
        return new AirspaceLabelStyler((float) aAlpha);
      } else {
        return new TLspStyler();
      }
    }
  }
}


