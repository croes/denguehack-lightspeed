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
package samples.lightspeed.common.tracks;

import java.util.Collections;

import com.luciad.gui.TLcdImageIcon;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelDescriptor;
import com.luciad.view.lightspeed.layer.ALspSingleLayerFactory;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.TLspPaintState;
import com.luciad.view.lightspeed.layer.shape.TLspShapeLayerBuilder;
import com.luciad.view.lightspeed.style.TLspIconStyle;
import com.luciad.view.lightspeed.style.TLspIconStyle.ScalingMode;
import com.luciad.view.lightspeed.style.styler.TLspStyler;

/**
 * Factory that creates track layers based on position models provided by
 * {@link LonLatTrajectories
 * LonLatTrajectories}.
 *
 * @since 2012.0
 */
public class TracksLayerFactory extends ALspSingleLayerFactory {

  @Override
  public boolean canCreateLayers(ILcdModel aModel) {
    ILcdModelDescriptor descriptor = aModel.getModelDescriptor();
    return descriptor.getDisplayName().equals("Tracks");
  }

  @Override
  public ILspLayer createLayer(ILcdModel aModel) {
    TLspShapeLayerBuilder layerBuilder = TLspShapeLayerBuilder.newBuilder();
    TLspIconStyle iconStyle =
        TLspIconStyle.newBuilder()
                     .icon(new TLcdImageIcon("images/mif/mif20_airplane.gif"))
                     .scalingMode(ScalingMode.WORLD_SCALING_CLAMPED)
                     .worldSize(20000)
                     .scale(1)
                     .useOrientation(true)
                     .build();
    final TLspStyler styler = new TLspStyler();
    styler.addStyles(AirbornTrackProvider.getProvider(), Collections.singletonList(iconStyle));
    layerBuilder.model(aModel)
                .label("Tracks")
                .selectable(false)
                .bodyEditable(false)
                .bodyStyler(TLspPaintState.REGULAR, styler);
    return layerBuilder.build();
  }

}
