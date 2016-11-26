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
package samples.gxy.grid.multilevel.osgr;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.geom.Rectangle2D;
import java.text.ParseException;

import com.luciad.model.ILcdModelReference;
import com.luciad.reference.format.TLcdEPSGReferenceParser;
import com.luciad.shape.shape2D.TLcdXYPoint;
import com.luciad.transformation.ILcdModelXYWorldTransformation;
import com.luciad.util.TLcdOutOfBoundsException;
import com.luciad.view.gxy.ILcdGXYContext;
import com.luciad.view.gxy.ILcdGXYViewXYWorldTransformation;
import com.luciad.view.gxy.ILcdNotLabelDeconflictableLayer;
import com.luciad.view.gxy.TLcdGXYPainterColorStyle;
import com.luciad.view.map.TLcdGridPen;
import com.luciad.view.map.multilevelgrid.ILcdMultilevelGridCoordinate;
import com.luciad.view.map.multilevelgrid.TLcdMultilevelGridLayer;
import com.luciad.view.map.multilevelgrid.TLcdMultilevelGridUtil;

import samples.gxy.grid.GridLayerFactory;

/**
 * A layer containing the OSGR grid.
 * The supported grid references consist of two letters followed by four digits, mapping to a resolution of up to 1km.
 */
public class OSGRGridLayer
    extends TLcdMultilevelGridLayer
    implements ILcdNotLabelDeconflictableLayer {

  private static ILcdModelReference sBritishGridReference;
  private TLcdXYPoint fLabelModelPoint = new TLcdXYPoint();
  private TLcdXYPoint fLabelWorldPoint = new TLcdXYPoint();
  private Point fLabelViewPoint = new Point();
  // First letters giving 500 by 500 km squares
  private char[][] LETTERS_1 = new char[][]{{'S', 'N', 'H'}, {'T', 'O', 'J'}};

  static {
    // For convenience, we use an EPSG parser to instantiate a reference for the British National Grid
    TLcdEPSGReferenceParser epsg_reference_parser = new TLcdEPSGReferenceParser();
    try {
      sBritishGridReference = epsg_reference_parser.parseModelReference("EPSG:27700");
    } catch (ParseException e) {
      throw new IllegalArgumentException("Make sure the EPSG resources are in your classpath.");
    }
  }

  public OSGRGridLayer() {
    super(new OSGRGrid(), sBritishGridReference);
    double[] level_switch_scale = new double[3];
    level_switch_scale[0] = 4E-4;
    level_switch_scale[1] = 5E-3;
    level_switch_scale[2] = 1;
    setLevelSwitchScales(level_switch_scale);
    setLabel("British grid");
    setLevelStyle(0, new TLcdGXYPainterColorStyle(GridLayerFactory.GRID_COLOR));
    setLevelStyle(1, new TLcdGXYPainterColorStyle(GridLayerFactory.GRID_SUB_COLOR));
    setGXYPen(new TLcdGridPen(false));
  }

  protected void paintLabel(ILcdMultilevelGridCoordinate aMultilevelGridCoordinate, ILcdGXYContext aGXYContext, Graphics aGraphics) {

    ILcdModelXYWorldTransformation mwt = aGXYContext.getModelXYWorldTransformation();
    ILcdGXYViewXYWorldTransformation vwt = aGXYContext.getGXYViewXYWorldTransformation();

    try {
      TLcdMultilevelGridUtil.pointAtSFCT(aMultilevelGridCoordinate, getMultilevelGrid(), TLcdMultilevelGridUtil.CENTER, fLabelModelPoint);
      mwt.modelPoint2worldSFCT(fLabelModelPoint, fLabelWorldPoint);
      vwt.worldPoint2viewAWTPointSFCT(fLabelWorldPoint, fLabelViewPoint);
      StringBuffer label_buffer = getLabel(aMultilevelGridCoordinate);
      String label = label_buffer.toString();
      Rectangle2D label_bounds = aGraphics.getFontMetrics().getStringBounds(label, aGraphics);
      aGraphics.setColor(GridLayerFactory.GRID_LABEL_COLOR);
      aGraphics.drawString(label,
                           (int) (fLabelViewPoint.getX() - label_bounds.getWidth() / 2),
                           (int) (fLabelViewPoint.y + label_bounds.getHeight() / 2));
    } catch (TLcdOutOfBoundsException e) {
      // do nothing, we cannot render a label.
    }
  }

  private StringBuffer getLabel(ILcdMultilevelGridCoordinate aMultilevelGridCoordinate) {
    // highest level, just letters
    int x_coordinate = aMultilevelGridCoordinate.getCoordinate(0, 0);
    int y_coordinate = aMultilevelGridCoordinate.getCoordinate(0, 1);

    char first_char = LETTERS_1[(x_coordinate) / 5][(y_coordinate) / 5];
    int remainder_x = x_coordinate % 5;
    int remainder_y = y_coordinate % 5;

    char second_char = adaptchar((char) ('A' + (char) (remainder_x) + (char) (4 - remainder_y) * 5));
    StringBuffer label_buffer = new StringBuffer();
    label_buffer.append(first_char);
    label_buffer.append(second_char);

    if (aMultilevelGridCoordinate.getCoordinateLevelCount() == 2) {
      int x_coordinate_level_1 = aMultilevelGridCoordinate.getCoordinate(1, 0);
      label_buffer.append(x_coordinate_level_1);
      int y_coordinate_level_1 = aMultilevelGridCoordinate.getCoordinate(1, 1);
      label_buffer.append(y_coordinate_level_1);
    }

    return label_buffer;
  }

  private char adaptchar(char aLabelLetter) {
    if (aLabelLetter < 'I') {
      return aLabelLetter;
    } else {
      return (char) (aLabelLetter + 1);
    }
  }
}
