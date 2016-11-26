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
package samples.decoder.grib;

import com.luciad.format.raster.ILcdRaster;
import com.luciad.shape.ILcdBounds;
import com.luciad.shape.ILcdMatrixView;

/**
 * A <code>TLcdGRIBRasterMatrix</code> is an <code>ILcdMatrix</code> wrapper around
 * a part of an <code>ILcdRaster</code>.
 * This implementation assumes that the bounds and all rasters have the same
 * model reference.
 * The implementation of the <code>ILcdMatrix</code> interface is for backward
 * compatibility.
 */
public class GRIBRasterMatrix implements ILcdMatrixView {

  private ILcdRaster[] fRasters;
  private double fXStart;
  private double fYStart;
  private double fXFactor;
  private double fYFactor;
  private int fXLength;
  private int fYLength;
  private double fDefaultValue = 0.0;

  public GRIBRasterMatrix( ILcdRaster aRaster,
                           ILcdBounds aBounds,
                           int aXLength,
                           int aYLength ) {
    this( new ILcdRaster[] { aRaster }, aBounds, aXLength, aYLength );
  }

  public GRIBRasterMatrix( ILcdRaster[] aRasters,
                           ILcdBounds aBounds,
                           int aXLength,
                           int aYLength ) {
    fRasters = aRasters;
    fXStart = aBounds.getLocation().getX();
    fYStart = aBounds.getLocation().getY();
    fXLength = aXLength;
    fYLength = aYLength;
    fXFactor = aBounds.getWidth() / ( aXLength - 1 );
    fYFactor = aBounds.getHeight() / ( aYLength - 1 );
  }

  /**
   * Sets the value that needs to be returned in case no raster is found at
   * the specified location.
   */
  public void setDefaultValue( double aDefaultValue ) {
    fDefaultValue = aDefaultValue;
  }

  /**
   * @see #setDefaultValue
   */
  public double getDefaultValue() {
    return fDefaultValue;
  }

  public double getValue( int i, int j ) {
    double x = fXStart + fXFactor * i;
    double y = fYStart + fYFactor * j;
    for ( int k = 0; k < fRasters.length ; k++ ) {
      if ( fRasters[ k ].getBounds().contains2D( x, y ) ) {
        return ( fRasters[ k ].retrieveValue( x, y ) & 0xffff );
      }
    }
    return fDefaultValue;
  }

  public double retrieveAssociatedPointX( int i, int j ) {
    return fXStart + fXFactor * i;
  }

  public double retrieveAssociatedPointY( int i, int j ) {
    return fYStart + fYFactor * j;
  }

  public int getRowCount() {
    return fYLength;
  }

  public int getColumnCount() {
    return fXLength;
  }

  /**
   * @deprecated Use {@link #retrieveAssociatedPointX( int, int )}.
   */
  public double getX( int i, int j ) {
    return retrieveAssociatedPointX( i, j );
  }

  /**
   * @deprecated Use {@link #retrieveAssociatedPointY( int, int )}.
   */
  public double getY( int i, int j ) {
    return retrieveAssociatedPointY( i, j );
  }

  /**
   * @deprecated Use {@link #getColumnCount()}.
   */
  public int getWidth() {
    return getColumnCount();
  }

  /**
   * @deprecated Use {@link #getRowCount()}.
   */
  public int getHeight() {
    return getRowCount();
  }
}
