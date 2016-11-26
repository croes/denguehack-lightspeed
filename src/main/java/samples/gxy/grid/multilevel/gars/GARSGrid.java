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

import com.luciad.shape.ILcdBounds;
import com.luciad.shape.shape2D.TLcdLonLatBounds;
import com.luciad.view.map.multilevelgrid.ILcdMultilevelGrid;

/**
 * Defines a GARS grid, covering the world.
 * A GARS grid consists out of 3 levels.
 * The first level (level 0) is build out of cells. Each cell covers an area of a half degree on a half degree
 * A cell is divided in quadrants (level 1), in each cell there are 4 quadrants.
 * A quadrant is divided in keypads (level 2), in each quadrant there are 9 keypads.
 */
public class GARSGrid
    implements Cloneable, ILcdMultilevelGrid {

  private TLcdLonLatBounds fBounds = new TLcdLonLatBounds(-180, -90, 360, 180);

  /**
   * Create a new GARS grid
   */
  public GARSGrid() {
  }

  /**
   * GARS grids are equal when their defining bounds are equal.
   */
  public boolean equals(Object obj) {
    return (obj instanceof GARSGrid);
  }

  public int hashCode() {
    return fBounds.hashCode();
  }

  public int getLevelCount() {
    return 3;
  }

  public int getDivisions(int aLevel, int aAxis) {
    if (aLevel == 0) {
      if (aAxis == 0) {
        return 720;
      } else {
        return 360;
      }
    } else if (aLevel == 1) {
      return 2;
    } else if (aLevel == 2) {
      return 3;
    } else {
      throw new IllegalArgumentException("GARS grid is a three level grid.");
    }
  }

  protected Object clone() throws CloneNotSupportedException {
    return super.clone();
  }

  public ILcdBounds getBounds() {
    return fBounds;
  }
}


