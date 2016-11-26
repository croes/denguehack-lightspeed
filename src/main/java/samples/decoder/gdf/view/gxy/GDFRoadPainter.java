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
package samples.decoder.gdf.view.gxy;

import static samples.decoder.gdf.view.gxy.GDFLinePainterProvider.Mode;
import static samples.decoder.gdf.view.gxy.GDFLinePainterProvider.Mode.ALL_ROAD_BORDER;
import static samples.decoder.gdf.view.gxy.GDFLinePainterProvider.Mode.ROAD_INNER;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;

import com.luciad.format.gdf.ILcdGDFFeature;
import com.luciad.format.gdf.ILcdGDFLineFeature;
import com.luciad.gui.ILcdIcon;
import com.luciad.network.function.ILcdEdgeValueFunction;
import com.luciad.network.graph.TLcdTraversalDirection;
import com.luciad.network.graph.route.TLcdRoute;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.shape2D.ILcd2DEditableBounds;
import com.luciad.shape.shape2D.TLcdXYPoint;
import com.luciad.util.TLcdDistanceUnit;
import com.luciad.util.TLcdNoBoundsException;
import com.luciad.view.gxy.ALcdGXYPainter;
import com.luciad.view.gxy.ILcdGXYContext;
import com.luciad.view.gxy.ILcdGXYEditor;
import com.luciad.view.gxy.ILcdGXYPainterStyle;
import com.luciad.view.gxy.TLcdGXYPainterColorStyle;
import com.luciad.view.gxy.painter.TLcdGXYIconPainter;
import com.luciad.view.gxy.painter.TLcdGXYPointListPainter;
import com.luciad.view.map.TLcdMapG2DLineStyle;

import samples.decoder.gdf.network.function.GDFDirectionOfFlowEdgeValueFunction;
import samples.decoder.gdf.network.function.GDFRoadClassFunction;

/**
 * A painter class for painting roads of a GDF model. Roads are painted based on
 * their road class, which is attached as a GDF attribute to each road feature in the model.
 */
public class GDFRoadPainter extends ALcdGXYPainter implements ILcdGXYEditor {

  private static final Color BORDER_COLOR_1 = new Color(210, 170, 060);
  private static final Color BORDER_COLOR_2 = new Color(220, 210, 170);
  private static final Color BORDER_COLOR_3 = new Color(190, 180, 160);

  private static final Color INNER_COLOR_1 = new Color(255, 200, 070);
  private static final Color INNER_COLOR_2 = new Color(255, 255, 140);
  private static final Color INNER_COLOR_3 = new Color(255, 255, 255);

  /**
   * The total number of road classes.
   */
  private static final int ROAD_CLASSES = 10;

  /**
   * Flag indicating whether to draw directional arrows or not.
   */
  private boolean fPaintArrows = true;

  /**
   * Flag indicating whether to use an adaptive level of detail, depending on
   * the map scale.
   */
  private boolean fUseScaleRange = false;

  private GDFLinePainterProvider.Mode fMode;

  private GDFRenderingSettings fRenderingSettings;

  /**
   * The current object to be painted.
   */
  private Object fObject;

  /**
   * GDF functions.
   */
  private ILcdEdgeValueFunction fRoadClassFunction = new GDFRoadClassFunction();
  private ILcdEdgeValueFunction fDirectionOfFlowEdgeValueFunction = new GDFDirectionOfFlowEdgeValueFunction();

  /**
   * Painters.
   */
  private TLcdGXYPointListPainter fPointListPainter = new TLcdGXYPointListPainter(TLcdGXYPointListPainter.POLYLINE);
  private TLcdGXYIconPainter fIconPainter = new TLcdGXYIconPainter();
  private ArrowIcon fArrowIcon = new ArrowIcon();
  private BlockIcon fBlockIcon = new BlockIcon();

  /**
   * Road styles, for each road class and for each mode.
   */
  private TLcdMapG2DLineStyle[][] fRoadStylesHQ = new TLcdMapG2DLineStyle[ROAD_CLASSES][3];
  private TLcdGXYPainterColorStyle[][] fRoadStylesLQ = new TLcdGXYPainterColorStyle[ROAD_CLASSES][3];

  private boolean fHighlight = false;
  private Color fHighlightColor = Color.BLUE;

  /**
   * Creates a new road painter.
   *
   * @param aMode the mode of this road painter.
   */
  public GDFRoadPainter(GDFLinePainterProvider.Mode aMode, GDFRenderingSettings aRenderingSettings) {
    fMode = aMode;
    fRenderingSettings = aRenderingSettings;
    initRoadStyles();
  }

  /**
   * Sets flag whether to paint all roads or to adapt the level of detail to the
   * map scale.
   *
   * @param aUseScaleRange if <code>true</code>, the level of detail will be
   *                       adapted to the map scale, if <code>false</code>, all
   *                       details will be painted, whatever the map scale is.
   */
  public void setUseScaleRange(boolean aUseScaleRange) {
    fUseScaleRange = aUseScaleRange;
  }

  public boolean isUseScaleRange() {
    return fUseScaleRange;
  }

  public void setRoadClassFunction(ILcdEdgeValueFunction aRoadClassFunction) {
    fRoadClassFunction = aRoadClassFunction;
  }

  public ILcdEdgeValueFunction getRoadClassFunction() {
    return fRoadClassFunction;
  }

  public void setDirectionOfFlowFunction(ILcdEdgeValueFunction aDirectionOfFlowFunction) {
    fDirectionOfFlowEdgeValueFunction = aDirectionOfFlowFunction;
  }

  public ILcdEdgeValueFunction getDirectionOfFlowFunction() {
    return fDirectionOfFlowEdgeValueFunction;
  }

  public boolean isHighlight() {
    return fHighlight;
  }

  public void setHighlight(boolean aHighlight) {
    fHighlight = aHighlight;
  }

  public void setHighlightColor(Color aHighlightColor) {
    fHighlightColor = aHighlightColor;
  }

  // ILcdGXYPainter implementations.

  public void paint(Graphics aGraphics, int aMode, ILcdGXYContext aLcdGXYContext) {

    if (fRenderingSettings != null && fRenderingSettings.getSetting(GDFRenderingSettings.Key.QUALITY) == GDFRenderingSettings.Value.QUALITY_LOW
        && fMode == GDFLinePainterProvider.Mode.ROAD_INNER && !fHighlight) {
      return;
    }

    if (getObject() instanceof ILcdGDFFeature) {
      ILcdGDFFeature feature = (ILcdGDFFeature) getObject();

      switch (feature.getFeatureClass().getFeatureClassCode()) {
      case 4110:
        paintRoad(aGraphics, aMode, aLcdGXYContext);
        break;
      default:
      }
    }
  }

  public boolean isTouched(Graphics graphics, int i, ILcdGXYContext iLcdGXYContext) {
    return fPointListPainter.isTouched(graphics, i, iLcdGXYContext);
  }

  public void boundsSFCT(Graphics graphics,
                         int i,
                         ILcdGXYContext iLcdGXYContext,
                         ILcd2DEditableBounds iLcd2DEditableBounds) throws TLcdNoBoundsException {
    fPointListPainter.setObject(getObject());
    fPointListPainter.boundsSFCT(graphics, i, iLcdGXYContext, iLcd2DEditableBounds);
  }

  public Object getObject() {
    return fObject;
  }

  public void setObject(Object aObject) {
    fObject = aObject;
    fPointListPainter.setObject(aObject);
  }

  // ILcdGXYEditor implementations.

  public boolean edit(Graphics graphics, int i, ILcdGXYContext iLcdGXYContext) {
    return fPointListPainter.edit(graphics, i, iLcdGXYContext);
  }

  public int getCreationClickCount() {
    return fPointListPainter.getCreationClickCount();
  }

  public boolean acceptSnapTarget(Graphics graphics, ILcdGXYContext iLcdGXYContext) {
    return fPointListPainter.acceptSnapTarget(graphics, iLcdGXYContext);
  }

  // Private helper methods.

  private void paintRoad(Graphics aGraphics, int aMode, ILcdGXYContext aLcdGXYContext) {

    if (fMode == GDFLinePainterProvider.Mode.ALL_ROAD_BORDER || fMode == GDFLinePainterProvider.Mode.ROAD_INNER) {
      paintRoadLines(aGraphics, aMode, aLcdGXYContext);
    } else {
      paintRoadSigns(aGraphics, aMode, aLcdGXYContext);
    }
  }

  private void paintRoadLines(Graphics aGraphics, int aMode, ILcdGXYContext aLcdGXYContext) {

    final int road_class = (int) fRoadClassFunction.computeEdgeValue(null, null, (Object) getObject(), TLcdTraversalDirection.FORWARD);

    if (!fUseScaleRange || (road_class <= getMaximumRoadClassForScale(aLcdGXYContext))) {

      if (fRenderingSettings.getSetting(GDFRenderingSettings.Key.QUALITY) == GDFRenderingSettings.Value.QUALITY_HIGH) {
        fPointListPainter.setLineStyle(fRoadStylesHQ[road_class][getIndex(fMode)]);
      } else {
        fPointListPainter.setLineStyle(fRoadStylesLQ[road_class][getIndex(fMode)]);
      }

      if (fHighlight) {
        fPointListPainter.setLineStyle(new HighlightStyle(fPointListPainter.getLineStyle(), fHighlightColor));
      }

      // And paint the road
      fPointListPainter.setObject(fObject);
      fPointListPainter.paint(aGraphics, aMode, aLcdGXYContext);
    }
  }

  private void paintRoadSigns(Graphics aGraphics, int aMode, ILcdGXYContext aLcdGXYContext) {

    // Paint a directional arrow
    if (fPaintArrows) {

      boolean blocked_forward = false;
      boolean blocked_backward = false;
      ILcdGDFLineFeature line_feature = (ILcdGDFLineFeature) getObject();

      if (fDirectionOfFlowEdgeValueFunction.computeEdgeValue(null,
                                                             new TLcdRoute(null, line_feature.getFromPoint()),
                                                             line_feature,
                                                             TLcdTraversalDirection.FORWARD) == Double.POSITIVE_INFINITY) {
        blocked_forward = true;
      }
      if (fDirectionOfFlowEdgeValueFunction.computeEdgeValue(null,
                                                             new TLcdRoute(null, line_feature.getToPoint()),
                                                             line_feature,
                                                             TLcdTraversalDirection.FORWARD) == Double.POSITIVE_INFINITY) {
        blocked_backward = true;
      }

      if (blocked_forward ^ blocked_backward) {
        int point_count = line_feature.getPointCount();
        ILcdPoint point1 = line_feature.getPoint(point_count / 2 - 1);
        ILcdPoint point2 = line_feature.getPoint(point_count / 2);

        double theta;
        if (point2.getX() == point1.getX()) {
          if (point2.getY() > point1.getY()) {
            theta = Math.PI / 2;
          } else {
            theta = -Math.PI / 2;
          }
        } else {
          theta = Math.atan((point2.getY() - point1.getY()) / (point2.getX() - point1.getX()));
          if (point2.getX() < point1.getX()) {
            theta += Math.PI;
          }
        }
        if (blocked_forward) {
          theta = theta + Math.PI;
        }
        ILcdPoint point3 = new TLcdXYPoint((point1.getX() + point2.getX()) / 2,
                                           (point1.getY() + point2.getY()) / 2);
        fArrowIcon.setRotation(-theta);
        fArrowIcon.setRadius((int) (aLcdGXYContext.getGXYView().getScale() * 6));
        fIconPainter.setObject(point3);
        fIconPainter.setIcon(fArrowIcon);
        fIconPainter.setSelectionIcon(fArrowIcon);
        if (point1 != point2) {
          fIconPainter.paint(aGraphics, aMode, aLcdGXYContext);
        }
      }

      if (blocked_forward && blocked_backward) {
        int point_count = line_feature.getPointCount();
        ILcdPoint point1 = line_feature.getPoint(point_count / 2 - 1);
        ILcdPoint point2 = line_feature.getPoint(point_count / 2);
        ILcdPoint point3 = new TLcdXYPoint((point1.getX() + point2.getX()) / 2,
                                           (point1.getY() + point2.getY()) / 2);
        fBlockIcon.setRadius((int) (aLcdGXYContext.getGXYView().getScale() * 6));
        fIconPainter.setObject(point3);
        fIconPainter.setIcon(fBlockIcon);
        fIconPainter.setSelectionIcon(fBlockIcon);
        fIconPainter.paint(aGraphics, aMode, aLcdGXYContext);
      }
    }
  }

  private int getMaximumRoadClassForScale(ILcdGXYContext aLcdGXYContext) {
    double scale = aLcdGXYContext.getGXYView().getScale();
    if (scale > 0.2) {
      return 8;
    } else if (scale > 0.1) {
      return 7;
    } else if (scale > 0.05) {
      return 6;
    } else if (scale > 0.02) {
      return 5;
    } else if (scale > 0.01) {
      return 4;
    } else if (scale > 0.005) {
      return 3;
    } else {
      return 2;
    }
  }

  /**
   * Definition of road styles
   */
  private void initRoadStyles() {
    registerHighRoadStyle(0, BORDER_COLOR_1, INNER_COLOR_1, 20);
    registerHighRoadStyle(1, BORDER_COLOR_2, INNER_COLOR_2, 18);
    registerHighRoadStyle(2, BORDER_COLOR_2, INNER_COLOR_2, 18);
    registerHighRoadStyle(3, BORDER_COLOR_3, INNER_COLOR_3, 17);
    registerHighRoadStyle(4, BORDER_COLOR_3, INNER_COLOR_3, 16);
    registerHighRoadStyle(5, BORDER_COLOR_3, INNER_COLOR_3, 16);
    registerHighRoadStyle(6, BORDER_COLOR_3, INNER_COLOR_3, 16);
    registerHighRoadStyle(7, BORDER_COLOR_3, INNER_COLOR_3, 14);
    registerHighRoadStyle(8, BORDER_COLOR_3, INNER_COLOR_3, 14);
    registerHighRoadStyle(9, BORDER_COLOR_3, INNER_COLOR_3, 14);

    registerLowRoadStyle(0, INNER_COLOR_1);
    registerLowRoadStyle(1, INNER_COLOR_2);
    registerLowRoadStyle(2, INNER_COLOR_2);
    registerLowRoadStyle(3, INNER_COLOR_3);
    registerLowRoadStyle(4, INNER_COLOR_3);
    registerLowRoadStyle(5, INNER_COLOR_3);
    registerLowRoadStyle(6, INNER_COLOR_3);
    registerLowRoadStyle(7, INNER_COLOR_3);
    registerLowRoadStyle(8, INNER_COLOR_3);
    registerLowRoadStyle(9, INNER_COLOR_3);
  }

  private void registerHighRoadStyle(int aRoadClass,
                                     Color aBorderColor,
                                     Color aInnerColor,
                                     int aWidth) {
    int borderIndex = getIndex(ALL_ROAD_BORDER);
    fRoadStylesHQ[aRoadClass][borderIndex] = new TLcdMapG2DLineStyle();
    fRoadStylesHQ[aRoadClass][borderIndex].setAntiAliasing(true);
    fRoadStylesHQ[aRoadClass][borderIndex].setColor(aBorderColor);
    fRoadStylesHQ[aRoadClass][borderIndex].setLineWidthUnit(TLcdDistanceUnit.METRE_UNIT);
    fRoadStylesHQ[aRoadClass][borderIndex].setLineWidth(aWidth);

    int innerIndex = getIndex(ROAD_INNER);
    fRoadStylesHQ[aRoadClass][innerIndex] = new TLcdMapG2DLineStyle();
    fRoadStylesHQ[aRoadClass][innerIndex].setAntiAliasing(true);
    fRoadStylesHQ[aRoadClass][innerIndex].setColor(aInnerColor);
    fRoadStylesHQ[aRoadClass][innerIndex].setLineWidthUnit(TLcdDistanceUnit.METRE_UNIT);
    fRoadStylesHQ[aRoadClass][innerIndex].setLineWidth(aWidth - 6);
    fRoadStylesHQ[aRoadClass][innerIndex].setSelectionColor(Color.magenta);
    fRoadStylesHQ[aRoadClass][innerIndex].setSelectionLineWidth(aWidth - 6);
  }

  private void registerLowRoadStyle(int aRoadClass,
                                    Color aBorderColor) {
    registerLowRoadStyle(aRoadClass, aBorderColor, getIndex(ALL_ROAD_BORDER));
    registerLowRoadStyle(aRoadClass, aBorderColor, getIndex(ROAD_INNER));
  }

  private void registerLowRoadStyle(int aRoadClass, Color aBorderColor, int aBorderIndex) {
    fRoadStylesLQ[aRoadClass][aBorderIndex] = new TLcdGXYPainterColorStyle() {
      @Override
      public void setupGraphics(Graphics aGraphics, Object aObject, int aMode, ILcdGXYContext aGXYContext) {
        super.setupGraphics(aGraphics, aObject, aMode, aGXYContext);
        ((Graphics2D) aGraphics).setStroke(new BasicStroke());
      }
    };
    fRoadStylesLQ[aRoadClass][aBorderIndex].setDefaultColor(aBorderColor);
    fRoadStylesLQ[aRoadClass][aBorderIndex].setSelectionColor(Color.magenta);
  }

  private int getIndex(Mode aMode) {
    switch (aMode) {
    case ALL_ROAD_BORDER:
      return 0;
    case ROAD_INNER:
      return 1;
    case ROAD_SIGNS:
      return 2;
    }
    throw new IllegalArgumentException();
  }

  /**
   * This class paints arrow icons with different orientations.
   */
  private class ArrowIcon implements ILcdIcon {

    private int fRadius = 4;
    private double fRotation;
    private Color fColor = new Color(150, 180, 200);

    public void setRotation(double aRotation) {
      fRotation = aRotation;
    }

    public void setRadius(int aRadius) {
      fRadius = aRadius;
    }

    public void paintIcon(Component component, Graphics graphics, int x, int y) {
      x += 2;
      y += 2;
      graphics.setColor(fColor);

      ((Graphics2D) graphics).setStroke(new BasicStroke(fRadius / 1.8f));

      int[] x_points = new int[]{(int) (x + fRadius * Math.cos(fRotation + Math.PI * 3 / 4)),
                                 (int) (x + fRadius * Math.cos(fRotation)),
                                 (int) (x + fRadius * Math.cos(fRotation + Math.PI * 5 / 4))};
      int[] y_points = new int[]{(int) (y + fRadius * Math.sin(fRotation + Math.PI * 3 / 4)),
                                 (int) (y + fRadius * Math.sin(fRotation)),
                                 (int) (y + fRadius * Math.sin(fRotation + Math.PI * 5 / 4))};

      graphics.fillPolygon(x_points, y_points, 3);

    }

    public int getIconWidth() {
      return 5;
    }

    public int getIconHeight() {
      return 5;
    }

    public Object clone() {
      return new ArrowIcon();
    }

  }

  /**
   * This class paints block icons with different orientations.
   */
  private class BlockIcon implements ILcdIcon {

    private int fRadius = 6;

    public void setRadius(int aRadius) {
      fRadius = aRadius;
    }

    public void paintIcon(Component component, Graphics graphics, int x, int y) {

      graphics.setColor(Color.red);
      ((Graphics2D) graphics).setStroke(new BasicStroke(fRadius / 1.8f));
      graphics.fillOval(x, y, fRadius * 2, fRadius * 2);

      graphics.setColor(Color.white);
      ((Graphics2D) graphics).setStroke(new BasicStroke(fRadius / 1.6f));
      int[] x_points = new int[]{x + fRadius * 2 / 3, x + fRadius * 4 / 3};
      int[] y_points = new int[]{y + fRadius, y + fRadius};
      graphics.drawPolyline(x_points, y_points, 2);

    }

    public int getIconWidth() {
      return fRadius * 2;
    }

    public int getIconHeight() {
      return fRadius * 2;
    }

    public Object clone() {
      return new ArrowIcon();
    }

  }

  /**
   * Wrapper painter style overriding the color.
   */
  private static class HighlightStyle implements ILcdGXYPainterStyle {

    private ILcdGXYPainterStyle fWrappedStyle;
    private Color fHighlightColor;

    private HighlightStyle(ILcdGXYPainterStyle aWrappedStyle,
                           Color aHighlightColor) {
      fWrappedStyle = aWrappedStyle;
      fHighlightColor = aHighlightColor;
    }

    public void setupGraphics(Graphics aGraphics, Object aObject, int aMode, ILcdGXYContext aGXYContext) {
      fWrappedStyle.setupGraphics(aGraphics, aObject, aMode, aGXYContext);
      aGraphics.setColor(fHighlightColor);
    }
  }

}
