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

import com.luciad.shape.ILcdBounds;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.shape2D.TLcdLonLatBounds;
import com.luciad.shape.shape2D.TLcdLonLatPoint;
import com.luciad.view.map.multilevelgrid.ILcdMultilevelGrid;

/**
 * A CGRS grid is defined by 2 points that are aligned with 30 minute parallels and
 * meridians. The constructors automatically round given values to the nearest 30 minutes.
 */
public class CGRSGrid
    implements Cloneable, ILcdMultilevelGrid {

  private TLcdLonLatBounds fBounds;

  /**
   * Create a new CGRS grid with a given origin, width and height. Values are rounded to the nearest multiple of 30
   * minutes.
   *
   * @param aOrigin The origin of the grid (southwest corner).
   * @param aWidth  Width of the grid in degrees longitude.
   * @param aHeight Height of the grid in degrees latitude.
   */
  public CGRSGrid(ILcdPoint aOrigin, double aWidth, double aHeight) {
    this(aOrigin.getX(), aOrigin.getY(), aWidth, aHeight);
  }

  /**
   * Create a new CGRS grid with a given origin, width and height. Values are rounded to the nearest multiple of 30
   * minutes.
   *
   * @param aOriginLon Longitude of the origin of the grid.
   * @param aOriginLat Latitude of the origin of the grid.
   * @param aWidth     Width of the grid in degrees longitude.
   * @param aHeight    Height of the grid in degrees latitude.
   */
  public CGRSGrid(double aOriginLon, double aOriginLat, double aWidth, double aHeight) {
    init(aOriginLon, aOriginLat, aWidth, aHeight);
  }

  /**
   * Create a new CGRS grid from two given points. The constructed grid will be the smallest possible grid
   * that contains both points. Values are rounded to the nearest multiple of 30 minutes.
   *
   * @param aFirstPoint  First defining point.
   * @param aSecondPoint Second defining point.
   */
  public CGRSGrid(ILcdPoint aFirstPoint, ILcdPoint aSecondPoint) {
    TLcdLonLatBounds bounds = new TLcdLonLatBounds();
    bounds.move2D(aFirstPoint);
    bounds.setToIncludePoint2D(aSecondPoint);
    init(bounds.getLocation().getX(), bounds.getLocation().getY(), bounds.getWidth(), bounds.getHeight());
  }

  private void init(double aOriginLon, double aOriginLat, double aWidth, double aHeight) {
    TLcdLonLatPoint ll_point = new TLcdLonLatPoint();
    TLcdLonLatPoint ur_point = new TLcdLonLatPoint();
    ll_point.move2D(roundToNearestHalf(aOriginLon), roundToNearestHalf(aOriginLat));
    ur_point.move2D(roundToNearestHalf(aOriginLon + aWidth), roundToNearestHalf(aOriginLat + aHeight));
    double width = ur_point.getX() - ll_point.getX();
    if (width < 0) {
      width += 360.0;
    }
    double height = ur_point.getY() - ll_point.getY();
    fBounds = new TLcdLonLatBounds();
    fBounds.move2D(ll_point);
    fBounds.setWidth(width);
    fBounds.setHeight(height);
  }

  private static double roundToNearestHalf(double aCoordinate) {
    return ((double) (Math.round(aCoordinate * 2))) / 2;
  }

  /**
   * CGRS grids are equal when their defining bounds are equal.
   */
  public boolean equals(Object obj) {
    if (obj instanceof CGRSGrid) {
      CGRSGrid grid = (CGRSGrid) obj;
      return (fBounds.equals(grid.getBounds()));
    }
    return false;
  }

  public int hashCode() {
    return fBounds.hashCode();
  }

  /**
   * A CGRS grid consists of 3 levels: cells, keypads and quadrants.
   * @return always 3.
   */
  public final int getLevelCount() {
    return 3;
  }

  /**
   * Utility method that returns the number of divisions of the grid at a given level for a given axis.
   * It enables generalisation of code looping over all elements in a grid.
   * @param aLevel the level for which to find the number of divisions.
   * @param aAxis 0 for X, 1 for Y.
   * @return twice the width or height at cell level, 3 at keypad level, 2 at quadrant level.
   */
  public final int getDivisions(int aLevel, int aAxis) {
    if (aLevel == 0) {
      if (aAxis == 0) {
        return (int) (fBounds.getWidth() * 2);
      } else {
        return (int) (fBounds.getHeight() * 2);
      }
    } else if (aLevel == 1) {
      return 3;
    } else if (aLevel == 2) {
      return 2;
    } else {
      throw new IllegalArgumentException("CGRS grid is a three level grid.");
    }
  }

  /**
   * Clones the defining points of the grid, the lower left and the upper right point.
   * @return a copy of this grid.
   * @throws CloneNotSupportedException when the call to super.clone fails.
   */
  protected Object clone() throws CloneNotSupportedException {
    Object clone = super.clone();
    CGRSGrid grid = (CGRSGrid) clone;
    grid.fBounds = (TLcdLonLatBounds) fBounds.clone();
    return clone;
  }

  public ILcdBounds getBounds() {
    return fBounds;
  }
}
