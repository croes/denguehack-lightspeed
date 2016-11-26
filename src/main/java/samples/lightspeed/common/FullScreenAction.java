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
package samples.lightspeed.common;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import com.luciad.gui.ALcdAction;
import com.luciad.gui.ILcdAction;
import com.luciad.gui.TLcdIconFactory;
import com.luciad.util.TLcdSystemPropertiesUtil;
import com.luciad.view.lightspeed.ILspAWTView;

import samples.common.MacUtil;

/**
 * An action which allows a view to be displayed in full screen mode.
 */
public class FullScreenAction extends ALcdAction {

  public enum Mode {
    /**
     * The view is displayed on the entire screen
     */
    FULL_SCREEN("Full screen mode"),

    /**
     * The view is displayed in a window
     */
    WINDOWED("Windowed mode");

    private String fDisplayName;

    Mode(String aDisplayName) {
      fDisplayName = aDisplayName;
    }

    @Override
    public String toString() {
      return fDisplayName;
    }
  }

  private final ILspAWTView fView;

  private Container fWindowedParent;
  private JFrame fFullScreenWindow;
  private Component fFullScreenComponent;
  private final ILcdAction fRestoreAction;

  public FullScreenAction(ILspAWTView aView) {
    fView = aView;
    setIcon(TLcdIconFactory.create(TLcdIconFactory.FULL_SCREEN_ICON));
    setName("");
    setShortDescription("Full screen");
    fRestoreAction = new ALcdAction(getName(), TLcdIconFactory.create(TLcdIconFactory.EXIT_FULL_SCREEN_ICON)) {
      @Override
      public void actionPerformed(ActionEvent e) {
        FullScreenAction.this.actionPerformed(e);
      }
    };
    fRestoreAction.putValue(ILcdAction.VISIBLE, false);
  }

  /**
   * Returns an action that restores a full screen view to windowed mode.
   * This can be used in a button that is showed on the map itself.
   * The action is automatically made {@link ILcdAction#VISIBLE visible} when appropriate.
   * @return an action that restores a full screen view to windowed mode
   */
  public ILcdAction getRestoreAction() {
    return fRestoreAction;
  }

  /**
   * Returns the current mode with which the view is displayed.
   * @return the current mode with which the view is displayed
   */
  public Mode getViewMode() {
    return fFullScreenWindow != null ? Mode.FULL_SCREEN : Mode.WINDOWED;
  }

  /**
   * Set the window full screen/windowed via MacUtil.
   */
  private void doMacOS() {
    fFullScreenComponent = fView.getHostComponent();
    fWindowedParent = fFullScreenComponent.getParent();
    Window window = SwingUtilities.getWindowAncestor(fWindowedParent);
    //set the window full screen if it is windowed or vice versa
    MacUtil.setFullScreen(window);
  }

  @Override
  public void actionPerformed(ActionEvent e) {

    if (TLcdSystemPropertiesUtil.isMacOS()) {
      doMacOS();
    } else {
      Mode oldMode = getViewMode();

      if (oldMode.equals(Mode.WINDOWED)) {
        // Switch from windowed to full screen.

        // Find the component to make full screen.
        fFullScreenComponent = fView.getHostComponent();

        // Remove it from its parent.
        fWindowedParent = fFullScreenComponent.getParent();
        fWindowedParent.remove(fFullScreenComponent);

        // Hide the old window.
        Window window = SwingUtilities.getWindowAncestor(fWindowedParent);
        window.setVisible(false);

        // Create a new window and add the full screen component to it.
        // The GraphicsConfiguration is chosen so that it corresponds to a screen that
        // has the most overlap with the window.
        GraphicsConfiguration configuration = getSuitableGraphicsConfiguration(window);
        fFullScreenWindow = new JFrame(configuration);
        fFullScreenWindow.setUndecorated(true);
        fFullScreenWindow.setFocusableWindowState(true);
        fFullScreenWindow.setLayout(new BorderLayout());
        fFullScreenWindow.add(fFullScreenComponent);
        fFullScreenWindow.setFocusable(true);
        fFullScreenWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        fFullScreenWindow.setIconImages(window.getIconImages());

        // Switch to full screen mode.
        configuration.getDevice().setFullScreenWindow(fFullScreenWindow);
        fFullScreenComponent.requestFocusInWindow();

        fRestoreAction.putValue(ILcdAction.VISIBLE, true);
        putValue(ILcdAction.VISIBLE, false);
      } else {
        // Switch from full screen to windowed.

        // Exit full screen mode.
        GraphicsDevice device = fFullScreenWindow.getGraphicsConfiguration().getDevice();
        device.setFullScreenWindow(null);

        // Remove the full screen component from the full screen window.
        fFullScreenWindow.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        fFullScreenWindow.remove(fFullScreenComponent);
        fFullScreenWindow.dispose();

        // Put the full screen component back in its old parent.
        fWindowedParent.add(fFullScreenComponent);

        // Make the old window visible again.
        Window window = SwingUtilities.getWindowAncestor(fWindowedParent);
        window.setVisible(true);
        fFullScreenComponent.requestFocusInWindow();

        fFullScreenWindow = null;
        fFullScreenComponent = null;
        fWindowedParent = null;

        fRestoreAction.putValue(ILcdAction.VISIBLE, false);
        putValue(ILcdAction.VISIBLE, true);
      }

      Mode newMode = getViewMode();
      fView.invalidate(true, this, newMode.toString());
      firePropertyChange("ViewMode", oldMode, newMode);
    }
  }

  /**
   * Finds the screen that overlaps the most with the given Window.
   *
   * @param aWindow the window
   * @return the GraphicsConfiguration corresponding to the screen that has the most overlap with the Window.
   */
  private static GraphicsConfiguration getSuitableGraphicsConfiguration(Window aWindow) {
    Rectangle windowBounds = aWindow.getBounds();
    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
    GraphicsDevice[] gs = ge.getScreenDevices();

    double maxArea = -Double.MAX_VALUE;
    GraphicsConfiguration bestConfig = null;
    for (GraphicsDevice device : gs) {
      GraphicsConfiguration[] gc = device.getConfigurations();
      for (GraphicsConfiguration aGc : gc) {
        Rectangle bounds = aGc.getBounds();
        Rectangle intersection = windowBounds.intersection(bounds);
        double area = intersection.getWidth() * intersection.getHeight();
        if (area > maxArea) {
          bestConfig = aGc;
          maxArea = area;
        }
      }
    }
    return bestConfig;
  }

}
