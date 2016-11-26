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
package samples.gxy.contour;

import com.luciad.format.raster.ILcdRaster;
import com.luciad.format.raster.ILcdTile;
import com.luciad.format.raster.TLcdDTEDTileDecoder;
import com.luciad.shape.ILcdBounds;
import com.luciad.shape.ILcdMatrixView;

/**
 * ILcdMatrixView wrapper around {@link ILcdRaster}. Contour finders use a {@link ILcdMatrixView} to calculate
 * contours, whereas raster layers contain an {@link ILcdRaster}, therefore this wrapper is used.
 */
public class RasterMatrixView implements ILcdMatrixView {
  private ILcdRaster fRaster;
  private double fXStart;
  private double fYStart;
  private double fXFactor;
  private double fYFactor;
  private int fXLength;
  private int fYLength;
  private double fDefaultValue = 0.0;

  /**
   * Construct a new raster matrix for the specified raster. The bounds argument is used to find the
   * associated points of the matrix view.
   *
   * @param aRaster The raster to be used.
   * @param aBounds The bounds of the raster.
   */
  public RasterMatrixView(ILcdRaster aRaster,
                          ILcdBounds aBounds) {

    fRaster = aRaster;
    ILcdTile tile = aRaster.retrieveTile(0, 0);

    fXLength = (int) (aBounds.getWidth() * (tile.getWidth() / fRaster.getTileWidth()));
    fYLength = (int) (aBounds.getHeight() * (tile.getHeight() / fRaster.getTileHeight()));

    int numCellsX = fXLength - 1;
    int numCellsY = fYLength - 1;

    double epsilon = 1e-6;
    double cellSizeX = aBounds.getWidth() / (numCellsX);
    double cellSizeY = aBounds.getHeight() / (numCellsY);
    fXStart = aBounds.getLocation().getX() + cellSizeX * epsilon;
    fYStart = aBounds.getLocation().getY() + cellSizeY * epsilon;
    fXFactor = cellSizeX - (cellSizeX * epsilon * 2) / numCellsX;
    fYFactor = cellSizeY - (cellSizeY * epsilon * 2) / numCellsY;
  }

  /**
   * Sets the value that needs to be returned in case no raster is found at the specified location.
   *
   * @param aDefaultValue The default value.
   */
  public void setDefaultValue(double aDefaultValue) {
    fDefaultValue = aDefaultValue;
  }

  /**
   * Returns the default value.
   *
   * @return the default value.
   *
   * @see #setDefaultValue(double)
   */
  public double getDefaultValue() {
    return fDefaultValue;
  }

  public double getValue(int i, int j) {

    double x = fXStart + fXFactor * i;
    double y = fYStart + fYFactor * j;

    if (fRaster.getBounds().contains2D(x, y)) {
      return fRaster.retrieveValue(x, y);
    }

    return fDefaultValue;
  }

  public double retrieveAssociatedPointX(int i, int j) {
    return fXStart + fXFactor * i;
  }

  public double retrieveAssociatedPointY(int i, int j) {
    return fYStart + fYFactor * j;
  }

  public int getRowCount() {
    return fYLength;
  }

  public int getColumnCount() {
    return fXLength;
  }
}
