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
package samples.lightspeed.demo.application.data.uav;

import com.luciad.model.ILcdModel;
import com.luciad.model.TLcdModelDescriptor;
import com.luciad.model.TLcdVectorModel;
import com.luciad.reference.TLcdGeodeticReference;

/**
 * Creates the model for indicating a specific point of the 2D video on the 3D terrain.
 */
public class UAVProjectedPointModelFactory {

  private UAVProjectedPointModelFactory() {
  }

  public static ILcdModel newModel() {
    return new TLcdVectorModel(
        new TLcdGeodeticReference(),
        new TLcdModelDescriptor(null, "UAVProjectedPoint", "UAVProjectedPoint")
    );
  }

  public static void setProjectedPoint(ILcdModel aProjectedPointModel, double aVideoPixelX, double aVideoPixelY) {
    TLcdVectorModel model = (TLcdVectorModel) aProjectedPointModel;
    if (model.size() == 0) {
      model.addElement(new UAVVideoPoint(aVideoPixelX, aVideoPixelY), ILcdModel.FIRE_NOW);
    } else {
      UAVVideoPoint UAVVideoPoint = (UAVVideoPoint) model.elementAt(0);
      UAVVideoPoint.setVideoPoint(aVideoPixelX, aVideoPixelY);
      model.allElementsChanged(ILcdModel.FIRE_NOW);
    }
  }

  public static void removeProjectedPoint(ILcdModel aProjectedPointModel) {
    TLcdVectorModel model = (TLcdVectorModel) aProjectedPointModel;
    if (model.size() > 0) {
      model.removeAllElements(ILcdModel.FIRE_NOW);
    }
  }

}
