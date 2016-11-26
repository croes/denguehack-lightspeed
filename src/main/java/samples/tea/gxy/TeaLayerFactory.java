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
package samples.tea.gxy;

import java.awt.Color;
import java.util.HashMap;

import com.luciad.contour.TLcdIntervalContour;
import com.luciad.contour.TLcdValuedContour;
import com.luciad.format.raster.TLcdDTEDTileDecoder;
import com.luciad.gui.ILcdIcon;
import com.luciad.gui.TLcdSymbol;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelDescriptor;
import com.luciad.tea.ILcdLineOfSightCoverage;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.ILcdGXYLayerFactory;
import com.luciad.view.gxy.ILcdGXYPainter;
import com.luciad.view.gxy.ILcdGXYPainterProvider;
import com.luciad.view.gxy.TLcdG2DLineStyle;
import com.luciad.view.gxy.TLcdGXYLayer;
import com.luciad.view.gxy.TLcdGXYPainterColorStyle;
import com.luciad.view.gxy.painter.TLcdGXYIconPainter;
import com.luciad.view.gxy.painter.TLcdGXYPointListPainter;

import samples.gxy.decoder.MapSupport;

/**
 * This is an implementation of <code>ILcdGXYLayerFactory</code> to create a
 * layer for the objects created during Terrain Analysis.
 */
public class TeaLayerFactory implements ILcdGXYLayerFactory {

  // For point layer ...
  private TLcdGXYIconPainter fIconPainter = new TLcdGXYIconPainter();
  private ILcdIcon fPointIcon = new TLcdSymbol( TLcdSymbol.FILLED_TRIANGLE, 10, Color.white, Color.blue );
  private ILcdIcon fSnapIcon = new TLcdSymbol( TLcdSymbol.RECT, 12, Color.cyan );

  // For polyline layer ...
  private TLcdGXYPointListPainter fPolylinePainter = new TLcdGXYPointListPainter( TLcdGXYPointListPainter.POLYLINE );

  // For other layer ...
  /**
   * The color levels for line-of-sight.
   */
  public static double[] sColorLevels = new double[] {
          ILcdLineOfSightCoverage.LOS_UNKNOWN_VALUE,    // value used for unknown visibilities
          1, 50, 100, 250, 500
  };
  /**
   * The colors corresponding to different line-of-sight levels.
   */
  public static Color[] sColors = new Color[] {
          Color.blue,                                   // color used for unknown visibilities
          Color.red,
          new Color( 220, 100, 10 ),
          Color.yellow,
          Color.green,
          new Color( 20, 100, 30 )
  };

  public static final double[] sContourLevels = new double[]{
      ILcdLineOfSightCoverage.LOS_UNKNOWN_VALUE,    // value used for unknown visibilities
      0.01, 360.0, 700, 1050, 1400, 2100, 2800, 5000
  };
  public static final Color[] sContourColors = new Color[]{
      Color.blue,
      new Color( 140, 150, 210 ),
      new Color( 132, 178, 100 ),
      new Color( 237, 181, 79 ),
      new Color( 229, 108, 53 ),
      new Color( 154, 78, 46 ),
      new Color( 120, 58, 30 ),
      new Color( 90, 43, 22 ),
      new Color( 45, 20, 10 )
  };

  public static final double[] sContourLevelsInterval = new double[]{
      -20000, 0.01, 360.0, 700, 1050, 1400, 2100, 2800, 5000
  };
  public static final Color[] sContourColorsInterval = new Color[]{
      new Color( 140, 150, 210 ),
      new Color( 132, 178, 100 ),
      new Color( 237, 181, 79 ),
      new Color( 229, 108, 53 ),
      new Color( 154, 78, 46 ),
      new Color( 120, 58, 30 ),
      new Color( 90, 43, 22 ),
      new Color( 45, 20, 10 ),
  };

  public static final double[] sContourLevelsSpecial = new double[]{
      ILcdLineOfSightCoverage.LOS_UNKNOWN_VALUE,    // value used for unknown visibilities
  };
  public static final Color[] sContourColorsSpecial = new Color[]{
      Color.blue,
  };


  public TeaLayerFactory() {
    // For point layer ...
    fIconPainter.setIcon( fPointIcon );
    fIconPainter.setSnapIcon( fSnapIcon );

    // For polyline layer ...
    TLcdG2DLineStyle line_style = new TLcdG2DLineStyle();
    line_style.setColor( Color.blue );
    line_style.setSelectionColor( Color.red );
    line_style.setLineWidth( 2 );
    line_style.setSelectionLineWidth( 2 );
    fPolylinePainter.setLineStyle( line_style );
  }

  public ILcdGXYLayer createGXYLayer( ILcdModel aModel ) {
    ILcdModelDescriptor model_descriptor = aModel.getModelDescriptor();
    if ( "Point".equals( model_descriptor.getTypeName() ) ) {
      return createPointLayer( aModel );
    }
    if ( "Polyline".equals( model_descriptor.getTypeName() ) ) {
      return createPolylineLayer( aModel );
    }
    return createTEALayer( aModel );
  }

  private ILcdGXYLayer createPointLayer( ILcdModel aModel ) {
    TLcdGXYLayer layer = createLayer( aModel );
    layer.setGXYPainterProvider( fIconPainter );
    layer.setGXYEditorProvider( fIconPainter );
    return layer;
  }

  private ILcdGXYLayer createPolylineLayer( ILcdModel aModel ) {
    TLcdGXYLayer layer = createLayer( aModel );
    layer.setGXYPainterProvider( fPolylinePainter );
    layer.setGXYEditorProvider( fPolylinePainter );
    return layer;
  }

  // Creates a new <code>ILcdGXYLayer</code> for aModel. Adds specific painters
  // for the Terrain Analysis objects, <code>ILcdLineOfSightCoverage</code> and
  // <code>ILcdPoint2PointIntervisibility</code>
  private ILcdGXYLayer createTEALayer( ILcdModel aModel ) {
    TLcdGXYLayer layer = createLayer( aModel );
    layer.setGXYPainterProvider( new MyPainterProvider( layer.getLabel(), aModel ) );
    return layer;
  }

  private TLcdGXYLayer createLayer( ILcdModel aModel ) {
    TLcdGXYLayer layer = new TLcdGXYLayer() {
      public String toString() {
        return getLabel();
      }
    };
    layer.setModel( aModel );
    layer.setLabel( aModel.getModelDescriptor().getDisplayName() );
    layer.setGXYPen(MapSupport.createPen(aModel.getModelReference()));
    layer.setSelectable( true );
    layer.setEditable( true );
    return layer;
  }

  public static double[] getContourLevels() {
    return sContourLevels;
  }

  public static Color[] getContourColors() {
    return sContourColors;
  }

  public static double[] getContourLevelsInterval() {
    return sContourLevelsInterval;
  }

  public static Color[] getContourColorsInterval() {
    return sContourColorsInterval;
  }

  public static double[] getContourLevelsSpecial() {
    return sContourLevelsSpecial;
  }

  public static Color[] getContourColorsSpecial() {
    return sContourColorsSpecial;
  }

  static private class MyContourPainter extends TLcdGXYPointListPainter {
    Color[] fColors;
    HashMap<Double, Integer> fIndexes = new HashMap<Double, Integer>();
    int fAlpha;

    /**
     * Creates a new ColoredPolylinePainter
     *
     * @param aValues Values from the objects this painter recognises. There must be a much values
     *                as colors.
     * @param aColors Colors to give to each value. Order of colors and corresponding values should
     *                match.
     * @param aAlpha  transparency (255 for opaque)
     */
    MyContourPainter( double[] aValues, Color[] aColors, int aAlpha ) {
      setMode( OUTLINED_FILLED );
      setLineStyle( new TLcdGXYPainterColorStyle( Color.black ) );
      fAlpha = aAlpha;
      setColors( aValues, aColors );
    }

    /**
     * Creates a new ColoredPolylinePainter
     *
     * @param aIntervalValues Values from the objects this painter recognises. There must be one
     *                        more interval value than interval colors. The painter uses the min and
     *                        max interval value of TLcdIntervalContour objects to recognise which
     *                        color it needs.
     * @param aIntervalColors Colors to give to each value. Order of colors and corresponding values
     *                        should match.
     * @param aSpecialValues  Values from the objects this painter recognises. There must be a much
     *                        special values as special colors.
     * @param aSpecialColors  Colors to give to each value. Order of colors and corresponding values
     *                        should match.
     * @param aAlpha          transparency (255 for opaque)
     */
    MyContourPainter( double[] aIntervalValues, Color[] aIntervalColors, double[] aSpecialValues, Color[] aSpecialColors, int aAlpha ) {
      setMode( OUTLINED_FILLED );
      setLineStyle( new TLcdGXYPainterColorStyle( Color.black ) );
      fAlpha = aAlpha;
      setColors( aIntervalValues, aIntervalColors, aSpecialValues, aSpecialColors );
    }

    public void setColors( double[] aIntervalValues, Color[] aIntervalColors, double[] aSpecialValues, Color[] aSpecialColors ) {
      Color[] colors = new Color[aIntervalColors.length + aSpecialColors.length];
      for ( int i = 0; i < colors.length; i++ )
        colors[ i ] = i < aIntervalColors.length ? aIntervalColors[ i ] : aSpecialColors[ i - aIntervalColors.length ];
      double[] values = new double[aIntervalValues.length + aSpecialValues.length - 1];
      for ( int i = 0; i < values.length; i++ )
        values[ i ] = i < aIntervalValues.length - 1 ? aIntervalValues[ i ] : aSpecialValues[ i - aIntervalValues.length + 1 ];

      setColors( values, colors );
    }

    public void setColors( double[] aValues, Color[] aColors ) {
      fColors = aColors;
      for ( int i = 0; i < aValues.length; i++ ) {
        fIndexes.put( aValues[ i ], i );
      }
    }

    public void setAlpha( int aAlpha ) {
      fAlpha = aAlpha;
    }

    @Override
    public void setObject( Object aObject ) {

      Integer index;

      if ( aObject instanceof TLcdValuedContour ) {
        super.setObject( ( ( TLcdValuedContour ) aObject ).getShape() );
        index = fIndexes.get( ( ( TLcdValuedContour ) aObject ).getValue() );
      }
      else {
        super.setObject( ( ( TLcdIntervalContour ) aObject ).getShape() );
        index = fIndexes.get( ( ( TLcdIntervalContour ) aObject ).getInterval().getMin() );
      }

      Color c = index == null ? Color.red : fColors[ index ];
      if ( fAlpha != 255 ) c = new Color( c.getRed(), c.getGreen(), c.getBlue(), fAlpha );
      setFillStyle( new TLcdGXYPainterColorStyle( c ) );
    }
  }

  private static class MyPainterProvider implements ILcdGXYPainterProvider {
    private MyContourPainter fContourPainter;

    public MyPainterProvider( String aLayerName, ILcdModel aModel ) {
      fContourPainter = new MyContourPainter( getContourLevelsInterval(), getContourColorsInterval(), getContourLevelsSpecial(), getContourColorsSpecial(), 255 );
    }

    public ILcdGXYPainter getGXYPainter( Object aObject ) {
      fContourPainter.setObject( aObject );
      return fContourPainter;
    }

    public Object clone() {
      return this;
    }
  }

}
