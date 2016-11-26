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
package samples.tea.gxy.hypsometry;

import com.luciad.earth.tileset.ILcdEarthTileSet;
import com.luciad.format.raster.TLcdJetIndexColorModelFactory;
import com.luciad.shape.ILcdBounds;
import com.luciad.shape.shape2D.TLcdXYBounds;
import com.luciad.tea.hypsometry.ILcdHypsometricValueProvider;
import com.luciad.tea.hypsometry.TLcdHypsometricEarthPainter;
import com.luciad.transformation.ILcdModelXYWorldTransformation;
import com.luciad.util.TLcdNoBoundsException;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.ILcdGXYView;
import com.luciad.view.gxy.TLcdGXYLayer;

import java.awt.Color;
import java.awt.image.ColorModel;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;

/**
 * Class that contains some utility code for the hypsometry sample
 */
class HypsometryUtil {

  static final int MINIMUM_HYPSOMETRIC_VALUE = 0;
  static final int MAXIMUM_HYPSOMETRIC_VALUE = 254;
  static final int UNKNOWN_HYPSOMETRIC_VALUE = 255;

  ///////////////////////////////////////////////////////////////////////////
  // COLOR MODELS
  ///////////////////////////////////////////////////////////////////////////

  /**
   * Creates a color model that is suitable for displaying azimuths.
   * The color model maps the circle of azimuths on the circle of hues
   * (cyan to magenta, to yellow, to cyan again).
   */
  static ColorModel createAzimuthColorModel() {
    TLcdJetIndexColorModelFactory factory = new TLcdJetIndexColorModelFactory();
    factory.setBasicColor( MAXIMUM_HYPSOMETRIC_VALUE * 0 / 3, Color.cyan );
    factory.setBasicColor( MAXIMUM_HYPSOMETRIC_VALUE * 1 / 3, Color.magenta );
    factory.setBasicColor( MAXIMUM_HYPSOMETRIC_VALUE * 2 / 3, Color.yellow );
    factory.setBasicColor( MAXIMUM_HYPSOMETRIC_VALUE * 3 / 3, Color.cyan );
    factory.setTransparentIndex( UNKNOWN_HYPSOMETRIC_VALUE );

    return factory.createColorModel();
  }


  /**
   * Creates a color model that is suitable for displaying orientations.
   * The color model maps orientations to the North (the reference direction)
   * on blue, and orientations to the South on red.
   */
  static ColorModel createOrientationColorModel() {

    TLcdJetIndexColorModelFactory factory = new TLcdJetIndexColorModelFactory();
    factory.setBasicColor( MINIMUM_HYPSOMETRIC_VALUE, Color.red );
    factory.setBasicColor( MAXIMUM_HYPSOMETRIC_VALUE, Color.blue );
    factory.setTransparentIndex( UNKNOWN_HYPSOMETRIC_VALUE );

    return factory.createColorModel();
  }


  /**
   * Creates a color model that is suitable for displaying ridges and valleys.
   * The color model maps ridges to red and valleys to blue.
   */
  static ColorModel createRidgeValleyColorModel() {

    TLcdJetIndexColorModelFactory factory = new TLcdJetIndexColorModelFactory();
    factory.setBasicColor( MAXIMUM_HYPSOMETRIC_VALUE * 0 / 2, Color.red );
    factory.setBasicColor( MAXIMUM_HYPSOMETRIC_VALUE * 1 / 2, Color.white );
    factory.setBasicColor( MAXIMUM_HYPSOMETRIC_VALUE * 2 / 2, Color.blue );
    factory.setTransparentIndex( UNKNOWN_HYPSOMETRIC_VALUE );

    return factory.createColorModel();
  }


  /**
   * Creates a color model that is suitable for displaying slopes.
   * The color model maps horizontal areas to white and increasingly steep
   * areas to shades of grey.
   */
  static ColorModel createSlopeAngleColorModel() {

    TLcdJetIndexColorModelFactory factory = new TLcdJetIndexColorModelFactory();
    factory.setBasicColor( MAXIMUM_HYPSOMETRIC_VALUE * 0 / 90, Color.green );
    factory.setBasicColor( MAXIMUM_HYPSOMETRIC_VALUE * 5 / 90, Color.yellow );
    factory.setBasicColor( MAXIMUM_HYPSOMETRIC_VALUE * 10 / 90, Color.orange );
    factory.setBasicColor( MAXIMUM_HYPSOMETRIC_VALUE * 20 / 90, Color.red );
    factory.setBasicColor( MAXIMUM_HYPSOMETRIC_VALUE * 40 / 90, Color.black );
    factory.setBasicColor( UNKNOWN_HYPSOMETRIC_VALUE, new Color( 0f, 0f, 0f, 0f ) );

    return factory.createColorModel();
  }


  /**
   * Creates a color model that is suitable for displaying shading.
   * All colors are black, with partial opacity for slopes that are
   * oriented away from the reference direction, up to full transparency
   * or slopes that are oriented in the reference direction.
   */
  static ColorModel createShadingColorModel() {

    TLcdJetIndexColorModelFactory factory = new TLcdJetIndexColorModelFactory();
    factory.setBasicColor( MINIMUM_HYPSOMETRIC_VALUE, new Color( 0f, 0f, 0f, 0.5f ) );
    factory.setBasicColor( MAXIMUM_HYPSOMETRIC_VALUE, new Color( 0f, 0f, 0f, 0f ) );
    factory.setBasicColor( UNKNOWN_HYPSOMETRIC_VALUE, new Color( 0f, 0f, 0f, 0f ) );

    return factory.createColorModel();
  }

  ///////////////////////////////////////////////////////////////////////////
  // TRANSFORMATIONS
  ///////////////////////////////////////////////////////////////////////////

  /**
   * Computes the elevation scale needed for some hypsometric function calls.
   * @param aModelBounds the bounds of the model
   * @param aView        the view
   * @param aElevationLayer the layer that contains elevation information
   * @return the elevation scale
   */
  static double computeElevationScale( ILcdBounds aModelBounds, ILcdGXYView aView, ILcdGXYLayer aElevationLayer ) {
    // Compute a rough estimate of scale factors for computing the normals,
    // based on the transformed bounds. This estimate is assuming an
    // axis-aligned rectangle in model coordinates approximately remains
    // an axis-aligned rectangle in world coordinates.
    TLcdXYBounds worldBounds = new TLcdXYBounds();

    try {
      ILcdModelXYWorldTransformation mwt = createModelXYWorldTransformation( aView, aElevationLayer );
      mwt.modelBounds2worldSFCT( aModelBounds, worldBounds );

      return Math.sqrt( ( aModelBounds.getWidth() * aModelBounds.getHeight() ) /
                        ( worldBounds.getWidth() * worldBounds.getHeight() ) );
    }
    catch ( TLcdNoBoundsException aException ) {
      // use default elevation scale (1.0)
      return 1.0;
    }
  }

  /**
   * Creates a model to world transformation
   *
   * @param aGXYView  a view
   * @param aGXYLayer a layer
   *
   * @return a model to world transformation
   */
  static ILcdModelXYWorldTransformation createModelXYWorldTransformation( ILcdGXYView aGXYView, ILcdGXYLayer aGXYLayer ) {
    try {
      ILcdModelXYWorldTransformation mwt = ( ILcdModelXYWorldTransformation ) aGXYLayer.getModelXYWorldTransfoClass().newInstance();
      mwt.setModelReference( aGXYLayer.getModel().getModelReference() );
      mwt.setXYWorldReference( aGXYView.getXYWorldReference() );
      return mwt;
    } catch ( InstantiationException aException ) {
      // Exception found, throw runtime exception.
      throw new RuntimeException( aException );
    } catch ( IllegalAccessException aException ) {
      // Exception found, return null.
      throw new RuntimeException( aException );
    }
  }

  ///////////////////////////////////////////////////////////////////////////
  // EARTH TILESETS
  ///////////////////////////////////////////////////////////////////////////

  /**
   * Creates a container for a earth tileset-based layer. The container contains
   * a reference to the painter (it it is a TLcdHypsometricEarthPainter) and a
   * reference to the ILcdEarthTileSet.
   *
   * @param aLayer a layer containing a reference to the TLcdHypsometricEarthPainter and
   *               the ILcdEarthTileSet used by the layer.
   *
   * @return a HypsometricEarthTileContext, or null if the layer does not
   *         contain a hypsometric earth painter
   */
  static HypsometryEarthTileContext createHypsometricEarthTileSetContext( ILcdGXYLayer aLayer ) {
    if ( aLayer instanceof TLcdGXYLayer &&
         ( ( TLcdGXYLayer ) aLayer ).getGXYPainterProvider() instanceof TLcdHypsometricEarthPainter &&
         aLayer.getModel().elements().hasMoreElements() &&
         aLayer.getModel().elements().nextElement() instanceof ILcdEarthTileSet ) {
      return new HypsometryEarthTileContext( ( ILcdEarthTileSet ) aLayer.getModel().elements().nextElement(),
                                             ( TLcdHypsometricEarthPainter ) ( ( TLcdGXYLayer ) aLayer ).getGXYPainterProvider() );
    }
    return null;
  }

  ///////////////////////////////////////////////////////////////////////////
  // FORMATS
  ///////////////////////////////////////////////////////////////////////////

  /**
   * Creates a hypsometric format, capable of formatting hypsometric values
   * @param aHypsometricValueProvider a hypsometric value provider
   * @param aDelegateFormat           a delegate format to delegate the final result to
   * @return A format
   */
  static Format createHypsometricFormat( ILcdHypsometricValueProvider aHypsometricValueProvider, Format aDelegateFormat ) {
    double factor  = ( aHypsometricValueProvider.getMaximumValue() - aHypsometricValueProvider.getMinimumValue() ) / 254d;
    double minimum = aHypsometricValueProvider.getMinimumValue();
    return new ConvertToHypsometricValueFormat( aDelegateFormat, minimum, factor );
  }


  ///////////////////////////////////////////////////////////////////////////
  // INNER CLASSES
  ///////////////////////////////////////////////////////////////////////////

  /**
   * A format that converts a hypsometric value to a different value by
   * offsetting and scaling it. The final result is delegated to a different
   * format.
   */
  static class ConvertToHypsometricValueFormat extends Format {
    private final Format fFormat;
    private final double fMinimum;
    private final double fFactor;

    ConvertToHypsometricValueFormat( Format aFormat, double aMinimum, double aFactor ) {
      fFormat = aFormat;
      fMinimum = aMinimum;
      fFactor = aFactor;
    }

    public StringBuffer format( Object object, StringBuffer toAppendTo, FieldPosition pos ) {
      if ( object instanceof Number && ( ( Number ) object ).doubleValue() != UNKNOWN_HYPSOMETRIC_VALUE ) {
        object = fMinimum + ( ( Number ) object ).doubleValue() * fFactor;
        return fFormat.format( object, toAppendTo, pos );
      }
      return new StringBuffer( "Unknown" );
    }

    public Object parseObject( String source, ParsePosition pos ) {
      throw new UnsupportedOperationException( "Currently not supported!" );
    }
  }

  /**
   * A format that converts input to degrees
   */
  static class ConvertToDegreeFormat extends Format {
    private final Format fFormat;

    ConvertToDegreeFormat( Format aFormat ) {
      fFormat = aFormat;
    }

    public StringBuffer format( Object object, StringBuffer toAppendTo, FieldPosition pos ) {
      if ( object instanceof Number ) {
        object = Math.toDegrees( ( ( Number ) object ).doubleValue() );
      }
      return fFormat.format( object, toAppendTo, pos ).append( " \u00b0" );
    }

    public Object parseObject( String source, ParsePosition pos ) {
      throw new UnsupportedOperationException( "Currently not supported!" );
    }
  }

  /**
   * A simple immutable container that holds a painter and a tileset.
   */
  static class HypsometryEarthTileContext {
    private final ILcdEarthTileSet fEarthTileSet;
    private final TLcdHypsometricEarthPainter fGXYRasterPainter;

    HypsometryEarthTileContext( ILcdEarthTileSet aEarthTileSet, TLcdHypsometricEarthPainter aGXYRasterPainter ) {
      fEarthTileSet = aEarthTileSet;
      fGXYRasterPainter = aGXYRasterPainter;
    }

    ILcdEarthTileSet getEarthTileSet() {
      return fEarthTileSet;
    }

    TLcdHypsometricEarthPainter getGXYRasterPainter() {
      return fGXYRasterPainter;
    }
  }

}
