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

import static samples.gxy.fundamentals.step2.FlightPlanDataTypes.POLYLINE;

import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Random;

import com.luciad.datamodel.ILcdDataObject;
import com.luciad.model.ILcdModel;
import com.luciad.shape.shape2D.ILcd2DEditablePolyline;
import com.luciad.util.ILcdFilter;
import com.luciad.util.ILcdFireEventMode;
import com.luciad.util.concurrent.TLcdLockUtil;
import com.luciad.util.concurrent.TLcdLockUtil.Lock;
import com.luciad.view.ALcdObjectSelectionAction;
import com.luciad.view.ILcdLayer;
import com.luciad.view.ILcdView;
import com.luciad.view.TLcdDomainObjectContext;

/**
 * This action modifies the selected elements of the flight plan model, then notifies the model about the
 * change. It also enables or disables itself, depending on whether there are any selected elements
 * in the layer that contains the model.
 */
class ChangeSelectionAction extends ALcdObjectSelectionAction {
  private Random fRandom = new Random(37);

  public ChangeSelectionAction(ILcdView aView, final ILcdLayer aFlightPlanLayer) {
    super(aView, new DomainObjectContextFilter(aFlightPlanLayer));
    setName("Change selection");
  }

  @Override
  protected void actionPerformed(ActionEvent aActionEvent, List<TLcdDomainObjectContext> aSelection) {
    // This action is limited to a single model.
    ILcdModel model = aSelection.get(0).getModel();
    for (TLcdDomainObjectContext context : aSelection) {
      // We can make this cast as we know the selection only contains flight plans
      ILcdDataObject object = (ILcdDataObject) context.getDomainObject();

      // Randomly shift it
      ILcd2DEditablePolyline polyline = (ILcd2DEditablePolyline) object.getValue(POLYLINE);
      polyline.translate2D(fRandom.nextDouble() - 0.5, fRandom.nextDouble() - 0.5);

      // Models are often accessed in multiple threads, so it's good practice to guard model changes with a write lock.
      try (Lock autolock = TLcdLockUtil.writeLock(model)){
        // Notify the model that this object has changed.
        // The model should not fire an event yet, because more elements might change soon.
        model.elementChanged(object, ILcdFireEventMode.FIRE_LATER);
      }
    }
    model.fireCollectedModelChanges();
  }

  /**
   * Only accepts domain objects belonging to a given layer.
   */
  static class DomainObjectContextFilter implements ILcdFilter<TLcdDomainObjectContext> {

    private final ILcdLayer fLayer;

    public DomainObjectContextFilter(ILcdLayer aLayer) {
      fLayer = aLayer;
    }

    @Override
    public boolean accept(TLcdDomainObjectContext aObject) {
      return aObject.getLayer() == fLayer;
    }
  }
}
