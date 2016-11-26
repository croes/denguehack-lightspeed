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
package samples.decoder.asterix.lightspeed.trackdisplay;

import com.luciad.format.asterix.TLcdASTERIXTrackModelDescriptor;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelContainer;
import com.luciad.view.lightspeed.controller.manipulation.ALspInteractiveLabelProvider;
import com.luciad.view.lightspeed.layer.ALspSingleLayerFactory;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.TLspLayerTreeNode;
import com.luciad.view.lightspeed.layer.TLspPaintRepresentationState;
import com.luciad.view.lightspeed.layer.TLspPaintState;
import com.luciad.view.lightspeed.layer.shape.TLspShapeLayerBuilder;
import com.luciad.view.lightspeed.painter.label.TLspLabelPainter;
import com.luciad.view.lightspeed.style.styler.ILspStyler;

import samples.decoder.asterix.TrackSimulationModelDescriptor;
import samples.lightspeed.labels.interactive.LabelComponentProvider;

/**
 * Layer factory that styles ASTERIX tracks using history points, and with interactive track labels.
 */
class ASTERIXTrackLayerFactory extends ALspSingleLayerFactory {

  private final ALspInteractiveLabelProvider fInteractiveLabelProvider;

  private final LabelComponentProvider fRegularLabelComponentProvider;
  private final LabelComponentProvider fHighlightedLabelComponentProvider;
  private final LabelComponentProvider fInteractiveLabelComponentProvider;

  private final ASTERIXTrackAdditionalData fAdditionalData;

  public ASTERIXTrackLayerFactory(ALspInteractiveLabelProvider aInteractiveLabelProvider,
                                  LabelComponentProvider aInteractiveLabelComponentProvider,
                                  ASTERIXTrackAdditionalData aAdditionalData) {
    fInteractiveLabelProvider = aInteractiveLabelProvider;
    fAdditionalData = aAdditionalData;
    fInteractiveLabelComponentProvider = aInteractiveLabelComponentProvider;
    fRegularLabelComponentProvider = new ASTERIXTrackLabel(false, false, aAdditionalData);
    fHighlightedLabelComponentProvider = new ASTERIXTrackLabel(true, false, aAdditionalData);
  }

  @Override
  public boolean canCreateLayers(ILcdModel aModel) {
    if (isTrackModel(aModel)) {
      return true;
    }
    if (aModel instanceof ILcdModelContainer) {
      ILcdModelContainer modelContainer = (ILcdModelContainer) aModel;
      for (int i = 0; i < modelContainer.modelCount(); i++) {
        ILcdModel model = modelContainer.getModel(i);
        if (!canCreateLayers(model)) {
          return false;
        }
      }
      return true;
    }
    return false;
  }

  @Override
  public ILspLayer createLayer(ILcdModel aModel) {
    if (aModel instanceof ILcdModelContainer) {
      TLspLayerTreeNode node = new TLspLayerTreeNode(aModel.getModelDescriptor().getDisplayName());
      ILcdModelContainer modelContainer = (ILcdModelContainer) aModel;
      for (int i = 0; i < modelContainer.modelCount(); i++) {
        ILcdModel model = modelContainer.getModel(i);
        ILspLayer layer = createLayer(model);
        if (layer != null) {
          node.addLayer(layer);
        }
      }
      return node;
    } else if (isTrackModel(aModel)) {
      return createTrackLayer(aModel);
    } else {
      throw new IllegalArgumentException("Could not create layer for model: " + aModel);
    }
  }

  private ILspLayer createTrackLayer(ILcdModel aModel) {
    // Disable label fading
    TLspLabelPainter labelPainter = new TLspLabelPainter();
    labelPainter.setDefaultOpacityFadeDuration(0);

    TLspShapeLayerBuilder builder = TLspShapeLayerBuilder.newBuilder()
                                                         .model(aModel)
                                                         .bodyStyler(TLspPaintState.REGULAR, new ASTERIXTrackStyler(fAdditionalData))
                                                         .bodyStyler(TLspPaintState.SELECTED, new ASTERIXTrackStyler(fAdditionalData))
                                                         .labelPainter(labelPainter)
                                                         .labelStyler(TLspPaintState.REGULAR, createLabelStyler(TLspPaintRepresentationState.REGULAR_LABEL))
                                                         .labelStyler(TLspPaintState.SELECTED, createLabelStyler(TLspPaintRepresentationState.SELECTED_LABEL))
                                                         .labelStyler(TLspPaintState.EDITED, createLabelStyler(TLspPaintRepresentationState.EDITED_LABEL))
                                                         .labelEditable(true);

    // This is needed to make sure that objects/labels outside the view bounds are still painted.
    // See knowledge base article about disappearing shapes for more info
    builder.objectViewMargin(200);

    return builder.build();
  }

  private ILspStyler createLabelStyler(TLspPaintRepresentationState aPaintRepresentationState) {
    return new ASTERIXTrackLabelStyler(fInteractiveLabelProvider,
                                       aPaintRepresentationState,
                                       fRegularLabelComponentProvider,
                                       fHighlightedLabelComponentProvider,
                                       fInteractiveLabelComponentProvider,
                                       fAdditionalData);
  }

  static boolean isTrackModel(ILcdModel aModel) {
    return aModel != null &&
           (aModel.getModelDescriptor() instanceof TLcdASTERIXTrackModelDescriptor ||
            aModel.getModelDescriptor() instanceof TrackSimulationModelDescriptor);
  }
}
