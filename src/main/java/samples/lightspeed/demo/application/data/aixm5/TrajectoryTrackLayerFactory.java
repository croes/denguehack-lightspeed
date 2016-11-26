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
package samples.lightspeed.demo.application.data.aixm5;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.luciad.format.object3d.ILcd3DMesh;
import com.luciad.format.object3d.obj.TLcdOBJMeshDecoder;
import com.luciad.model.ILcdModel;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.TLspPaintState;
import com.luciad.view.lightspeed.layer.shape.TLspShapeLayerBuilder;
import com.luciad.view.lightspeed.painter.mesh.TLspMesh3DIcon;
import com.luciad.view.lightspeed.style.ALspStyle;
import com.luciad.view.lightspeed.style.TLsp3DIconStyle;
import com.luciad.view.lightspeed.style.TLspVerticalLineStyle;
import com.luciad.view.lightspeed.style.styler.ALspStyleCollector;
import com.luciad.view.lightspeed.style.styler.ALspStyler;

import samples.lightspeed.demo.framework.data.AbstractLayerFactory;

/**
 * Layer factory for the track models of a {@code TrajectoryTrackSimulatorModel}.
 */
public final class TrajectoryTrackLayerFactory extends AbstractLayerFactory {

  @Override
  public boolean canCreateLayers(ILcdModel aModel) {
    return aModel.getModelDescriptor() != null &&
           TrajectoryTrackSimulatorModel.TRAJECTORY_TRACK_SIMULATOR_MODEL_TYPE.equals(aModel.getModelDescriptor().getTypeName());
  }

  @Override
  public Collection<ILspLayer> createLayers(ILcdModel aModel) {
    ILspLayer layer = TLspShapeLayerBuilder.newBuilder().
        model(aModel).
                                               layerType(ILspLayer.LayerType.REALTIME).
                                               bodyStyler(TLspPaintState.REGULAR, new IconStyler()).
                                               objectWorldMargin(IconStyler.WORLD_SIZE).
                                               label("Track Layer").
                                               build();
    return Collections.singletonList(layer);
  }

  /**
   * Styler for 3D icons.
   * <p/>
   * Submits both a 3D icon style and a vertical line style for the icon objects.
   */
  private static class IconStyler extends ALspStyler {
    private List<ALspStyle> fStyles;
    private static final int WORLD_SIZE = 125;

    public IconStyler() {
      TLcdOBJMeshDecoder decoder = new TLcdOBJMeshDecoder();
      ILcd3DMesh mesh;
      try {
        mesh = decoder.decodeMesh("Data/3d_icons/plane.obj");
      } catch (IOException e) {
        throw new RuntimeException("Could not create icon style", e);
      }
      fStyles = new ArrayList<ALspStyle>();
      fStyles.add(
          TLsp3DIconStyle.newBuilder().
              icon(new TLspMesh3DIcon(mesh)).
                             worldSize(WORLD_SIZE).
                             verticalOffsetFactor(1.0).
                             iconSizeMode(TLsp3DIconStyle.ScalingMode.WORLD_SCALING).
                             transparent(true).
                             build()
      );
      fStyles.add(TLspVerticalLineStyle.newBuilder().color(new Color(255, 255, 255, 125)).build());
    }

    @Override
    public void style(Collection<?> aObjects, ALspStyleCollector aStyleCollector, TLspContext aContext) {
      aStyleCollector.objects(aObjects).styles(fStyles).submit();
    }
  }
}
