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
package samples.lucy.density;

import java.io.IOException;

import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.lucy.format.ALcyFileFormat;
import com.luciad.lucy.map.ILcyGXYLayerTypeProvider;
import com.luciad.lucy.model.ILcyModelContentTypeProvider;
import com.luciad.lucy.util.properties.ALcyProperties;
import com.luciad.lucy.workspace.ALcyWorkspaceObjectCodec;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelDecoder;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.ILcdGXYLayerFactory;

import samples.common.SampleData;
import samples.gxy.density.DensityLayerFactory;
import samples.lightspeed.common.tracks.EnrouteTrajectoryModelFactory;
import samples.lightspeed.density.TrajectoriesModelFactory;

/**
 * Format that is capable of visualizing the model stored in
 * {@link SampleData#US_TRAJECTORIES} as a density plot.
 *
 * @since 2012.0
 */
class DensityFormat extends ALcyFileFormat {

  DensityFormat(ILcyLucyEnv aLucyEnv,
                String aLongPrefix,
                String aShortPrefix,
                ALcyProperties aProperties) {
    super(aLucyEnv, aLongPrefix, aShortPrefix, aProperties);
  }

  @Override
  public boolean isModelOfFormat(ILcdModel aModel) {
    return aModel.getModelDescriptor() instanceof
        EnrouteTrajectoryModelFactory.TrajectoriesModelDescriptor;
  }

  @Override
  protected ILcyModelContentTypeProvider createModelContentTypeProvider() {
    return new ILcyModelContentTypeProvider() {
      @Override
      public int getModelContentType(ILcdModel aModel) {
        return ILcyModelContentTypeProvider.POLYLINE;
      }
    };
  }

  @Override
  protected ILcdModelDecoder[] createModelDecoders() {
    return new ILcdModelDecoder[]{new ILcdModelDecoder() {
      @Override
      public String getDisplayName() {
        return "Density model decoder";
      }

      @Override
      public boolean canDecodeSource(String s) {
        return SampleData.US_TRAJECTORIES.equals(s);
      }

      @Override
      public ILcdModel decode(String s) throws IOException {
        return TrajectoriesModelFactory.createTrajectoriesModel();
      }
    }};
  }

  @Override
  protected ILcyGXYLayerTypeProvider createGXYLayerTypeProvider() {
    return new ILcyGXYLayerTypeProvider() {
      @Override
      public int getGXYLayerType(ILcdGXYLayer aGXYLayer) {
        return ILcyGXYLayerTypeProvider.BACKGROUND;
      }
    };
  }

  @Override
  protected ILcdGXYLayerFactory createGXYLayerFactory() {
    return new DensityLayerFactory();
  }

  @Override
  protected ALcyWorkspaceObjectCodec[] createGXYLayerWorkspaceCodecs() {
    return new ALcyWorkspaceObjectCodec[]{
        new WorkspaceGXYLayerCodec(getLongPrefix() + "gxyLayerCodec",
                                   getShortPrefix(),
                                   createModelOfFormatFilter(),
                                   getGXYLayerFactory())
    };
  }
}
