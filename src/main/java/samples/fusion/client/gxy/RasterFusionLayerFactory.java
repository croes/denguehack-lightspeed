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
package samples.fusion.client.gxy;

import java.awt.Color;
import java.awt.image.ColorModel;
import java.util.Enumeration;

import com.luciad.earth.tileset.ILcdEarthMultivaluedRasterTileSetCoverage;
import com.luciad.earth.tileset.ILcdEarthRasterTileSetCoverage;
import com.luciad.earth.tileset.ILcdEarthTileSet;
import com.luciad.earth.tileset.ILcdEarthTileSetCoverage;
import com.luciad.earth.view.gxy.TLcdEarthGXYMultivaluedRasterPainter;
import com.luciad.earth.view.gxy.TLcdEarthGXYRasterPainter;
import com.luciad.earth.view.gxy.TLcdEarthGXYSinglevaluedRasterPainter;
import com.luciad.format.raster.ILcdParameterizedIcon;
import com.luciad.format.raster.TLcdIndexColorModelFactory;
import com.luciad.fusion.tilestore.metadata.ALfnCoverageMetadata;
import com.luciad.fusion.tilestore.model.TLfnRasterTileStoreModelDescriptor;
import com.luciad.fusion.tilestore.model.TLfnTileStoreModelDescriptor;
import com.luciad.model.ILcdModel;
import com.luciad.util.ILcdInterval;
import com.luciad.util.TLcdColorMap;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.ILcdGXYLayerFactory;
import com.luciad.view.gxy.TLcdGXYLayer;
import com.luciad.view.gxy.painter.TLcdGXYImagePainter;

import samples.fusion.client.common.MultivaluedDataUtil;
import samples.gxy.decoder.MapSupport;

/**
 * The layer factory for LuciadFusion models with raster data.
 */
public class RasterFusionLayerFactory implements ILcdGXYLayerFactory {

  @Override
  public ILcdGXYLayer createGXYLayer(ILcdModel aModel) {
    if (!(aModel.getModelDescriptor() instanceof TLfnRasterTileStoreModelDescriptor)) {
      return null;
    }
    TLfnTileStoreModelDescriptor modelDescriptor = (TLfnTileStoreModelDescriptor) aModel.getModelDescriptor();
    ALfnCoverageMetadata coverageMetadata = modelDescriptor.getCoverageMetadata();

    TLcdGXYLayer layer = new NoSelectionLayer(aModel);
    layer.setGXYPen(MapSupport.createPen(aModel.getModelReference(),
                                         false)); // use a pen that correctly paints bounds that wrap around the date line

    switch (coverageMetadata.getType()) {
    case RASTER:
    case ELEVATION:
    case IMAGE:
      layer.setGXYPainterProvider(TLcdGXYImagePainter.create(aModel, null));
      break;
    case MULTIVALUED:
      if (isMultiValuedModel(aModel)) {
        TLcdEarthGXYMultivaluedRasterPainter multivaluedRasterPainter = new TLcdEarthGXYMultivaluedRasterPainter();
        configureMultivaluedPainterForModel(aModel, multivaluedRasterPainter);

        multivaluedRasterPainter.setUpdateInterval(15L * 60L * 1000L); // update the tiles every 15 minutes
        multivaluedRasterPainter.setAllowOutdatedTiles(true); // allow using cached, outdated tiles when retrieving updates fails

        layer.setGXYPainterProvider(multivaluedRasterPainter);
      } else if (isSingleValuedModel(aModel)) {
        TLcdEarthGXYRasterPainter singleValuedRasterPainter = configureSinglevaluedPainterForModel(new TLcdEarthGXYSinglevaluedRasterPainter(), aModel);
        layer.setGXYPainterProvider(singleValuedRasterPainter);
      } else {
        return null;
      }
    }

    return layer;
  }

  private static TLcdEarthGXYRasterPainter configurePainter(TLcdEarthGXYRasterPainter aPainter) {
    aPainter.setUpdateInterval(15L * 60L * 1000L); // update the tiles every 15 minutes
    aPainter.setAllowOutdatedTiles(true); // allow using cached, outdated tiles when retrieving updates fails
    aPainter.setWarpBlockSize(16); // lower warp block size to avoid distortions
    aPainter.setAsynchronousTileRequestAllowed(false);
    aPainter.setRepaintViewWhenTileAvailable(false);
    return aPainter;
  }

  /**
   * The single-valued raster painter needs a color model to map values to colors.
   *
   * @param aPainter the painter
   * @param aModel   the model
   */
  private TLcdEarthGXYRasterPainter configureSinglevaluedPainterForModel(TLcdEarthGXYSinglevaluedRasterPainter aPainter, ILcdModel aModel) {
    // Get the color map
    ILcdEarthMultivaluedRasterTileSetCoverage coverage = (ILcdEarthMultivaluedRasterTileSetCoverage) getCoverage(aModel);
    TLcdColorMap colorMap = MultivaluedDataUtil.createColorMap(coverage.getParameters().get(0));
    ILcdInterval interval = colorMap.getLevelInterval();
    // Determine a matching 16-bit color model
    final int bits = 16;
    final int size = 1 << bits;
    // Determine the scale and offset that map [min, max] to [Short.MIN_VALUE, Short.MAX_VALUE - 1], NaN is mapped to Short.MAX_VALUE
    final double toColorModelScale = (size - 2) / (interval.getMax() - interval.getMin());
    final double toColorModelOffsetBeforeScale = -interval.getMin(); // offset to apply before the scaling
    final double toColorModelOffsetAfterScale = Short.MIN_VALUE; // offset to apply after the scaling
    final double toColorModelOffset = toColorModelOffsetBeforeScale * toColorModelScale + toColorModelOffsetAfterScale;
    ColorModel colorModel = createColorModel(colorMap, bits, size, toColorModelOffset, toColorModelScale);
    // Configure the painter
    aPainter.setValueToShortOffset(toColorModelOffset);
    aPainter.setValueToShortScale(toColorModelScale);
    aPainter.setColorModel(colorModel);
    aPainter.setValueRange(Short.MIN_VALUE, Short.MAX_VALUE - 1);
    aPainter.setDefaultValue(Short.MAX_VALUE);
    return configurePainter(aPainter);
  }

  private ColorModel createColorModel(TLcdColorMap aColorMap, int aBits, int aSize, double aToColorModelOffset,
                                      double aToColorModelScale) {
    TLcdIndexColorModelFactory factory = new TLcdIndexColorModelFactory();
    factory.setBits(aBits);
    factory.setSize(aSize);
    int minSigned = -(1 << (aBits - 1));
    int maxSigned = minSigned + aSize - 1;
    int mask = (1 << aBits) - 1;
    for (int i = minSigned; i < maxSigned; i++) {
      double level = (i - aToColorModelOffset) / aToColorModelScale;
      Color color = aColorMap.retrieveColor(level);
      factory.setColor(i & mask, color.getRGB());
    }
    factory.setColor(maxSigned & mask, aColorMap.retrieveColor(Double.NaN).getRGB());
    return factory.createColorModel();
  }

  /**
   * The multi-valued raster painter needs a parameterized icon to paint individual points.
   *
   * @param aModel   the model
   * @param aPainter the painter
   */
  private void configureMultivaluedPainterForModel(ILcdModel aModel, TLcdEarthGXYMultivaluedRasterPainter aPainter) {
    ILcdEarthMultivaluedRasterTileSetCoverage coverage = (ILcdEarthMultivaluedRasterTileSetCoverage) getCoverage(
        aModel);
    ILcdParameterizedIcon icon = MultivaluedDataUtil.createParameterizedIcon(coverage.getParameters());
    aPainter.setParameterizedIcon(icon);
  }

  private boolean isMultiValuedModel(ILcdModel aModel) {
    ILcdEarthRasterTileSetCoverage coverage = getCoverage(aModel);
    return coverage.getCoverageType() == ILcdEarthTileSetCoverage.CoverageType.MULTIVALUED &&
           coverage instanceof ILcdEarthMultivaluedRasterTileSetCoverage &&
           ((ILcdEarthMultivaluedRasterTileSetCoverage) coverage).getParameters().size() > 1;
  }

  private boolean isSingleValuedModel(ILcdModel aModel) {
    ILcdEarthRasterTileSetCoverage coverage = getCoverage(aModel);
    return coverage.getCoverageType() == ILcdEarthTileSetCoverage.CoverageType.MULTIVALUED &&
           coverage instanceof ILcdEarthMultivaluedRasterTileSetCoverage &&
           ((ILcdEarthMultivaluedRasterTileSetCoverage) coverage).getParameters().size() == 1;
  }

  private ILcdEarthRasterTileSetCoverage getCoverage(ILcdModel aModel) {
    Enumeration elements = aModel.elements();
    ILcdEarthTileSet tileSet = (ILcdEarthTileSet) elements.nextElement();
    return (ILcdEarthRasterTileSetCoverage) tileSet.getTileSetCoverage(0);
  }

}
