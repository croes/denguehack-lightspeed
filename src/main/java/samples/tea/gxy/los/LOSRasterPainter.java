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
package samples.tea.gxy.los;

import com.luciad.format.raster.TLcdDTEDColorModelFactory;
import com.luciad.format.raster.TLcdRasterPainter;
import com.luciad.shape.ILcdBounded;
import com.luciad.shape.ILcdBounds;
import com.luciad.shape.shape2D.ILcd2DEditablePoint;
import com.luciad.transformation.ILcdModelXYWorldTransformation;
import com.luciad.util.TLcdOutOfBoundsException;
import com.luciad.view.gxy.ILcdGXYContext;
import com.luciad.view.gxy.ILcdGXYPainter;
import com.luciad.view.gxy.ILcdGXYPen;
import com.luciad.view.gxy.ILcdGXYViewXYWorldTransformation;

import java.awt.Color;
import java.awt.Graphics;

/**
 * A raster painter for rasters resulting from line-of-sight computations.
 */
class LOSRasterPainter extends TLcdRasterPainter {

  private static TLcdDTEDColorModelFactory sColorModelFactory            = new LOSColorModelFactory( LOSPainter.getLosLevelsAll(), LOSPainter.getLosColorsAll() );
  private static TLcdDTEDColorModelFactory sColorModelFactoryFixedHeight = new LOSColorModelFactory( LOSPainter.getLosLevelsFixedHeight(), LOSPainter.getLosColorsFixedHeight() );

  private double fRasterStartResolution = 1000;
  private double fRasterStopResolution = 0;

  LOSRasterPainter() {
    setOutlineColor( Color.cyan );
    setStartResolutionFactor( fRasterStartResolution );
    setStopResolutionFactor( fRasterStopResolution );
    setColorModel( sColorModelFactory.createColorModel() );

    // Set additional properties to enhance the performance.
    setUseDeferredSubTileDecoding( true );
    setUseSubTileImageCaching    ( true );
  }

  public void paint( Graphics aGraphics, int aMode, ILcdGXYContext aGXYContext ) {
    super.paint( aGraphics, aMode, aGXYContext );
    if ( ( aMode & ILcdGXYPainter.SELECTED ) != 0 ) {
      // draw a red line around the selected raster, so that we can see that it is selected.
      // in the sample we will then be able to delete it.
      ILcdBounded bounded = (ILcdBounded) getObject();
      ILcdBounds bounds = bounded.getBounds();
      ILcd2DEditablePoint point = bounds.getLocation().cloneAs2DEditablePoint();
      ILcdGXYPen pen = aGXYContext.getGXYPen();

      ILcdModelXYWorldTransformation mwt = aGXYContext.getModelXYWorldTransformation();
      ILcdGXYViewXYWorldTransformation vwt = aGXYContext.getGXYViewXYWorldTransformation();
      aGraphics.setColor( Color.red );
      boolean start_point_in_bounds;
      // we check for every line of the raster bounds whether we can paint it.
      try {
        pen.moveTo( bounds.getLocation(), mwt, vwt );
        start_point_in_bounds = true;
      } catch (TLcdOutOfBoundsException e) {
        start_point_in_bounds = false;
      }
      point.move2D( bounds.getLocation().getX() + bounds.getWidth(), bounds.getLocation().getY() );
      try {
        if ( start_point_in_bounds ) {
          pen.drawLineTo( point, mwt, vwt, aGraphics );
        }
        else {
          pen.moveTo( point, mwt, vwt );
          start_point_in_bounds = true;
        }
      } catch (TLcdOutOfBoundsException e) {
        start_point_in_bounds = false;
      }
      point.move2D( bounds.getLocation().getX() + bounds.getWidth(), bounds.getLocation().getY() + bounds.getHeight() );
      try {
        if ( start_point_in_bounds ) {
          pen.drawLineTo( point, mwt, vwt, aGraphics );
        }
        else {
          pen.moveTo( point, mwt, vwt );
          start_point_in_bounds = true;
        }
      } catch (TLcdOutOfBoundsException e) {
        start_point_in_bounds = false;
      }
      point.move2D( bounds.getLocation().getX(), bounds.getLocation().getY() + bounds.getHeight() );
      try {
        if ( start_point_in_bounds ) {
          pen.drawLineTo( point, mwt, vwt, aGraphics );
        }
        else {
          pen.moveTo( point, mwt, vwt );
          start_point_in_bounds = true;
        }
      } catch (TLcdOutOfBoundsException e) {
        start_point_in_bounds = false;
      }
      point.move2D( bounds.getLocation().getX(), bounds.getLocation().getY() );
      try {
        if ( start_point_in_bounds ) {
          pen.drawLineTo( point, mwt, vwt, aGraphics );
        }
        else {
          pen.moveTo( point, mwt, vwt );
        }
      } catch (TLcdOutOfBoundsException e) {
        // we do not draw the line.
      }
    }
  }

  // Change the computation algorithm, adjust the color settings.
  public void setComputationAlgorithm( int aComputationAlgorithm ) {
    switch ( aComputationAlgorithm ) {
      case LOSPanel.PROPAGATION_FUNCTION_FIXED_HEIGHT:
        setColorModel( sColorModelFactoryFixedHeight.createColorModel() );
        break;
      default:
        setColorModel( sColorModelFactory.createColorModel() );
        break;
    }
  }

  private static class LOSColorModelFactory extends TLcdDTEDColorModelFactory {
    public LOSColorModelFactory( double[] aLevels, Color[] aColors ) {
      // the numer of levels should be one less than the number of colors.
      double[] levels = new double[ aLevels.length ];
      System.arraycopy( aLevels, 0, levels, 0, aLevels.length );
      setLevels( levels );

      Color[] colors = new Color[ aColors.length + 1 ];
      System.arraycopy( aColors, 0, colors, 0, aColors.length );
      colors[ colors.length - 1 ] = aColors[ aColors.length - 1 ];
      setColors( colors );
    }
  }

}
