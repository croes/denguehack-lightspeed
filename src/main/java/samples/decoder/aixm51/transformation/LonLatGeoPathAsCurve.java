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
package samples.decoder.aixm51.transformation;

import com.luciad.ais.geodesy.TLcdForwardAzimuthUtil;
import com.luciad.ais.shape.ILcdGeoPath;
import com.luciad.ais.shape.ILcdGeoPathLeg;
import com.luciad.ais.shape.TLcdDiscretizedLonLatGeoPath;
import com.luciad.format.gml32.model.TLcdGML32Arc;
import com.luciad.format.gml32.model.TLcdGML32ArcByCenterPoint;
import com.luciad.format.gml32.model.TLcdGML32CircleByCenterPoint;
import com.luciad.format.gml32.model.TLcdGML32Curve;
import com.luciad.format.gml32.model.TLcdGML32GeodesicString;
import com.luciad.format.gml32.model.TLcdGML32LineStringSegment;
import com.luciad.geodesy.ILcdEllipsoid;
import com.luciad.geodesy.TLcdGeodeticDatum;
import com.luciad.model.ILcdModelReference;
import com.luciad.shape.ILcdBounds;
import com.luciad.shape.ILcdEditableCompositeCurve;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.shape2D.ILcd2DEditableCircularArcBy3Points;
import com.luciad.shape.shape2D.ILcd2DEditableCircularArcByCenterPoint;
import com.luciad.shape.shape2D.ILcd2DEditablePoint;
import com.luciad.shape.shape2D.TLcdLonLatPoint;
import com.luciad.util.TLcdLineType;
import com.luciad.util.logging.ILcdLogger;
import com.luciad.util.logging.TLcdLoggerFactory;

/**
 * An implementation of <code>TLcdGML32Curve</code> which wraps an
 * <code>ILcdGeoPath</code>. This presents the geo path as a standard
 * <code>ILcdShape</code>.
 * <p/>
 * Certain parts of the geo path are discretized using
 * <code>TLcdDiscretizedLonLatGeoPath</code>, as this class solves a number of
 * issues that can occur when discretizing an <code>ILcdGeoPath</code>. Arcs are
 * not discretized, but encoded using the original geometry. Rhumb lines are
 * discretized because they do not have an equivalent in GML.
 * <p/>
 * GML 3.2 geometries are used to simplify export to AIXM 5.x.
 *
 */
public class LonLatGeoPathAsCurve extends TLcdGML32Curve {

  private static ILcdLogger sLogger = TLcdLoggerFactory.getLogger(LonLatGeoPathAsCurve.class.getName());

  // Ellipsoid of wgs84
  private static final ILcdEllipsoid ELLIPSOID_WGS84 = new TLcdGeodeticDatum().getEllipsoid();
  private ILcdEllipsoid fEllipsoid = ELLIPSOID_WGS84;

  // is it a circle or not
  private boolean fCircular = false;

  // Indication that the polygon has been updated or not since last recalculate
  // The variable is transient; therefore the initial value will be false
  // after deserialization.  This is ok: the polygon must be recomputed in that case.
  private transient boolean fIsValidShape = false;

  private final ILcdGeoPath fGeoPath;
  private final ILcdModelReference fModelReference;
  private TLcdDiscretizedLonLatGeoPath fDiscretizedLonLatGeoPath;

  /**
   * Default constructor.
   */
  public LonLatGeoPathAsCurve(ILcdGeoPath aGeoPath, ILcdModelReference aModelReference) {
    fGeoPath = aGeoPath;
    fModelReference = aModelReference;
    fDiscretizedLonLatGeoPath = new TLcdDiscretizedLonLatGeoPath();
    for (int i = 0; i < aGeoPath.getLegCount(); i++) {
      fDiscretizedLonLatGeoPath.addLeg(aGeoPath.getLeg(i));
    }
    invalidate();
  }

  /**
   * Returns the bounds of the discretized GeoPath.
   * @return an <code>ILcdBounds</code>
   */
  @Override
  public ILcdBounds getBounds() {
    if (!fIsValidShape) {
      recalculate();
    }
    return super.getBounds();
  }

  /**
   * Creates and returns a copy of this object.
   * <p/>
   * The discretization is not copied or cloned at all,
   * but recalculated when needed.
   */
  @Override
  public TLcdGML32Curve clone() {
    LonLatGeoPathAsCurve clone;
    clone = (LonLatGeoPathAsCurve) super.clone();

    clone.fDiscretizedLonLatGeoPath = (TLcdDiscretizedLonLatGeoPath) fDiscretizedLonLatGeoPath.clone();
    clone.fIsValidShape = false;

    return clone;
  }

  private int getLegCount() {
    return fGeoPath.getLegCount();
  }

  private ILcdGeoPathLeg getLeg(int aIndex) {
    return fGeoPath.getLeg(aIndex);
  }

  /**
   * Discards the geometry cached with this GeoPath and recalculates it.
   * Whenever this is done, the ILcdModel containing this
   * GeoPath should be notified by means of a call to elementChanged()!
   */
  private void invalidate() {
    recalculate();
  }

  /**
   * Recalculate the curve and the bounds.
   * This may be called if the object is certain to have changed
   * without calling any of the methods of this object,
   * e.g., one of its points has moved.
   * <P>
   * Care should be taken that this is not used too much:
   * all operations that are done on this object
   * and that make the object change, already incorporate
   * this recalculate.
   */
  private void recalculate() {
    recalculate(this);
  }

  /**
   * needed to be sure that between recalculate and successive operations
   * the buffer weak/soft reference is not thrown away
   */
  private void recalculate(ILcdEditableCompositeCurve aCurve) {
    fIsValidShape = true;
    int leg_count = getLegCount();
    aCurve.getCurves().clear();

    if (leg_count > 0) {

      ILcdGeoPathLeg nextLeg = null;
      ILcdGeoPathLeg currentLeg = getLeg(0);
      int discretizedPointLegStart = 0;
      int discretizedPointLegEnd = 0;
      for (int counter = 0; counter < leg_count; counter++) {
        if (fDiscretizedLonLatGeoPath.getLegCount() > 1) {
          while (discretizedPointLegEnd < fDiscretizedLonLatGeoPath.getPointCount() && fDiscretizedLonLatGeoPath.getLegIndexForPoint(discretizedPointLegEnd) == counter) {
            discretizedPointLegEnd++;
          }
        } else {
          discretizedPointLegEnd = fDiscretizedLonLatGeoPath.getPointCount();
        }
        final int currentIndex = (counter + 1) % leg_count;
        nextLeg = getLeg(currentIndex);
        discretizeGeoPathLeg(nextLeg, currentLeg, discretizedPointLegStart,
                             discretizedPointLegEnd < fDiscretizedLonLatGeoPath.getPointCount() ? discretizedPointLegEnd + 1 : discretizedPointLegEnd, aCurve);
        discretizedPointLegStart = discretizedPointLegEnd;
        if (currentLeg.getType() == ILcdGeoPathLeg.ARC || currentLeg.getType() == ILcdGeoPathLeg.ARC_BY_EDGE) {
          discretizedPointLegStart--;
        }
        currentLeg = nextLeg;
      }

    }

  }

  /* Discretization */
  private void discretizeGeoPathLeg(ILcdGeoPathLeg aNextLeg, ILcdGeoPathLeg aCurrentLeg,
                                    int aDiscretizedPointLegStart, int aDiscretizedPointLegEnd, ILcdEditableCompositeCurve aCurve) {
    int leg_type = aCurrentLeg.getType();

    switch (leg_type) {
    case ILcdGeoPathLeg.GEODESIC_LINE:
      discretizeLine(aNextLeg, aCurrentLeg, TLcdLineType.GREATCIRCLE, aDiscretizedPointLegStart, aDiscretizedPointLegEnd, aCurve);
      break;
    case ILcdGeoPathLeg.RHUMB_LINE:
      discretizeLine(aNextLeg, aCurrentLeg, TLcdLineType.RHUMBLINE, aDiscretizedPointLegStart, aDiscretizedPointLegEnd, aCurve);
      break;
    case ILcdGeoPathLeg.ARC:
      discretizeArc(aNextLeg, aCurrentLeg, aCurve);
      break;
    case ILcdGeoPathLeg.ARC_BY_EDGE:
      discretizeArcByEdge(aNextLeg, aCurrentLeg, aCurve);
      break;
    case ILcdGeoPathLeg.CIRCLE_POINT:
      discretizeCirclePoint(aNextLeg, aCurrentLeg, aCurve);
      break;
    case ILcdGeoPathLeg.CIRCLE_RADIUS:
      discretizeCircleRadius(aNextLeg, aCurrentLeg, aCurve);
      break;
    case ILcdGeoPathLeg.SUB_POINT_LIST:
      discretizeSubPointList(aNextLeg, aCurrentLeg, aDiscretizedPointLegStart, aDiscretizedPointLegEnd, aCurve);
      break;
    default:
      sLogger.error("discretizeGeoPathLeg(): Unknown leg type: " + leg_type + ". Ignoring.");
    }
  }

  private void discretizeLine(ILcdGeoPathLeg aNextLeg, ILcdGeoPathLeg aCurrentLeg,
                              TLcdLineType aLineType, int aDiscretizedPointLegStart, int aDiscretizedPointLegEnd,
                              ILcdEditableCompositeCurve aCurve) {

    if (fCircular) {
      aCurve.getCurves().clear();
      fCircular = false;
    }

    if (aLineType == TLcdLineType.GREATCIRCLE) {
      int nbCurves = aCurve.getCurves().size();
      TLcdGML32GeodesicString geodesic;
      if (nbCurves > 0 && aCurve.getCurves().get(nbCurves - 1) instanceof TLcdGML32GeodesicString) {
        geodesic = (TLcdGML32GeodesicString) aCurve.getCurves().get(nbCurves - 1);
        aDiscretizedPointLegStart++;// we concatenate, so don't repeat first point
      } else {
        geodesic = new TLcdGML32GeodesicString(fModelReference);
        aCurve.getCurves().add(geodesic);
      }
      // we copy the discretized points of the leg partially, because those are
      // already corrected
      int nbPoints = geodesic.getPointCount();
      for (int i = aDiscretizedPointLegStart; i < aDiscretizedPointLegEnd; i++) {
        ILcdPoint pointListPoint = fDiscretizedLonLatGeoPath.getPoint(i);
        geodesic.insert2DPoint(nbPoints++, pointListPoint.getX(), pointListPoint.getY());

        if (i == aDiscretizedPointLegStart + 1) {
          // skip intermediate points: we use a geodesic, so these are not
          // needed
          i = i < aDiscretizedPointLegEnd - 3 ? aDiscretizedPointLegEnd - 3 : i;
        }
      }
      // close the curve if we've reached the end
      if (aDiscretizedPointLegEnd == fDiscretizedLonLatGeoPath.getPointCount()) {
        ILcdPoint pointListPoint = fDiscretizedLonLatGeoPath.getPoint(0);
        geodesic.insert2DPoint(nbPoints++, pointListPoint.getX(), pointListPoint.getY());
      }

    } else if (aLineType == TLcdLineType.RHUMBLINE) {
      // not supported in GML, use discretized approximation
      TLcdGML32LineStringSegment polyline = new TLcdGML32LineStringSegment(fModelReference);
      for (int i = aDiscretizedPointLegStart; i < aDiscretizedPointLegEnd; i++) {
        ILcdPoint pointListPoint = fDiscretizedLonLatGeoPath.getPoint(i);
        polyline.insert2DPoint(i - aDiscretizedPointLegStart, pointListPoint.getX(), pointListPoint.getY());
      }
      // close the curve if we've reached the end
      if (aDiscretizedPointLegEnd == fDiscretizedLonLatGeoPath.getPointCount()) {
        ILcdPoint pointListPoint = fDiscretizedLonLatGeoPath.getPoint(0);
        polyline.insert2DPoint(polyline.getPointCount(), pointListPoint.getX(), pointListPoint.getY());
      }
      aCurve.getCurves().add(polyline);
    }

  }

  private void discretizeArc(ILcdGeoPathLeg aCurrentLeg, ILcdGeoPathLeg aPreviousLeg,
                             ILcdEditableCompositeCurve aCurve) {

    if (fCircular) {
      aCurve.getCurves().clear();
      fCircular = false;
    }

    double[] data = new double[aPreviousLeg.getDataLength()];
    aPreviousLeg.getDataSFCT(data);

    // The data buffer contains the X,Y coordinates of the circle center.
    ILcd2DEditablePoint arcCenterPoint = new TLcdLonLatPoint(data[0], data[1]);
    ILcd2DEditablePoint arcStartPoint = new TLcdLonLatPoint(aPreviousLeg);
    ILcd2DEditablePoint arcEndPoint = new TLcdLonLatPoint(aCurrentLeg.getX(), aCurrentLeg.getY());

    double radius = fEllipsoid.geodesicDistance(arcStartPoint.getX(), arcStartPoint.getY(), arcCenterPoint.getX(), arcCenterPoint.getY()); //data[0], data[1]

    double start_angle = Math.toDegrees(fEllipsoid.forwardAzimuth2D(arcCenterPoint, arcStartPoint));

    double end_angle = Math.toDegrees(fEllipsoid.forwardAzimuth2D(arcCenterPoint, arcEndPoint));

    final boolean CW = data[3] < 0.5;

    ILcd2DEditableCircularArcByCenterPoint arcByCenterPoint = new TLcdGML32ArcByCenterPoint(fModelReference);
    arcByCenterPoint.setStartAngle(TLcdForwardAzimuthUtil.forwardAzimuth2ArcAngle(start_angle));
    arcByCenterPoint.move2D(arcCenterPoint);
    arcByCenterPoint.setArcAngle(TLcdForwardAzimuthUtil.forwardAzimuthDelta2ArcDelta(start_angle, end_angle, !CW));
    arcByCenterPoint.setRadius(radius);
    aCurve.getCurves().add(arcByCenterPoint);

  }

  private void discretizeArcByEdge(ILcdGeoPathLeg aNextLeg, ILcdGeoPathLeg aCurrentLeg,
                                   ILcdEditableCompositeCurve aCurve) {
    if (fCircular) {
      aCurve.getCurves().clear();
      fCircular = false;
    }

    double[] data = new double[aCurrentLeg.getDataLength()];
    aCurrentLeg.getDataSFCT(data);

    ILcd2DEditablePoint arcStartPoint = new TLcdLonLatPoint(aCurrentLeg);
    ILcd2DEditablePoint arcEndPoint = new TLcdLonLatPoint(aNextLeg);
    ILcd2DEditablePoint pointOnArc = new TLcdLonLatPoint(data[0], data[1]);

    ILcd2DEditableCircularArcBy3Points arc = new TLcdGML32Arc(fModelReference);
    arc.moveStartPoint2D(arcStartPoint.getX(), arcStartPoint.getY());
    arc.moveIntermediatePoint2D(pointOnArc.getX(), pointOnArc.getY());
    arc.moveEndPoint2D(arcEndPoint.getX(), arcEndPoint.getY());
    aCurve.getCurves().add(arc);
  }

  private void discretizeSubPointList(ILcdGeoPathLeg aNextLeg, ILcdGeoPathLeg aSubpointListLeg,
                                      int aDiscretizedPointLegStart, int aDiscretizedPointLegEnd, ILcdEditableCompositeCurve aCurve) {

    TLcdGML32GeodesicString polyline = new TLcdGML32GeodesicString(fModelReference);
    for (int i = aDiscretizedPointLegStart; i < aDiscretizedPointLegEnd; i++) {
      ILcdPoint pointListPoint = fDiscretizedLonLatGeoPath.getPoint(i);
      polyline.insert2DPoint(i - aDiscretizedPointLegStart, pointListPoint.getX(), pointListPoint.getY());
    }
    // close the curve if we've reached the end
    if (aDiscretizedPointLegEnd == fDiscretizedLonLatGeoPath.getPointCount()) {
      ILcdPoint pointListPoint = fDiscretizedLonLatGeoPath.getPoint(0);
      polyline.insert2DPoint(polyline.getPointCount(), pointListPoint.getX(), pointListPoint.getY());
    }
    aCurve.getCurves().add(polyline);
  }

  private void discretizeCirclePoint(ILcdGeoPathLeg aNextLeg, ILcdGeoPathLeg aCurrentLeg,
                                     ILcdEditableCompositeCurve aCurve) {
    aCurve.getCurves().clear();
    fCircular = true;

    double[] data = new double[aCurrentLeg.getDataLength()];
    aCurrentLeg.getDataSFCT(data);

    TLcdLonLatPoint circleCenter = new TLcdLonLatPoint(aCurrentLeg.getX(), aCurrentLeg.getY());
    TLcdLonLatPoint pointOnCircle = new TLcdLonLatPoint(data[0], data[1]);

    double radius = fEllipsoid.geodesicDistance(circleCenter, pointOnCircle);

    aCurve.getCurves().add(new TLcdGML32CircleByCenterPoint(circleCenter.getX(), circleCenter.getY(), radius, fModelReference));

  }

  private void discretizeCircleRadius(ILcdGeoPathLeg aNextLeg, ILcdGeoPathLeg aCurrentLeg,
                                      ILcdEditableCompositeCurve aCurve) {
    aCurve.getCurves().clear();
    fCircular = true;

    double[] data = new double[aCurrentLeg.getDataLength()];
    aCurrentLeg.getDataSFCT(data);

    TLcdLonLatPoint circleCenter = new TLcdLonLatPoint(aCurrentLeg.getX(), aCurrentLeg.getY());
    aCurve.getCurves().add(new TLcdGML32CircleByCenterPoint(circleCenter.getX(), circleCenter.getY(), data[0], fModelReference));
  }

}
