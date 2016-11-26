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
package samples.lucy.undo;

import com.luciad.gui.ALcdAction;
import com.luciad.gui.ILcdIcon;
import com.luciad.gui.ILcdUndoable;
import com.luciad.gui.ILcdUndoableListener;
import com.luciad.gui.TLcdUndoSupport;

/**
 * Action that has an undo support.
 */
public abstract class UndoableAction extends ALcdAction {

  private TLcdUndoSupport fUndoSupport = new TLcdUndoSupport(this);

  protected UndoableAction() {
  }

  protected UndoableAction(String aSourceName) {
    super(aSourceName);
  }

  protected UndoableAction(String aSourceName, ILcdIcon aILcdIcon) {
    super(aSourceName, aILcdIcon);
  }

  public void addUndoableListener(ILcdUndoableListener aListener) {
    fUndoSupport.addUndoableListener(aListener);
  }

  public void removeUndoableListener(ILcdUndoableListener aListener) {
    fUndoSupport.removeUndoableListener(aListener);
  }

  protected void fireUndoableHappened(ILcdUndoable aUndoable) {
    fUndoSupport.fireUndoableHappened(aUndoable);
  }
}
