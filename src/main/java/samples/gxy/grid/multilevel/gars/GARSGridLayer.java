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
package samples.gxy.grid.multilevel.gars;

import java.awt.BasicStroke;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;

import com.luciad.geodesy.ILcdGeodeticDatum;
import com.luciad.geodesy.TLcdGeodeticDatum;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.shape2D.TLcdLonLatBounds;
import com.luciad.shape.shape2D.TLcdXYPoint;
import com.luciad.shape.shape3D.TLcdLonLatHeightPoint;
import com.luciad.util.TLcdOutOfBoundsException;
import com.luciad.view.gxy.ILcdGXYContext;
import com.luciad.view.gxy.ILcdGXYPainterStyle;
import com.luciad.view.gxy.ILcdGXYPen;
import com.luciad.view.gxy.ILcdNotLabelDeconflictableLayer;
import com.luciad.view.map.TLcdGeodeticPen;
import com.luciad.view.map.multilevelgrid.ILcdMultilevelGridCoordinate;
import com.luciad.view.map.multilevelgrid.TLcdMultilevelGridLayer;
import com.luciad.view.map.multilevelgrid.TLcdMultilevelGridUtil;

import samples.gxy.grid.GridLayerFactory;

/**
 * A layer containing a GARS grid.
 */
public class GARSGridLayer
    extends TLcdMultilevelGridLayer
    implements ILcdNotLabelDeconflictableLayer {

  // temp variables for label painting.
  private TLcdLonLatHeightPoint fTempLabelPointModel = new TLcdLonLatHeightPoint();
  private TLcdXYPoint fTempLabelPointXYWorld = new TLcdXYPoint();
  private Point fTempLabelPointAWT = new Point();
  private TLcdLonLatBounds fTempModelBounds = new TLcdLonLatBounds();

  // font for label painting.
  private Font fQuadrantCellFont = new Font("Arial", Font.BOLD, 16);

  /**
   * Creates a GARS grid layer, using WGS84 as datum.
   */
  public GARSGridLayer() {
    this(new TLcdGeodeticDatum());
  }

  /**
   * Creates a GARS grid layer, starting spanning the whole world, with a given datum
   *
   * @param aDatum the datum the GARS is based on.
   */
  public GARSGridLayer(ILcdGeodeticDatum aDatum) {
    super(new GARSGrid(), new TLcdGeodeticReference(aDatum));
    init();
  }

  private void init() {
    TLcdGeodeticPen pen = new TLcdGeodeticPen(false);
    // Improves the discretization when grid lines of different levels overlap.
    pen.setAngleThreshold(0.01d);
    super.setGXYPen(pen);

    setLevelStyle(GARSGridCoordinate.CELL, new CellStyle());
    setLevelStyle(GARSGridCoordinate.QUADRANT, new QuadrantStyle());
    setLevelStyle(GARSGridCoordinate.KEYPAD, new KeypadStyle());

    setMultilevelGridCoordinateFormat(new GARSCoordinateFormat());
  }

  public boolean isLabelLevel(int aLevel, ILcdGXYContext aGXYContext) {
    // we delay labeling slightly since the labels may be too large
    // zooming in increments the scale, so we have the multiple the low scale with a factor >  1.
    double[] level_switch_scales = getLevelSwitchScales();
    double view_scale = aGXYContext.getGXYView().getScale();

    double low_scale = level_switch_scales[aLevel];
    return (view_scale > low_scale * 2.0);
  }

  /**
   * Always returns "GARS grid"
   *
   * @return GARS grid
   */
  public String getLabel() {
    return "GARS grid";
  }

  /**
   * The outline of a GARS grid layer does not need to be painted.
   *
   * @return always false.
   */
  public boolean isPaintOutline(ILcdGXYContext aGXYContext) {
    return false;
  }

  /**
   * Sets the format with which the labels for the grid elements will be formatted.
   *
   * @param aGARSCoordinateFormat formats the GARS grid elements into human readable form for labeling.
   */
  public void setGARSCoordinateFormat(GARSCoordinateFormat aGARSCoordinateFormat) {
    super.setMultilevelGridCoordinateFormat(aGARSCoordinateFormat);
  }

  public GARSCoordinateFormat getGARSCoordinateFormat() {
    return (GARSCoordinateFormat) super.getMultilevelGridCoordinateFormat();
  }

  /**
   * Empty implementation, this layer always renders its lines with a <code>TLcdGeodeticPen</code>.
   *
   * @param aPen not taken into account.
   */
  public final void setGXYPen(ILcdGXYPen aPen) {
  }

  /**
   * Returns the GARS grid on which this layer is based.
   *
   * @return the GARS grid on which this layer is based.
   */
  public GARSGrid getGARSGrid() {
    return (GARSGrid) super.getMultilevelGrid();
  }

  /**
   * Renders different labels depending on the level of the multilevel grid coordinate and the levels
   * that will be rendered. This implementation depends on the implementation that when a level is rendered
   * all levels above are also rendered.
   *
   * @param aMultilevelGridCoordinate the multilevelgrid coordinate to render the label for.
   * @param aGXYContext               the context in which to render the label.
   * @param aGraphics                 the Graphics on which to render the label.
   */
  protected void paintLabel(ILcdMultilevelGridCoordinate aMultilevelGridCoordinate,
                            ILcdGXYContext aGXYContext,
                            Graphics aGraphics) {

    StringBuffer label_buffer = new StringBuffer();
    aGraphics.setColor(GridLayerFactory.GRID_LABEL_COLOR);

    // which levels are being painted now ?
    if (isPaintLevel(GARSGridCoordinate.KEYPAD, aGXYContext)) {
      // keypads are being rendered, this means also cells and quadrnats are rendered.
      int level = aMultilevelGridCoordinate.getCoordinateLevelCount() - 1;

      // for keypads, find the center of the coordinate and render the appropriate label.
      if (level == GARSGridCoordinate.KEYPAD) {
        try {
          findPointAWTat(aMultilevelGridCoordinate, aGXYContext, TLcdMultilevelGridUtil.CENTER, fTempLabelPointAWT);
          getGARSCoordinateFormat().formatKeypad(label_buffer,
                                                 GARSGridUtil.getKeypad(aMultilevelGridCoordinate));
          String label = label_buffer.toString();
          int label_width = aGraphics.getFontMetrics().stringWidth(label);
          int label_height = aGraphics.getFontMetrics().getHeight();
          aGraphics.drawString(label, (int) (fTempLabelPointAWT.getX() - label_width / 2.0), (int) (fTempLabelPointAWT.getY() + label_height / 2));
        } catch (TLcdOutOfBoundsException e) {
          // the coordinate is not defined in this grid, so we don't have to render a label.
          // this should not happen.
        }
      }
      // for quadrants, render the cell and the quadrant information in the corner
      else if (level == GARSGridCoordinate.QUADRANT) {
        int position = TLcdMultilevelGridUtil.LOWER_LEFT;
        switch (GARSGridUtil.getQuadrant(aMultilevelGridCoordinate)) {
        case GARSGridCoordinate.QUADRANT_NE:
          position = TLcdMultilevelGridUtil.UPPER_RIGHT;
          break;
        case GARSGridCoordinate.QUADRANT_NW:
          position = TLcdMultilevelGridUtil.UPPER_LEFT;
          break;
        case GARSGridCoordinate.QUADRANT_SE:
          position = TLcdMultilevelGridUtil.LOWER_RIGHT;
          break;
        case GARSGridCoordinate.QUADRANT_SW:
          position = TLcdMultilevelGridUtil.LOWER_LEFT;
          break;
        }
        try {
          // find the center point
          findPointAWTat(aMultilevelGridCoordinate, aGXYContext, TLcdMultilevelGridUtil.CENTER, fTempLabelPointAWT);
          double center_x = fTempLabelPointAWT.getX();
          double center_y = fTempLabelPointAWT.getY();
          findPointAWTat(aMultilevelGridCoordinate, aGXYContext, position, fTempLabelPointAWT);
          // move the point slightly towards the center of the quadrant.
          double factor = 10.0;
          fTempLabelPointAWT.translate((int) ((center_x - fTempLabelPointAWT.getX()) / factor),
                                       (int) ((center_y - fTempLabelPointAWT.getY()) / factor));

          aGraphics.setFont(fQuadrantCellFont);
          getGARSCoordinateFormat().formatCell(label_buffer,
                                               GARSGridUtil.getCellColumn(aMultilevelGridCoordinate),
                                               GARSGridUtil.getCellRow(aMultilevelGridCoordinate));
          getGARSCoordinateFormat().formatQuadrant(label_buffer,
                                                   GARSGridUtil.getQuadrant(aMultilevelGridCoordinate));
          String label = label_buffer.toString();
          int label_width = aGraphics.getFontMetrics().stringWidth(label);
          int label_height = aGraphics.getFontMetrics().getHeight();
          if (position == TLcdMultilevelGridUtil.UPPER_RIGHT || position == TLcdMultilevelGridUtil.LOWER_RIGHT) {
            fTempLabelPointAWT.translate(-label_width, 0);
          }
          if (position == TLcdMultilevelGridUtil.UPPER_LEFT || position == TLcdMultilevelGridUtil.UPPER_RIGHT) {
            fTempLabelPointAWT.translate(0, label_height);
          }

          // are we now outside the view ? Try to put a label in one of views corners
          if ((fTempLabelPointAWT.getX() < 0) ||
              (fTempLabelPointAWT.getX() > aGXYContext.getGXYView().getWidth() - label_width) ||
              (fTempLabelPointAWT.getY() < label_height) ||
              (fTempLabelPointAWT.getY() > aGXYContext.getGXYView().getHeight())) {

            TLcdMultilevelGridUtil.pointAtSFCT(aMultilevelGridCoordinate,
                                               getGARSGrid(),
                                               TLcdMultilevelGridUtil.LOWER_LEFT,
                                               fTempLabelPointModel);
            fTempModelBounds.move2D(fTempLabelPointModel);
            // quadrants have a width and height of half a cell, this is a quarter of a degree
            fTempModelBounds.setWidth(0.25);
            fTempModelBounds.setHeight(0.25);

            // find the corners of the view that are inside this quadrant.
            int border = 5;
            // top left corner
            int corner_x = border + label_width / 2;
            int corner_y = border + label_height;
            paintQuadrantCornerLabel(corner_x, corner_y, aGXYContext, aGraphics, label);

            // bottom left corner
            corner_x = border + label_width / 2;
            corner_y = aGXYContext.getGXYView().getHeight() - border;
            paintQuadrantCornerLabel(corner_x, corner_y, aGXYContext, aGraphics, label);

            // top right corner
            corner_x = aGXYContext.getGXYView().getWidth() - (border + label_width);
            corner_y = border + label_height;
            paintQuadrantCornerLabel(corner_x, corner_y, aGXYContext, aGraphics, label);

            // bottom right corner
            corner_x = aGXYContext.getGXYView().getWidth() - (border + label_width);
            corner_y = aGXYContext.getGXYView().getHeight() - border;
            paintQuadrantCornerLabel(corner_x, corner_y, aGXYContext, aGraphics, label);
          } else {
            aGraphics.drawString(label, (int) fTempLabelPointAWT.getX(), (int) fTempLabelPointAWT.getY());
          }
        } catch (TLcdOutOfBoundsException e) {
          // the location is not in the world, we do no need to render a label.
          // this should not happen, though.
        }
      } else if (level == GARSGridCoordinate.CELL) {
        // we don't do anything, since the cell information is already included in the quadrant.
      }
    } else if (isPaintLevel(GARSGridCoordinate.QUADRANT, aGXYContext)) {

      int level = aMultilevelGridCoordinate.getCoordinateLevelCount() - 1;

      // find the center of the coordinate and render the appropriate label.
      try {
        findPointAWTat(aMultilevelGridCoordinate, aGXYContext, TLcdMultilevelGridUtil.CENTER, fTempLabelPointAWT);
        if (level == GARSGridCoordinate.CELL) {
          getGARSCoordinateFormat().formatCell(label_buffer,
                                               GARSGridUtil.getCellColumn(aMultilevelGridCoordinate),
                                               GARSGridUtil.getCellRow(aMultilevelGridCoordinate));
        } else if (level == GARSGridCoordinate.QUADRANT) {
          int keypad = GARSGridUtil.getQuadrant(aMultilevelGridCoordinate);
          getGARSCoordinateFormat().formatQuadrant(label_buffer, keypad);
        }
        String label = label_buffer.toString();
        int label_width = aGraphics.getFontMetrics().stringWidth(label);
        int label_height = aGraphics.getFontMetrics().getHeight();
        if (level == GARSGridCoordinate.CELL) {
          aGraphics.drawString(label, (int) (fTempLabelPointAWT.getX() + 2), (int) (fTempLabelPointAWT.getY() - 2));
        } else if (level == GARSGridCoordinate.QUADRANT) {
          aGraphics.drawString(label, (int) (fTempLabelPointAWT.getX() - label_width / 2.0), (int) (fTempLabelPointAWT.getY() + label_height / 2));
        }
      } catch (TLcdOutOfBoundsException e) {
        // the coordinate is not defined in this grid, so we don't have to render a label.
        // this should not happen.
      }
    } else if (isPaintLevel(GARSGridCoordinate.CELL, aGXYContext)) {
      // we only see cells, the default will do
      super.paintLabel(aMultilevelGridCoordinate, aGXYContext, aGraphics);
    }
  }

  private void paintQuadrantCornerLabel(int aCorner_x, int aCorner_y, ILcdGXYContext aGXYContext, Graphics aGraphics, String aLabel) {
    fTempLabelPointAWT.move(aCorner_x, aCorner_y);
    aGXYContext.getGXYViewXYWorldTransformation().viewAWTPoint2worldSFCT(fTempLabelPointAWT, fTempLabelPointXYWorld);
    try {
      aGXYContext.getModelXYWorldTransformation().worldPoint2modelSFCT(fTempLabelPointXYWorld, fTempLabelPointModel);
      if (fTempModelBounds.contains2D((ILcdPoint) fTempLabelPointModel)) {
        aGraphics.drawString(aLabel, (int) fTempLabelPointAWT.getX(), (int) fTempLabelPointAWT.getY());
      }
    } catch (TLcdOutOfBoundsException e) {
      // the corner point is not part of the world, we cannot render a label there
    }
  }

  /**
   * Moves the point passed to the center point of a multilevel grid coordinate in the given context.
   *
   * @param aMultilevelGridCoordinate the multilevel grid coordinate to determine the center for.
   * @param aGXYContext               the context in which the center should be determined
   * @param aRelativeLocation         the location relative in the multilevel grid coordinate to move the point to.
   * @param aPointAWTSFCT             the point to move the center of the multilevel grid coordinate.
   * @throws TLcdOutOfBoundsException when the multilevel grid coordinate cannot be defined in the grid of this layer.
   */
  private void findPointAWTat(ILcdMultilevelGridCoordinate aMultilevelGridCoordinate,
                              ILcdGXYContext aGXYContext,
                              int aRelativeLocation,
                              Point aPointAWTSFCT) throws TLcdOutOfBoundsException {
    TLcdMultilevelGridUtil.pointAtSFCT(aMultilevelGridCoordinate,
                                       getMultilevelGrid(),
                                       aRelativeLocation,
                                       fTempLabelPointModel);
    aGXYContext.getModelXYWorldTransformation().modelPoint2worldSFCT(fTempLabelPointModel, fTempLabelPointXYWorld);
    aGXYContext.getGXYViewXYWorldTransformation().worldPoint2viewAWTPointSFCT(fTempLabelPointXYWorld, aPointAWTSFCT);
  }

  /**
   * Renders cells with gray lines with a width that changes whether quadrants are rendered or not.
   */
  private class CellStyle implements ILcdGXYPainterStyle {

    private BasicStroke fCellStroke = new BasicStroke(1);
    private BasicStroke fCellWideStroke = new BasicStroke(2);
    private BasicStroke fCellVeryWideStroke = new BasicStroke(3);

    public void setupGraphics(Graphics aGraphics,
                              Object aObject,
                              int aMode,
                              ILcdGXYContext aGXYContext) {

      Graphics2D graphics_2d = (Graphics2D) aGraphics;

      graphics_2d.setColor(GridLayerFactory.GRID_SUB_COLOR);

      // will there be any quadrants or keypads rendered ?
      if (isPaintLevel(GARSGridCoordinate.KEYPAD, aGXYContext)) {
        graphics_2d.setStroke(fCellVeryWideStroke);
      }
      if (isPaintLevel(GARSGridCoordinate.QUADRANT, aGXYContext)) {
        graphics_2d.setStroke(fCellWideStroke);
      } else {
        graphics_2d.setStroke(fCellStroke);
      }
    }
  }

  /**
   * Renders quadrants with dark gray lines with a width that changes whether keypads are rendered or not.
   */
  private class QuadrantStyle implements ILcdGXYPainterStyle {

    private BasicStroke fQuadrantStroke = new BasicStroke(1);

    public void setupGraphics(Graphics aGraphics,
                              Object aObject,
                              int aMode,
                              ILcdGXYContext aGXYContext) {

      Graphics2D graphics_2d = (Graphics2D) aGraphics;
      graphics_2d.setColor(GridLayerFactory.GRID_COLOR);
      graphics_2d.setStroke(fQuadrantStroke);
    }
  }

  /**
   * Renders keypads with dark gray dashed lines.
   */
  private class KeypadStyle implements ILcdGXYPainterStyle {

    private BasicStroke fKeypadStroke = new BasicStroke(1,
                                                        BasicStroke.CAP_BUTT,
                                                        BasicStroke.JOIN_BEVEL,
                                                        0.0f,
                                                        new float[]{16.0f, 8.0f},
                                                        0.0f);

    public void setupGraphics(Graphics aGraphics,
                              Object aObject,
                              int aMode,
                              ILcdGXYContext aGXYContext) {

      Graphics2D graphics_2d = (Graphics2D) aGraphics;
      graphics_2d.setColor(GridLayerFactory.GRID_COLOR);
      graphics_2d.setStroke(fKeypadStroke);
    }
  }

}
