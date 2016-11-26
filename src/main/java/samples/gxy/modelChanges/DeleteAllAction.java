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

import java.awt.event.ActionEvent;

import com.luciad.gui.ALcdAction;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelListener;
import com.luciad.model.TLcdModelChangedEvent;
import com.luciad.util.ILcdFireEventMode;
import com.luciad.util.concurrent.TLcdLockUtil;

/**
 * ILcdAction implementation that removes the selected objects from the models of the
 * ILcdGXYLayer objects in an ILcdGXYView from their respective ILcdModel.
 */
class DeleteAllAction extends ALcdAction {
  private ILcdModel fModel;

  public DeleteAllAction(ILcdModel aModel) {
    super("Delete all");
    fModel = aModel;

    // Listen for changes in the model to update the enabled state of this action
    aModel.addModelListener(new ILcdModelListener() {
      public void modelChanged(TLcdModelChangedEvent aEvent) {
        updateEnabledState();
      }
    });
    // Make sure the initial enabled state is correct
    updateEnabledState();
  }

  private void updateEnabledState() {
    setEnabled(fModel.elements().hasMoreElements());
  }

  public void actionPerformed(ActionEvent e) {
    // Models are often accessed in multiple threads, so it's good practice to guard model changes with a write lock.
    try (TLcdLockUtil.Lock autolock = TLcdLockUtil.writeLock(fModel)) {
      fModel.removeAllElements(ILcdFireEventMode.FIRE_NOW);
    }
  }
}
