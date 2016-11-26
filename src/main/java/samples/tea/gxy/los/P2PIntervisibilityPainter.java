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

import com.luciad.shape.ILcdPointList;
import com.luciad.tea.ILcdPoint2PointIntervisibility;
import com.luciad.transformation.ILcdModelXYWorldTransformation;
import com.luciad.util.*;
import com.luciad.view.gxy.*;
import com.luciad.view.gxy.painter.TLcdGXYPointListPainter;

import java.awt.*;

/**
 * <p>
 * An extension of <code>TLcdGXYPointListPainter</code> to paint objects of
 * type <code>P2PIntervisibility</code>.
 * </p>
 * <p>
 * The radar intervisibility is represented by a line between the two points.
 * This line is colored green for intervisible points, orange for an unknown
 * intervisibility and red for points which are not intervisible.
 * </p>
 * <p>
 * The sky in background intervisibility is represented by an arrow head in the
 * direction of the second point. This arrow is colored green if the points are
 * visible with sky in background, orange if the sky in background result is not
 * certain and red if the points are not visible with sky in background.
 * </p>
 */
class P2PIntervisibilityPainter extends TLcdGXYPointListPainter {

  private ILcdGXYPainterStyle fVisibleColorStyle    = new TLcdGXYPainterColorStyle( Color.green,  Color.green  );
  private ILcdGXYPainterStyle fUnknownColorStyle    = new TLcdGXYPainterColorStyle( Color.orange, Color.orange );
  private ILcdGXYPainterStyle fNonvisibleColorStyle = new TLcdGXYPainterColorStyle( Color.red,    Color.red    );

  private transient ILcdAWTPath fAccumulatedAWTPath = new TLcdAWTPath();

  private int[] fArrowX = new int[3];
  private int[] fArrowY = new int[3];

  public P2PIntervisibilityPainter() {
    super( TLcdGXYPointListPainter.POLYLINE );
  }

  public void paint( Graphics aGraphics, int aMode, ILcdGXYContext aGXYContext ) {

    Object object = getObject();
    if ( !(object instanceof P2PIntervisibility) ) {
      return;
    }

    ILcdGXYPen pen = aGXYContext.getGXYPen();
    ILcdModelXYWorldTransformation   mwt = aGXYContext.getModelXYWorldTransformation();
    ILcdGXYViewXYWorldTransformation vwt = aGXYContext.getGXYViewXYWorldTransformation();

    try {
      // Draw a line to represent the point-to-point visibility.
      super.setLineStyle( findColorStyle( ( (P2PIntervisibility) object ).getP2PIntervisibilityRadar() ) );
      super.setupGraphicsForLine( aGraphics, aMode, aGXYContext );

      ILcdPointList point_list = (ILcdPointList) object;
      pen.moveTo      ( point_list.getPoint( 0 ), mwt, vwt );
      pen.appendLineTo( point_list.getPoint( 1 ), mwt, vwt, fAccumulatedAWTPath );
      fAccumulatedAWTPath.drawPolyline( aGraphics );
      // Line drawn.

      // Draw an arrow to represent the point-to-point visibility with sky in background.
      super.setLineStyle( findColorStyle( ( (P2PIntervisibility) object ).getP2PIntervisibilitySkyBackground() ) );
      super.setupGraphicsForLine( aGraphics, aMode, aGXYContext );

      int m_index = fAccumulatedAWTPath.subPathLength( 0 ) - 2;
      if(m_index >= 0) {
        int X1 = fAccumulatedAWTPath.getX( 0, m_index );
        int Y1 = fAccumulatedAWTPath.getY( 0, m_index );
        int X2 = fAccumulatedAWTPath.getX( 0, m_index + 1 );
        int Y2 = fAccumulatedAWTPath.getY( 0, m_index + 1 );
        if(Math.abs(X2 - X1) > 2 || Math.abs(Y2 - Y1) > 2) {
          drawArrow(
                  X1, Y1,
                  X2, Y2,
                  35,   // arrow angle (degrees)
                  20,   // arrow length (pixels)
                  aGraphics
          );
        }
        // Arrow drawn.
      }

      // Reset path.
      fAccumulatedAWTPath.reset();
    }
    catch ( TLcdOutOfBoundsException e ) {
      // out of bounds, do not draw anything.
    }
  }

  private ILcdGXYPainterStyle findColorStyle( ILcdPoint2PointIntervisibility aPoint2PointIntervisibility ) {
    if ( !aPoint2PointIntervisibility.isIntervisibilityCertain() ) {
      return fUnknownColorStyle;
    }
    return aPoint2PointIntervisibility.isIntervisible() ? fVisibleColorStyle : fNonvisibleColorStyle;
  }

  private void drawArrow( int X1, int Y1,
                          int X2, int Y2,
                          double   aAngle, 
                          int      aLength,
                          Graphics aGraphics ) {
    double angle = Math.atan2( Y2 - Y1, X2 - X1 );
    double ang1 = angle - aAngle / 2.0 * TLcdConstant.DEG2RAD;
    double ang2 = angle + aAngle / 2.0 * TLcdConstant.DEG2RAD;
    float dx1 = (float)( aLength * Math.cos( ang1 ) );
    float dy1 = (float)( aLength * Math.sin( ang1 ) );
    float dx2 = (float)( aLength * Math.cos( ang2 ) );
    float dy2 = (float)( aLength * Math.sin( ang2 ) );
    fArrowX[0] = Math.round( X2 - dx1 );
    fArrowY[0] = Math.round( Y2 - dy1 );
    fArrowX[1] = Math.round( X2 - dx2 );
    fArrowY[1] = Math.round( Y2 - dy2 );
    fArrowX[2] = Math.round( X2 );
    fArrowY[2] = Math.round( Y2 );
    aGraphics.fillPolygon( fArrowX, fArrowY, 3 );
  }

}
