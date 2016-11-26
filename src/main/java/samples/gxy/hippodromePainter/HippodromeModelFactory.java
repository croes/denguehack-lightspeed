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
package samples.gxy.hippodromePainter;

import com.luciad.geodesy.TLcdGeodeticDatum;
import com.luciad.model.ILcdModel;
import com.luciad.model.TLcd2DBoundsIndexedModel;
import com.luciad.model.TLcdModelDescriptor;
import com.luciad.model.TLcdVectorModel;
import com.luciad.projection.TLcdEquidistantCylindrical;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.reference.TLcdGridReference;
import com.luciad.shape.shape2D.TLcdLonLatPoint;
import com.luciad.shape.shape2D.TLcdXYPoint;
import com.luciad.util.ILcdFireEventMode;

public class HippodromeModelFactory {

  /**
   * Creates a model containing 1 hippodrome whose coordinates are expressed in lat lon. Note that
   * the model can be any model implementation. This implementation returns a vector model.
   *
   * @return a model containing 1 hippodrome whose coordinates are expressed in lat lon.
   */
  public static ILcdModel createGeodeticHippodromeModel() {
    TLcdVectorModel model = new TLcdVectorModel();
    TLcdGeodeticDatum geodetic_datum = new TLcdGeodeticDatum();
    model.setModelReference(new TLcdGeodeticReference(geodetic_datum));
    model.setModelDescriptor(new TLcdModelDescriptor(
        "Layer containing geodetic hippodromes",   // source name (is used as tooltip text)
        "Hippodrome",       // data type
        "LonLat Hippodrome"   // display name (user)
    ));
    LonLatHippodrome hippodrome = new LonLatHippodrome(new TLcdLonLatPoint(-3.0, 50), new TLcdLonLatPoint(3.4, 53), 30000, geodetic_datum.getEllipsoid());
    model.addElement(hippodrome, ILcdFireEventMode.NO_EVENT);
    return model;
  }

  /**
   * Creates a 2D bounds indexed model containing 1 hippodrome whose coordinates are expressed in
   * XY. Note that the model can be any model implementation. This implementation returns a 2D
   * bounds indexed model.
   *
   * @return a 2D bounds indexed model containing 1 hippodrome whose coordinates are expressed in
   *         XY.
   */
  public static ILcdModel createGridHippodromeModel() {
    TLcd2DBoundsIndexedModel model = new TLcd2DBoundsIndexedModel();
    model.setModelReference(new TLcdGridReference(new TLcdGeodeticDatum(), new TLcdEquidistantCylindrical()));
    model.setModelDescriptor(new TLcdModelDescriptor(
        "Layer containing grid hippodromes",   // source name (is used as tooltip text)
        "Hippodrome",       // data type
        "XY Hippodrome"   // display name (user)
    ));
    XYHippodrome hippodrome = new XYHippodrome(new TLcdXYPoint(185000, 5360000), new TLcdXYPoint(390000, 5800000), 10000);
    model.addElement(hippodrome, ILcdFireEventMode.NO_EVENT);
    return model;
  }

}
