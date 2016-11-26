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
package samples.gxy.rectification;

import java.awt.Component;
import java.util.Arrays;

import com.luciad.format.raster.TLcdDEMModelDecoder;
import com.luciad.format.raster.TLcdDMEDModelDecoder;
import com.luciad.format.raster.TLcdDTEDModelDecoder;
import com.luciad.format.raster.TLcdGeoTIFFModelDecoder;
import com.luciad.gui.TLcdImageIcon;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelDecoder;
import com.luciad.model.ILcdModelProducerListener;
import com.luciad.model.TLcdModelProducerEvent;
import com.luciad.reference.TLcdModelReferenceDecoder;
import com.luciad.util.TLcdSharedBuffer;
import com.luciad.view.gxy.ILcdGXYView;

import samples.common.formatsupport.OpenAction;
import samples.common.formatsupport.OpenSupport;
import samples.gxy.common.ProgressUtil;
import samples.gxy.common.toolbar.ToolBar;
import samples.gxy.rectification.util.OpenImageAction;
import samples.gxy.rectification.util.RasterMouseLocationLabel;

/**
 * A ToolBar with a few specific controls:
 * - a button to open a raster
 * - a button to open an elevation raster
 * - a tie point controller button
 * - a label displaying the raster mouse location
 */
public class TiePointsToolbar extends ToolBar {

  public TiePointsToolbar(ILcdGXYView aImgView,
                          TiePointsRectifier aRectifier,
                          final LayerManager aLayerManager,
                          ILcdModelDecoder aModelDecoder,
                          boolean aIsStandalone) {
    super(aImgView, false, null);

    CreateTiePointController tie_point_controller = new CreateTiePointController(aImgView, aRectifier);

    addGXYController(tie_point_controller, true);

    addSpace();
    if (aIsStandalone) {
      OpenImageAction openImage = new OpenImageAction((Component) aImgView, aModelDecoder) {
        @Override
        protected void rasterModelLoaded(ILcdModel aModel) {
          aLayerManager.loadRasterModel(aModel);
        }
      };
      addAction(openImage);

      OpenSupport open_support = new OpenSupport(this, Arrays.asList(createElevationModelDecoders()));
      open_support.addStatusListener(ProgressUtil.createProgressDialog(this, "Loading elevation raster"));
      OpenAction openElevation = new OpenAction(open_support);
      openElevation.setIcon(new TLcdImageIcon("images/gui/i16_terrain.gif"));
      openElevation.setShortDescription("Open an elevation raster to be used during ortho-rectification");
      open_support.addModelProducerListener(new ILcdModelProducerListener() {
        public void modelProduced(TLcdModelProducerEvent aModelProducerEvent) {
          aLayerManager.loadTerrainModel(aModelProducerEvent.getModel());
        }
      });
      openElevation.setName("Load elevation...");
      addAction(openElevation);
    }
    addAction(new EditRasterReferencerAction(aRectifier));

    // Sets the initial ILcdGXYController
    aImgView.setGXYController(getGXYController(tie_point_controller));
  }

  @Override
  protected Component createMouseLocationComponent() {
    return new RasterMouseLocationLabel(getGXYView());
  }

  private ILcdModelDecoder[] createElevationModelDecoders() {
    TLcdDMEDModelDecoder dmed = new TLcdDMEDModelDecoder(TLcdSharedBuffer.getBufferInstance());
    dmed.setSampleStrategy(TLcdDMEDModelDecoder.BILINEAR);
    return new ILcdModelDecoder[]{
        new TLcdDEMModelDecoder(TLcdSharedBuffer.getBufferInstance()),
        new TLcdDTEDModelDecoder(TLcdSharedBuffer.getBufferInstance()),
        dmed,
        new TLcdGeoTIFFModelDecoder(new TLcdModelReferenceDecoder()),
    };
  }

}
