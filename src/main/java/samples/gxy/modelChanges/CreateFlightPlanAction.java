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
package samples.gxy.modelChanges;

import static samples.gxy.fundamentals.step2.FlightPlanDataTypes.FLIGHT_PLAN_DATA_TYPE;

import java.awt.event.ActionEvent;

import com.luciad.datamodel.ILcdDataObject;
import com.luciad.datamodel.TLcdDataObject;
import com.luciad.gui.ALcdAction;
import com.luciad.model.ILcdModel;
import com.luciad.shape.shape2D.TLcdLonLatPolyline;
import com.luciad.shape.shape3D.ILcd3DEditablePoint;
import com.luciad.shape.shape3D.TLcd3DEditablePointList;
import com.luciad.shape.shape3D.TLcdLonLatHeightPoint;
import com.luciad.util.ILcdFireEventMode;
import com.luciad.util.concurrent.TLcdLockUtil;

import samples.gxy.fundamentals.step2.FlightPlanDataTypes;

/**
 * ILcdAction implementation that removes the selected objects from the ILcdModel's of the
 * ILcdGXYLayer objects in an ILcdGXYView from their respective ILcdModel.
 */
class CreateFlightPlanAction extends ALcdAction {

  private ILcdModel fModel;

  public CreateFlightPlanAction(ILcdModel aFlightPlanModel) {
    super("Create new");
    fModel = aFlightPlanModel;
  }

  public void actionPerformed(ActionEvent e) {
    TLcd3DEditablePointList pl = new TLcd3DEditablePointList(new ILcd3DEditablePoint[]{
        changeRandomly(-110, 50), changeRandomly(-90, 40)}, false);
    // Models are often accessed in multiple threads, so it's good practice to guard model changes with a write lock.
    try (TLcdLockUtil.Lock autolock = TLcdLockUtil.writeLock(fModel)) {
      ILcdDataObject flightPlan = new TLcdDataObject(FLIGHT_PLAN_DATA_TYPE);
      flightPlan.setValue(FlightPlanDataTypes.NAME, "Flight plan");
      flightPlan.setValue(FlightPlanDataTypes.POLYLINE, new TLcdLonLatPolyline(pl));
      fModel.addElement(flightPlan, ILcdFireEventMode.FIRE_NOW);
    }
  }

  private TLcdLonLatHeightPoint changeRandomly(double aLon, double aLat) {
    TLcdLonLatHeightPoint point = new TLcdLonLatHeightPoint(aLon, aLat, 0);
    point.translate2D(Math.random() * 10 - 5, Math.random() * 10 - 5);
    return point;
  }
}
