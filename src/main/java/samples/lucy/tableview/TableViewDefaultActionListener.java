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
package samples.lucy.tableview;

import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.SwingUtilities;

import com.luciad.gui.ILcdAction;
import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.lucy.gui.ILcyActionBar;
import com.luciad.lucy.gui.ILcyActiveSettable;
import com.luciad.lucy.gui.TLcyActionBarUtil;

/**
 * Listener which will trigger the default action/active settable of an action bar when a double
 * left click is detected
 */
class TableViewDefaultActionListener extends MouseAdapter {
  private String fActionBarID;
  private ILcyLucyEnv fLucyEnv;
  private Object fContext;

  TableViewDefaultActionListener(String aActionBarID,
                                 Object aActionBarContext,
                                 ILcyLucyEnv aLucyEnv) {
    fActionBarID = aActionBarID;
    fLucyEnv = aLucyEnv;
    fContext = aActionBarContext;
  }

  @Override
  public void mouseClicked(MouseEvent aMouseEvent) {
    if (aMouseEvent.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(aMouseEvent)) {
      ILcyActionBar actionBar = fLucyEnv.getUserInterfaceManager().getActionBarManager().getActionBar(fActionBarID, fContext);
      if (actionBar != null) {
        Object firstDefaultActionItem = TLcyActionBarUtil.findFirstDefaultActionItem(actionBar);
        if (firstDefaultActionItem instanceof ILcdAction &&
            ((ILcdAction) firstDefaultActionItem).isEnabled()) {
          ActionEvent actionEvent = new ActionEvent(aMouseEvent.getSource(), ActionEvent.ACTION_PERFORMED, "actionPerformed");
          ((ILcdAction) firstDefaultActionItem).actionPerformed(actionEvent);
        } else if (firstDefaultActionItem instanceof ILcyActiveSettable &&
                   ((ILcyActiveSettable) firstDefaultActionItem).isEnabled()) {
          ((ILcyActiveSettable) firstDefaultActionItem).setActive(!(((ILcyActiveSettable) firstDefaultActionItem).isActive()));
        }
      }

    }
  }

}
