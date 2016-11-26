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

import com.luciad.reference.ILcdGeoReference;
import com.luciad.shape.ILcdBounds;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.shape2D.ILcd2DEditableBounds;
import com.luciad.shape.shape2D.ILcd2DEditablePoint;
import com.luciad.util.ILcdFunction;
import com.luciad.util.TLcdNoBoundsException;
import com.luciad.util.TLcdOutOfBoundsException;
import com.luciad.view.map.multilevelgrid.ILcdMultilevelGrid;
import com.luciad.view.map.multilevelgrid.ILcdMultilevelGridCoordinate;
import com.luciad.view.map.multilevelgrid.TLcdMultilevelGridUtil;

/**
 * A utility class for GARS grid coordinates. It enables to:
 * <ul>
 * <li>
 * find the location of a GARS grid coordinate in the reference on which the grid is based,
 * </li>
 * <li>
 * find the bounds of a GARS grid coordinate in the reference on which the grid is based,
 * </li>
 * <li>
 * find the GARS coordinate at a given level that covers a location expressed in the reference on which the grid
 * is based,
 * </li>
 * <li>
 * apply a function to all GARS coordinates at a given level that overlap with a bounds given in the
 * reference on which the grid is based.
 * </li>
 * </ul>
 */
public class GARSGridUtil {

  /**
   * Find the GARS cell in which the specified lon/lat point lies.
   * @param aPoint                  The point (longitude/latitude) to transform to GARS.
   * @param aGARSGrid               the grid with regard to which the coordinate is defined.
   * @param aGeoReference           the reference in which the point and the multilevelgrid are defined.
   * @param aEditableCoordinateSFCT The GARS coordinate in which to store the result.
   * @throws TLcdOutOfBoundsException if the point is not situated in the grid passed.
   */
  public static void GARSCellAtSFCT(ILcdPoint aPoint,
                                    GARSGrid aGARSGrid,
                                    ILcdGeoReference aGeoReference,
                                    GARSGridCoordinate aEditableCoordinateSFCT)
      throws TLcdOutOfBoundsException {
    TLcdMultilevelGridUtil.multilevelCoordinateAtSFCT(aPoint, 1, aGARSGrid, aGeoReference, aEditableCoordinateSFCT);
  }

  /**
   * Find the GARS quadrant in which the specified lon/lat point lies.
   * @param aPoint                  The point (longitude/latitude) to transform to GARS.
   * @param aGARSGrid               the grid with regard to which the coordinate is defined.
   * @param aGeoReference           the reference in which the point and the multilevelgrid are defined.
   * @param aEditableCoordinateSFCT The GARS coordinate in which to store the result.
   * @throws TLcdOutOfBoundsException if the point is not situated in the grid passed.
   */
  public static void GARSQuadrantAtSFCT(ILcdPoint aPoint,
                                        GARSGrid aGARSGrid,
                                        ILcdGeoReference aGeoReference,
                                        GARSGridCoordinate aEditableCoordinateSFCT)
      throws TLcdOutOfBoundsException {
    TLcdMultilevelGridUtil.multilevelCoordinateAtSFCT(aPoint, 2, aGARSGrid, aGeoReference, aEditableCoordinateSFCT);
  }

  /**
   * Find the GARS keypad in which the specified lon/lat point lies.
   * @param aPoint                  The point (longitude/latitude) to transform to GARS.
   * @param aGARSGrid               the grid with regard to which the coordinate is defined.
   * @param aGeoReference           the reference in which the point and the multilevelgrid are defined.
   * @param aEditableCoordinateSFCT The GARS coordinate in which to store the result.
   * @throws TLcdOutOfBoundsException if the point is not situated in the grid passed.
   */
  public static void GARSKeypadAtSFCT(ILcdPoint aPoint,
                                      GARSGrid aGARSGrid,
                                      ILcdGeoReference aGeoReference,
                                      GARSGridCoordinate aEditableCoordinateSFCT)
      throws TLcdOutOfBoundsException {
    TLcdMultilevelGridUtil.multilevelCoordinateAtSFCT(aPoint, 3, aGARSGrid, aGeoReference, aEditableCoordinateSFCT);
  }

  /**
   * Find the origin of the GARS grid element specified by the given GARS coordinate.
   *
   * @param aCoordinate          The GARS coordinate.
   * @param aGARSGrid            the grid with regard to which the coordinate is defined.
   * @param a2DEditablePointSFCT Side-effect point in which to store the origin.
   */
  public static void pointAtGARSCoordinateSFCT(GARSGridCoordinate aCoordinate,
                                               GARSGrid aGARSGrid,
                                               ILcd2DEditablePoint a2DEditablePointSFCT) {
    try {
      TLcdMultilevelGridUtil.pointAtSFCT(aCoordinate, aGARSGrid, TLcdMultilevelGridUtil.LOWER_LEFT, a2DEditablePointSFCT);
    } catch (TLcdOutOfBoundsException e) {
      throw new IllegalArgumentException("GARS coordinate has no cell specified.");
    }
  }

  /**
   * Apply the given function exactly once to all GARS cells within the given bounds.
   * The objects passed to the function will be {@link GARSGridCoordinate} instances for the requested cells.
   *
   * @param aBounds   Bounds in which to find cells.
   * @param aFunction Function to apply on cell coordinates.
   * @param aGARSGrid the grid with regard to which the cells are defined.
   * @param aGeoReference the reference in which the bounds and the multilevelgrid are defined.
   */
  public static void applyOnGARSCellsInBounds(ILcdBounds aBounds,
                                              ILcdFunction aFunction,
                                              GARSGrid aGARSGrid,
                                              ILcdGeoReference aGeoReference) {
    TLcdMultilevelGridUtil.applyOnInteract(aBounds, aFunction, 0, aGARSGrid, aGeoReference);
  }

  /**
   * Apply the given function exactly once to all GARS quadrants within the given bounds.
   * The objects passed to the function will be {@link GARSGridCoordinate} instances for the requested quadrants.
   *
   * @param aBounds   Bounds in which to find quadrants.
   * @param aFunction Function to apply on quadrant coordinates.
   * @param aGARSGrid the grid with regard to which the quadrants are defined.
   * @param aGeoReference the reference in which the bounds and the multilevelgrid are defined.
   */
  public static void applyOnGARSQuadrantsInBounds(ILcdBounds aBounds,
                                                  ILcdFunction aFunction,
                                                  GARSGrid aGARSGrid,
                                                  ILcdGeoReference aGeoReference) {
    TLcdMultilevelGridUtil.applyOnInteract(aBounds, aFunction, 1, aGARSGrid, aGeoReference);
  }

  /**
   * Apply the given function exactly once to all GARS keypads within the given bounds.
   * The objects passed to the function will be {@link GARSGridCoordinate} instances for the requested keypads.
   *
   * @param aBounds   Bounds in which to find keypads.
   * @param aFunction Function to apply on keypad coordinates.
   * @param aGARSGrid the grid with regard to which the keypads are defined.
   * @param aGeoReference the reference in which the bounds and the multilevelgrid are defined.
   */
  public static void applyOnGARSKeypadsInBounds(final ILcdBounds aBounds,
                                                final ILcdFunction aFunction,
                                                final GARSGrid aGARSGrid,
                                                ILcdGeoReference aGeoReference) {
    TLcdMultilevelGridUtil.applyOnInteract(aBounds, aFunction, 2, aGARSGrid, aGeoReference);
  }

  /**
   * Find the bounds of the GARS grid element specified by the given GARS coordinate.
   *
   * @param aCoordinate           The GARS coordinate.
   * @param aGARSGrid The grid with regards to which the coordinate passed is specified.
   * @param a2DEditableBoundsSFCT Side-effect bounds object in which to store the bounds.
   */
  public static void GARSCoordinateBoundsSFCT(GARSGridCoordinate aCoordinate,
                                              GARSGrid aGARSGrid,
                                              ILcd2DEditableBounds a2DEditableBoundsSFCT) {
    try {
      TLcdMultilevelGridUtil.multilevelCoordinateBoundsSFCT(aCoordinate, aGARSGrid, a2DEditableBoundsSFCT);
    } catch (TLcdNoBoundsException e) {
      throw new IllegalArgumentException("GARS coordinate has no cell specified.");
    }
  }

  /**
   * Returns the keypad value for a multilevel grid coordinate which is interpreted as a GARS
   * coordinate.
   * @param aMultilevelGridCoordinate a multilevel grid coordinate which is interpreted as a GARS
   * coordinate. Note that this need not be a GARSGridCoordinate.
   * @return the keypad value, ranging from 1 to 9.
   */
  public static int getKeypad(ILcdMultilevelGridCoordinate aMultilevelGridCoordinate) {
    int x = aMultilevelGridCoordinate.getCoordinate(2, ILcdMultilevelGrid.X_AXIS);
    int y = aMultilevelGridCoordinate.getCoordinate(2, ILcdMultilevelGrid.Y_AXIS);
    return (2 - y) * 3 + x + 1;
  }

  /**
   * Returns the column index of a multilevel grid coordinate as if it were a GARS coordinate.
   * Column numbering starts at 1 for GARS.
   * @param aMultilevelGridCoordinate the multilevelgrid coordinate to find the column cell for. This need
   * not be a GARS grid coordinate.
   * @return the cell column of the coordinate as if it were a GARS coordinate.
   */
  public static int getCellColumn(ILcdMultilevelGridCoordinate aMultilevelGridCoordinate) {
    return aMultilevelGridCoordinate.getCoordinate(0, 0) + 1;
  }

  /**
   * Returns the row index of a multilevel grid coordinate as if it were a GARS coordinate.
   * Column numbering starts at 1 for GARS.
   * @param aMultilevelGridCoordinate the multilevelgrid coordinate to find the row cell for. This need
   * not be a GARS grid coordinate.
   * @return the cell row of the coordinate as if it were a GARS coordinate.
   */
  public static int getCellRow(ILcdMultilevelGridCoordinate aMultilevelGridCoordinate) {
    return aMultilevelGridCoordinate.getCoordinate(0, 1) + 1;
  }

  /**
   * Returns the quadrant of a multilevel grid coordinate as if it were a GARS coordinate.
   * @param aMultilevelGridCoordinate the multilevel grid coordinate to interpret the coordinates of.
   * @return a number representing a GARS quadrant.
   */
  public static int getQuadrant(ILcdMultilevelGridCoordinate aMultilevelGridCoordinate) {
    int xCoordinate = aMultilevelGridCoordinate.getCoordinate(1, ILcdMultilevelGrid.X_AXIS);
    int yCoordinate = aMultilevelGridCoordinate.getCoordinate(1, ILcdMultilevelGrid.Y_AXIS);
    if (xCoordinate == 0) {
      if (yCoordinate == 0) {
        return GARSGridCoordinate.QUADRANT_SW;
      } else if (yCoordinate == 1) {
        return GARSGridCoordinate.QUADRANT_NW;
      }
    } else if (xCoordinate == 1) {
      if (yCoordinate == 0) {
        return GARSGridCoordinate.QUADRANT_SE;
      } else if (yCoordinate == 1) {
        return GARSGridCoordinate.QUADRANT_NE;
      }
    }
    throw new IllegalArgumentException("A GARS Quadrant should have x and y coordinates equal to 0 or 1.");
  }
}
