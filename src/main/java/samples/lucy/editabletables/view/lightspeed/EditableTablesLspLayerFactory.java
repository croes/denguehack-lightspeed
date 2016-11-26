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
package samples.lucy.editabletables.view.lightspeed;

import java.awt.Color;

import com.luciad.gui.ILcdIcon;
import com.luciad.gui.TLcdSymbol;
import com.luciad.model.ILcdModel;
import com.luciad.view.lightspeed.layer.ALspSingleLayerFactory;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.TLspLayer;
import com.luciad.view.lightspeed.layer.TLspPaintRepresentation;
import com.luciad.view.lightspeed.layer.TLspPaintState;
import com.luciad.view.lightspeed.painter.shape.TLspShapePainter;
import com.luciad.view.lightspeed.style.TLspIconStyle;
import com.luciad.view.lightspeed.style.styler.TLspStyler;

import samples.lucy.editabletables.model.EditableTablesModelDescriptor;

/**
 * Simple Lightspeed layer factory for the sample model.
 *
 * @since 2013.0
 */
public class EditableTablesLspLayerFactory extends ALspSingleLayerFactory {

  private static final ILcdIcon DEFAULT_ICON =
      new TLcdSymbol(TLcdSymbol.FILLED_CIRCLE, 17, Color.YELLOW.darker(), Color.YELLOW);
  private static final ILcdIcon SELECTED_ICON =
      new TLcdSymbol(TLcdSymbol.FILLED_CIRCLE, 18, Color.RED.darker(), Color.RED);

  @Override
  public ILspLayer createLayer(ILcdModel aModel) {
    if (!canCreateLayers(aModel)) {
      throw new IllegalArgumentException("Can't create layer for this model: " + aModel);
    }
    TLspLayer layer = new TLspLayer(aModel, "Editable in table") {
      @Override
      public boolean isEditableSupported() {
        return true;
      }
    };
    layer.setEditable(true);

    TLspStyler defaultStyler = new TLspStyler(TLspIconStyle.newBuilder().icon(DEFAULT_ICON).build());
    TLspStyler selectedStyler = new TLspStyler(TLspIconStyle.newBuilder().icon(SELECTED_ICON).build());
    TLspShapePainter painter = new TLspShapePainter();
    painter.setStyler(TLspPaintState.REGULAR, defaultStyler);
    painter.setStyler(TLspPaintState.SELECTED, selectedStyler);
    layer.setPainter(TLspPaintRepresentation.BODY, painter);

    return layer;
  }

  @Override
  public boolean canCreateLayers(ILcdModel aModel) {
    return aModel.getModelDescriptor() instanceof EditableTablesModelDescriptor;
  }
}
