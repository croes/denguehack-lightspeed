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
package samples.lucy.cop.addons.missioncontroltheme;

import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import javax.imageio.ImageIO;

import com.luciad.earth.tileset.ALcdEarthTileSet;
import com.luciad.earth.tileset.ILcdEarthRasterTileSetCoverage;
import com.luciad.earth.tileset.ILcdEarthTileSetCallback;
import com.luciad.earth.tileset.ILcdEarthTileSetCoverage;
import com.luciad.earth.tileset.TLcdEarthTile;
import com.luciad.earth.tileset.TLcdEarthTileFormat;
import com.luciad.earth.tileset.TLcdEarthTileOperationMode;
import com.luciad.reference.ILcdGeoReference;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.shape.ILcdBounds;
import com.luciad.shape.shape2D.ILcd2DEditableBounds;
import com.luciad.shape.shape2D.TLcdLonLatBounds;

/**
 * {@link com.luciad.earth.tileset.ILcdEarthTileSet} implementation for the Boston data set. All
 * tiles are stored on disk. A specific naming convention (see {@linkplain
 * #getTileUri(URI, int, long, long) getTileUri method})allows to quickly retrieved a
 * specific tile at a certain level.
 */
final class BostonTileSet extends ALcdEarthTileSet {
  private final URI fBaseUri;
  private final ILcdEarthTileSetCoverage fCoverage;
  private final ILcdBounds fBounds = new TLcdLonLatBounds(-180, -90, 360, 180);

  public BostonTileSet(URI aBaseUri) {
    super(15, 2, 4);
    fCoverage = new ILcdEarthRasterTileSetCoverage() {

      private TLcdGeodeticReference fReference = new TLcdGeodeticReference();

      @Override
      public String getName() {
        return "Boston";
      }

      @Override
      public CoverageType getCoverageType() {
        return CoverageType.IMAGE;
      }

      @Override
      public TLcdEarthTileFormat getNativeFormat() {
        return new TLcdEarthTileFormat(BufferedImage.class);
      }

      @Override
      public ILcdGeoReference getNativeGeoReference() {
        return fReference;
      }

      @Override
      public double getPixelDensity(int i) {
        return 256 * 256 * ((getTileRowCount(i) * getTileColumnCount(i)) / (fBounds.getWidth() * fBounds.getHeight()));
      }

      @Override
      public int getTileWidth(int i) {
        return 256;
      }

      @Override
      public int getTileHeight(int i) {
        return 256;
      }
    };
    fBaseUri = aBaseUri;
  }

  @Override
  public boolean isFormatSupported(ILcdEarthTileSetCoverage aCoverage, TLcdEarthTileFormat aFormat) {
    return aFormat.getFormatClass() != null && aFormat.getFormatClass().isAssignableFrom(BufferedImage.class);
  }

  @Override
  public void produceTile(ILcdEarthTileSetCoverage aCoverage, int aLevel, long aTileX, long aTileY, ILcdGeoReference aReference, TLcdEarthTileFormat aFormat, TLcdEarthTileOperationMode aMode, ILcdEarthTileSetCallback aCallback, Object aUserData) {
    URI uri = getTileUri(fBaseUri, aLevel, aTileX, aTileY);
    try {
      InputStream stream = uri.toURL().openStream();
      BufferedImage data;
      try {
        data = ImageIO.read(stream);
      } finally {
        stream.close();
      }
      ILcd2DEditableBounds tileBounds = fBounds.cloneAs2DEditableBounds();
      getTileBoundsSFCT(aLevel, aTileX, aTileY, tileBounds);
      aCallback.tileAvailable(new TLcdEarthTile(tileBounds, data, aLevel, aTileX, aTileY, aCoverage, aReference, aFormat), aUserData);
    } catch (FileNotFoundException e) {
      //file not found means there is no data for that tile coordinate at that level, which is an expected case.
      //don't flag it as an error, by _not_ passing the exception along.
      aCallback.tileNotAvailable(aLevel, aTileX, aTileY, aCoverage, aReference, aFormat, aUserData, e.getMessage(), null);
    } catch (IOException e) {
      aCallback.tileNotAvailable(aLevel, aTileX, aTileY, aCoverage, aReference, aFormat, aUserData, e.getMessage(), e);
    }
  }

  protected URI getTileUri(URI aBaseUri, int aLevel, long aTileX, long aTileY) {
    return aBaseUri.resolve(aLevel + "_" + aTileX + "_" + aTileY + ".png");
  }

  @Override
  public int getTileSetCoverageCount() {
    return 1;
  }

  @Override
  public ILcdEarthTileSetCoverage getTileSetCoverage(int i) {
    return fCoverage;
  }

  @Override
  public ILcdBounds getBounds() {
    return fBounds;
  }
}
