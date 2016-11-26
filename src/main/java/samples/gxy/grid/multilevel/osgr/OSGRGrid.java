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

import com.luciad.shape.ILcdBounds;
import com.luciad.shape.shape2D.TLcdXYBounds;
import com.luciad.view.map.multilevelgrid.ILcdMultilevelGrid;

/**
 * A <code>ILcdMultilevelGrid</code> representation of the British national grid.
 * The extent of the grid is defined by 2 fields in order to enable extending the grid to
 * Ireland if so desired.
 */
public class OSGRGrid implements ILcdMultilevelGrid {

  private int fExtentEast = 7;
  private int fExtentNorth = 13;
  private TLcdXYBounds fBounds = new TLcdXYBounds(0, 0, 1E5 * fExtentEast, 1E5 * fExtentNorth);

  public int getLevelCount() {
    return 2;
  }

  public int getDivisions(int aLevel, int aAxis) {
    switch (aAxis) {
    case ILcdMultilevelGrid.X_AXIS:
      if (aLevel == 0) {
        return fExtentEast;
      } else {
        return 10;
      }
    case ILcdMultilevelGrid.Y_AXIS:
      if (aLevel == 0) {
        return fExtentNorth;
      } else {
        return 10;
      }
    default:
      throw new IllegalArgumentException("The OSGR grid has only 2 levels");
    }
  }

  public ILcdBounds getBounds() {
    return fBounds;
  }
}
