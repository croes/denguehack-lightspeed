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
package samples.lucy.frontend.internalframes;

import java.awt.Container;
import java.awt.Point;

import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.SwingUtilities;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

import com.luciad.gui.TLcdAWTUtil;
import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.lucy.gui.ILcyApplicationPane;
import com.luciad.lucy.gui.ILcyApplicationPaneFactory;
import com.luciad.lucy.gui.ILcyApplicationPaneOwner;
import com.luciad.lucy.map.ILcyMapComponent;
import com.luciad.lucy.map.ILcyMapManager;

/**
 * Implementation of <code>ILcyApplicationPaneFactory</code> that creates <code>InternalFrameAppPane</code>s,
 * based on Swings <code>JDesktopPane</code> and <code>JInternalFrame</code>.
 */
public class InternalFrameAppPaneFactory implements ILcyApplicationPaneFactory {
  private final ActiveMapMediator fActiveMapMediator = new ActiveMapMediator();

  private JDesktopPane fDesktopPane;
  private ILcyMapManager fMapManager;
  private ILcyLucyEnv fLucyEnv;

  public InternalFrameAppPaneFactory(JDesktopPane aDesktopPane) {
    fDesktopPane = aDesktopPane;
  }

  public ILcyMapManager getMapManager() {
    return fMapManager;
  }

  public void setLucyEnv(ILcyLucyEnv aLucyEnv) {
    fLucyEnv = aLucyEnv;
    fMapManager = fLucyEnv.getMapManager();
  }

  @Override
  public ILcyApplicationPane createApplicationPane(ILcyApplicationPaneOwner aOwner) {
    return createApplicationPane(0, aOwner);
  }

  @Override
  public ILcyApplicationPane createApplicationPane(int aLocationIndex, ILcyApplicationPaneOwner aOwner) {
    final InternalFrameAppPane internal_frame = new InternalFrameAppPane(aOwner, fLucyEnv);

    //Make sure the active map is up to date
    internal_frame.addInternalFrameListener(fActiveMapMediator);

    //Cascade the new frame relative to the previous one
    JInternalFrame[] frames = fDesktopPane.getAllFrames();
    if (frames.length > 0) {
      JInternalFrame selected = fDesktopPane.getSelectedFrame();
      if (selected == null) {
        selected = frames[frames.length - 1];
      }
      Point loc = selected.getLocation();
      internal_frame.setLocation(loc.x + 30, loc.y + 30);
    }

    fDesktopPane.add(internal_frame);

    fLucyEnv.getUserInterfaceManager().getApplicationPaneManager().applicationPaneAdded(internal_frame);

    return internal_frame;
  }

  /**
   * When an internal frame is selected that contains a map, this listener makes sure that map
   * becomes the active map.
   */
  private class ActiveMapMediator extends InternalFrameAdapter {

    @Override
    public void internalFrameActivated(final InternalFrameEvent e) {
      //Use invokeLater allowing all components to be added to the internal frame before checking
      //whether the active map must be changed
      TLcdAWTUtil.invokeLater(new Runnable() {
        @Override
        public void run() {
          handleEvent(e);
        }
      });
    }

    private void handleEvent(InternalFrameEvent e) {
      if (fMapManager != null) {
        Container content_pane = e.getInternalFrame().getContentPane();

        for (int i = 0, c = fMapManager.getMapComponentCount(); i < c; i++) {
          ILcyMapComponent map = fMapManager.getMapComponent(i);
          if (SwingUtilities.isDescendingFrom(map.getComponent(), content_pane)) {
            fMapManager.setActiveMapComponent(map);
          }
        }
      }
    }
  }
}
