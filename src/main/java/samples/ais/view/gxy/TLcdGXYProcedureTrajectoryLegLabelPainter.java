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
package samples.ais.view.gxy;

import com.luciad.ais.model.procedure.ILcdProcedure;
import com.luciad.ais.model.procedure.ILcdProcedureGeometryHandler;
import com.luciad.ais.model.procedure.ILcdProcedureLeg;
import com.luciad.ais.model.procedure.TLcdProcedureTrajectory;
import com.luciad.ais.model.procedure.type.TLcdProcedureGeometryType;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.shape3D.TLcdXYZPoint;
import com.luciad.util.TLcdNoBoundsException;
import com.luciad.util.TLcdOutOfBoundsException;
import com.luciad.view.gxy.ILcdGXYContext;
import com.luciad.view.gxy.ILcdGXYLabelPainter;
import com.luciad.view.gxy.ILcdGXYLabelPainterProvider;
import com.luciad.view.gxy.ILcdGXYMultiLabelPainter;
import com.luciad.view.gxy.TLcdGXYDataObjectLabelPainter;
import com.luciad.view.gxy.TLcdGXYLabelPainter;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.text.DecimalFormat;

/**
 * A label painter for procedure trajectory legs.
 * <p/>
 * The label painter is attached to a TLcdProcedureTrajectory. To paint the
 * labels, the procedure geometry is requested. For every leg, a label is placed
 * in the center of the first NORMAL or ERROR segment. CONNECTOR and DECORATION
 * segments are ignored.
 * <p/>
 * The painting of the labels is delegated to a standard
 * TLcdGXYFeaturedLabelPainter, which can be configured as usual. It can be
 * obtained by calling getInternalLabelPainter().
 * <p/>
 * Alternatively, the application can call the setLabelProperties() method to
 * use some default procedure leg properties (as opposed to features) in the
 * labels. This method overrides the feature indices set on the internal label
 * painter. To re-enable the standard featured label painter behavior, call
 * setLabelProperties(null).
 */
public class TLcdGXYProcedureTrajectoryLegLabelPainter implements ILcdGXYMultiLabelPainter,
                                                                  ILcdGXYLabelPainterProvider {

  public static final int SEQUENCE_NUMBER = 1;
  public static final int ROUTE_TYPE = 2;
  public static final int LEG_TYPE = 3;
  public static final int OVERFLY_TYPE = 4;
  public static final int TURN_DIRECTION = 5;
  public static final int RHO = 6;
  public static final int THETA = 7;
  public static final int COURSE = 8;
  public static final int DISTANCE = 9;
  public static final int DURATION = 10;
  public static final int ALTITUDE_DESCRIPTION = 11;
  public static final int ALTITUDE_UPPER = 12;
  public static final int ALTITUDE_LOWER = 13;
  public static final int IAP_FIX_ROLE = 14;

  // The internal label painter (derived from TLcdGXYFeaturedLabelPainter).
  private CustomGXYLabelPainter fLabelPainter;

  private TLcdProcedureTrajectory fProcedureTrajectory;
  private TLcdXYZPoint fAnchorPoint;

  // A geometry handler that calculates label anchor points.
  private LabelPaintHandler fLabelPaintHandler;

  // A geometry handler that calculates label bounds.
  private LabelBoundsHandler fLabelBoundsHandler;

  // The index of the label that is currently processed.
  private int fLabelIndex = 0;

  private int[] fLabelProperties = new int[]{SEQUENCE_NUMBER, LEG_TYPE, COURSE};

  public TLcdGXYDataObjectLabelPainter getInternalLabelPainter() {
    return fLabelPainter;
  }

  /**
   * Creates a new <code>TLcdGXYProcedureTrajectoryLegLabelPainter</code>.
   */
  public TLcdGXYProcedureTrajectoryLegLabelPainter() {
    fLabelPainter = new CustomGXYLabelPainter();
    fAnchorPoint = new TLcdXYZPoint();
    fLabelPaintHandler = new LabelPaintHandler();
    fLabelBoundsHandler = new LabelBoundsHandler();

    fLabelPainter.setFrame(true);
    fLabelPainter.setBackground(Color.white);
    fLabelPainter.setFilled(true);
    fLabelPainter.setPositionList(new int[]{TLcdGXYLabelPainter.CENTER, TLcdGXYLabelPainter.SOUTH_WEST, TLcdGXYLabelPainter.SOUTH_EAST});
    fLabelPainter.setWithPin(true);
    fLabelPainter.setShiftLabelPosition(25);
    fLabelPainter.setLocationIndex(0);
  }

  /**
   * Sets the procedure leg properties that need to be displayed on the labels.
   * Call this method with a null argument to revert to the default behavior
   * of the internal label painter (i.e. retrieve the labels based on data object expressions).
   *
   * @param aLabelProperties An array containing any of the SEQUENCE_NUMBER, ROUTE_TYPE, etc. constants.
   */
  public void setLabelProperties(int[] aLabelProperties) {
    fLabelProperties = aLabelProperties;
  }

  public void setObject(Object o) {
    if (o instanceof TLcdProcedureTrajectory) {
      fProcedureTrajectory = (TLcdProcedureTrajectory) o;
    } else {
      fProcedureTrajectory = null;
    }
  }

  public Object getObject() {
    return fProcedureTrajectory;
  }

  public void setLocationIndex(int i) {
    fLabelPainter.setLocationIndex(i);
  }

  public int getLocationIndex() {
    return fLabelPainter.getLocationIndex();
  }

  public void setLabelIndex(int aIndex) {
    fLabelIndex = aIndex;
  }

  public int getLabelIndex() {
    return fLabelIndex;
  }

  public int getLabelCount(Graphics graphics, ILcdGXYContext iLcdGXYContext) {
    return fProcedureTrajectory.getProcedure().getLegCount();
  }

  public int getSubLabelIndex() {
    return 0;
  }

  public void setSubLabelIndex(int i) {
  }

  public int getSubLabelCount(int i) {
    return 1;
  }

  public void paintLabel(Graphics aGraphics, int aMode, ILcdGXYContext aContext) {

    /* Calculate the procedure geometry. The geometry handler will calculate
       anchor points and then delegate to fLabelPainter to draw the labels. */
    if (fProcedureTrajectory != null) {
      fLabelPaintHandler.fGraphics = aGraphics;
      fLabelPaintHandler.fMode = aMode;
      fLabelPaintHandler.fContext = aContext;
      fProcedureTrajectory.processProcedureGeometry(fLabelPaintHandler);
    }
  }

  public int getPossibleLocationCount(Graphics aGraphics) {
    return fLabelPainter.getPossibleLocationCount(aGraphics);
  }

  public double labelBoundsSFCT(Graphics aGraphics, int aMode, ILcdGXYContext aContext, Rectangle aRectangle) throws TLcdNoBoundsException {
    /* Calculate the procedure geometry. The geometry handler will calculate
       anchor points and then delegate to fLabelPainter to retrieve the bounds of the labels. */
    if (fProcedureTrajectory != null) {
      fLabelBoundsHandler.fGraphics = aGraphics;
      fLabelBoundsHandler.fMode = aMode;
      fLabelBoundsHandler.fContext = aContext;
      fLabelBoundsHandler.fLabelBounds = aRectangle;
      fProcedureTrajectory.processProcedureGeometry(fLabelBoundsHandler);

      if(fLabelBoundsHandler.fLabelBounds == null) throw new TLcdNoBoundsException();
    }

    return 0.0;
  }

  public Object clone() {
    TLcdGXYProcedureTrajectoryLegLabelPainter clone;
    try {
      clone = (TLcdGXYProcedureTrajectoryLegLabelPainter) super.clone();
    } catch (CloneNotSupportedException e) {
      // This should never occur.
      throw new InternalError("Clone not supported by " + this.getClass().getName());
    }

    // Deeply cloned fields
    clone.fLabelPainter = (CustomGXYLabelPainter) fLabelPainter.clone();
    clone.fAnchorPoint = (TLcdXYZPoint) fAnchorPoint.clone();
    clone.fLabelProperties = (int[]) fLabelProperties.clone();

    return clone;
  }

  public ILcdGXYLabelPainter getGXYLabelPainter(Object o) {
    setObject(o);
    return this;
  }

  /**
   * Geometry handler for label painting.
   */
  private class LabelPaintHandler extends ALabelingHandler {

    public void execute() {
      fLabelPainter.paintLabel(fGraphics, fMode, fContext);
    }
  }

  /**
   * Geometry handler for label bounds calculations.
   */
  private class LabelBoundsHandler extends ALabelingHandler {

    private Rectangle fLabelBounds;

    public void execute() {
      try {
        fLabelPainter.labelBoundsSFCT(fGraphics, fMode, fContext, fLabelBounds);
      }
      catch (TLcdNoBoundsException e) {
        fLabelBounds = null;
      }
    }
  }

  /**
   * General geometry handler for labeling actions.
   */
  private abstract class ALabelingHandler implements ILcdProcedureGeometryHandler {
    private boolean fCurrentLegLabeled;
    private ILcdProcedureLeg fCurrentLeg = null;
    private int fCurrentLegIndex;
    protected Graphics fGraphics;
    protected int fMode;
    protected ILcdGXYContext fContext;

    // We only place labels on NORMAL and ERROR segments.
    private boolean typeIsLabeled(TLcdProcedureGeometryType aType) {
      return (aType == TLcdProcedureGeometryType.NORMAL) ||
              (aType == TLcdProcedureGeometryType.ERROR);
    }

    // For points, the anchor point is the same as the point itself.
    public void handlePoint(ILcdPoint aPoint, TLcdProcedureGeometryType aType) {

      if (!typeIsLabeled(aType)) return;

      if (!fCurrentLegLabeled && (getLabelIndex() == fCurrentLegIndex)) {
        fAnchorPoint.move2D(aPoint);
        fLabelPainter.setObject(fCurrentLeg);
        execute();
        fCurrentLegLabeled = true;
      }
    }

    // For lines, the anchor point is in the middle of the line segment.
    public void handleLine(ILcdPoint aStartPoint, ILcdPoint aEndPoint, TLcdProcedureGeometryType aType) {

      if (!typeIsLabeled(aType)) return;

      if (!fCurrentLegLabeled && (getLabelIndex() == fCurrentLegIndex)) {
        fAnchorPoint.move2D((aStartPoint.getX() + aEndPoint.getX()) / 2.0,
                (aStartPoint.getY() + aEndPoint.getY()) / 2.0);
        fLabelPainter.setObject(fCurrentLeg);
        execute();
        fCurrentLegLabeled = true;
      }
    }

    public void beginAngleArc(
            ILcdPoint aCenter, double aRadius,
            double aStartAngle, double aArcAngle,
            double aBeginHeight, double aEndHeight,
            TLcdProcedureGeometryType aType
    ) {
    }

    public void handleArcSegment(ILcdPoint aP1, ILcdPoint aP2, int aIndex, int aNumSegments, TLcdProcedureGeometryType aType) {

      if (!typeIsLabeled(aType)) return;

      int anchor_index = (aNumSegments + 1) / 2;

      if ((!fCurrentLegLabeled) && (getLabelIndex() == fCurrentLegIndex) && (aIndex == anchor_index)) {
        fAnchorPoint.move2D(aP2);
        fLabelPainter.setObject(fCurrentLeg);
        execute();
        fCurrentLegLabeled = true;
      }
    }

    public void endAngleArc() {
    }

    public void beginProcedureLeg(ILcdProcedureLeg aLeg) {
      fCurrentLeg = aLeg;
      fCurrentLegLabeled = false;
      fCurrentLegIndex++;
    }

    public void beginProcedure( ILcdProcedure aProcedure ) {
      fCurrentLegIndex = -1;
    }

    public void endProcedure() {
    }

    abstract public void execute();
  }

  /**
   * This TLcdGXYFeaturedLabelPainter takes the anchor point from the outer
   * class instead of calculating it based on the object's bounds.
   */
  private class CustomGXYLabelPainter extends TLcdGXYDataObjectLabelPainter {

    private DecimalFormat fNumberFormat = new DecimalFormat("#");

    private String formatNumber(double aNumber, String aUnitSymbol) {
      if (Double.isNaN(aNumber)) {
        return "-";
      } else {
        return fNumberFormat.format(aNumber) + aUnitSymbol;
      }
    }

    protected void anchorPointSFCT(Graphics aGraphics, int i, ILcdGXYContext aGXYContext, Point aPointSFCT) throws TLcdNoBoundsException {
      try {
        ILcdPoint point = fAnchorPoint;
        if (point != null) {
          aGXYContext.getGXYPen().moveTo(
                  point,
                  aGXYContext.getModelXYWorldTransformation(),
                  aGXYContext.getGXYViewXYWorldTransformation()
          );

          aPointSFCT.x = aGXYContext.getGXYPen().getX();
          aPointSFCT.y = aGXYContext.getGXYPen().getY();
        } else {
          aPointSFCT.x = 0;
          aPointSFCT.y = 0;
        }
      }
      catch (TLcdOutOfBoundsException ex) {
        aPointSFCT.x = 0;
        aPointSFCT.y = 0;
      }
    }

    protected String[] retrieveLabels(int aMode, ILcdGXYContext aGXYContext) {

      if (fLabelProperties != null) {
        ILcdProcedureLeg leg = (ILcdProcedureLeg) this.getObject();

        String[] labels = new String[fLabelProperties.length];
        for (int i = 0; i < fLabelProperties.length; i++) {
          switch (fLabelProperties[i]) {
            case SEQUENCE_NUMBER:
              labels[i] = Integer.toString(leg.getSequenceNumber());
              break;
            case ROUTE_TYPE:
              labels[i] = leg.getRouteType().toString();
              break;
            case LEG_TYPE:
              labels[i] = leg.getType().toString();
              break;
            case OVERFLY_TYPE:
              labels[i] = leg.getFixOverflyType().toString();
              break;
            case TURN_DIRECTION:
              labels[i] = leg.getTurnDirectionType().toString();
              break;
            case RHO:
              labels[i] = formatNumber(leg.getRho(), "");
              break;
            case THETA:
              labels[i] = formatNumber(leg.getTheta(), "");
              break;
            case COURSE:
              labels[i] = formatNumber(leg.getCourse(), "\u00b0");
              break;
            case DISTANCE:
              labels[i] = formatNumber(leg.getDistance(), "");
              break;
            case DURATION:
              labels[i] = formatNumber(leg.getDuration(), "");
              break;
            case ALTITUDE_DESCRIPTION:
              labels[i] = leg.getAltitudeDescription().toString();
              break;
            case ALTITUDE_UPPER:
              labels[i] = formatNumber(leg.getAltitudeUpper(), "");
              break;
            case ALTITUDE_LOWER:
              labels[i] = formatNumber(leg.getAltitudeLower(), "");
              break;
            case IAP_FIX_ROLE:
              labels[i] = leg.getIAPFixRole().toString();
              break;
          }
        }

        return labels;
      } else {
        return super.retrieveLabels(aMode, aGXYContext);
      }
    }

    public Object clone() {
      CustomGXYLabelPainter clone = (CustomGXYLabelPainter) super.clone();

      // Deeply cloned fields
      clone.fNumberFormat = (DecimalFormat) fNumberFormat.clone();

      return clone;
    }
  }


}
