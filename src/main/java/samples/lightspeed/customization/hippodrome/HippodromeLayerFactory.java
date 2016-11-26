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
package samples.lightspeed.customization.hippodrome;

import com.luciad.model.ILcdModel;
import com.luciad.reference.ILcdGridReference;
import com.luciad.view.lightspeed.editor.TLspCompositeEditor;
import com.luciad.view.lightspeed.editor.TLspExtrudedShapeEditor;
import com.luciad.view.lightspeed.layer.ALspSingleLayerFactory;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.shape.TLspShapeLayerBuilder;
import com.luciad.view.lightspeed.painter.shape.TLspShapePainter;

/**
 * Layer factory for the hippodrome sample.
 */
class HippodromeLayerFactory extends ALspSingleLayerFactory {

  public boolean canCreateLayers(ILcdModel aModel) {
    return (aModel.getModelDescriptor().getDisplayName().equals("HippodromeModel"));
  }

  public ILspLayer createLayer(ILcdModel aModel) {
    if (aModel.getModelDescriptor().getDisplayName().equals("HippodromeModel")) {
      return createHippodromeLayer(aModel);
    } else {
      throw new IllegalArgumentException("Cannot create layer for given model [" + aModel + "], " +
                                         "reason: model not recognized");
    }
  }

  private ILspLayer createHippodromeLayer(ILcdModel aModel) {
    TLspShapeLayerBuilder layerBuilder = TLspShapeLayerBuilder.newBuilder();

    HippodromeShapeDiscretizer shapeDiscretizer = new HippodromeShapeDiscretizer();
    // We will paint bodies with this painter. The layer implementation automatically
    // also uses this painter for selected and edited objects, so we do not
    // have to set this explicitly.
    TLspShapePainter painter = new TLspShapePainter();
    // Use our custom shape discretization to also enable discretization support for hippodromes.
    painter.setShapeDiscretizer(shapeDiscretizer);

    // Editor (note that we also allow editing of extruded hippodromes).
    TLspCompositeEditor compositeEditor = new TLspCompositeEditor();
    compositeEditor.addEditor(new HippodromeEditor());
    TLspExtrudedShapeEditor extrudedShapeEditor = new TLspExtrudedShapeEditor(compositeEditor);
    compositeEditor.addEditor(extrudedShapeEditor);
    extrudedShapeEditor.setInitialZValue(1500);

    layerBuilder.model(aModel)
                .label((aModel
                    .getModelReference() instanceof ILcdGridReference) ? "Grid Hippodrome" : "Geod Hippodrome")
                .selectable(true)
                .bodyEditable(true)
                .bodyPainter(painter)
                .bodyEditor(compositeEditor);
    return layerBuilder.build();
  }
}
