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
package samples.decoder.asterix;

import java.awt.Color;
import java.awt.Graphics;

import com.luciad.format.asterix.TLcdASTERIXTrack;
import com.luciad.format.asterix.TLcdASTERIXTrajectory;
import com.luciad.geodesy.ILcdEllipsoid;
import com.luciad.gui.TLcdSymbol;
import com.luciad.model.ILcdModelReference;
import com.luciad.reference.ILcdGeoReference;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.shape2D.ILcd2DEditablePoint;
import com.luciad.transformation.ILcdModelXYWorldTransformation;
import com.luciad.util.TLcdConstant;
import com.luciad.util.TLcdOutOfBoundsException;
import com.luciad.view.gxy.ILcdGXYContext;
import com.luciad.view.gxy.ILcdGXYViewXYWorldTransformation;
import com.luciad.view.gxy.TLcdGXYPainterColorStyle;
import com.luciad.view.gxy.painter.TLcdGXYIconPainter;

/**
 * Painter that paints an icon for a track when it is active, or nothing if it is not active.
 */
public class TrackGXYPainter extends TLcdGXYIconPainter {

  private static final Color ICON_COLOR = Color.black;
  private static final Color ICON_BORDER_COLOR = Color.lightGray;
  private static final Color DEFAULT_HEADING_COLOR = Color.orange;
  private static final Color SELECTION_HEADING_COLOR = Color.red;

  private static final int HEADING_FACTOR = 40;
  private static final int SYMBOL_SIZE = 8;

  protected static final TLcdSymbol SELECTION_ICON = new TLcdSymbol(
      TLcdSymbol.FILLED_RECT,
      SYMBOL_SIZE,
      SELECTION_HEADING_COLOR, //border color
      SELECTION_HEADING_COLOR  //fill color
  );
  protected static final TLcdSymbol DEFAULT_ICON = new TLcdSymbol(
      TLcdSymbol.FILLED_RECT,
      SYMBOL_SIZE,
      ICON_BORDER_COLOR,
      ICON_COLOR
  );

  private TLcdGXYPainterColorStyle fColorStyle = new TLcdGXYPainterColorStyle(DEFAULT_HEADING_COLOR, SELECTION_HEADING_COLOR);

  private ILcd2DEditablePoint fWorkingPoint;

  public TrackGXYPainter() {
    setIcon(DEFAULT_ICON);
    setSelectionIcon(SELECTION_ICON);
  }

  public void paint(Graphics aGraphics, int aMode, ILcdGXYContext aGXYContext) {
    //Paint heading indicator
    TLcdASTERIXTrack track = ((TLcdASTERIXTrack) getObject());
    ILcdModelReference modelReference = aGXYContext.getGXYLayer().getModel().getModelReference();
    double orientationDegrees = getTrackOrientation(track, modelReference);
    if (!Double.isNaN(orientationDegrees)) {
      paintHeadingIndicator(track, orientationDegrees, aGraphics, aMode, aGXYContext);
    }

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
  public static double getTrackOrientation(TLcdASTERIXTrack aTrack, ILcdModelReference aModelReference) {
    double orientationDegrees = aTrack.getOrientation();

    //if the track itself did not contain any orientation information, we can calculate the
    //orientation to the next point in the trajectory.
    if (Double.isNaN(orientationDegrees)) {
      TLcdASTERIXTrajectory trajectory = aTrack.getTrajectory();
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
        ILcdGeoReference geoReference = (ILcdGeoReference) aModelReference;
        ILcdEllipsoid ellipsoid = geoReference.getGeodeticDatum().getEllipsoid();
        ILcdPoint p1 = trajectory.getPoint(index);
        ILcdPoint p2 = trajectory.getPoint(index + 1);

        //Only calculate the azimuth if the points are different
        if (p1.getX() != p2.getX() || p1.getY() != p2.getY()) {
          double azimuth = ellipsoid.forwardAzimuth2D(p1, p2);
          orientationDegrees = TLcdConstant.RAD2DEG * azimuth;
        }
      }
    }
    return orientationDegrees;
  }

  private void paintHeadingIndicator(TLcdASTERIXTrack aTrack, double aOrientationDegrees, Graphics aGraphics, int aMode, ILcdGXYContext aGXYContext) {
    ILcdModelReference modelReference = aGXYContext.getGXYLayer().getModel().getModelReference();
    if (modelReference instanceof ILcdGeoReference) {

      //we paint the heading indicator with a length relative to the scale of the view. As such
      //the line will not become too long or too small when zooming in and zooming out.
      double distance = HEADING_FACTOR / aGXYContext.getGXYView().getScale();

      //retrieve the ellipsoid and calculate the point to which the heading indicator must extend.
      ILcdGeoReference geoReference = (ILcdGeoReference) modelReference;
      ILcdEllipsoid ellipsoid = geoReference.getGeodeticDatum().getEllipsoid();
      if (fWorkingPoint == null) {
        fWorkingPoint = aTrack.cloneAs2DEditablePoint();
      }
      ellipsoid.geodesicPointSFCT(aTrack, distance, aOrientationDegrees, fWorkingPoint);

      //set the color on the graphics.
      fColorStyle.setupGraphics(aGraphics, aTrack, aMode, aGXYContext);

      //now that we have calculated all this information, we can actually paint the heading
      //indicator.
      ILcdModelXYWorldTransformation mwt = aGXYContext.getModelXYWorldTransformation();
      ILcdGXYViewXYWorldTransformation vwt = aGXYContext.getGXYViewXYWorldTransformation();
      try {
        aGXYContext.getGXYPen().drawLine(aTrack, fWorkingPoint, mwt, vwt, aGraphics);
      } catch (TLcdOutOfBoundsException e) {
        //do nothing, the line will not be painted.
      }
    }
  }
}
