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
package samples.lightspeed.debug;

import java.awt.event.ActionEvent;

import javax.swing.JToggleButton;

import com.luciad.gui.ALcdAction;
import com.luciad.gui.TLcdIconFactory;
import com.luciad.view.animation.ALcdAnimation;
import com.luciad.view.animation.ALcdAnimationManager;
import com.luciad.view.animation.ILcdAnimation;
import com.luciad.view.lightspeed.ILspAWTView;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.layer.TLspLayerTreeNode;

/**
 * Action that enables or disables a performance and debug overlay in an
 * {@code ILspView}.<br/>
 * An animation is used to continuously trigger repaints of the view.
 * <p/>
 * The action also triggers listeners for key presses:
 * <ul>
 * <li>'t' Shows the contents of the view cache in a tree</li>
 * <li>'g' Shows the contents of the GL resource cache in a tree</li>
 * <li>'gc' Triggers garbage collection</li>
 * <li>'q' Clears the GL resource cache</li>
 * <li>'s' Loads a dummy texture in video memory, this allows you to increase
 * memory pressure artificially</li>
 * </ul>
 */
public class DebugOverlayToggleAction extends ALcdAction {

  private ILspView fView;
  private TLspLayerTreeNode fLayer;
  private DebugCacheKeyListener fMKCListener;
  private DebugCacheKeyListener fGLRCListener;
  private ILcdAnimation fRepaintLoop;

  public DebugOverlayToggleAction(ILspView aView) {
    super("Debug info", TLcdIconFactory.create(TLcdIconFactory.TABLE_ICON));
    setShortDescription("Enable performance benchmarking and debug overlays");

    fLayer = new TLspLayerTreeNode("Statistics");

    // Shows performance statistics such as FPS, number of vertices, etc.
    PerformanceOverlay performanceOverlay = new PerformanceOverlay();
    fLayer.addLayer(performanceOverlay);

    // Shows OpenGL resources in the GL resource cache, by default this
    // layer is not visible.
    GLResourceOverlay glResourceOverlay = new GLResourceOverlay();
    glResourceOverlay.setVisible(false);
    fLayer.addLayer(glResourceOverlay);

    fView = aView;

    // Creating an ALcdAnimation with a view ensures that the view
    // gets repainted all the time.
    fRepaintLoop = new ALcdAnimation(1.0, aView) {
      @Override
      public boolean isLoop() {
        return true;
      }

      @Override
      protected void setTimeImpl(double aTime) {
      }
    };
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    if (((JToggleButton) e.getSource()).isSelected()) {
      addStatisticsOverlay();
    } else {
      removeStatisticsOverlay();
    }
  }

  /**
   * Adds a layer tree node with a layer that displays some statistics such as
   * the current frame rate, CPU load and memory usage. Also registers key
   * listeners to enable cache inspection and starts a continuous view repaint.
   *
   */
  private void addStatisticsOverlay() {
    fView.addLayer(fLayer);
    fMKCListener = new DebugCacheKeyListener(fView, fView.getServices().getMultiKeyCache(), 't');
    ((ILspAWTView) fView).getHostComponent().addKeyListener(fMKCListener);
    fGLRCListener = new DebugCacheKeyListener(fView, fView.getServices().getGLResourceCache(), 'g');
    ((ILspAWTView) fView).getHostComponent().addKeyListener(fGLRCListener);
    ALcdAnimationManager.getInstance().putAnimation(this, fRepaintLoop);
    fView.setAutoUpdate(false);
  }

  /**
   * Undoes all the changes made in {@link #addStatisticsOverlay()}.
   */
  private void removeStatisticsOverlay() {
    fView.removeLayer(fLayer);
    ((ILspAWTView) fView).getHostComponent().removeKeyListener(fMKCListener);
    ((ILspAWTView) fView).getHostComponent().removeKeyListener(fGLRCListener);
    ALcdAnimationManager.getInstance().removeAnimation(this);
    fView.setAutoUpdate(true);
  }
}
