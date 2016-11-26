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

import java.util.Vector;

import com.luciad.geodesy.ILcdGeodeticDatum;
import com.luciad.geodesy.TLcdGeodeticDatum;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.shape.ILcdBounds;
import com.luciad.shape.shape2D.TLcdLonLatBounds;
import com.luciad.view.map.multilevelgrid.TLcdMultilevelGridCoordinateModel;

/**
 * A <code>ILcdModel</code> implementation for <code>ILcdGARSCoordinate</code> objects,
 * that takes into account the structure of a <code>TLcdGARSGrid</code>.
 */
public class GARSCoordinateModel
    extends TLcdMultilevelGridCoordinateModel {

  private TLcdLonLatBounds fBounds = new TLcdLonLatBounds(-180, -90, 360, 180);

  /**
   * Constructs a GARS model to contain <code>ILcdGARSCoordinate</code> objects which are defined with regard to
   * a GARS grid based on WGS 84.
   */
  public GARSCoordinateModel() {
    this(new TLcdGeodeticDatum());
  }

  /**
   * Constructs a GARS model to contain <code>ILcdGARSCoordinate</code> objects which are defined with regard to
   * the GARS grid based on a geodetic reference based on the datum passed.
   * @param aDatum the datum on which the geodetic reference on which the grid is based is based.
   */
  public GARSCoordinateModel(ILcdGeodeticDatum aDatum) {
    super(new GARSGrid(), new TLcdGeodeticReference(aDatum));
  }

  /**
   * Returns the grid on which this model is based.
   * Elements in this model should have valid coordinates with regard to this grid.
   * @return the grid on which this model is based.
   */
  public GARSGrid getGARSGrid() {
    return (GARSGrid) super.getMultilevelGrid();
  }

  /**
   * Puts all the model elements which are in the model at the location of the GARS coordinate passed. Only
   * elements which are at exactly the same level are returned, not elements at higher or lower levels.
   * @param aGARSCoordinate the location of the elements to look for, expressed as a GARS coordinate.
   * @param aCollectedObjectsVector the vector to put the elements in.
   */
  public void elementsAtSFCT(GARSGridCoordinate aGARSCoordinate, Vector aCollectedObjectsVector) {
    super.elementsAtSFCT(aGARSCoordinate, aCollectedObjectsVector);
  }

  /**
   * Only <code>TLcdGARSGridCoordinate</code> implementations can be added to this model. This method also checks
   * whether the coordinates comply to the GARS grid.
   * @param aObject the object to check.
   * @return true if the object passed implements <code>TLcdGARSGridCoordinate</code>, false otherwise.
   */
  public boolean canAddElement(Object aObject) {
    return (aObject instanceof GARSGridCoordinate) && (super.canAddElement(aObject));
  }

  /**
   * Returns the bounds of the whole world.
   * @return the bounds of the whole world.
   */
  public ILcdBounds getBounds() {
    return fBounds;
  }
}
