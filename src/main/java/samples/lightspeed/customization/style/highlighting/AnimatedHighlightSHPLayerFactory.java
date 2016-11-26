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
package samples.lightspeed.customization.style.highlighting;

import static samples.lightspeed.customization.style.highlighting.AnimatedHighlightStyler.COUNTRY_BG_FILL_COLOR;
import static samples.lightspeed.customization.style.highlighting.AnimatedHighlightStyler.COUNTRY_HL_FILL_COLOR;

import java.util.ArrayList;
import java.util.Collection;

import com.luciad.format.shp.TLcdSHPModelDescriptor;
import com.luciad.model.ILcdModel;
import com.luciad.view.lightspeed.layer.ALspSingleLayerFactory;
import com.luciad.view.lightspeed.layer.ILspInteractivePaintableLayer;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.TLspPaintRepresentation;
import com.luciad.view.lightspeed.layer.TLspPaintState;
import com.luciad.view.lightspeed.layer.shape.TLspShapeLayerBuilder;

/**
 * Layer factory for the highlight style sample.
 */
public class AnimatedHighlightSHPLayerFactory extends ALspSingleLayerFactory {

  // We store a reference to the highlight controllers to be able to register the styles as highlight listeners.
  private Collection<HighlightController> fHighlightControllers = new ArrayList<HighlightController>();

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
    return (aModel.getModelDescriptor() instanceof TLcdSHPModelDescriptor);
  }

  public ILspLayer createLayer(ILcdModel aModel) {
    if (aModel.getModelDescriptor() instanceof TLcdSHPModelDescriptor) {
      return createCustomSHPLayer(aModel);
    } else {
      throw new IllegalArgumentException("Cannot create layer for given model [" + aModel + "], " +
                                         "reason: model does not have model descriptor of type TLcdSHPModelDescriptor");
    }
  }

  private ILspLayer createCustomSHPLayer(ILcdModel aModel) {
    if (fHighlightControllers.isEmpty()) {
      throw new IllegalArgumentException("Please set the view on this layer factory before creating layers.");
    }

    // The highlight styler uses an animated style for objects under the mouse cursor.
    // The animation of the styles is performed through the view's animation manager.
    AnimatedHighlightAreaShapeStyler highlightStyler = new AnimatedHighlightAreaShapeStyler(COUNTRY_BG_FILL_COLOR);
    AnimatedHighlightAreaShapeStyler highlightStylerSelected = new AnimatedHighlightAreaShapeStyler(COUNTRY_HL_FILL_COLOR);
    AnimatedHighlightAreaLabelStyler labelHighlightStyler = new AnimatedHighlightAreaLabelStyler();

    TLspShapeLayerBuilder layerBuilder = TLspShapeLayerBuilder.newBuilder();
    layerBuilder.model(aModel)
                .selectable(true)
                .bodyStyler(TLspPaintState.REGULAR, highlightStyler)
                .bodyStyler(TLspPaintState.SELECTED, highlightStylerSelected)
                .labelStyler(TLspPaintState.REGULAR, labelHighlightStyler)
                .labelStyler(TLspPaintState.SELECTED, labelHighlightStyler);
    ILspInteractivePaintableLayer layer = layerBuilder.build();

    layer.setVisible(TLspPaintRepresentation.LABEL, true);

    for (HighlightController highlightController : fHighlightControllers) {
      highlightController.addHighlightListener(highlightStyler);
      highlightController.addHighlightListener(highlightStylerSelected);
      highlightController.addHighlightListener(labelHighlightStyler);
      highlightController.registerLayer(layer, TLspPaintRepresentation.BODY);
    }

    // Return a default layer which is selectable (this is important, or mouse motion listener will not work correctly)
    return layer;
  }
}
