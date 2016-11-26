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
package samples.lucy.drawing.styles;

import java.util.HashMap;
import java.util.Map;

import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.lucy.addons.ALcyAddOn;
import com.luciad.lucy.addons.drawing.format.TLcySLDDomainObject;
import com.luciad.lucy.addons.drawing.model.TLcyDrawingStyleRepository;
import com.luciad.lucy.map.ILcyGenericMapComponent;
import com.luciad.lucy.map.ILcyGenericMapManagerListener;
import com.luciad.lucy.map.TLcyGenericMapManagerEvent;
import com.luciad.view.ILcdLayer;
import com.luciad.view.ILcdView;

/**
 * This add-on adds a apply style menu to the drawing toolbar. It
 * illustrates how styles can be put in the {@link TLcyDrawingStyleRepository}
 * and how they can be applied on {@link TLcySLDDomainObject} instances.
 */
public abstract class ApplyStyleAddOn extends ALcyAddOn {

  private Map<ILcyGenericMapComponent<?, ?>, ApplyStyleActionFactory> fStyleActionFactories = new HashMap<ILcyGenericMapComponent<?, ?>, ApplyStyleActionFactory>();
  private ILcyGenericMapManagerListener<ILcdView, ILcdLayer> fMapManagerListener;

  /**
   * The only thing we basically need to do is to add the apply style
   * action to each map component.
   */
  @Override
  public void plugInto(ILcyLucyEnv aLucyEnv) {
    fMapManagerListener = new MyMapContainerListener();
    aLucyEnv.getCombinedMapManager().addMapManagerListener(fMapManagerListener, true);
  }

  @Override
  public void unplugFrom(ILcyLucyEnv aLucyEnv) {
    aLucyEnv.getCombinedMapManager().removeMapManagerListener(fMapManagerListener);
  }

  protected abstract boolean canHandleMapComponent(ILcyGenericMapComponent<?, ?> aMapComponent);

  protected abstract ApplyStyleActionFactory createApplyStyleActionFactory(ILcyGenericMapComponent<?, ?> aMapComponent);

  private class MyMapContainerListener implements ILcyGenericMapManagerListener<ILcdView, ILcdLayer> {
    @Override
    public void mapManagerChanged(TLcyGenericMapManagerEvent aMapManagerEvent) {
      final ILcyGenericMapComponent mapComponent = aMapManagerEvent.getMapComponent();
      if (!canHandleMapComponent(mapComponent)) {
        return;
      }
      if (aMapManagerEvent.getId() == TLcyGenericMapManagerEvent.MAP_COMPONENT_ADDED) {
        fStyleActionFactories.put(mapComponent, createApplyStyleActionFactory(mapComponent));
      } else if (aMapManagerEvent.getId() == TLcyGenericMapManagerEvent.MAP_COMPONENT_REMOVED) {
        ApplyStyleActionFactory applyStyleActionFactory = fStyleActionFactories.remove(mapComponent);
        if (applyStyleActionFactory != null) {
          applyStyleActionFactory.cleanup();
        }
      }
    }
  }

}
