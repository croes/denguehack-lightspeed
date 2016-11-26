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
package samples.tea.lightspeed.los.model;

import com.luciad.geodesy.ILcdEllipsoid;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.shape2D.TLcdLonLatArcBand;
import com.luciad.shape.shape2D.TLcdLonLatPoint;

/**
 * <p>
 * This class represents the input shape used for LOS coverages in this
 * sample. The shape can be edited to modify the input parameters of the
 * LOS calculation visually. The shape can be painted with a regular
 * shape painter, and edited with a regular shape editor.
 * </p>
 */
public class LOSCoverageInputShape extends TLcdLonLatArcBand {

  private static final int MINIMUM_ANGLE = 10;
  private static final double MAXIMUM_RADIUS = 10000;
  private double fCenterPointAltitude;
  private double fRadiusStep;
  private double fAngleStep;
  private boolean fAllowResize;
  private double fMinVerticalAngle;
  private double fMaxVerticalAngle;



  public LOSCoverageInputShape( LOSCoverageInputShape aInputShape ) {
    this( ( ILcdPoint ) aInputShape.getCenter().clone(),
          aInputShape.getMaxRadius(),
          aInputShape.getRadiusStep(),
          aInputShape.getStartAngle(),
          aInputShape.getArcAngle(),
          aInputShape.getAngleStep(),
          aInputShape.getMinVerticalAngle(),
          aInputShape.getMaxVerticalAngle(),
          aInputShape.getEllipsoid(),
          aInputShape.isAllowResize());
  }

  public LOSCoverageInputShape( ILcdPoint aCenter, double aMaxRadius, double aRadiusStep, double aStartAngle, double aArcAngle, double aAngleStep, double aMinVerticalAngle, double aMaxVerticalAngle, ILcdEllipsoid aEllipsoid ) {
    this( aCenter, aMaxRadius, aRadiusStep, aStartAngle, aArcAngle, aAngleStep, aMinVerticalAngle, aMaxVerticalAngle, aEllipsoid, true );
  }
  
  public LOSCoverageInputShape( ILcdPoint aCenter, double aMaxRadius, double aRadiusStep, double aStartAngle, double aArcAngle, double aAngleStep, double aMinVerticalAngle, double aMaxVerticalAngle, ILcdEllipsoid aEllipsoid, boolean aAllowResize ) {
    super( aCenter,0, aMaxRadius,aStartAngle, aArcAngle );
    fAllowResize = aAllowResize;
    fCenterPointAltitude = 10.0;
    fRadiusStep = aRadiusStep;
    fAngleStep = aAngleStep;
    fMinVerticalAngle = aMinVerticalAngle;
    fMaxVerticalAngle = aMaxVerticalAngle;
  }

  @Override
  public void setMaxRadius( double aRadius ) {
    if ( fAllowResize ) {
      if ( aRadius<MAXIMUM_RADIUS ) {
        super.setMaxRadius( aRadius );
      }else{
        super.setMaxRadius( MAXIMUM_RADIUS );
      }
    }
  }

  @Override public void setMinRadius( double aMinRadius ) {
    //do not allow modifying the minimum radius
  }

  public double getRadiusStep() {
    return fRadiusStep;
  }

  public void setRadiusStep( double aRadiusStep ) {
    fRadiusStep = aRadiusStep;
  }

  public double getCenterPointHeightOffset() {
    return fCenterPointAltitude;
  }

  public void setCenterPointHeightOffset( double aCenterPointAltitude ) {
    fCenterPointAltitude = aCenterPointAltitude;
  }

  public double getAngleStep() {
    return fAngleStep;
  }

  public void setAngleStep( double aAngleStep ) {
    fAngleStep = aAngleStep;
  }

  public boolean isAllowResize() {
    return fAllowResize;
  }

  public double getMinVerticalAngle() {
    return fMinVerticalAngle;
  }

  public void setMinVerticalAngle( double aMinVerticalAngle ) {
    fMinVerticalAngle = aMinVerticalAngle;
  }

  public double getMaxVerticalAngle() {
    return fMaxVerticalAngle;
  }

  public void setMaxVerticalAngle( double aMaxVerticalAngle ) {
    fMaxVerticalAngle = aMaxVerticalAngle;
  }

  @Override
  public void moveCornerPoint2D( int aCorner, double aX, double aY ) {
    super.moveCornerPoint2D( aCorner, aX, aY );
    if(getArcAngle()< MINIMUM_ANGLE ){
      if ( aCorner==MAX_RADIUS_END_CORNER ) {
        TLcdLonLatPoint otherPoint = new TLcdLonLatPoint();
        corner2DEditablePointSFCT( MAX_RADIUS_START_CORNER, otherPoint );
        super.moveCornerPoint2D( aCorner, otherPoint.getX(), otherPoint.getY() );
        setArcAngle( 360 );
      }
      else {
        TLcdLonLatPoint otherPoint = new TLcdLonLatPoint();
        corner2DEditablePointSFCT( MIN_RADIUS_END_CORNER, otherPoint );
        super.moveCornerPoint2D( MAX_RADIUS_START_CORNER, otherPoint.getX(), otherPoint.getY());
        setArcAngle( 360 );
      }
    }
    if(getMaxRadius()>MAXIMUM_RADIUS){
      setMaxRadius( MAXIMUM_RADIUS );
    }
  }

  @Override public Object clone() {
    return new LOSCoverageInputShape( this );
  }
}
