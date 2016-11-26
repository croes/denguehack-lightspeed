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
package samples.gxy.editing.actions;

import static com.luciad.util.concurrent.TLcdLockUtil.Lock;
import static com.luciad.util.concurrent.TLcdLockUtil.writeLock;

import java.awt.event.ActionEvent;
import java.util.List;

import com.luciad.gui.TLcdActionAtLocationEvent;
import com.luciad.model.ILcdModel;
import com.luciad.shape.ILcdCompositeCurve;
import com.luciad.shape.ILcdCurve;
import com.luciad.shape.ILcdEditableCompositeCurve;
import com.luciad.shape.ILcdRing;
import com.luciad.shape.constraints.TLcdCurveConnectorUtil;
import com.luciad.shape.constraints.TLcdDefaultCurveConnectorProvider;
import com.luciad.util.ILcdFilter;
import com.luciad.view.ALcdObjectSelectionAction;
import com.luciad.view.TLcdDomainObjectContext;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.ILcdGXYView;
import com.luciad.view.gxy.TLcdGXYContext;

/**
 * Removes the selected curve from a composite curve.
 */
public class RemoveCurveAction extends ALcdObjectSelectionAction {

  public RemoveCurveAction(ILcdGXYView aView) {
    super(aView,
          new ILcdFilter<TLcdDomainObjectContext>() {
            @Override
            public boolean accept(TLcdDomainObjectContext aObject) {
              Object domainObject = aObject.getDomainObject();
              return domainObject instanceof ILcdEditableCompositeCurve &&
                     ((ILcdEditableCompositeCurve) domainObject).getCurves().size() > 1;
            }
          });
    setName("Remove sub-curve");
  }

  @Override
  protected void actionPerformed(ActionEvent aActionEvent, List<TLcdDomainObjectContext> aSelection) {
    TLcdDomainObjectContext domainObjectContext = aSelection.get(0);
    ILcdEditableCompositeCurve compositeCurve = (ILcdEditableCompositeCurve) domainObjectContext.getDomainObject();
    ILcdGXYLayer layer = (ILcdGXYLayer) domainObjectContext.getLayer();

    // Retrieves the touched segment.

    if (!(aActionEvent instanceof TLcdActionAtLocationEvent)) {
      throw new IllegalArgumentException("This action needs a location to operate");
    }
    TLcdActionAtLocationEvent event = (TLcdActionAtLocationEvent) aActionEvent;
    TLcdGXYContext context = new TLcdGXYContext((ILcdGXYView) getView(), layer);
    context.setX(event.getLocation().x);
    context.setY(event.getLocation().y);


    ILcdCurve touchedSegment = InsertCurveAction.retrieveTouchedSegment(compositeCurve, context);
    if (touchedSegment == null) {
      return;
    }

    // Removes the segment.

    ILcdModel model = layer.getModel();
    try (Lock autoUnlock = writeLock(model)) {
      int index = compositeCurve.getCurves().indexOf(touchedSegment);
      compositeCurve.getCurves().remove(index);
      connectCompositeCurve(layer, compositeCurve,
                            index != 0 ? index - 1 :
                            (compositeCurve instanceof ILcdRing ? compositeCurve.getCurves().size() - 1 : 0));
      if (compositeCurve.getCurves().size() == 0) {
        model.removeElement(compositeCurve, ILcdModel.FIRE_LATER);
      }
      model.elementChanged(compositeCurve, ILcdModel.FIRE_LATER);
    }
    model.fireCollectedModelChanges();

  }

  private void connectCompositeCurve(ILcdGXYLayer aLayer, ILcdCompositeCurve aCompositeCurve, int aStartIndex) {
    TLcdDefaultCurveConnectorProvider connectorProvider = new TLcdDefaultCurveConnectorProvider();
    if (aCompositeCurve instanceof ILcdRing) {
      TLcdCurveConnectorUtil.connectCompositeRing(aCompositeCurve, aStartIndex, connectorProvider, aLayer.getModel().getModelReference());
    } else {
      TLcdCurveConnectorUtil.connectCompositeCurve(aCompositeCurve, aStartIndex, connectorProvider, aLayer.getModel().getModelReference());
    }
  }
}
