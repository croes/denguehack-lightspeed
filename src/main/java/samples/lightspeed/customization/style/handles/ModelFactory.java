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
package samples.lightspeed.customization.style.handles;

import com.luciad.geodesy.TLcdEllipsoid;
import com.luciad.geodesy.TLcdGeodeticDatum;
import com.luciad.model.ILcdModel;
import com.luciad.model.TLcdModelDescriptor;
import com.luciad.model.TLcdVectorModel;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.shape.shape2D.TLcdLonLatEllipse;

class ModelFactory {

  public static ILcdModel createEllipseModel() {
    TLcdVectorModel model = new TLcdVectorModel(new TLcdGeodeticReference());
    TLcdGeodeticDatum datum = new TLcdGeodeticDatum();
    TLcdGeodeticReference modelRef = new TLcdGeodeticReference(datum);
    model.setModelReference(modelRef);
    model.setModelDescriptor(new TLcdModelDescriptor(
        "Shapes Data Set",    // source name
        "Shapes",             // data type
        "Shapes Model"        // display name (user)
    ));
    addEllipses(model);
    return model;
  }

  private static void addEllipses(ILcdModel aModel) {
    TLcdLonLatEllipse ellipse = new TLcdLonLatEllipse(-122.45, 37.74, 2000, 1000, 10, TLcdEllipsoid.DEFAULT);
    aModel.addElement(ellipse, ILcdModel.NO_EVENT);
    TLcdLonLatEllipse ellipse2 = new TLcdLonLatEllipse(-122.452, 37.745, 1800, 900, 40, TLcdEllipsoid.DEFAULT);
    aModel.addElement(ellipse2, ILcdModel.NO_EVENT);
  }
}
