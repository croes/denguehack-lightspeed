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

import com.luciad.contour.TLcdIntervalContour;
import com.luciad.contour.TLcdValuedContour;
import com.luciad.shape.ILcdPointList;
import com.luciad.shape.ILcdShape;
import com.luciad.view.gxy.TLcdGXYPainterColorStyle;
import com.luciad.view.gxy.painter.TLcdGXYPointListPainter;

import java.awt.Color;
import java.util.HashMap;

/**
 * A painter for complex polygon contours resulting from line-of-sight computations.
 */
class LOSContourPainter extends TLcdGXYPointListPainter {
  HashMap<Double, Color> fColors = new HashMap<Double, Color>();

  /**
   * Creates a new LOSContourPainter
   *
   * @param aValues Values from the objects this painter recognises. There must be a much values
   *                as colors.
   * @param aColors Colors to give to each value. Order of colors and corresponding values should
   *                match.
   */
  LOSContourPainter( double[] aValues, Color[] aColors ) {
    setMode( FILLED );
    setLineStyle( new TLcdGXYPainterColorStyle( Color.black ) );
    setColors( aValues, aColors );
  }

  /**
   * Creates a new LOSContourPainter
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
   */
  LOSContourPainter( double[] aIntervalValues, Color[] aIntervalColors, double[] aSpecialValues, Color[] aSpecialColors ) {
    setMode( OUTLINED_FILLED );
    setLineStyle( new TLcdGXYPainterColorStyle( Color.black ) );
    setColors( aIntervalValues, aIntervalColors, aSpecialValues, aSpecialColors );
  }

  public void setColors( double[] aIntervalValues, Color[] aIntervalColors, double[] aSpecialValues, Color[] aSpecialColors ) {
    Color[] colors = new Color[aIntervalColors.length + aSpecialColors.length];
    System.arraycopy( aIntervalColors, 0, colors, 0, aIntervalColors.length );
    System.arraycopy( aSpecialColors, 0, colors, aIntervalColors.length, aSpecialColors.length );
    double[] values = new double[aIntervalValues.length + aSpecialValues.length - 1];
    System.arraycopy( aIntervalValues, 0, values, 0, aIntervalValues.length - 1 );
    System.arraycopy( aSpecialValues, 0, values, aIntervalValues.length - 1, aSpecialValues.length );

    setColors( values, colors );
  }

  public void setColors( double[] aValues, Color[] aColors ) {
    for ( int i = 0; i < aValues.length; i++ ) {
      fColors.put( aValues[ i ], aColors[i] );
    }
  }

  @Override
  public void setObject( Object aObject ) {
    Color color;
    ILcdShape shape;

    if ( aObject instanceof TLcdValuedContour ) {
      shape = ( ( TLcdValuedContour ) aObject ).getShape();
      color = fColors.get( ( ( TLcdValuedContour ) aObject ).getValue() );
    }
    else {
      shape = ( ( TLcdIntervalContour ) aObject ).getShape();
      color = fColors.get( ( ( TLcdIntervalContour ) aObject ).getInterval().getMin() );
    }

    super.setObject( shape );

    if ( color != null ) {
      setFillStyle( new TLcdGXYPainterColorStyle( color ) );
    }
  }

  public boolean canSetObject( Object aObject ) {
    return ( aObject instanceof TLcdValuedContour && ( ( TLcdValuedContour ) aObject ).getShape() instanceof ILcdPointList )
           || ( aObject instanceof TLcdIntervalContour && ( ( TLcdIntervalContour ) aObject ).getShape() instanceof ILcdPointList );
  }
}
