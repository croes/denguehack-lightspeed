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
package samples.lightspeed.icons3d;

import com.luciad.geodesy.TLcdGeodeticDatum;
import com.luciad.model.ILcdModel;
import com.luciad.model.TLcdModelDescriptor;
import com.luciad.model.TLcdVectorModel;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.shape.shape3D.TLcdLonLatHeightPoint;
import com.luciad.view.animation.ALcdAnimationManager;

class FlyCircleModelFactory {

  public static TLcdVectorModel createPointModel() {
    TLcdVectorModel pointModel = new TLcdVectorModel();
    pointModel.setModelReference(new TLcdGeodeticReference(new TLcdGeodeticDatum()));
    pointModel.setModelDescriptor(new TLcdModelDescriptor("Points", "Points", "Points"));

    createFlyCirclePoint(pointModel, -2, -1, 40000, 3e5, true);
    createFlyCirclePoint(pointModel, -2, -1, 80000, 6e5, false);
    createFlyCirclePoint(pointModel, -2, -1, 120000, 9e5, true);

    return pointModel;
  }

  public static void createFlyCirclePoint(TLcdVectorModel aModel, double aLon, double aLat, double aHeight, double aRadius, boolean aCCW) {
    OrientedPoint orientedPoint = new OrientedPoint(aLon, aLat, aHeight, 0, 0, 0);
    FlyCircleAnimation animation = new FlyCircleAnimation(aModel, orientedPoint, new TLcdLonLatHeightPoint(0, 0, 0), aRadius, aCCW, 277000.0);
    ALcdAnimationManager.getInstance().putAnimation(orientedPoint, animation);
    aModel.addElement(orientedPoint, ILcdModel.NO_EVENT);
  }
}
