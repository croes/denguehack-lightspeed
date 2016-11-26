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
package samples.common.gui;

import java.awt.Dimension;
import java.awt.LayoutManager;

import javax.swing.JPanel;

import com.luciad.view.animation.ALcdAnimation;
import com.luciad.view.animation.ALcdAnimationManager;

/**
 * Panel that can resize animatedly whenever its content changes: children are added or removed,
 * children are being made visible or invisible or their content changes.
 *
 * To do so, it animatedly changes its preferred size when its children have (abruptly) changed
 * theirs. It also offers a method {@link #getPreferredSizeAfterAnimation()} to query the eventual
 * target size. Layout managers can use it to anticipate on the final result.
 *
 * If a component is added for example, the getPreferredSizeAfterAnimation() immediately grows to
 * include the component. The regular preferred size however is updated in a number of steps, and
 * finally matches getPreferredSizeAfterAnimation().
 */
public class AnimatedResizingPanel extends JPanel {
  public static final double ANIMATION_DELAY = 0.1; //s
  private static final double ANIMATION_DURATION = 0.3; //s

  private Dimension fLastSuperPrefSize = new Dimension();
  private Dimension fAnimatedSize = new Dimension();

  private Runnable fAfterNextAnimation;

  public AnimatedResizingPanel() {
    setOpaque(false);
  }

  public AnimatedResizingPanel(LayoutManager aLayout) {
    this();
    setLayout(aLayout);
  }

  /**
   * Sets the runnable to execute after the next animation. It is for example used to animatedly
   * remove a panel: it first animates to reach zero size, and then the queued runnable actually
   * removes the panel from the Swing component tree.
   *
   * @param aRunnable The runnable.
   */
  public void setAfterNextAnimationRunnable(Runnable aRunnable) {
    fAfterNextAnimation = aRunnable;
  }

  @Override
  public Dimension getPreferredSize() {
    updateAnimatedSize();
    return new Dimension(fAnimatedSize);
  }

  public Dimension getPreferredSizeAfterAnimation() {
    return updateAnimatedSize();
  }

  /**
   * Update animated size, and queue animations if needed.
   * @return The eventual, target size after any possible animations have finished.
   */
  private Dimension updateAnimatedSize() {
    Dimension pref = super.getPreferredSize();

    // If the size hasn't changed compared to the previous invocation, don't do anything.
    if (!pref.equals(fLastSuperPrefSize)) {
      fLastSuperPrefSize = pref;

      // Avoid any animations when not on screen
      if (!isShowing()) {
        fAnimatedSize = pref;
      } else {
        // This removeAnimation can call stop on the ApplyNewSize animation which can in turn
        // queue an animation, but it will be overruled by the putAnimation call
        ALcdAnimationManager.getInstance().removeAnimation(getKey());
        ALcdAnimationManager.getInstance().putAnimation(getKey(), new ApplyNewSize(fAnimatedSize, pref));
      }
    }
    return pref;
  }

  /**
   * Both animations use the same key, so that starting one also cancels the others.
   */
  private Object getKey() {
    return this;
  }

  /**
   * Timer that starts the animated resize. It is not really an animation, it only does something
   * when it ends.
   */
  private class ApplyNewSize extends ALcdAnimation {
    private final Dimension fFrom;
    private final Dimension fTo;

    private ApplyNewSize(Dimension aFrom, Dimension aTo) {
      super(ANIMATION_DELAY);
      fFrom = aFrom;
      fTo = aTo;
    }

    @Override
    protected void setTimeImpl(double aTime) {
    }

    @Override
    public void stop() {
      ALcdAnimationManager.getInstance().putAnimation(getKey(), new ResizeAnimation(fFrom, fTo));
    }
  }

  /**
   * Animates between two given sizes.
   */
  private class ResizeAnimation extends ALcdAnimation {
    private final Dimension fFrom;
    private final Dimension fTo;

    private ResizeAnimation(Dimension aFrom, Dimension aTo) {
      super(ANIMATION_DURATION);
      setInterpolator(Interpolator.SMOOTH_STEP);
      fFrom = aFrom;
      fTo = aTo;
    }

    @Override
    protected void setTimeImpl(double aTime) {
      double fraction = aTime / getDuration();
      fAnimatedSize = new Dimension((int) interpolate(fFrom.width, fTo.width, fraction),
                                    (int) interpolate(fFrom.height, fTo.height, fraction));
      revalidate();
    }

    @Override
    public void stop() {
      if (fAfterNextAnimation != null) {
        fAfterNextAnimation.run();
        fAfterNextAnimation = null;
      }
    }
  }
}
