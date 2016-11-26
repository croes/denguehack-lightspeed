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
 * A utility class for CGRS grid coordinates. It enables to:
 * <ul>
 * <li>
 * find the location of a CGRS grid coordinate in the reference on which the grid is based,
 * </li>
 * <li>
 * find the bounds of a CGRS grid coordinate in the reference on which the grid is based,
 * </li>
 * <li>
 * find the CGRS coordinate at a given level that covers a location expressed in the reference on which the grid
 * is based,
 * </li>
 * <li>
 * apply a function to all CGRS coordinates at a given level that overlap with a bounds given in the
 * reference on which the grid is based.
 * </li>
 * </ul>
 */
public class CGRSGridUtil {

  /**
   * Find the CGRS cell in which the specified lon/lat point lies.
   *
   * @param aPoint                  The point (longitude/latitude) to transform to CGRS.
   * @param aCGRSGrid               the grid with regard to which the coordinate is defined.
   * @param aGeoReference           the reference in which the point and the grid are defined.
   * @param aEditableCoordinateSFCT The CGRS coordinate in which to store the result.
   * @throws TLcdOutOfBoundsException if the point is not situated in the grid passed.
   */
  public static void CGRSCellAtSFCT(ILcdPoint aPoint,
                                    CGRSGrid aCGRSGrid,
                                    ILcdGeoReference aGeoReference,
                                    CGRSGridCoordinate aEditableCoordinateSFCT)
      throws TLcdOutOfBoundsException {
    TLcdMultilevelGridUtil.multilevelCoordinateAtSFCT(aPoint, 1, aCGRSGrid, aGeoReference, aEditableCoordinateSFCT);
  }

  /**
   * Find the CGRS keypad in which the specified lon/lat point lies.
   *
   * @param aPoint                  The point (longitude/latitude) to transform to CGRS.
   * @param aCGRSGrid               the grid with regard to which the coordinate is defined.
   * @param aGeoReference           the reference in which the point and the grid are defined.
   * @param aEditableCoordinateSFCT The CGRS coordinate in which to store the result.
   * @throws TLcdOutOfBoundsException if the point is not situated in the grid passed.
   */
  public static void CGRSKeypadAtSFCT(ILcdPoint aPoint,
                                      CGRSGrid aCGRSGrid,
                                      ILcdGeoReference aGeoReference,
                                      CGRSGridCoordinate aEditableCoordinateSFCT)
      throws TLcdOutOfBoundsException {
    TLcdMultilevelGridUtil.multilevelCoordinateAtSFCT(aPoint, 2, aCGRSGrid, aGeoReference, aEditableCoordinateSFCT);
  }

  /**
   * Find the CGRS quadrant in which the specified lon/lat point lies.
   *
   * @param aPoint                  The point (longitude/latitude) to transform to CGRS.
   * @param aCGRSGrid               the grid with regard to which the coordinate is defined.
   * @param aGeoReference           the reference in which the point and the grid are defined.
   * @param aEditableCoordinateSFCT The CGRS coordinate in which to store the result.
   * @throws TLcdOutOfBoundsException if the point is not situated in the grid passed.
   */
  public static void CGRSQuadrantAtSFCT(ILcdPoint aPoint,
                                        CGRSGrid aCGRSGrid,
                                        ILcdGeoReference aGeoReference,
                                        CGRSGridCoordinate aEditableCoordinateSFCT)
      throws TLcdOutOfBoundsException {
    TLcdMultilevelGridUtil.multilevelCoordinateAtSFCT(aPoint, 3, aCGRSGrid, aGeoReference, aEditableCoordinateSFCT);
  }

  /**
   * Finds the lower left point of the CGRS grid element specified by the given CGRS coordinate.
   *
   * @param aCoordinate          The CGRS coordinate.
   * @param aCGRSGrid            the grid with regard to which the coordinate is defined.
   * @param a2DEditablePointSFCT Side-effect point in which to store the origin.
   */
  public static void pointAtCGRSCoordinateSFCT(CGRSGridCoordinate aCoordinate,
                                               CGRSGrid aCGRSGrid,
                                               ILcd2DEditablePoint a2DEditablePointSFCT) {
    try {
      TLcdMultilevelGridUtil.pointAtSFCT(aCoordinate, aCGRSGrid, TLcdMultilevelGridUtil.LOWER_LEFT, a2DEditablePointSFCT);
    } catch (TLcdOutOfBoundsException e) {
      throw new IllegalArgumentException("CGRS coordinate has no cell specified.");
    }
  }

  /**
   * Find the bounds of the CGRS grid element specified by the given CGRS coordinate.
   *
   * @param aCoordinate           The CGRS coordinate.
   * @param aCGRSGrid The grid with regards to which the coordinate passed is specified.
   * @param a2DEditableBoundsSFCT Side-effect bounds object in which to store the bounds.
   * @throws TLcdNoBoundsException when the CGRS coordinate passed does not have at least a cell specified.
   */
  public static void CGRSCoordinateBoundsSFCT(CGRSGridCoordinate aCoordinate,
                                              CGRSGrid aCGRSGrid, ILcd2DEditableBounds a2DEditableBoundsSFCT
                                             ) throws TLcdNoBoundsException {
    try {
      TLcdMultilevelGridUtil.multilevelCoordinateBoundsSFCT(aCoordinate, aCGRSGrid, a2DEditableBoundsSFCT);
    } catch (TLcdNoBoundsException e) {
      throw new TLcdNoBoundsException("Cannot determine bounds, CGRS element has no cell defined.");
    }
  }

  /**
   * Apply the given function to all CGRS cells within the given bounds.
   * The objects passed to the function will be {@link CGRSGridCoordinate} instances for the requested cells.
   *
   * @param aBounds   Bounds in which to find cells.
   * @param aFunction Function to apply on cell coordinates.
   * @param aGeoReference the reference in which the bounds and the grid are defined.
   * @param aCGRSGrid the grid with regard to which the cells are defined.
   */
  public static void applyOnCGRSCellsInBounds(ILcdBounds aBounds,
                                              ILcdFunction aFunction,
                                              CGRSGrid aCGRSGrid,
                                              ILcdGeoReference aGeoReference) {
    TLcdMultilevelGridUtil.applyOnInteract(aBounds, aFunction, 0, aCGRSGrid, aGeoReference);
  }

  /**
   * Apply the given function to all CGRS keypads within the given bounds.
   * The objects passed to the function will be {@link CGRSGridCoordinate} instances for the requested keypads.
   *
   * @param aBounds   Bounds in which to find keypads.
   * @param aFunction Function to apply on keypad coordinates.
   * @param aCGRSGrid the grid with regard to which the keypads are defined.
   * @param aGeoReference the reference in which the bounds and the grid are defined.
   */
  public static void applyOnCGRSKeypadsInBounds(final ILcdBounds aBounds,
                                                final ILcdFunction aFunction,
                                                final CGRSGrid aCGRSGrid,
                                                ILcdGeoReference aGeoReference) {
    TLcdMultilevelGridUtil.applyOnInteract(aBounds, aFunction, 1, aCGRSGrid, aGeoReference);
  }

  /**
   * Apply the given function to all CGRS quadrants within the given bounds.
   * The objects passed to the function will be {@link CGRSGridCoordinate} instances for the requested quadrants.
   *
   * @param aBounds   Bounds in which to find quadrants.
   * @param aFunction Function to apply on quadrant coordinates.
   * @param aCGRSGrid the grid with regard to which the quadrants are defined.
   * @param aGeoReference the reference in which the bounds and the grid are defined.
   */
  public static void applyOnCGRSQuadrantsInBounds(final ILcdBounds aBounds,
                                                  final ILcdFunction aFunction,
                                                  final CGRSGrid aCGRSGrid,
                                                  ILcdGeoReference aGeoReference) {
    TLcdMultilevelGridUtil.applyOnInteract(aBounds, aFunction, 2, aCGRSGrid, aGeoReference);
  }

  /**
   * Returns the keypad value for a multilevel grid coordinate which is interpreted as a CGRS
   * coordinate.
   * @param aMultilevelGridCoordinate a multilevel grid coordinate which is interpreted as a CGRS
   * coordinate. Note that this need not be a CGRSGridCoordinate.
   * @return the keypad value, ranging from 1 to 9.
   */
  public static int getKeypad(ILcdMultilevelGridCoordinate aMultilevelGridCoordinate) {
    int x = aMultilevelGridCoordinate.getCoordinate(1, ILcdMultilevelGrid.X_AXIS);
    int y = aMultilevelGridCoordinate.getCoordinate(1, ILcdMultilevelGrid.Y_AXIS);
    return (2 - y) * 3 + x + 1;
  }

  /**
   * Returns the column index of a multilevel grid coordinate as if it were a CGRS coordinate.
   * Column numbering starts at 1 for CGRS.
   * @param aMultilevelGridCoordinate the multilevelgrid coordinate to find the column cell for. This need
   * not be a CGRS grid coordinate.
   * @return the cell column of the coordinate as if it were a CGRS coordinate.
   */
  public static int getCellColumn(ILcdMultilevelGridCoordinate aMultilevelGridCoordinate) {
    return aMultilevelGridCoordinate.getCoordinate(0, 0) + 1;
  }

  /**
   * Returns the row index of a multilevel grid coordinate as if it were a CGRS coordinate.
   * Column numbering starts at 1 for CGRS.
   * @param aMultilevelGridCoordinate the multilevelgrid coordinate to find the row cell for. This need
   * not be a CGRS grid coordinate.
   * @return the cell row of the coordinate as if it were a CGRS coordinate.
   */
  public static int getCellRow(ILcdMultilevelGridCoordinate aMultilevelGridCoordinate) {
    return aMultilevelGridCoordinate.getCoordinate(0, 1) + 1;
  }

  /**
   * Returns the quadrant of a multilevel grid coordinate as if it were a CGRS coordinate.
   * @param aMultilevelGridCoordinate the multilevel grid coordinate to interpret the coordinates of.
   * @return a number representing a CGRS quadrant.
   */
  public static int getQuadrant(ILcdMultilevelGridCoordinate aMultilevelGridCoordinate) {
    int xCoordinate = aMultilevelGridCoordinate.getCoordinate(2, ILcdMultilevelGrid.X_AXIS);
    int yCoordinate = aMultilevelGridCoordinate.getCoordinate(2, ILcdMultilevelGrid.Y_AXIS);
    if (xCoordinate == 0) {
      if (yCoordinate == 0) {
        return CGRSGridCoordinate.QUADRANT_SW;
      } else if (yCoordinate == 1) {
        return CGRSGridCoordinate.QUADRANT_NW;
      }
    } else if (xCoordinate == 1) {
      if (yCoordinate == 0) {
        return CGRSGridCoordinate.QUADRANT_SE;
      } else if (yCoordinate == 1) {
        return CGRSGridCoordinate.QUADRANT_NE;
      }
    }
    throw new IllegalArgumentException("A CGRS Quadrant should have x and y coordinates equal to 0 or 1.");
  }

}
