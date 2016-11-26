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
package samples.decoder.asdi;

import java.awt.Color;
import java.awt.Graphics;
import java.util.WeakHashMap;

import com.luciad.format.asdi.TLcdASDITrack;
import com.luciad.format.asdi.TLcdASDITrajectory;
import com.luciad.geodesy.ILcdEllipsoid;
import com.luciad.gui.TLcdSymbol;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelReference;
import com.luciad.reference.ILcdGeoReference;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.shape2D.TLcdLonLatPoint;
import com.luciad.util.TLcdConstant;
import com.luciad.util.TLcdOutOfBoundsException;
import com.luciad.util.TLcdSpeedUnit;
import com.luciad.util.iso19103.ILcdISO19103Measure;
import com.luciad.util.iso19103.TLcdISO19103Measure;
import com.luciad.view.gxy.ILcdGXYContext;
import com.luciad.view.gxy.ILcdGXYView;
import com.luciad.view.gxy.TLcdGXYPainterColorStyle;
import com.luciad.view.gxy.painter.TLcdGXYIconPainter;

/**
 * Painter for <code>TLcdASDITrack</code> objects. It paints an icon with a velocity leader.  The length of that
 * leader is defined by the speed of the track, the direction is the heading of the track.
 */
public class TrackGXYPainterZoomedIn extends TLcdGXYIconPainter {

  private static final Color ICON_COLOR = new Color(0, 0, 255);
  private static final Color ICON_BORDER_COLOR = Color.white;
  private static final Color SELECTION_ICON_COLOR = Color.orange;
  private static final Color SELECTION_BORDER_COLOR = Color.black;
  private static final Color DEFAULT_HEADING_COLOR = Color.orange;
  private static final Color SELECTION_HEADING_COLOR = DEFAULT_HEADING_COLOR;

  private static final double HEADING_FACTOR = 0.2;
  private static final int SYMBOL_SIZE = 8;

  private final TLcdISO19103Measure fWorkingMeasure = new TLcdISO19103Measure(0, TLcdSpeedUnit.MS);
  private final WeakHashMap<TLcdASDITrack, MyCachedObject> fHeadingCache = new WeakHashMap<TLcdASDITrack, MyCachedObject>();

  private TLcdGXYPainterColorStyle fColorStyle = new TLcdGXYPainterColorStyle(DEFAULT_HEADING_COLOR, SELECTION_HEADING_COLOR);

  public TrackGXYPainterZoomedIn() {
    setIcon(new TLcdSymbol(TLcdSymbol.FILLED_RECT, SYMBOL_SIZE, ICON_BORDER_COLOR, ICON_COLOR));
    setSelectionIcon(new TLcdSymbol(TLcdSymbol.FILLED_RECT, SYMBOL_SIZE, SELECTION_BORDER_COLOR, SELECTION_ICON_COLOR));
  }

  public void paint(Graphics aGraphics, int aMode, ILcdGXYContext aGXYContext) {
    //Paint heading indicator
    paintHeading(aGXYContext, aGraphics, aMode);

    //Paint normal icon
    super.paint(aGraphics, aMode, aGXYContext);
  }

  /**
   * Returns the orientation of the given track in degrees, or Double.NaN if no
   * orientation is available.
   *
   * @param aTrack The track.
   * @param aModelReference The model reference.
   * @return Double.NaN or an orientation in degrees (positive clockwise, 0 is 12 o' clock).
   */
  public static double getTrackOrientation(TLcdASDITrack aTrack, ILcdModelReference aModelReference) {
    //We can calculate the orientation to the next point in the trajectory.
    TLcdASDITrajectory trajectory = aTrack.getTrajectory();
    int index = aTrack.getTrajectoryPointIndex();
    int point_count = trajectory.getPointCount();
    if (index == point_count - 1 && point_count > 1) {
      //when we are at the end of the trajectory, we cannot calculate the
      //azimuth to the next point, hence we calculate the azimuth from the
      //previous point.
      //We can only do this if the trajectory has at least two points.
      index--;
    }
    if (aModelReference instanceof ILcdGeoReference && index < point_count - 1) {
      //we can only calculate the forward azimuth when we are not at the last point in the
      //trajectory (index < point_count - 1).
      ILcdGeoReference geo_ref = (ILcdGeoReference) aModelReference;
      ILcdEllipsoid ellipsoid = geo_ref.getGeodeticDatum().getEllipsoid();
      ILcdPoint p1 = trajectory.getPoint(index);
      ILcdPoint p2 = trajectory.getPoint(index + 1);

      //Only calculate the azimuth if the points are different
      if (p1.getX() != p2.getX() || p1.getY() != p2.getY()) {
        double azimuth = ellipsoid.forwardAzimuth2D(p1, p2);
        return TLcdConstant.RAD2DEG * azimuth;
      }
    }
    return Double.NaN;
  }

  /**
   * Returns the speed of the given track as meter/second.
   * @param aTrack The track to retrieve the speed of.
   * @param aTrackModel The model the given track belongs to.
   * @return The speed of the given track in meter/second, or Double.NaN if no speed can be found.
   */
  private double getSpeed(TLcdASDITrack aTrack, ILcdModel aTrackModel) {
    ILcdISO19103Measure speed = getSpeedMeasure(aTrack, aTrackModel);
    if (speed != null) {
      speed.convert(fWorkingMeasure.getUnitOfMeasure(), fWorkingMeasure);
      return fWorkingMeasure.getValue();
    }
    return Double.NaN;
  }

  private ILcdISO19103Measure getSpeedMeasure(TLcdASDITrack aTrack, ILcdModel aTrackModel) {
    boolean to = TrackSelectionMediator.isTOModel(aTrackModel);
    if (to) {
      return (ILcdISO19103Measure) aTrack.getValue("CalculatedSpeed");
    } else {
      return (ILcdISO19103Measure) aTrack.getValue("GroundSpeed");
    }
  }

  private void paintHeading(ILcdGXYContext aGXYContext, Graphics aGraphics, int aMode) {
    TLcdASDITrack track = ((TLcdASDITrack) getObject());

    MyCachedObject heading_end = (MyCachedObject) fHeadingCache.get(track);
    if (heading_end == null || !heading_end.isValid(track, aGXYContext.getGXYView())) {
      //Heading end point is not in cache or no longer valid. Calculate the point and store
      //it into the cache for the next paint operation.
      heading_end = retrieveHeadingEndPoint(track, aGXYContext);
      fHeadingCache.put(track, heading_end);
    }

    if (heading_end != null) {
      //set the color on the graphics.
      fColorStyle.setupGraphics(aGraphics, track, aMode, aGXYContext);
      try {
        //Actually paint the heading line
        aGXYContext.getGXYPen().drawLine(track, heading_end, aGXYContext.getModelXYWorldTransformation(),
                                         aGXYContext.getGXYViewXYWorldTransformation(), aGraphics);
      } catch (TLcdOutOfBoundsException e1) {
        //This means the line is not visible in the current projection.  No harm, just don't paint it.
      }
    }
  }

  /**
   * Retrieves the end point of the heading line.  The length is relative to the track speed, the direction is the
   * track's direction of movement.
   * @param aTrack The track to paint the indicator for.
   * @param aGXYContext The context for painting.
   *
   * @return the end point of the heading line.
   */
  private MyCachedObject retrieveHeadingEndPoint(TLcdASDITrack aTrack, ILcdGXYContext aGXYContext) {
    ILcdModelReference model_ref = aGXYContext.getGXYLayer().getModel().getModelReference();
    if (model_ref instanceof ILcdGeoReference) {
      double orientation_degrees = getTrackOrientation(aTrack, model_ref);
      double speed = getSpeed(aTrack, aGXYContext.getGXYLayer().getModel());

      if (!Double.isNaN(orientation_degrees) && !Double.isNaN(speed)) {
        //we paint the heading indicator with a length relative to the scale of the view. As such
        //the line will not become too long or too small when zooming in and zooming out.
        double distance = HEADING_FACTOR / aGXYContext.getGXYView().getScale();

        //Make the length dependant on the speed
        distance *= speed;

        //retrieve the ellipsoid and calculate the point to which the heading indicator must extend.
        ILcdGeoReference geo_ref = (ILcdGeoReference) model_ref;
        ILcdEllipsoid ellipsoid = geo_ref.getGeodeticDatum().getEllipsoid();
        MyCachedObject heading_end_point = new MyCachedObject(aTrack, aGXYContext.getGXYView());
        ellipsoid.geodesicPointSFCT(aTrack, distance, orientation_degrees, heading_end_point);

        return heading_end_point;
      }
    }
    return null;
  }

  private static class MyCachedObject extends TLcdLonLatPoint {
    private double fTrackX;
    private double fTracky;
    private double fScale;

    public MyCachedObject(TLcdASDITrack aTrack, ILcdGXYView aGXYView) {
      fTrackX = aTrack.getX();
      fTracky = aTrack.getY();
      fScale = aGXYView.getScale();
    }

    public boolean isValid(TLcdASDITrack aTrack, ILcdGXYView aGXYView) {
      return fTrackX == aTrack.getX() && fTracky == aTrack.getY() && fScale == aGXYView.getScale();
    }
  }
}
