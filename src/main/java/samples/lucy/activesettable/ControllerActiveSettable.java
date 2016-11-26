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
package samples.lucy.activesettable;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import com.luciad.lucy.gui.ALcyActiveSettable;
import com.luciad.view.gxy.ILcdGXYController;
import com.luciad.view.gxy.ILcdGXYView;

/**
 * Active settable equivalent of TLcdGXYSetControllerAction. This ILcyActiveSettable
 * returns 'true' for 'isActive' if the ILcdGXYController of the ILcdGXYView is the
 * ILcdGXYController that was given to the constructor of this ControllerActiveSettable.
 *
 * This action is also readily available as <code>TLcyGXYSetControllerActiveSettable</code>, but it is
 * provided here to demonstrate how to write an <code>ALcyActiveSettable</code> that retrieves
 * its active state from elsewhere (fGXYView.getGXYController).
 */
final class ControllerActiveSettable extends ALcyActiveSettable implements PropertyChangeListener {
  private ILcdGXYController fGXYController;
  private ILcdGXYView fGXYView;

  /**
   * Creates a new ControllerActiveSettable for the given controller and the given view.
   * @param aGXYController The controller to set enable.
   * @param aGXYView The view where the controller will be set on.
   */
  public ControllerActiveSettable(ILcdGXYController aGXYController, ILcdGXYView aGXYView) {
    setIcon(aGXYController.getIcon());
    setName(aGXYController.getName());
    setShortDescription(aGXYController.getShortDescription());

    fGXYController = aGXYController;
    fGXYView = aGXYView;
    fGXYView.addPropertyChangeListener(this);
  }

  @Override
  public boolean isActive() {
    return fGXYView.getGXYController() == fGXYController;
  }

  @Override
  public void setActive(boolean aActive) {
    //ignore a setActive( false ) because we never want to disable the controller,
    //it can only be disabled by enabling another controller.
    if (aActive && fGXYView.getGXYController() != fGXYController) {
      //Note that this will cause the view to fire a "GXYController" property change,
      //which will in turn throw an "active" property change (see below)
      fGXYView.setGXYController(fGXYController);
    }
  }

  /**
   * Converts the "GXYController" property change event from the <code>ILcdGXYView</code> that
   * indicates a controller change to an "active" property change of this active settable.
   */
  @Override
  public void propertyChange(PropertyChangeEvent aEvent) {
    if (aEvent.getPropertyName().equalsIgnoreCase("GXYController")) {
      boolean was_active = aEvent.getOldValue() == fGXYController;
      boolean is_active = aEvent.getNewValue() == fGXYController;
      if (was_active != is_active) {
        //indicate to the listeners of this ALcyActiveSettable that our active state has changed
        firePropertyChange("active", was_active, is_active);
      }
    }
  }
}
