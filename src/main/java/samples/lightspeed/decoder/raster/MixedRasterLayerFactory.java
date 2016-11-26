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
package samples.lightspeed.decoder.raster;

import java.util.Enumeration;

import com.luciad.earth.model.ILcdEarthModelDescriptor;
import com.luciad.earth.tileset.ILcdEarthTileSet;
import com.luciad.earth.tileset.ILcdEarthTileSetCoverage;
import com.luciad.format.raster.TLcdMultilevelRasterModelDescriptor;
import com.luciad.format.raster.TLcdRasterModelDescriptor;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelTreeNode;
import com.luciad.util.service.LcdService;
import com.luciad.view.lightspeed.layer.ALspSingleLayerFactory;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.ILspLayerFactory;
import com.luciad.view.lightspeed.layer.raster.TLspRasterLayerBuilder;

/**
 * Layer factory for the raster samples.
 */
@LcdService(service = ILspLayerFactory.class, priority = LcdService.DEFAULT_PRIORITY + 1)
public class MixedRasterLayerFactory extends ALspSingleLayerFactory {

  public boolean canCreateLayers(ILcdModel aModel) {
    return samples.lightspeed.style.raster.RasterLayerFactory.canCreateLayersForModel(aModel) &&
           !(aModel instanceof ILcdModelTreeNode);
  }

  public ILspLayer createLayer(ILcdModel aModel) {
    if (!canCreateLayers(aModel)) {
      return null;
    }

    return TLspRasterLayerBuilder.newBuilder()
                                 .model(aModel)
                                 .build();
  }

  /**
   * Returns whether the specified model contains image data.
   *
   * @param aModel a model
   *
   * @return {@code true} if {@code aModel} contains image data
   */
  public static boolean containsImageData(ILcdModel aModel) {
    if (aModel.getModelDescriptor() instanceof TLcdRasterModelDescriptor) {
      return !((TLcdRasterModelDescriptor) aModel.getModelDescriptor()).isElevation();
    }
    if (aModel.getModelDescriptor() instanceof TLcdMultilevelRasterModelDescriptor) {
      return !((TLcdMultilevelRasterModelDescriptor) aModel.getModelDescriptor()).isElevation();
    }
    if (aModel.getModelDescriptor() instanceof ILcdEarthModelDescriptor) {
      Enumeration en = aModel.elements();
      while (en.hasMoreElements()) {
        ILcdEarthTileSet tileSet = (ILcdEarthTileSet) en.nextElement();
        if (containsImageData(tileSet)) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Returns whether the specified model contains elevation data.
   *
   * @param aModel a model
   *
   * @return {@code true} if {@code aModel} contains elevation data
   */
  public static boolean containsElevationData(ILcdModel aModel) {
    if (aModel.getModelDescriptor() instanceof TLcdRasterModelDescriptor) {
      return ((TLcdRasterModelDescriptor) aModel.getModelDescriptor()).isElevation();
    }
    if (aModel.getModelDescriptor() instanceof TLcdMultilevelRasterModelDescriptor) {
      return ((TLcdMultilevelRasterModelDescriptor) aModel.getModelDescriptor()).isElevation();
    }
    if (aModel.getModelDescriptor() instanceof ILcdEarthModelDescriptor) {
      Enumeration en = aModel.elements();
      while (en.hasMoreElements()) {
        ILcdEarthTileSet tileSet = (ILcdEarthTileSet) en.nextElement();
        if (containsElevationData(tileSet)) {
          return true;
        }
      }
    }
    return false;
  }

  private static boolean containsElevationData(ILcdEarthTileSet aTileSet) {
    boolean hasElevation = false;
    for (int i = 0; i < aTileSet.getTileSetCoverageCount(); i++) {
      ILcdEarthTileSetCoverage.CoverageType coverageType = aTileSet.getTileSetCoverage(i).getCoverageType();
      if (coverageType == ILcdEarthTileSetCoverage.CoverageType.ELEVATION) {
        return true;
      }
    }
    return hasElevation;
  }

  private static boolean containsImageData(ILcdEarthTileSet aTileSet) {
    boolean hasElevation = false;
    for (int i = 0; i < aTileSet.getTileSetCoverageCount(); i++) {
      ILcdEarthTileSetCoverage.CoverageType coverageType = aTileSet.getTileSetCoverage(i).getCoverageType();
      if (coverageType == ILcdEarthTileSetCoverage.CoverageType.IMAGE) {
        return true;
      }
    }
    return hasElevation;
  }

}
