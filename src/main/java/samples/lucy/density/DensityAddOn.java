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

import com.luciad.lucy.addons.ALcyFormatAddOn;
import com.luciad.lucy.format.ALcyFormat;
import com.luciad.lucy.format.TLcyGXYLspAsynchronousFormatWrapper;
import com.luciad.lucy.format.TLcySafeGuardFormatWrapper;
import com.luciad.lucy.util.ALcyTool;
import com.luciad.model.ILcdModel;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.TLcdGXYDensityLayer;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.density.TLspDensityLayerBuilder;
import com.luciad.view.lightspeed.style.ILspWorldElevationStyle;
import com.luciad.view.lightspeed.style.TLspDensityLineStyle;
import com.luciad.view.lightspeed.style.TLspIndexColorModelStyle;

import samples.lightspeed.common.tracks.EnrouteTrajectoryModelFactory;

/**
 * This add-on demonstrates how easily you can extend your GXY format with Lightspeed technology. To
 * do so, the add-on wraps a GXY format with {@link TLcyGXYLspAsynchronousFormatWrapper}.
 *
 * @since 2012.0
 */
public class DensityAddOn extends ALcyFormatAddOn {

  /**
   * Default constructor.
   */
  public DensityAddOn() {
    super(ALcyTool.getLongPrefixWithClassName(DensityAddOn.class),
          ALcyTool.getShortPrefix(DensityAddOn.class));
  }

  @Override
  protected ALcyFormat createBaseFormat() {
    return new DensityFormat(getLucyEnv(), getLongPrefix(), getShortPrefix(), getPreferences());
  }

  @Override
  protected ALcyFormat createFormatWrapper(ALcyFormat aBaseFormat) {
    TLcySafeGuardFormatWrapper formatWrapper = new TLcySafeGuardFormatWrapper(aBaseFormat);
    // Wrap the format with TLcyGXYLspAsynchronousFormatWrapper to add support
    // for visualizing layers using Lightspeed technology.
    LightspeedDensityLayerFactory densityLayerFactory = new LightspeedDensityLayerFactory();
    return new TLcyGXYLspAsynchronousFormatWrapper(formatWrapper, densityLayerFactory);
  }

  /**
   * Factory that creates Lightspeed density layers, given GXY density layers.
   */
  private static class LightspeedDensityLayerFactory implements TLcyGXYLspAsynchronousFormatWrapper.GXYLspLayerFactory {

    @Override
    public boolean canCreateLayer(ILcdGXYLayer aGXYLayer) {
      ILcdModel model = aGXYLayer.getModel();
      return aGXYLayer instanceof TLcdGXYDensityLayer &&
             model.getModelDescriptor() instanceof EnrouteTrajectoryModelFactory.TrajectoriesModelDescriptor;
    }

    @Override
    public ILspLayer createLayer(ILcdGXYLayer aGXYLayer) {
      ILcdModel model = aGXYLayer.getModel();
      TLcdGXYDensityLayer densityLayer = (TLcdGXYDensityLayer) aGXYLayer;

      // Use the color model from the gxy layer.
      TLspIndexColorModelStyle colorModelStyle =
          TLspIndexColorModelStyle.newBuilder()
                                  .indexColorModel(densityLayer.getIndexColorModel())
                                  .build();

      TLspDensityLineStyle densityStyle = TLspDensityLineStyle.newBuilder()
                                                              .hardness(0.5f)
                                                              .pixelSize(5.5f)
                                                              .build();

      return TLspDensityLayerBuilder.newBuilder()
                                    .model(model)
                                    .label(model.getModelDescriptor().getDisplayName())
                                    .indexColorModel(colorModelStyle)
                                    .elevationMode(ILspWorldElevationStyle.ElevationMode.ABOVE_ELLIPSOID)
                                    .bodyStyler(densityStyle)
                                    .build();
    }
  }
}
