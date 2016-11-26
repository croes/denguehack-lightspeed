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
package samples.lightspeed.decoder.custom1;

import static com.luciad.view.lightspeed.style.ILspWorldElevationStyle.ElevationMode.ABOVE_TERRAIN;
import static com.luciad.view.lightspeed.style.ILspWorldElevationStyle.ElevationMode.ON_TERRAIN;

import java.awt.Color;

import com.luciad.gui.TLcdCompositeIcon;
import com.luciad.gui.TLcdImageIcon;
import com.luciad.gui.TLcdResizeableIcon;
import com.luciad.gui.TLcdSymbol;
import com.luciad.model.ILcdModel;
import com.luciad.util.service.LcdService;
import com.luciad.view.lightspeed.layer.ALspSingleLayerFactory;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.ILspLayerFactory;
import com.luciad.view.lightspeed.layer.TLspPaintState;
import com.luciad.view.lightspeed.layer.shape.TLspShapeLayerBuilder;
import com.luciad.view.lightspeed.style.TLspFillStyle;
import com.luciad.view.lightspeed.style.TLspIconStyle;
import com.luciad.view.lightspeed.style.TLspLineStyle;
import com.luciad.view.lightspeed.style.styler.TLspStyler;

import samples.gxy.decoder.custom1.Custom1ModelDecoder;

/**
 * This implementation of ILspLayerFactory creates ILspLayer objects for
 * models decoded with a {@link Custom1ModelDecoder}.
 */
@LcdService(service = ILspLayerFactory.class)
public class Custom1LayerFactory extends ALspSingleLayerFactory {

  @Override
  public boolean canCreateLayers(ILcdModel aModel) {
    return aModel.getModelDescriptor().getTypeName()
                 .equalsIgnoreCase(Custom1ModelDecoder.TYPE_NAME);
  }

  @Override
  public ILspLayer createLayer(ILcdModel aModel) {
    if (!(aModel.getModelDescriptor().getTypeName()
                .equalsIgnoreCase(Custom1ModelDecoder.TYPE_NAME))) {
      throw new IllegalArgumentException("Can't create a layer for [" + aModel + "]: not a custom1 ILcdModel !");
    }

    // Create icon styles
    TLspIconStyle regularStyle = TLspIconStyle.newBuilder().icon(
        new TLcdImageIcon("images/mif/mif20_airplane.gif")).elevationMode(ABOVE_TERRAIN).build();
    TLcdCompositeIcon compositeIcon = new TLcdCompositeIcon();
    compositeIcon
        .addIcon(new TLcdResizeableIcon(new TLcdImageIcon("images/mif/mif20_airplane.gif")));
    compositeIcon
        .addIcon(new TLcdResizeableIcon(new TLcdSymbol(TLcdSymbol.CIRCLE, 20, Color.red)));
    TLspIconStyle selectedStyle = TLspIconStyle.newBuilder().icon(compositeIcon).elevationMode(ABOVE_TERRAIN).build();

    TLspShapeLayerBuilder layerBuilder = TLspShapeLayerBuilder.newBuilder();
    layerBuilder.model(aModel)
                .selectable(true)
                .bodyEditable(true)
                .bodyStyler(TLspPaintState.REGULAR,
                            new TLspStyler(regularStyle, TLspLineStyle.newBuilder().color(Color.black).elevationMode(ON_TERRAIN).build(),
                                           TLspFillStyle.newBuilder().color(Color.gray).opacity(.5f).elevationMode(ON_TERRAIN).build())
                )
                .bodyStyler(TLspPaintState.SELECTED,
                            new TLspStyler(selectedStyle, TLspLineStyle.newBuilder().color(Color.red).elevationMode(ON_TERRAIN).build(),
                                           TLspFillStyle.newBuilder().color(Color.red).opacity(.5f).elevationMode(ON_TERRAIN).build())
                );
    return layerBuilder.build();
  }
}

