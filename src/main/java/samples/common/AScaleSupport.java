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
package samples.common;

import java.awt.Point;

import com.luciad.geodesy.TLcdGeodeticDatum;
import com.luciad.geometry.cartesian.TLcdCartesian;
import com.luciad.reference.ILcdGeocentricReference;
import com.luciad.reference.ILcdGridReference;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.shape2D.TLcdXYPoint;
import com.luciad.shape.shape3D.ILcd3DEditablePoint;
import com.luciad.shape.shape3D.TLcdLonLatHeightPoint;
import com.luciad.shape.shape3D.TLcdXYZPoint;
import com.luciad.transformation.TLcdGeodetic2Grid;
import com.luciad.util.TLcdConstant;
import com.luciad.util.TLcdOutOfBoundsException;
import com.luciad.view.ILcdXYWorldReference;
import com.luciad.view.ILcdXYZWorldReference;

/**
 * A support class that converts map screen scales to map paper scales.
 * It supports two methods to calculate the scale: {@link #getMapCenterScale} and {@link #getMapProjectionOriginScale}.
 * Equivalent setters are available as well.
 */
public abstract class AScaleSupport {

  private final TLcdGeodeticReference WGS84_REFERENCE = new TLcdGeodeticReference(new TLcdGeodeticDatum());
  private static final int CENTER_SCALE_ITERATE_COUNT = 20;

  public enum ZoomOperation {
    ZOOM_IN,
    ZOOM_OUT
  }

  /**
   * Returns the scale of the view
   * @return the scale of the view
   */
  protected abstract double getScale();

  /**
   * Sets the scale of the view
   * @return the scale of the view
   */
  protected abstract void setScale(double aScale);

  /**
   * Returns the width of the view
   * @return the width of the view
   */
  protected abstract int getWidth();

  /**
   * Returns the height of the view
   * @return the height of the view
   */
  protected abstract int getHeight();

  /**
   * Returns the world reference of the view
   * @return the world reference of the view
   */
  protected abstract Object getWorldReference();

  /**
   * Transforms a view point to a world point
   * @param aViewPoint the view point
   * @param aWorldPointSFCT the corresponding world point
   */
  protected abstract void view2World(ILcdPoint aViewPoint, ILcd3DEditablePoint aWorldPointSFCT);

  /**
   * Converts the given internal scale (pixels / world unit) to a map scale
   * (meter / meter).
   *
   * @param aInternalScale The internal scale, in pixels per world unit (see
   *  {@link com.luciad.view.gxy.ILcdGXYView#getScale()} ).
   *
   * @param aScreenResolution The screen resolution, in dots per inch (dpi).
   *  Provide a negative value for the default screen resolution.
   *
   * @param aMapReference the world reference of the map, used to retrieve the world unit of measure.
   *                      Either ILcdXYWorldReference or ILcdXYZWorldReference. If null, the world unit is assumed to be 1 meter.
   *
   * @return The map scale, e.g. 1/13000.
   */
  public static double internalScale2MapScale(double aInternalScale, double aScreenResolution, Object aMapReference) {
    return getScaleAsPixelsPerMeter(aInternalScale, aMapReference) / dpi2dpm(aScreenResolution);
  }

  /**
   * Converts the given the given map scale (meter / meter) to internal scale
   * (pixels / world unit).
   *
   * @param aMapScale The map scale, in meter per meter, e.g. 1/13000.
   *
   * @param aScreenResolution The screen resolution, in dots per inch (dpi).
   *  Provide a negative value for the default screen resolution.
   *
   * @param aMapReference the world reference of the map, used to retrieve the world unit of measure.
   *                      Either ILcdXYWorldReference or ILcdXYZWorldReference. If null, the world unit is assumed to be 1 meter.
   *
   * @return The internal scale, in pixels per world unit (see
   *  {@link com.luciad.view.gxy.ILcdGXYView#getScale()} )
   */
  public static double mapScale2InternalScale(double aMapScale, double aScreenResolution, Object aMapReference) {
    return getScaleAsPixelsPerWorldUnit(aMapScale * dpi2dpm(aScreenResolution), aMapReference);
  }

  /**
   * Returns the map scale at the origin of the projection. This is the scale
   * that is commonly used on paper maps. The accuracy depends on the distance
   * (in meters) between the origin of the projection, and what is currently
   * visible on screen. Typically, the larger the distance, the greater the
   * distortion will be that is caused by the projection, and the less accurate
   * the scale is.
   *
   * If for example the projection is centered on Paris, the scale is calculated
   * for Paris. Therefore, if the view is showing the US, the scale calculated
   * by this method is potentially way off compared to what is visible on
   * screen.
   *
   * @param aScreenResolution The screen resolution, in dots per inch (dpi).
   *  Provide a negative value for the default screen resolution.
   *
   * @return The scale at the origin of the projection of the map (contrary to
   *  the center of the view). The result is a map scale, e.g. 1/13000.
   */
  public double getMapProjectionOriginScale(double aScreenResolution) {
    return internalScale2MapScale(getScale(), aScreenResolution, getWorldReference());
  }

  /**
   * Adjusts the map's scale using the given scale relative to the origin of the projection.
   * This is the scale that is commonly used on paper maps.
   * @param aMapScale The desired scale at the origin of the projection of the map, e.g. 1/13000.
   * @param aScreenResolution The screen resolution, in dots per inch (dpi), or a negative value for the default screen resolution.
   * @see #getMapProjectionOriginScale(double)
   */
  public void setMapProjectionOriginScale(double aMapScale, double aScreenResolution) {
    setScale(mapScale2InternalScale(aMapScale, aScreenResolution, getWorldReference()));
  }

  /**
   * Returns the approximate map scale at the center of the current view
   * extents. Contrary to {@link #getMapProjectionOriginScale}, it calculates
   * the scale at the center of the current view extents. So if the projection
   * is centered on a spot far away of the current view extents, the scale
   * calculated by this method is still accurate (it is measured horizontally).
   * This does imply however that the result of this method changes by simply
   * panning the map around.
   *
   * @param aScreenResolution The screen resolution, in dots per inch (dpi).
   *  Provide a negative value for the default screen resolution.
   *
   * @return The scale in the center of the view of the map (contrary to the
   *  origin of the projection).  The result is a map scale, e.g. 1/13000
   */
  public double getMapCenterScale(double aScreenResolution) {
    double worldUnitPerMeter = calculateWorldUnitPerMeterRatio();
    return getScale() * worldUnitPerMeter / dpi2dpm(aScreenResolution);
  }

  /**
   * Adjusts the map's scale using the given scale at the center of the current view extents.
   * @param aMapScale The desired scale at the origin of the projection of the map, e.g. 1/13000.
   * @param aScreenResolution The screen resolution, in dots per inch (dpi), or a negative value for the default screen resolution.
   * @see #getMapCenterScale(double)
   */
  public void setMapCenterScale(double aMapScale, int aScreenResolution) {
    double dpm = dpi2dpm(aScreenResolution);
    // We calculate the center scale in a few iterations, because changing the scale
    // causes calculateWorldUnitPerMeterRatio to return a slightly different value.
    double mapCenterScale = getMapCenterScale(aScreenResolution);
    for (int i = 0; i < CENTER_SCALE_ITERATE_COUNT && Math.abs(mapCenterScale - aMapScale) > 1e-17; i++) {
      setScale(aMapScale / calculateWorldUnitPerMeterRatio() * dpm);
      mapCenterScale = getMapCenterScale(aScreenResolution);
    }
  }

  /**
   * Retrieves the next or previous scale in the given list of scale, depending on the current scale and zoom operation.
   * @param aPossibleScales scales, increasingly detailed.
   */
  public static double retrieveSnappedScale(double aCurrentScale, ZoomOperation aZoomOperation, double[] aPossibleScales) {
    if (aPossibleScales == null || aPossibleScales.length == 0) {
      return -1;
    }

    double newScale = aCurrentScale;
    boolean newScaleFound = false;

    // Check if the current scale equals one of the fixed scales.
    if (equalScale(newScale, aPossibleScales[0]) && aZoomOperation == ZoomOperation.ZOOM_IN && aPossibleScales.length > 1) {
      newScale = aPossibleScales[1];
      newScaleFound = true;
    } else if (equalScale(newScale ,aPossibleScales[aPossibleScales.length - 1]) && aZoomOperation == ZoomOperation.ZOOM_OUT && aPossibleScales.length >= 2) {
      newScale = aPossibleScales[aPossibleScales.length - 2];
      newScaleFound = true;
    } else {
      for (int i = 1; i < aPossibleScales.length - 1; i++) {
        double scale = aPossibleScales[i];
        if (equalScale(scale ,newScale)) {
          newScale = aZoomOperation == ZoomOperation.ZOOM_OUT ? aPossibleScales[i - 1] : aPossibleScales[i + 1];
          newScaleFound = true;
          break;
        }
      }
    }

    // Check if the current scale are in between the fixed scales.
    if (!newScaleFound) {
      if (newScale < aPossibleScales[0] && aZoomOperation == ZoomOperation.ZOOM_IN) {
        newScale = aPossibleScales[0];
        newScaleFound = true;
      } else if (newScale > aPossibleScales[aPossibleScales.length - 1] && aZoomOperation == ZoomOperation.ZOOM_OUT) {
        newScale = aPossibleScales[aPossibleScales.length - 1];
        newScaleFound = true;
      } else {
        for (int i = 0; i < aPossibleScales.length - 1; i++) {
          double scale1 = aPossibleScales[i];
          double scale2 = aPossibleScales[i + 1];
          if (scale1 < newScale && newScale < scale2) {
            newScale = aZoomOperation == ZoomOperation.ZOOM_OUT ? scale1 : scale2;
            newScaleFound = true;
            break;
          }
        }
      }
    }

    if (newScaleFound) {
      return newScale;
    } else {
      return -1;
    }
  }

  private static double getScaleAsPixelsPerMeter(double aScaleAsPixelsPerWorldUnit, Object aWorldReference) {
    return aScaleAsPixelsPerWorldUnit / getUnitOfMeasure(aWorldReference);
  }

  private static double getScaleAsPixelsPerWorldUnit(double aScaleAsPixelsPerWorldUnit, Object aWorldReference) {
    return aScaleAsPixelsPerWorldUnit * getUnitOfMeasure(aWorldReference);
  }

  private static double getUnitOfMeasure(Object aWorldReference) {
    if (aWorldReference == null ||
        aWorldReference instanceof ILcdXYZWorldReference || aWorldReference instanceof ILcdXYWorldReference) {
      if (aWorldReference instanceof ILcdGridReference) {
        return ((ILcdGridReference) aWorldReference).getUnitOfMeasure();
      } else if (aWorldReference instanceof ILcdGeocentricReference) {
        return ((ILcdGeocentricReference) aWorldReference).getUnitOfMeasure();
      }
      return 1;
    }
    throw new IllegalArgumentException("Unsupported reference, expected ILcdXY(Z)WorldReference: " + aWorldReference);
  }

  /**
   * Converts dots (pixels) per inch to dots (pixels) per meter.
   * If the given dpi value is negative, the default screen resolution is used.
   *
   * @param aDPI The dots per inch (dpi) value.
   * @return The dots per meter value.
   */
  private static double dpi2dpm(double aDPI) {
    if (aDPI < 0) {
      aDPI = ScreenSupport.getScreenResolution();
    }
    return aDPI / TLcdConstant.I2CM * 100;
  }

  /**
   * Calculates the ratio between world units and meters, at the center of the
   * view (contrary to the ratio of 1 at the center of the projection).
   */
  protected double calculateWorldUnitPerMeterRatio() {
    return calculateWorldUnitPerMeterRatio(
        new Point(getWidth() / 2, getHeight() / 2),
        getWorldReference());
  }

  /**
   * Calculates the ratio between world units and meters by transforming two
   * world points to model points, and comparing the distance in world and model
   * coordinates. Therefore it takes into account the distortion caused by the
   * projection.
   *
   * @param aViewOrigin The point (in view coordinates, pixels) to calculate the ratio for.
   * @param aWorldReference The world reference.
   *
   * @return The ratio between a world unit and a meter.
   */
  private double calculateWorldUnitPerMeterRatio(Point aViewOrigin,
                                                 Object aWorldReference) {
    if (!(aWorldReference instanceof ILcdXYWorldReference)) {
      return 1.0;
    }

    TLcdGeodetic2Grid model_world_transformation = new TLcdGeodetic2Grid(WGS84_REFERENCE, (ILcdXYWorldReference) aWorldReference);

    // The points.
    TLcdXYZPoint world_left_point = new TLcdXYZPoint();
    TLcdXYZPoint world_right_point = new TLcdXYZPoint();
    TLcdLonLatHeightPoint model_left_point = new TLcdLonLatHeightPoint();
    TLcdLonLatHeightPoint model_right_point = new TLcdLonLatHeightPoint();

    // The points on the world reference
    view2World(new TLcdXYPoint(aViewOrigin.x - 50, aViewOrigin.y), world_left_point);
    view2World(new TLcdXYPoint(aViewOrigin.x + 50, aViewOrigin.y), world_right_point);

    try {
      // The points on the model reference
      model_world_transformation.worldPoint2modelSFCT(world_left_point, model_left_point);
      model_world_transformation.worldPoint2modelSFCT(world_right_point, model_right_point);

      // The distance between the points
      double meter_distance = WGS84_REFERENCE.getGeodeticDatum().getEllipsoid().geodesicDistance(model_left_point, model_right_point);

      if (meter_distance == 0.0) {
        //This happens when we are zoomed in a lot
        return 1;
      } else {
        double world_distance = TLcdCartesian.distance2D(world_left_point, world_right_point);
        double worldUnitPerMeterRatio = world_distance / meter_distance;

        // Now we discretize the results of the calculations.  This makes sure getting the map scale
        // after is was just set yields the same result.
        return discretize(worldUnitPerMeterRatio);
      }
    } catch (TLcdOutOfBoundsException e) {
      return 1;
    }
  }

  private static double discretize(double aNumber) {
    double value = 100000000;
    return Math.round(aNumber * value) / value;
  }

  private static boolean equalScale(double aFirstScale, double aSecondScale) {
    double epsilon = 1e-12;
    double abs = Math.abs(aFirstScale - aSecondScale);
    return (abs <= epsilon);
  }
}
