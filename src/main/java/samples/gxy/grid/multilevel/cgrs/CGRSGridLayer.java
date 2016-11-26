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
package samples.gxy.grid.multilevel.cgrs;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;

import com.luciad.geodesy.ILcdGeodeticDatum;
import com.luciad.geodesy.TLcdGeodeticDatum;
import com.luciad.reference.ILcdGeoReference;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.shape2D.TLcdXYPoint;
import com.luciad.shape.shape3D.TLcdLonLatHeightPoint;
import com.luciad.util.TLcdOutOfBoundsException;
import com.luciad.view.gxy.ILcdGXYContext;
import com.luciad.view.gxy.ILcdGXYPainterStyle;
import com.luciad.view.gxy.ILcdGXYPen;
import com.luciad.view.gxy.ILcdNotLabelDeconflictableLayer;
import com.luciad.view.gxy.TLcdG2DLineStyle;
import com.luciad.view.map.TLcdGeodeticPen;
import com.luciad.view.map.multilevelgrid.ILcdMultilevelGridCoordinate;
import com.luciad.view.map.multilevelgrid.TLcdMultilevelGridLayer;
import com.luciad.view.map.multilevelgrid.TLcdMultilevelGridUtil;

import samples.gxy.grid.GridLayerFactory;

/**
 * A <code>ILcdGXYLayer</code> implementation that displays a Common Grid Reference System grid.
 */
public class CGRSGridLayer extends TLcdMultilevelGridLayer
    implements ILcdNotLabelDeconflictableLayer {

  // temp variables for label painting.
  private TLcdLonLatHeightPoint fTempLabelPointModel = new TLcdLonLatHeightPoint();
  private TLcdXYPoint fTempLabelPointXYWorld = new TLcdXYPoint();
  private Point fTempLabelPointAWT = new Point();
  private CGRSGridCoordinate fTempGridCoordinate = new CGRSGridCoordinate();

  // fonts for label painting.
  private Font fQuadrantKeypadFont = new Font("Arial", Font.BOLD, 14);
  private Font fQuadrantCellFont = new Font("Arial", Font.BOLD, 16);

  /**
   * Creates a CGRS grid layer, starting from aLowerLeftPoint to aUpperRightPoint with a given datum
   * @param aLowerLeftPoint the lower left point of the cgrs grid layer
   * @param aUpperRightPoint the upper right point of the cgrs grid layer
   * @param aDatum the datum the cgrs is based on.
   */
  public CGRSGridLayer(ILcdPoint aLowerLeftPoint, ILcdPoint aUpperRightPoint, ILcdGeodeticDatum aDatum) {
    super(new CGRSGrid(aLowerLeftPoint, aUpperRightPoint), new TLcdGeodeticReference(aDatum));
    init();
  }

  /**
   * Creates a CGRS grid layer from a CGRS grid and a datum.
   * @param aCGRSGrid the grid to display in the layer.
   * @param aDatum the datum the cgrs is based on.
   */
  public CGRSGridLayer(CGRSGrid aCGRSGrid, ILcdGeodeticDatum aDatum) {
    super(aCGRSGrid, new TLcdGeodeticReference(aDatum));
    init();
  }

  /**
   * Creates a CGRS grid layer, starting from aLowerLeftPoint to aUpperRightPoint, using WGS84 as datum
   * @param aLowerLeftPoint the lower left point of the cgrs grid layer
   * @param aUpperRightPoint the upper right point of the cgrs grid layer
   */
  public CGRSGridLayer(ILcdPoint aLowerLeftPoint, ILcdPoint aUpperRightPoint) {
    this(aLowerLeftPoint, aUpperRightPoint, new TLcdGeodeticDatum());
  }

  private void init() {
    TLcdGeodeticPen pen = new TLcdGeodeticPen(false);
    // Improves the discretization when grid lines of different levels overlap.
    pen.setAngleThreshold(0.01d);
    super.setGXYPen(pen);

    TLcdG2DLineStyle outline_style = new TLcdG2DLineStyle();
    outline_style.setColor(Color.blue);
    outline_style.setLineWidth(3);
    setOutlineStyle(outline_style);

    setLevelStyle(CGRSGridCoordinate.CELL, new CellStyle());
    setLevelStyle(CGRSGridCoordinate.KEYPAD, new KeypadStyle());
    setLevelStyle(CGRSGridCoordinate.QUADRANT, new QuadrantStyle());

    setCGRSCoordinateFormat(new CGRSCoordinateFormat());
  }

  public boolean isLabelLevel(int aLevel, ILcdGXYContext aGXYContext) {
    // we delay labeling slightly since the labels may be too large
    // zooming in increments the scale, so we have the multiple the low scale with a factor >  1.
    double[] level_switch_scales = getLevelSwitchScales();
    double view_scale = aGXYContext.getGXYView().getScale();

    double low_scale = level_switch_scales[aLevel];
    return (view_scale > low_scale * 1.5);
  }

  /**
   * Always returns "CGRS grid".
   * @return CGRS grid
   */
  public String getLabel() {
    return "CGRS grid";
  }

  /**
   * Sets the format with which the labels for the grid elements will be formatted.
   * @param aCGRSCoordinateFormat formats the CGRS grid elements into human readable form for labeling. 
   */
  public void setCGRSCoordinateFormat(CGRSCoordinateFormat aCGRSCoordinateFormat) {
    super.setMultilevelGridCoordinateFormat(aCGRSCoordinateFormat);
  }

  public CGRSCoordinateFormat getCGRSCoordinateFormat() {
    return (CGRSCoordinateFormat) super.getMultilevelGridCoordinateFormat();
  }

  public final ILcdGXYPen getGXYPen() {
    return super.getGXYPen();
  }

  /**
   * Empty implementation, this layer always renders its lines with a <code>TLcdGeodeticPen</code>.
   * @param aPen not taken into account.
   */
  public final void setGXYPen(ILcdGXYPen aPen) {
  }

  /**
   * Returns the CGRS grid on which this layer is based.
   * @return the CGRS grid on which this layer is based.
   */
  public CGRSGrid getCGRSGrid() {
    return (CGRSGrid) super.getMultilevelGrid();
  }

  /**
   * Renders different labels depending on the level of the multilevel grid coordinate and the levels
   * that will be rendered. This implementation depends on the implementation that when a level is rendered
   * all levels above are also rendered.
   * @param aMultilevelGridCoordinate the multilevelgrid coordinate to render the label for.
   * @param aGXYContext the context in which to render the label.
   * @param aGraphics the Graphics on which to render the label.
   */
  protected void paintLabel(ILcdMultilevelGridCoordinate aMultilevelGridCoordinate,
                            ILcdGXYContext aGXYContext,
                            Graphics aGraphics) {

    // all labels have the same color.
    aGraphics.setColor(GridLayerFactory.GRID_LABEL_COLOR);

    StringBuffer label_buffer = new StringBuffer();

    // which levels are being painted now ?
    if (isPaintLevel(CGRSGridCoordinate.QUADRANT, aGXYContext)) {
      // quadrants are being rendered, this means also cells and keypads are rendered.
      int level = aMultilevelGridCoordinate.getCoordinateLevelCount() - 1;

      // for quadrants and keypads, find the center of the coordinate and render the appropriate label.
      if (level == CGRSGridCoordinate.KEYPAD || level == CGRSGridCoordinate.QUADRANT) {
        try {
          findCenterPointAWT(aMultilevelGridCoordinate, aGXYContext, fTempLabelPointAWT);
          if (level == CGRSGridCoordinate.KEYPAD) {
            getCGRSCoordinateFormat().formatKeypad(label_buffer,
                                                   CGRSGridUtil.getKeypad(aMultilevelGridCoordinate));
          } else {
            getCGRSCoordinateFormat().formatQuadrant(label_buffer,
                                                     CGRSGridUtil.getQuadrant(aMultilevelGridCoordinate));
          }
          String label = label_buffer.toString();
          if (level == CGRSGridCoordinate.KEYPAD) {
            aGraphics.setFont(fQuadrantKeypadFont);
          }
          int label_width = aGraphics.getFontMetrics().stringWidth(label);
          int label_height = aGraphics.getFontMetrics().getHeight();
          if (level == CGRSGridCoordinate.KEYPAD) {
            aGraphics.drawString(label, (int) (fTempLabelPointAWT.getX() + 2), (int) (fTempLabelPointAWT.getY() - 2));
          } else {
            aGraphics.drawString(label, (int) (fTempLabelPointAWT.getX() - label_width / 2.0), (int) (fTempLabelPointAWT.getY() + label_height / 2));
          }
        } catch (TLcdOutOfBoundsException e) {
          // the coordinate is not defined in this grid, so we don't have to render a label.
          // this should not happen.
        }
      } else if (level == CGRSGridCoordinate.CELL) {

        // find the center point in AWT coordinates
        double center_x = -1;
        double center_y = -1;
        boolean center_in_world = true;
        try {
          findCenterPointAWT(aMultilevelGridCoordinate, aGXYContext, fTempLabelPointAWT);
          center_x = fTempLabelPointAWT.getX();
          center_y = fTempLabelPointAWT.getY();
        } catch (TLcdOutOfBoundsException e) {
          // the center of the cell is outside the world.
          center_in_world = false;
        }

        // render the cell coordinate at the four corners of the cell.
        int[] locations = new int[]{
            TLcdMultilevelGridUtil.LOWER_LEFT,
            TLcdMultilevelGridUtil.LOWER_RIGHT,
            TLcdMultilevelGridUtil.UPPER_LEFT,
            TLcdMultilevelGridUtil.UPPER_RIGHT,
        };
        aGraphics.setFont(fQuadrantCellFont);
        getCGRSCoordinateFormat().formatCell(label_buffer,
                                             CGRSGridUtil.getCellColumn(aMultilevelGridCoordinate),
                                             CGRSGridUtil.getCellRow(aMultilevelGridCoordinate));
        String label = label_buffer.toString();
        int label_width = aGraphics.getFontMetrics().stringWidth(label);
        int label_height = aGraphics.getFontMetrics().getHeight();

        boolean cell_label_rendered = false;
        int view_border = 10;
        Rectangle view_rectangle = new Rectangle(view_border,
                                                 view_border,
                                                 aGXYContext.getGXYView().getWidth() - view_border * 2,
                                                 aGXYContext.getGXYView().getHeight() - view_border * 2);
        for (int location_index = 0; location_index < locations.length; location_index++) {
          int location = locations[location_index];
          try {
            TLcdMultilevelGridUtil.pointAtSFCT(aMultilevelGridCoordinate,
                                               getMultilevelGrid(),
                                               location,
                                               fTempLabelPointModel);
            aGXYContext.getModelXYWorldTransformation().modelPoint2worldSFCT(fTempLabelPointModel, fTempLabelPointXYWorld);
            aGXYContext.getGXYViewXYWorldTransformation().worldPoint2viewAWTPointSFCT(fTempLabelPointXYWorld, fTempLabelPointAWT);
            if (center_in_world) {
              // move the point slightly towards the center
              double factor = 12.0;
              fTempLabelPointAWT.translate((int) ((center_x - fTempLabelPointAWT.getX()) / factor),
                                           (int) ((center_y - fTempLabelPointAWT.getY()) / factor));
            }
            aGraphics.drawString(label, (int) (fTempLabelPointAWT.getX() - label_width / 2.0),
                                 (int) (fTempLabelPointAWT.getY() + label_height / 2.0));

            cell_label_rendered |= view_rectangle.contains(fTempLabelPointAWT);
          } catch (TLcdOutOfBoundsException e) {
            // the point is not visible in the world, so we don't have to render a label near it
          }
        }
        if (!cell_label_rendered) {
          // we did not render any cell labels, check if the cell contains one of the views corners and if
          // so, put a label there.

          double[][] view_corners = new double[][]{
              new double[]{5, label_height + 5},
              new double[]{aGXYContext.getGXYView().getWidth() - 5 - label_width, label_height + 5},
              new double[]{aGXYContext.getGXYView().getWidth() - 5 - label_width,
                           aGXYContext.getGXYView().getHeight() - label_height - 5},
              new double[]{5, aGXYContext.getGXYView().getHeight() - label_height - 5},
          };

          for (int corner_index = 0; corner_index < view_corners.length; corner_index++) {
            try {
              fTempLabelPointAWT.move((int) view_corners[corner_index][0], (int) view_corners[corner_index][1]);
              aGXYContext.getGXYViewXYWorldTransformation().viewAWTPoint2worldSFCT(fTempLabelPointAWT, fTempLabelPointXYWorld);
              aGXYContext.getModelXYWorldTransformation().worldPoint2modelSFCT(fTempLabelPointXYWorld, fTempLabelPointModel);
              TLcdMultilevelGridUtil.multilevelCoordinateAtSFCT(
                  fTempLabelPointModel,
                  1,
                  getMultilevelGrid(),
                  (ILcdGeoReference) getModel().getModelReference(),
                  fTempGridCoordinate);
              if (fTempGridCoordinate.getCoordinate(0, 0) == aMultilevelGridCoordinate.getCoordinate(0, 0) &&
                  fTempGridCoordinate.getCoordinate(0, 1) == aMultilevelGridCoordinate.getCoordinate(0, 1)) {
                aGraphics.drawString(label, (int) view_corners[corner_index][0], (int) view_corners[corner_index][1]);
              }
            } catch (TLcdOutOfBoundsException e) {
              // it's out of bounds, we don't draw anything.
            }
          }
        }
      }
    } else if (isPaintLevel(CGRSGridCoordinate.KEYPAD, aGXYContext)) {

      int level = aMultilevelGridCoordinate.getCoordinateLevelCount() - 1;

      // find the center of the coordinate and render the appropriate label.
      try {
        findCenterPointAWT(aMultilevelGridCoordinate, aGXYContext, fTempLabelPointAWT);
        if (level == CGRSGridCoordinate.CELL) {
          getCGRSCoordinateFormat().formatCell(label_buffer,
                                               CGRSGridUtil.getCellColumn(aMultilevelGridCoordinate),
                                               CGRSGridUtil.getCellRow(aMultilevelGridCoordinate));
        } else if (level == CGRSGridCoordinate.KEYPAD) {
          int keypad = CGRSGridUtil.getKeypad(aMultilevelGridCoordinate);
          // we don't want to show a label in the center keypad.
          if (keypad != 5) {
            getCGRSCoordinateFormat().formatKeypad(label_buffer,
                                                   keypad);
          }
        }
        String label = label_buffer.toString();
        int label_width = aGraphics.getFontMetrics().stringWidth(label);
        int label_height = aGraphics.getFontMetrics().getHeight();
        if (level == CGRSGridCoordinate.CELL) {
          aGraphics.setFont(fQuadrantCellFont);
        }
        aGraphics.drawString(label, (int) (fTempLabelPointAWT.getX() - label_width / 2.0), (int) (fTempLabelPointAWT.getY() + label_height / 2));
      } catch (TLcdOutOfBoundsException e) {
        // the coordinate is not defined in this grid, so we don't have to render a label.
        // this should not happen.
      }
    } else if (isPaintLevel(CGRSGridCoordinate.CELL, aGXYContext)) {
      // we only see cells, the default will do, just set a color
      super.paintLabel(aMultilevelGridCoordinate, aGXYContext, aGraphics);
    }
  }

  /**
   * Moves the point passed to the center point of a multilevel grid coordinate in the given context.
   * @param aMultilevelGridCoordinate the multilevel grid coordinate to determine the center for.
   * @param aGXYContext the context in which the center should be determined
   * @param aPointAWTSFCT the point to move the center of the multilevel grid coordinate.
   * @throws TLcdOutOfBoundsException when the multilevel grid coordinate cannot be defined in the grid of this layer.
   */
  private void findCenterPointAWT(ILcdMultilevelGridCoordinate aMultilevelGridCoordinate,
                                  ILcdGXYContext aGXYContext,
                                  Point aPointAWTSFCT) throws TLcdOutOfBoundsException {
    TLcdMultilevelGridUtil.pointAtSFCT(aMultilevelGridCoordinate,
                                       getMultilevelGrid(),
                                       TLcdMultilevelGridUtil.CENTER,
                                       fTempLabelPointModel);
    aGXYContext.getModelXYWorldTransformation().modelPoint2worldSFCT(fTempLabelPointModel, fTempLabelPointXYWorld);
    aGXYContext.getGXYViewXYWorldTransformation().worldPoint2viewAWTPointSFCT(fTempLabelPointXYWorld, aPointAWTSFCT);
  }

  /**
   * Renders cells with gray lines with a width that changes whether keypads are rendered or not.
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

      // will there be any keypads rendered ?
      if (isPaintLevel(CGRSGridCoordinate.QUADRANT, aGXYContext)) {
        graphics_2d.setStroke(fCellVeryWideStroke);
      } else if (isPaintLevel(CGRSGridCoordinate.KEYPAD, aGXYContext)) {
        graphics_2d.setStroke(fCellWideStroke);
      } else {
        graphics_2d.setStroke(fCellStroke);
      }
    }
  }

  /**
   * Renders keypads with dark gray lines with a width that changes whether quadrants are rendered or not.
   */
  private class KeypadStyle implements ILcdGXYPainterStyle {

    private BasicStroke fKeypadStroke = new BasicStroke(1);

    public void setupGraphics(Graphics aGraphics,
                              Object aObject,
                              int aMode,
                              ILcdGXYContext aGXYContext) {

      Graphics2D graphics_2d = (Graphics2D) aGraphics;

      graphics_2d.setColor(GridLayerFactory.GRID_COLOR);
      graphics_2d.setStroke(fKeypadStroke);
    }
  }

  /**
   * Renders quadrants with dark gray dashed lines.
   */
  private class QuadrantStyle implements ILcdGXYPainterStyle {

    private BasicStroke fQuadrantStroke = new BasicStroke(1,
                                                          BasicStroke.CAP_BUTT,
                                                          BasicStroke.JOIN_BEVEL,
                                                          0.0f,
                                                          new float[]{20.0f, 10.0f},
                                                          0.0f);

    public void setupGraphics(Graphics aGraphics,
                              Object aObject,
                              int aMode,
                              ILcdGXYContext aGXYContext) {

      Graphics2D graphics_2d = (Graphics2D) aGraphics;
      graphics_2d.setColor(GridLayerFactory.GRID_COLOR);
      graphics_2d.setStroke(fQuadrantStroke);
    }
  }
}
