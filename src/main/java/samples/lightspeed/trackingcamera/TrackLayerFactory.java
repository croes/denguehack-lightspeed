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
package samples.lightspeed.trackingcamera;

import java.awt.Color;

import com.luciad.model.ILcdModel;
import com.luciad.view.lightspeed.layer.ALspSingleLayerFactory;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.TLspPaintState;
import com.luciad.view.lightspeed.layer.shape.TLspShapeLayerBuilder;
import com.luciad.view.lightspeed.style.TLsp3DIconStyle;
import com.luciad.view.lightspeed.style.TLspVerticalLineStyle;
import com.luciad.view.lightspeed.style.styler.TLspStyler;

/**
 * Layer factory for the tracking camera sample that can create point layers.
 */
public class TrackLayerFactory extends ALspSingleLayerFactory {
  private static final int WORLD_SIZE = 25000;

  private final TLspVerticalLineStyle fVerticalLineStyle = TLspVerticalLineStyle.newBuilder().color(Color.green).build();
  private final TLsp3DIconStyle fIconStyle = TLsp3DIconStyle.newBuilder()
                                                            .icon("Data/3d_icons/plane.obj")
                                                            .worldSize(WORLD_SIZE)
                                                            .verticalOffsetFactor(1.0)
                                                            .iconSizeMode(TLsp3DIconStyle.ScalingMode.WORLD_SCALING)
                                                            .transparent(true)
                                                            .build();

  public TrackLayerFactory() {
  }

  @Override
  public boolean canCreateLayers(ILcdModel aModel) {
    return aModel.getModelDescriptor().getDisplayName().equals("Tracking Points");
  }

  @Override
  public ILspLayer createLayer(ILcdModel aModel) {
    TLspShapeLayerBuilder layerBuilder = TLspShapeLayerBuilder.newBuilder()
                                                              .model(aModel)
                                                              .synchronizePainters(false)
                                                              .layerType(ILspLayer.LayerType.REALTIME)
                                                              .bodyStyler(TLspPaintState.REGULAR, new TLspStyler(fIconStyle, fVerticalLineStyle))
                                                              .objectWorldMargin(WORLD_SIZE);
    return layerBuilder.build();
  }
}
