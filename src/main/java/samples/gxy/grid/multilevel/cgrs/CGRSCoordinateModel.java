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

import java.util.Vector;

import com.luciad.geodesy.ILcdGeodeticDatum;
import com.luciad.geodesy.TLcdGeodeticDatum;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.view.map.multilevelgrid.TLcdMultilevelGridCoordinateModel;

/**
 * A <code>TLcdMultilevelCoordinateModel</code> for <code>TLcdCGRSGridCoordinate</code> objects.
 */
public class CGRSCoordinateModel
    extends TLcdMultilevelGridCoordinateModel {

  /**
   * Constructs a CGRS model to contain <code>TLcdCGRSGridCoordinate</code> objects which are defined with regard to
   * the CGRS grid passed based on WGS 84.
   * @param aCGRSGrid the CGRS grid with regard to which the <code>TLcdCGRSGridCoordinate</code> objects
   * in the model should be interpreted.
   */
  public CGRSCoordinateModel(CGRSGrid aCGRSGrid) {
    this(aCGRSGrid, new TLcdGeodeticDatum());
  }

  /**
   * Constructs a CGRS model to contain <code>TLcdCGRSGridCoordinate</code> objects which are defined with regard to
   * the CGRS grid passed, which is based on a geodetic reference based on the datum passed.
   * @param aCGRSGrid the CGRS grid with regard to which the <code>TLcdCGRSGridCoordinate</code> objects
   * in the model should be interpreted.
   * @param aDatum the datum on which the geodetic reference on which the grid is based is based.
   */
  public CGRSCoordinateModel(CGRSGrid aCGRSGrid, ILcdGeodeticDatum aDatum) {
    super(aCGRSGrid, new TLcdGeodeticReference(aDatum));
  }

  /**
   * Puts all the model elements which are in the model at the location of the CGRS coordinate passed. Only
   * elements which are at exactly the same level are returned, not elements at higher or lower levels.
   * @param aCGRSCoordinate the location of the elements to look for, expressed as a CGRS coordinate.
   * @param aCollectedObjectsVector the vector to put the elements in.
   */
  public void elementsAtSFCT(CGRSGridCoordinate aCGRSCoordinate, Vector aCollectedObjectsVector) {
    super.elementsAtSFCT(aCGRSCoordinate, aCollectedObjectsVector);
  }

  /**
   * Only <code>TLcdCGRSGridCoordinate</code> objects can be added to this model.
   * @param aObject the object to check.
   * @return true if the object passed is a <code>TLcdCGRSGridCoordinate</code>, false otherwise.
   */
  public boolean canAddElement(Object aObject) {
    return (aObject instanceof CGRSGridCoordinate) && (super.canAddElement(aObject));
  }

}
