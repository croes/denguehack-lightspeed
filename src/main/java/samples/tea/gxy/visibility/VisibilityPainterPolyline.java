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
package samples.tea.gxy.visibility;

import com.luciad.geodesy.ILcdEllipsoid;
import com.luciad.model.ILcdModelReference;
import com.luciad.shape.ILcdBounds;
import com.luciad.shape.shape2D.*;
import com.luciad.tea.*;
import com.luciad.transformation.ILcdModelXYWorldTransformation;
import com.luciad.util.TLcdNoBoundsException;
import com.luciad.view.gxy.*;
import com.luciad.view.gxy.painter.TLcdGXYPointListPainter;

import java.awt.*;

/**
 * The painter used to paint to-polyline intervisibility results.
 */
class VisibilityPainterPolyline extends ALcdGXYPainter implements ILcdGXYPainter, ILcdGXYPainterProvider {

  private TLcdGXYPointListPainter fPolylinePainter = new TLcdGXYPointListPainter( TLcdGXYPointListPainter.POLYLINE );

  private TLcdG2DLineStyle fLineStyleInvisible = new TLcdG2DLineStyle();
  private TLcdG2DLineStyle fLineStyleUncertain = new TLcdG2DLineStyle();
  private TLcdG2DLineStyle fLineStyleVisible   = new TLcdG2DLineStyle();

  private ILcdVisibilityMatrixView fObject;
  private ILcdBounds               fObjectBounds;

  private transient TLcdLonLatPoint fTempLonLatPoint1 = new TLcdLonLatPoint();
  private transient TLcdLonLatPoint fTempLonLatPoint2 = new TLcdLonLatPoint();
  private transient TLcdLonLatPoint fTempLonLatPoint3 = new TLcdLonLatPoint();
  private transient TLcdXYBounds    fTempXYBounds     = new TLcdXYBounds();

  public VisibilityPainterPolyline() {
    fLineStyleInvisible.setLineWidth( 2 );
    fLineStyleInvisible.setColor( Color.red );

    fLineStyleUncertain.setLineWidth( 2 );
    fLineStyleUncertain.setColor( Color.orange );

    fLineStyleVisible.setLineWidth( 2 );
    fLineStyleVisible.setColor( Color.green );
  }

  public void boundsSFCT( Graphics aGraphics, int aMode, ILcdGXYContext aGXYContext, ILcd2DEditableBounds aBoundsSFCT ) throws TLcdNoBoundsException {
    if ( fObject == null ) {
      return;
    }
    ILcdModelXYWorldTransformation   mwt = aGXYContext.getModelXYWorldTransformation();
    ILcdGXYViewXYWorldTransformation vwt = aGXYContext.getGXYViewXYWorldTransformation();
    mwt.modelBounds2worldSFCT( fObjectBounds, fTempXYBounds );
    vwt.worldBounds2viewXYSFCT( fTempXYBounds, aBoundsSFCT );
  }

  public Object getObject() {
    return fObject;
  }

  public boolean isTouched( Graphics aGraphics, int aMode, ILcdGXYContext aGXYContext ) {
    return false;
  }

  public void paint( Graphics aGraphics, int aMode, ILcdGXYContext aGXYContext ) {
    if ( fObject == null || fObject.getRowCount() < 2 ) {
      return;
    }

    double value1  = fObject.getValue( 0, 0 );
    double point_x = fObject.retrieveAssociatedPointX( 0, 0 );
    double point_y = fObject.retrieveAssociatedPointY( 0, 0 );
    fTempLonLatPoint1.move2D( point_x, point_y );

    for ( int i = 1 ; i < fObject.getRowCount() ; i++ ) {
      point_x = fObject.retrieveAssociatedPointX( 0, i );
      point_y = fObject.retrieveAssociatedPointY( 0, i );
      fTempLonLatPoint2.move2D( point_x, point_y );

      double value2 = fObject.getValue( 0, i );
      if ( value2 == value1 ) {
        // Visibility value not changed.
        // Paint line with line style of the value of the second point.
        fPolylinePainter.setObject( new TLcdLonLatLine( fTempLonLatPoint1, fTempLonLatPoint2 ) );
        fPolylinePainter.setLineStyle( findLineStyle( value2 ) );
        fPolylinePainter.paint( aGraphics, aMode, aGXYContext );
      }
      else {
        // Visibility value changed, therefore compute the point in the
        // middle of the line between the first and the second point.
        final ILcdEllipsoid ellipsoid = fObject.getReference().getGeodeticDatum().getEllipsoid();
        ellipsoid.geodesicPointSFCT( fTempLonLatPoint1, fTempLonLatPoint2, 0.5, fTempLonLatPoint3 );

        // Paint line fTempLonLatPoint1 and fTempLonLatPoint3 with line style of the value of the first point.
        fPolylinePainter.setObject( new TLcdLonLatLine( fTempLonLatPoint1, fTempLonLatPoint3 ) );
        fPolylinePainter.setLineStyle( findLineStyle( value1 ) );
        fPolylinePainter.paint( aGraphics, aMode, aGXYContext );

        // Paint line fTempLonLatPoint1 and fTempLonLatPoint3 with line style of the value of the second point.
        fPolylinePainter.setObject( new TLcdLonLatLine( fTempLonLatPoint3, fTempLonLatPoint2 ) );
        fPolylinePainter.setLineStyle( findLineStyle( value2 ) );
        fPolylinePainter.paint( aGraphics, aMode, aGXYContext );
      }

      // Cache current values.
      fTempLonLatPoint1.move2D( fTempLonLatPoint2 );
      value1 = value2;
    }
  }

  public void setObject( Object aObject ) {
    // The object should be a 1xN matrix view otherwise this painter cannot it.
    if ( aObject instanceof ILcdVisibilityMatrixView && ( (ILcdVisibilityMatrixView) aObject ).getColumnCount() == 1 ) {
      fObject       = (ILcdVisibilityMatrixView) aObject;
      fObjectBounds = updateObjectBounds();
    }
    else {
      fObject       = null;
      fObjectBounds = null;
    }
  }

  /**
   * Finds the line style for the given visibility value.
   * @param aVisibilityValue The visibility value to query.
   * @return the line style for the given visibility value.
   */
  private ILcdGXYPainterStyle findLineStyle( double aVisibilityValue ) {
    TLcdVisibilityDescriptor descriptor = fObject.getVisibilityDescriptor();
    if ( descriptor.isSpecialValue( aVisibilityValue ) ) {
      TLcdVisibilityInterpretation interpretation = descriptor.getSpecialValueInterpretation( aVisibilityValue );
      if ( interpretation == TLcdVisibilityInterpretation.INVISIBLE ) {
        return fLineStyleInvisible;
      }
      if ( interpretation == TLcdVisibilityInterpretation.UNCERTAIN ) {
        return fLineStyleUncertain;
      }
      if ( interpretation == TLcdVisibilityInterpretation.VISIBLE ) {
        return fLineStyleVisible;
      }
    }
    throw new IllegalArgumentException( "Invalid visibility value found!" );
  }

  /**
   * Returns the model bounds of the object set on this instance.
   * @return the model bounds of the object set on this instance.
   */
  private ILcdBounds updateObjectBounds() {
    if ( fObject == null || fObject.getRowCount() < 2 ) {
      return null;
    }

    ILcd2DEditableBounds bounds = ( (ILcdModelReference) fObject.getReference() )
            .makeModelPoint().getBounds().cloneAs2DEditableBounds();

    // Move to first point.
    bounds.move2D(
            fObject.retrieveAssociatedPointX( 0, 0 ),
            fObject.retrieveAssociatedPointY( 0, 0 )
    );

    // Include all matrix view points.
    for ( int i = 0 ; i < fObject.getRowCount() ; i++ ) {
      bounds.setToIncludePoint2D(
              fObject.retrieveAssociatedPointX( 0, i ),
              fObject.retrieveAssociatedPointY( 0, i )
      );
    }
    return bounds;
  }

}
