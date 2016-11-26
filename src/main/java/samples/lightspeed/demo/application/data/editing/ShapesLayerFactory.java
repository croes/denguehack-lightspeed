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
package samples.lightspeed.demo.application.data.editing;

import java.awt.Color;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Properties;

import com.luciad.model.ILcdModel;
import com.luciad.view.lightspeed.editor.TLspShapeEditor;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.TLspPaintRepresentation;
import com.luciad.view.lightspeed.layer.TLspPaintState;
import com.luciad.view.lightspeed.layer.shape.TLspShapeLayerBuilder;
import com.luciad.view.lightspeed.painter.shape.TLspShapePaintingHints;
import com.luciad.view.lightspeed.style.TLspFillStyle;
import com.luciad.view.lightspeed.style.TLspLineStyle;
import com.luciad.view.lightspeed.style.styler.ILspStyler;
import com.luciad.view.lightspeed.style.styler.TLspEditableStyler;

import samples.lightspeed.demo.framework.data.AbstractLayerFactory;
import samples.lightspeed.style.editable.SelectionStyler;

/**
 * Layer factory for models created with the {@link ShapesModelFactory}.
 * <p/>
 * This layer factory can be configured with following properties:
 * <table cellspacing="10">
 * <tr> <td><b>Key</b></td> <td><b>Type</b></td> <td><b>Default Value</b></td>
 * <td><b>Description</b></td> </tr>
 * <tr> <td>editor.initialZ</td> <td>double</td> <td>250</td> <td>Specifies the initial Z-value of
 * the editor</td></tr>
 * </table>
 */
public class ShapesLayerFactory extends AbstractLayerFactory {

  private static final Color COLOR = new Color(0x009999);
  private static final float SHAPE_OPACITY = 0.8f;

  private double fInitialZ;

  @Override
  public void configure(Properties aProperties) {
    fInitialZ = Double.parseDouble(aProperties.getProperty("editor.initialZ", "250"));
  }

  @Override
  public Collection<ILspLayer> createLayers(ILcdModel aModel) {
    return Collections.singletonList(createLayer(aModel));
  }

  public ILspLayer createLayer(ILcdModel aModel) {

    // Set painter
    TLspEditableStyler defaultStyleProvider = getDefaultStyler();

    ILspStyler selectedStyleProvider = new SelectionStyler(defaultStyleProvider);

    // Set editor
    TLspShapeEditor shapeEditor = new TLspShapeEditor();
    shapeEditor.setInitialZValue(fInitialZ);

    ILspLayer layer = TLspShapeLayerBuilder.newBuilder().model(aModel)
                                           .bodyEditable(true)
                                           .selectable(true)
                                           .bodyStyler(TLspPaintState.REGULAR, defaultStyleProvider)
                                           .bodyStyler(TLspPaintState.SELECTED, selectedStyleProvider)
                                           .bodyStyler(TLspPaintState.EDITED, selectedStyleProvider)
                                           .bodyEditor(shapeEditor)
                                           .paintingHints(TLspShapePaintingHints.MAX_QUALITY)
                                           .build();

    // Explicitly set all paint representations visible
    Collection<TLspPaintRepresentation> paintRepresentations = layer.getPaintRepresentations();
    for (TLspPaintRepresentation paintRepresentation : paintRepresentations) {
      layer.setVisible(paintRepresentation, true);
    }

    return layer;
  }

  private static TLspEditableStyler getDefaultStyler() {
    return new TLspEditableStyler(Arrays.asList(TLspFillStyle.newBuilder().color(COLOR).opacity(SHAPE_OPACITY).build(),
                                                TLspLineStyle.newBuilder().color(COLOR.darker()).opacity(SHAPE_OPACITY).width(1.5f).build()));
  }

  @Override
  public boolean canCreateLayers(ILcdModel aModel) {
    return ShapesModelFactory.class.getName().equals(aModel.getModelDescriptor().getSourceName());
  }
}
