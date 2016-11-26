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
package samples.common.action;

import java.awt.Component;
import java.awt.Point;
import java.awt.event.ActionEvent;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import com.luciad.gui.ALcdAction;
import com.luciad.gui.ALcdActionWrapper;
import com.luciad.gui.ILcdAction;
import com.luciad.gui.TLcdActionAtLocationEvent;
import com.luciad.gui.swing.TLcdSWAction;
import com.luciad.util.logging.ILcdLogger;
import com.luciad.util.logging.TLcdLoggerFactory;

/**
 * This ILcdAction implementation pops up a PopupMenu to which you can add a set of actions
 * to execute.
 */
public class ShowPopupAction
    extends ALcdAction
    implements ILcdAction {

  private static ILcdLogger sLogger = TLcdLoggerFactory.getLogger(ShowPopupAction.class.getName());

  private ILcdAction[] fActionArray;
  private Component fComponent;

  public ShowPopupAction(ILcdAction[] aActionArray, Component aComponent) {
    super("PopupActionChoice");
    fActionArray = aActionArray;
    fComponent = aComponent;
  }

  public void actionPerformed(ActionEvent e) {
    if (fComponent != null) {
      int x, y;
      if (e instanceof TLcdActionAtLocationEvent) {
        x = ((TLcdActionAtLocationEvent) e).getLocation().x;
        y = ((TLcdActionAtLocationEvent) e).getLocation().y;
      } else {
        x = fComponent.getWidth() / 2;
        y = fComponent.getHeight() / 2;
      }
      JPopupMenu menu = makePopupMenu(x, y);
      if (menu.getComponentCount() > 0) {
        menu.show(fComponent, x, y);
      }
    } else {
      sLogger.error("actionPerformed: cannot show popup on null component");
    }
  }

  protected JPopupMenu makePopupMenu(final int aX, final int aY) {
    final JPopupMenu menu = new JPopupMenu("Actions:");
    for (final ILcdAction action : fActionArray) {
      if (action == null) {
        menu.addSeparator();
      } else {
        if (action.getValue(ILcdAction.VISIBLE) != Boolean.valueOf(false)) {
          menu.add(new JMenuItem(new TLcdSWAction(new ALcdActionWrapper(action) {
            @Override
            public void actionPerformed(ActionEvent e) {
              // pass the location to the sub-actions
              TLcdActionAtLocationEvent atLocationEvent = new TLcdActionAtLocationEvent(e.getSource(), menu, e.getID(), e.getActionCommand(), e.getModifiers(), new Point(aX, aY));
              action.actionPerformed(atLocationEvent);
            }
          })));
        }
      }
    }
    return menu;
  }
}
