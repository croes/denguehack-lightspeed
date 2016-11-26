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

import java.awt.AWTEvent;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import com.luciad.shape.ILcdPoint;
import com.luciad.shape.shape2D.TLcdXYPoint;
import com.luciad.view.lightspeed.editor.TLspEditContext;
import com.luciad.view.lightspeed.editor.TLspHandleGeometryType;
import com.luciad.view.lightspeed.editor.handle.ALspObjectTranslationHandle;
import com.luciad.view.lightspeed.editor.operation.ELspInteractionStatus;
import com.luciad.view.lightspeed.editor.operation.TLspEditHandleResult;
import com.luciad.view.lightspeed.editor.operation.TLspEditOperation;
import com.luciad.view.lightspeed.editor.operation.TLspEditOperationType;
import com.luciad.view.lightspeed.editor.operation.TLspMoveDescriptor;
import com.luciad.view.lightspeed.layer.ILspInteractivePaintableLayer;
import com.luciad.view.lightspeed.query.TLspIsTouchedQuery;
import com.luciad.view.lightspeed.style.styler.ALspStyleTargetProvider;

/**
 * A handle that allows translation of a whole object by touching the object and
 * dragging/moving after pressing/clicking the left mouse button.
 * <p/>
 */
class ScreenSpaceObjectTranslationHandle extends ALspObjectTranslationHandle {

  private ILcdPoint fTempPoint;

  /**
   * Creates a translation handle to move the given editor. Translating
   * the object is performed by pressing/clicking the left mouse button and dragging/moving.
   *
   * @param aDomainObject the domain object for which to create the handle
   */
  public ScreenSpaceObjectTranslationHandle(Object aDomainObject) {
    super(aDomainObject);
  }

  @Override
  public boolean isObjectTouched(int aX, int aY, TLspEditContext aEditContext) {
    return ((ILspInteractivePaintableLayer) aEditContext.getObjectContext().getLayer()).query(
        new TLspIsTouchedQuery(aEditContext.getObject(), UAVScreenOverlayLayerFactory.OVERLAY, new TLcdXYPoint(aX, aY), aEditContext.getSensitivity()),
        aEditContext.getObjectContext()
    );
  }

  @Override
  protected TLspEditHandleResult onActivate(AWTEvent aEvent, TLspEditContext aEditContext) {
    if (aEvent instanceof MouseEvent) {
      fTempPoint = new TLcdXYPoint(((MouseEvent) aEvent).getX(), ((MouseEvent) aEvent).getY());
    }
    return super.onActivate(aEvent, aEditContext);
  }

  @Override
  protected TLspEditHandleResult process(AWTEvent aEvent, TLspEditContext aEditContext) {
    if (aEvent instanceof MouseEvent) {
      ILcdPoint startPoint = fTempPoint;
      fTempPoint = new TLcdXYPoint(((MouseEvent) aEvent).getX(), ((MouseEvent) aEvent).getY());
      return createMoveEditResult(startPoint, fTempPoint, ELspInteractionStatus.IN_PROGRESS);
    }
    return createNoEditResult(aEvent, ELspInteractionStatus.IN_PROGRESS);
  }

  @Override
  protected TLspEditHandleResult onDeactivate(AWTEvent aEvent, TLspEditContext aEditContext) {
    fTempPoint = null;
    return super.onDeactivate(aEvent, aEditContext);
  }

  private TLspEditHandleResult createNoEditResult(AWTEvent aEvent, ELspInteractionStatus aInteractionStatus) {
    return new TLspEditHandleResult(aEvent, aInteractionStatus);
  }

  private TLspEditHandleResult createMoveEditResult(ILcdPoint aStartLocation, ILcdPoint aNewLocation, ELspInteractionStatus aInteractionStatus) {
    TLspMoveDescriptor moveDescriptor = new TLspMoveDescriptor(TLspMoveDescriptor.Constraints.XY, null, aStartLocation, aNewLocation);
    HashMap<Object, Object> properties = new HashMap<Object, Object>();
    properties.put(TLspEditOperationType.MOVE.getPropertyKey(), moveDescriptor);
    TLspEditOperation editEvent = new TLspEditOperation(TLspEditOperationType.MOVE, properties);
    return new TLspEditHandleResult(editEvent, null, aInteractionStatus);
  }

  @Override
  public List<ALspStyleTargetProvider> getStyleTargetProviders(TLspHandleGeometryType aType) {
    return Collections.emptyList();
  }
}
