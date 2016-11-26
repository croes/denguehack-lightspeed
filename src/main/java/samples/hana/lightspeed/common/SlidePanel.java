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
package samples.hana.lightspeed.common;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;

import javax.swing.JPanel;

import com.luciad.view.animation.ALcdAnimation;
import com.luciad.view.animation.ALcdAnimationManager;

/**
 * Container for a number of child panels. One of those children is the selected panel, which is shown, the others
 * aren't shown. When the selected index changes, an animation slides from one to the next component.
 */
class SlidePanel extends JPanel {
  /**
   * Index of selected component.
   */
  private int fSelectedIndex = 0;

  /**
   * Offset used during animation. When not animating, offset is zero. All panels are layed out side-by-side.
   * The offset is used during animations, to show two adjacent panels (partly).
   */
  private int fAnimationXOffset = 0;

  public SlidePanel() {
    setLayout(new SlideLayout());
  }

  public int getSelectedIndex() {
    return fSelectedIndex;
  }

  public void setSelectedIndex(int aSelectedIndex) {
    int oldIndex = fSelectedIndex;

    fSelectedIndex = aSelectedIndex;

    int delta = aSelectedIndex - oldIndex;
    if (delta != 0 && isShowing()) {
      // Animate when there is really a change in index and we are showing on screen
      int from = fAnimationXOffset + (delta * getPreferredSize().width);
      ALcdAnimationManager.getInstance().putAnimation(this, new SlideAnimation(from, 0));
    } else {
      //no animations when not on screen
      setAnimationXOffset(0);
    }

    revalidate();

    firePropertyChange("selectedIndex", oldIndex, fSelectedIndex);
  }

  private void setAnimationXOffset(int aAnimationXOffset) {
    fAnimationXOffset = aAnimationXOffset;
    revalidate();
  }

  /**
   * Simple smooth-step animation of fAnimationXOffset from and to the given values.
   */
  private class SlideAnimation extends ALcdAnimation {
    private final int fFrom;
    private final int fTo;

    public SlideAnimation(int aFrom, int aTo) {
      super(0.4); //magically aligns with times of AnimatedResizingPanel
      fFrom = aFrom;
      fTo = aTo;
      setInterpolator(Interpolator.SMOOTH_STEP);
      setAnimationXOffset(aFrom);
    }

    @Override
    protected void setTimeImpl(final double v) {
      setAnimationXOffset((int) Math.round(ALcdAnimation.interpolate(fFrom, fTo, v / getDuration())));
    }
  }

  /**
   * Lays out a number of components horizontally next to each other.
   * Only one of those is visible at a time, so think of the visible area as a window on the active component
   * (more less like a scroll pane). When changing to another active component, an animation is used.
   * The preferred size is defined by:
   * - the largest of the preferred widths of the components, so that the widest panel fits in without changes in
   * width for the container.
   * - the preferred height of the current component.
   */
  private class SlideLayout implements LayoutManager {

    @Override
    public void addLayoutComponent(String name, Component comp) {
    }

    @Override
    public void removeLayoutComponent(Component comp) {
    }

    @Override
    public Dimension minimumLayoutSize(Container parent) {
      return preferredLayoutSize(parent);
    }

    @Override
    public Dimension preferredLayoutSize(Container parent) {
      int width = 0;
      int height = 0;
      Component active = parent.getComponentCount() > fSelectedIndex ?
                         parent.getComponent(fSelectedIndex) : null;

      for (Component child : parent.getComponents()) {
        Dimension pref = child.getPreferredSize();
        width = Math.max(pref.width, width);
        if (child == active) {
          height = pref.height;
        }
      }

      Insets insets = parent.getInsets();
      width += insets.left + insets.right;
      height += insets.top + insets.bottom;
      return new Dimension(width, height);
    }

    @Override
    public void layoutContainer(Container parent) {
      Dimension pref = preferredLayoutSize(parent);

      Insets insets = parent.getInsets();
      int y = insets.top;
      int x = fAnimationXOffset + insets.left;
      for (int i = 0; i < fSelectedIndex; i++) {
        x -= pref.width;
      }

      for (Component child : parent.getComponents()) {
        child.setBounds(x, y, pref.width, child.getPreferredSize().height);
        x += pref.width;
      }
    }
  }
}
