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
package samples.lucy.frontend.dockableframes;

import java.awt.GraphicsDevice;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.beans.PropertyVetoException;

import javax.swing.AbstractAction;
import javax.swing.Action;

import com.jidesoft.docking.DockableFrame;
import com.luciad.gui.TLcdAWTUtil;
import samples.common.SwingUtil;
import com.luciad.lucy.util.language.TLcyLang;

/**
 * <p>Custom action to maximize a JIDE {@code DockableFrame}.</p>
 *
 * <p>When the frame is docked, we re-use the default action from JIDE. When the frame is undocked,
 * we maximize it directly. This is different from the default JIDE action, which re-docks the frame
 * before resizing.</p>
 */
class DockableFrameMaximizeAction extends AbstractAction {
  private static final String MAXIMIZE = TLcyLang.getString("Maximize");
  private static final String RESTORE = TLcyLang.getString("Restore");

  private Action fDefaultMaximizeAction;
  private DockableFrame fDockableFrame;
  private Rectangle fPreviousBounds;

  public DockableFrameMaximizeAction(DockableFrame aDockableFrame, Action aDefaultMaximizeAction) {
    super(MAXIMIZE);
    fDockableFrame = aDockableFrame;
    fDefaultMaximizeAction = aDefaultMaximizeAction;
  }

  @Override
  public void actionPerformed(ActionEvent aActionEvent) {
    if (fDockableFrame.isDocked()) {
      fDefaultMaximizeAction.actionPerformed(aActionEvent);
    } else {
      Window parent_window = TLcdAWTUtil.findParentWindow(fDockableFrame);
      if (fPreviousBounds != null) {
        parent_window.setBounds(fPreviousBounds);

        fPreviousBounds = null;
        putValue(NAME, MAXIMIZE);
        markToJIDEAsMaximized(false);
      } else {
        GraphicsDevice device = SwingUtil.getScreenDevice(parent_window);
        Rectangle usable_bounds = device.getDefaultConfiguration().getBounds();

        fPreviousBounds = parent_window.getBounds();
        parent_window.setBounds(usable_bounds);

        putValue(NAME, RESTORE);
        markToJIDEAsMaximized(true);
      }
    }
  }

  private void markToJIDEAsMaximized(boolean aMaximized) {
    try {
      fDockableFrame.setMaximized(aMaximized);
    } catch (PropertyVetoException e) {
      throw new RuntimeException(e);
    }
  }
}
