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
package samples.lightspeed.demo.application.data.support.layerfactories;

import java.awt.Color;
import java.util.Collection;
import java.util.Collections;

import com.luciad.gui.ILcdIcon;
import com.luciad.gui.TLcdImageIcon;
import com.luciad.model.ILcdModel;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.TLspPaintState;
import com.luciad.view.lightspeed.layer.shape.TLspShapeLayerBuilder;
import com.luciad.view.lightspeed.painter.label.style.TLspDataObjectLabelTextProviderStyle;
import com.luciad.view.lightspeed.style.ILspWorldElevationStyle;
import com.luciad.view.lightspeed.style.TLspIconStyle;

import samples.lightspeed.demo.framework.data.AbstractLayerFactory;

public class LasVegasPOILayerFactory extends AbstractLayerFactory {
  @Override
  public boolean canCreateLayers(ILcdModel aModel) {
    return aModel.getModelDescriptor().getTypeName().contains("Gml");
  }

  @Override
  public Collection<ILspLayer> createLayers(ILcdModel aModel) {

    Color modulationColor = new Color(36, 130, 154);
    ILcdIcon icon = new TLcdImageIcon("Data/LightspeedDemo/Imaging/markerwhite.png");
    int height = icon.getIconHeight();
    float scale = 30f / height;

    boolean isGolfPOI = aModel.getModelDescriptor().getDisplayName().contains("golf");

    if (isGolfPOI) {
      scale *= 0.8;
      modulationColor = new Color(192, 192, 192);
    }

    TLspIconStyle iconStyle = TLspIconStyle
        .newBuilder()
        .elevationMode(ILspWorldElevationStyle.ElevationMode.ABOVE_TERRAIN)
        .useOrientation(false)
        .icon(icon)
        .scale(scale)
        .modulationColor(modulationColor)
        .offset(0, -(scale * height) / 2)
        .build();

    TLspDataObjectLabelTextProviderStyle labelTextStyle = TLspDataObjectLabelTextProviderStyle
        .newBuilder()
        .expressions("Name")
        .build();

    ILspLayer layer = TLspShapeLayerBuilder
        .newBuilder()
        .model(aModel)
        .layerType(ILspLayer.LayerType.BACKGROUND)
        .bodyEditable(false)
        .labelEditable(false)
        .selectable(true)
        .bodyStyler(TLspPaintState.REGULAR, iconStyle)
        .labelStyler(TLspPaintState.REGULAR, null)
        .labelStyler(TLspPaintState.SELECTED, isGolfPOI ? labelTextStyle : null)
        .build();

    return Collections.singletonList(layer);
  }
}
