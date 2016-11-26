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
package samples.lightspeed.customization.hippodrome;

import com.luciad.geodesy.TLcdGeodeticDatum;
import com.luciad.model.ILcdModel;
import com.luciad.model.TLcdModelDescriptor;
import com.luciad.model.TLcdVectorModel;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.reference.TLcdLambert1972BelgiumGridReference;
import com.luciad.shape.shape2D.TLcdLonLatPoint;
import com.luciad.shape.shape2D.TLcdXYPoint;
import com.luciad.shape.shape3D.TLcdExtrudedShape;
import com.luciad.util.ILcdFireEventMode;

import samples.gxy.hippodromePainter.IHippodrome;
import samples.gxy.hippodromePainter.LonLatHippodrome;
import samples.gxy.hippodromePainter.XYHippodrome;

public class HippodromeModelFactory {

  /**
   * Creates a model containing 2 hippodromes whose coordinates are expressed in lat lon.
   * The first hippodrome is a regular one, the second an extruded one.
   * Note that the model can be any model implementation. This implementation returns a vector
   * model.
   *
   * @return a model containing 2 hippodromes whose coordinates are expressed in lat lon.
   */
  public static ILcdModel createGeodeticHippodromeModel() {
    TLcdVectorModel model = new TLcdVectorModel();
    TLcdGeodeticDatum geodetic_datum = new TLcdGeodeticDatum();
    model.setModelReference(new TLcdGeodeticReference(geodetic_datum));
    model.setModelDescriptor(new TLcdModelDescriptor(
        "Layer containing geodetic hippodromes",   // source name (is used as tooltip text)
        "Hippodrome",       // data type
        "HippodromeModel"   // display name (user)
    ));
    IHippodrome geodeticHippodrome1 = new LonLatHippodrome(new TLcdLonLatPoint(-3.0, 50), new TLcdLonLatPoint(3.4, 53), 30000, geodetic_datum
        .getEllipsoid());
    IHippodrome geodeticHippodrome2 = new LonLatHippodrome(new TLcdLonLatPoint(-3.0, 51), new TLcdLonLatPoint(5.0, 50), 50000, geodetic_datum
        .getEllipsoid());
    model.addElement(geodeticHippodrome1, ILcdFireEventMode.NO_EVENT);
    model
        .addElement(new TLcdExtrudedShape(geodeticHippodrome2, 30000, 60000), ILcdFireEventMode.NO_EVENT);
    return model;
  }

  /**
   * Creates a model containing 1 hippodrome whose coordinates are expressed in cartesian coordinates.
   * Note that the model can be any model implementation. This implementation returns a vector model.
   *
   * @return a vector model containing 1 hippodrome whose coordinates are expressed in cartesian coordinates.
   */
  public static ILcdModel createGridHippodromeModel() {
    TLcdVectorModel model = new TLcdVectorModel();
    model.setModelReference(new TLcdLambert1972BelgiumGridReference());
    model.setModelDescriptor(new TLcdModelDescriptor(
        "Layer containing grid hippodromes",   // source name (is used as tooltip text)
        "Hippodrome",       // data type
        "HippodromeModel"   // display name (user)
    ));
    IHippodrome hippodrome = new XYHippodrome(new TLcdXYPoint(110000, 70000), new TLcdXYPoint(70000, 450000), 10000);
    model.addElement(hippodrome, ILcdFireEventMode.NO_EVENT);
    return model;
  }
}
