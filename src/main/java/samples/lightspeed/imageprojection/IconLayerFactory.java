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
package samples.lightspeed.imageprojection;

import com.luciad.model.ILcdModel;
import com.luciad.view.lightspeed.layer.ALspSingleLayerFactory;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.TLspPaintState;
import com.luciad.view.lightspeed.layer.shape.TLspShapeLayerBuilder;
import com.luciad.view.lightspeed.style.TLsp3DIconStyle;
import com.luciad.view.lightspeed.style.TLspVerticalLineStyle;
import com.luciad.view.lightspeed.style.styler.TLspStyler;

/**
 * Layer factory to visualize 3D points with a plane 3D Icon and a vertical line.
 */
class IconLayerFactory extends ALspSingleLayerFactory {

  private static final String ICON = "Data/3d_icons/plane.obj";

  public boolean canCreateLayers(ILcdModel aModel) {
    return aModel.getModelDescriptor().getTypeName().equals("Plane");
  }

  public ILspLayer createLayer(ILcdModel aModel) {

    TLsp3DIconStyle iconStyle = TLsp3DIconStyle.newBuilder()
                                               .icon(ICON)
                                               .worldSize(100)
                                               .verticalOffsetFactor(1.0)
                                               .iconSizeMode(TLsp3DIconStyle.ScalingMode.WORLD_SCALING)
                                               .build();

    TLspStyler styler = new TLspStyler();
    styler.setStyles(iconStyle, TLspVerticalLineStyle.newBuilder().build());

    return TLspShapeLayerBuilder.newBuilder()
                                .model(aModel)
                                .bodyStyler(TLspPaintState.REGULAR, styler)
                                .selectable(true)
                                .culling(false)
                                .objectWorldMargin(iconStyle.getWorldSize())
                                .build();
  }
}
