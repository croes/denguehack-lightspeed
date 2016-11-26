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
package samples.lightspeed.common.tracks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.luciad.datamodel.ILcdDataObject;
import com.luciad.datamodel.TLcdDataProperty;
import com.luciad.datamodel.TLcdDataType;
import com.luciad.geodesy.TLcdEllipsoid;
import com.luciad.shape.ALcdPoint;
import com.luciad.shape.ILcdBounds;
import com.luciad.shape.ILcdCurve;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.ILcdShape;
import com.luciad.shape.ILcdShapeList;
import com.luciad.shape.TLcdCompositeCurve;
import com.luciad.shape.shape2D.ILcd2DEditableBounds;
import com.luciad.shape.shape2D.ILcd2DEditablePoint;
import com.luciad.shape.shape2D.TLcdLonLatBounds;
import com.luciad.shape.shape2D.TLcdLonLatPolyline;
import com.luciad.shape.shape3D.ILcd3DEditableBounds;
import com.luciad.shape.shape3D.ILcd3DEditablePoint;
import com.luciad.shape.shape3D.TLcdLonLatHeightBounds;
import com.luciad.shape.shape3D.TLcdLonLatHeightPoint;
import com.luciad.shape.shape3D.TLcdLonLatHeightPolygon;
import com.luciad.shape.shape3D.TLcdLonLatHeightPolyline;
import com.luciad.shape.shape3D.TLcdLonLatHeightPolypoint;
import com.luciad.util.ILcdOriented;

/**
 * A track that moves along a route.
 */
public class EnrouteAirwayTrack extends ALcdPoint implements ILcdDataObject, ILcdOriented {
  private static final TLcdEllipsoid ELLIPSOID = new TLcdEllipsoid();

  public static final double CRUISE_ALT = 10000;

  private Date fBeginDate, fEndDate;
  private double fSpeed;
  private double fDuration;
  private boolean fAirborne = false;
  private ILcdCurve fRoute;
  private ILcdDataObject fDataObject;
  private Date fDate = new Date();

  private ILcd3DEditablePoint fFuturePoint = new TLcdLonLatHeightPoint();
  private boolean fAirborneAtFuturePoint = false;

  private double fLength;

  private TrackHistory fTrackHistory;

  private double fRotation;

  private final String fTrackName;

  private double fFutureRotation;
  private double fLon, fLat, fHeight;
  private final Object fCoordsLock = new Object();

  /**
   * Creates a track for the given route and speed.
   *
   * @param aRoute The route to follow.
   * @param aSpeed The speed in m/sec at which the point should move
   */
  public EnrouteAirwayTrack(ILcdShape aRoute, double aSpeed) {
    this(aRoute, aSpeed, null, "");

  }

  /**
   * Creates a track for the given route and speed.
   *
   * @param aRoute        The route to follow.
   * @param aFlightSpeed  The speed in m/sec at which the point should move
   * @param aTrackHistory The history to update when the position changes
   * @param aTrackName
   */
  public EnrouteAirwayTrack(ILcdShape aRoute, double aFlightSpeed, TrackHistory aTrackHistory, String aTrackName) {
    fTrackHistory = aTrackHistory;
    fTrackName = aTrackName;
    if (aRoute instanceof ILcdDataObject) {
      fDataObject = (ILcdDataObject) aRoute;
    }
    fRoute = shapeToCurve(aRoute);
    fBeginDate = new Date();
    fSpeed = aFlightSpeed;

    fLength = fRoute.getLength2D(0., 1.);
    fDuration = fLength / fSpeed;

    fEndDate = new Date((long) (fBeginDate.getTime() + fDuration * 1000));
    fDate.setTime(fBeginDate.getTime());
  }

  @Override
  public ILcd2DEditableBounds cloneAs2DEditableBounds() {
    return new TLcdLonLatBounds(this.getBounds());
  }

  @Override
  public ILcd3DEditableBounds cloneAs3DEditableBounds() {
    return new TLcdLonLatHeightBounds(this.getBounds());
  }

  @Override
  public final double getX() {
    synchronized (fCoordsLock) {
      return fLon;
    }
  }

  @Override
  public final double getY() {
    synchronized (fCoordsLock) {
      return fLat;
    }
  }

  @Override
  public final double getZ() {
    synchronized (fCoordsLock) {
      return fHeight;
    }
  }

  /**
   * Tries to convert an arbitrary shape into an ILcdCurve.
   *
   * @param aShape
   *
   * @return A valid curve
   */
  public static ILcdCurve shapeToCurve(ILcdShape aShape) {
    if (aShape instanceof ILcdCurve) {
      return (ILcdCurve) aShape;
    } else if (aShape instanceof ILcdShapeList) {
      if (((ILcdShapeList) aShape).getShapeCount() == 1) {
        return shapeToCurve(((ILcdShapeList) aShape).getShape(0));
      } else if (((ILcdShapeList) aShape).getShapeCount() == 0) {
        return null;
      } else {
        final TLcdCompositeCurve compositeCurve = new TLcdCompositeCurve();
        for (int i = 0; i < ((ILcdShapeList) aShape).getShapeCount(); i++) {
          final ILcdCurve subCurve = shapeToCurve(((ILcdShapeList) aShape).getShape(i));
          if (subCurve != null) {
            compositeCurve.getCurves().add(subCurve);
          }
        }
        return compositeCurve;

      }
    } else if (aShape instanceof TLcdLonLatHeightPolypoint) {
      return new ProjectedLonLatHeightCurve((TLcdLonLatHeightPolypoint) aShape);
    } else {
      throw new IllegalArgumentException("Can not convert shape to curve: " + aShape);
    }
  }

  /**
   * An ILcdCurve wrapper around a lon lat height polyline. The lengths returned
   * by this curve are the lengths of the points projected on the ellipsoid.
   * Points on the curve are also computed based on their projections, the
   * elevation is linearly interpolated between the start and end point of the
   * segment.
   * <p/>
   * Note that this
   */
  private static final class ProjectedLonLatHeightCurve implements ILcdCurve {

    private TLcdLonLatHeightPolypoint fPolypoint;
    private TLcdLonLatPolyline fLonLatCurve;
    private List<Double> fSegmentLengths;

    public ProjectedLonLatHeightCurve(TLcdLonLatHeightPolypoint aPolypoint) {
      fPolypoint = aPolypoint;
      fLonLatCurve = new TLcdLonLatPolyline(aPolypoint);
      if (aPolypoint instanceof TLcdLonLatHeightPolyline) {
        fLonLatCurve.setEllipsoid(((TLcdLonLatHeightPolyline) aPolypoint).getEllipsoid());
      } else if (aPolypoint instanceof TLcdLonLatHeightPolygon) {
        fLonLatCurve.setEllipsoid(((TLcdLonLatHeightPolygon) aPolypoint).getEllipsoid());
      }
    }

    public boolean contains2D(ILcdPoint aPoint) {
      return fPolypoint.contains2D(aPoint);
    }

    public boolean contains3D(ILcdPoint aPoint) {
      return fPolypoint.contains3D(aPoint);
    }

    public ProjectedLonLatHeightCurve clone() {
      ProjectedLonLatHeightCurve clone;
      try {
        clone = (ProjectedLonLatHeightCurve) super.clone();
        ((ProjectedLonLatHeightCurve) clone).fPolypoint = (TLcdLonLatHeightPolypoint) fPolypoint.clone();
      } catch (CloneNotSupportedException e) {
        throw new RuntimeException(e);
      }
      return clone;
    }

    public ILcdBounds getBounds() {
      return fPolypoint.getBounds();
    }

    public ILcdPoint getFocusPoint() {
      return fPolypoint.getFocusPoint();
    }

    public boolean contains2D(double aX, double aY) {
      return fPolypoint.contains2D(aX, aY);
    }

    public boolean contains3D(double aX, double aY, double aZ) {
      return fPolypoint.contains3D(aX, aY, aZ);
    }

    public boolean equals(Object aObject) {
      return fPolypoint.equals(aObject);
    }

    public int hashCode() {
      return fPolypoint.hashCode();
    }

    public String toString() {
      return fPolypoint.toString();
    }

    @Override
    public ILcdPoint getStartPoint() {
      return fLonLatCurve.getStartPoint();
    }

    @Override
    public ILcdPoint getEndPoint() {
      return fLonLatCurve.getEndPoint();
    }

    public double getStartTangent2D() {
      return fLonLatCurve.getStartTangent2D();
    }

    public double getEndTangent2D() {
      return fLonLatCurve.getEndTangent2D();
    }

    public double getTangent2D(double aParam) {
      return fLonLatCurve.getTangent2D(aParam);
    }

    public double getLength2D(double aParam1, double aParam2) {
      return fLonLatCurve.getLength2D(aParam1, aParam2);
    }

    public void computePointSFCT(double aParam, ILcd3DEditablePoint aPointSFCT) {
      int segments = fLonLatCurve.getPointCount() - 1;
      if (segments >= 0) {
        if (segments == 0) {
          aPointSFCT.move3D(fLonLatCurve.getPoint(0));
          return;
        } else if (aParam == 1) {
          aPointSFCT.move2D(getEndPoint());
          return;
        } else {
          synchronized (this) {

            double totLength = getLength2D(0., 1.);
            double paramDistance = aParam * totLength;
            if (fSegmentLengths == null) {
              fSegmentLengths = new ArrayList<Double>(fLonLatCurve.getPointCount());
            }
            if (fSegmentLengths.size() > 0 && fSegmentLengths.get(fSegmentLengths.size() - 1) > paramDistance) {
              int segmentIndex = Math.abs(Collections.binarySearch(fSegmentLengths, paramDistance) + 1);
              final double segmentParam = (paramDistance - (segmentIndex > 0 ? fSegmentLengths.get(segmentIndex - 1) : 0))
                                          / (fSegmentLengths.get(segmentIndex) - (segmentIndex > 0 ? fSegmentLengths.get(segmentIndex - 1) : 0));
              fLonLatCurve.getEllipsoid().geodesicPointSFCT(
                  fLonLatCurve.getPoint(segmentIndex),
                  fLonLatCurve.getPoint(segmentIndex + 1),
                  segmentParam, aPointSFCT);
              aPointSFCT.move3D(aPointSFCT.getX(), aPointSFCT.getY(),
                                (1 - segmentParam) * fLonLatCurve.getPoint(segmentIndex).getZ() + segmentParam * fLonLatCurve.getPoint(segmentIndex + 1).getZ());
              return;
            } else {
              int i = fSegmentLengths.size();
              double length = i > 0 ? fSegmentLengths.get(i - 1) : 0.;
              while (i < segments) {
                double currLength;
                currLength = fLonLatCurve.getEllipsoid().geodesicDistance(fLonLatCurve.getPoint(i), fLonLatCurve.getPoint(i + 1));
                fSegmentLengths.add(length + currLength);
                if (length + currLength >= paramDistance) {
                  final double segmentParam = (paramDistance - length) / currLength;
                  fLonLatCurve.getEllipsoid().geodesicPointSFCT(fLonLatCurve.getPoint(i), fLonLatCurve.getPoint(i + 1), segmentParam, aPointSFCT);
                  aPointSFCT.move3D(aPointSFCT.getX(), aPointSFCT.getY(), (1 - segmentParam) * fLonLatCurve.getPoint(i).getZ() + segmentParam * fLonLatCurve.getPoint(i + 1).getZ());
                  return;
                } else {
                  length += currLength;
                }
                i++;
              }
            }
            return;
          }
        }
      }
    }

    public String getInterpolation() {
      return fLonLatCurve.getInterpolation();
    }

    public int getLineSegmentIntersectionCount(ILcdPoint aP1, ILcdPoint aP2) {
      return fLonLatCurve.getLineSegmentIntersectionCount(aP1, aP2);
    }

  }

  public void prepareDate(Date aDate) {

    if ((fDate.getTime() <= fBeginDate.getTime()) && (aDate.getTime() <= fBeginDate.getTime())) {
      fDate.setTime(aDate.getTime());
      fAirborneAtFuturePoint = false;
    } else if ((fDate.getTime() >= fEndDate.getTime()) && (aDate.getTime() >= fEndDate.getTime())) {
      fDate.setTime(aDate.getTime());
      fAirborneAtFuturePoint = false;
    } else if (aDate.getTime() == fDate.getTime()) {
      fAirborneAtFuturePoint = fAirborne;
    } else {
      fDate.setTime(aDate.getTime());
      fAirborneAtFuturePoint = getPointAtTimeSFCT(aDate, fFuturePoint);
      if (fAirborne) {
        fFutureRotation = Math.toDegrees(ELLIPSOID.forwardAzimuth2D(this, fFuturePoint));
      } else {
        fFutureRotation = Math.toDegrees(ELLIPSOID.forwardAzimuth2D(fRoute.getStartPoint(), fFuturePoint));
      }
    }
  }

  public boolean applyDate() {
    if (fAirborneAtFuturePoint) {
      final TLcdLonLatHeightPoint previousPosition = new TLcdLonLatHeightPoint(this);
      if (fAirborne && fTrackHistory != null) {
        fTrackHistory.addLatestPosition(previousPosition);
      }
      fAirborne = fAirborneAtFuturePoint;
      synchronized (fCoordsLock) {
        fLon = fFuturePoint.getX();
        fLat = fFuturePoint.getY();
        fHeight = fFuturePoint.getZ();
        fRotation = fFutureRotation;
      }
      return true;
    } else {
      fAirborne = false;
      return false;
    }
  }

  public Date getDate() {
    return fDate;
  }

  public Date getBeginDate() {
    return fBeginDate;
  }

  public Date getEndDate() {
    return fEndDate;
  }

  public boolean isAirborne() {
    return fAirborne;
  }

  /**
   * Changes the begin date of the track. The end date updates accordingly.
   *
   * @param aBeginDate
   */
  public void setBeginDate(Date aBeginDate) {
    fBeginDate = aBeginDate;
    fEndDate = new Date((long) (fBeginDate.getTime() + fDuration * 1000));
  }

  /**
   * Returns the duration of the track in seconds.
   *
   * @return the duration of the track in seconds
   */
  public double getDuration() {
    return fDuration;
  }

  /**
   * Retrieves the location of the track at the given time.
   *
   * @param aDate
   * @param aPointSFCT
   */
  private boolean getPointAtTimeSFCT(Date aDate, ILcd3DEditablePoint aPointSFCT) {
    if (aDate.getTime() <= fBeginDate.getTime()) {
      ILcdPoint point = fRoute.getStartPoint();
      aPointSFCT.move3D(point.getX(), point.getY(), CRUISE_ALT);
      return false;
    } else if (aDate.getTime() >= fEndDate.getTime()) {
      ILcdPoint point = fRoute.getEndPoint();
      aPointSFCT.move3D(point.getX(), point.getY(), CRUISE_ALT);
      return false;
    } else {
      double t = (aDate.getTime() - fBeginDate.getTime()) / (1000.0 * fDuration);
      fRoute.computePointSFCT(t, aPointSFCT);
      if (aPointSFCT.getZ() == 0.) {
        aPointSFCT.translate3D(0., 0., CRUISE_ALT);
      }
      return true;

    }
  }

  @Override
  public String toString() {
    return fRoute.toString();
  }

  @Override
  public ILcd2DEditablePoint cloneAs2DEditablePoint() {
    return new TLcdLonLatHeightPoint(this);
  }

  @Override
  public ILcd3DEditablePoint cloneAs3DEditablePoint() {
    return new TLcdLonLatHeightPoint(this);
  }

  public TLcdDataType getDataType() {
    return getDataObject().getDataType();
  }

  public Object getValue(TLcdDataProperty aProperty) {
    return getDataObject().getValue(aProperty);
  }

  public Object getValue(String aPropertyName) {
    return getDataObject().getValue(aPropertyName);
  }

  public void setValue(TLcdDataProperty aProperty, Object aValue) {
    getDataObject().setValue(aProperty, aValue);
  }

  public void setValue(String aPropertyName, Object aValue) {
    getDataObject().setValue(aPropertyName, aValue);
  }

  public boolean hasValue(TLcdDataProperty aProperty) {
    return getDataObject().hasValue(aProperty);
  }

  public boolean hasValue(String aPropertyName) {
    return getDataObject().hasValue(aPropertyName);
  }

  private ILcdDataObject getDataObject() {
    return (ILcdDataObject) fDataObject;
  }

  /**
   * Returns direction in which the point is heading as a forward azimuth.
   */
  @Override
  public double getOrientation() {
    synchronized (fCoordsLock) {
      return fRotation;
    }
  }

  /**
   * Sets the track history associated with this track.
   *
   * @param aTrackHistory
   */
  public void setTrackHistory(TrackHistory aTrackHistory) {
    fTrackHistory = aTrackHistory;

  }

  /**
   * Returns the history of the track.
   *
   * @return The history of the track, or null if no history is available.
   */
  public TrackHistory getTrackHistory() {
    return fTrackHistory;
  }

  /**
   * Returns the name of the track.
   *
   * @return The name of the track.
   */
  public String getTrackName() {
    return fTrackName;
  }

}
