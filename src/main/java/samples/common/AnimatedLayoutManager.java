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
package samples.common;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.LayoutManager2;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.luciad.view.animation.ALcdAnimation;
import com.luciad.view.animation.ALcdAnimationManager;

/**
 * LayoutManager wrapper that animates changes in the position and size of its children. When components are
 * added, a roll animation is used to make them appear. When components are removed they disappear immediately,
 * but the space they used to occupy disappears animatedly. As always, when adding or removing child components to an
 * already visible Swing hierarchy don't forget to call {@code parent.revalidate()}.
 *
 * Limitations:
 * - It does not support min/max sizes. Instead, it always uses the preferred size of the delegate layout manager, and
 * thus the preferred size of the child components. Depending on the inner layout manager, this can slightly change
 * its behavior.
 * - When new children are added, or when invisible children become visible, an attempt is made to make them appear
 * nicely. This for example works well for BorderLayout, but not always for BoxLayout. If the behavior of newly added
 * components is not as expected, it is best to not add/remove them (or show/hide) but instead set their preferred size
 * to have its width or height zero (depending on how you want it to appear animatedly).
 */
public class AnimatedLayoutManager implements LayoutManager {
  private static final double ANIMATION_DURATION = 0.3; //s

  final LayoutManager fDelegate;

  private final Map<Component, Rectangle> fLastDelegateBounds = new HashMap<>();
  private final Map<Component, Rectangle> fAnimatedBounds = new HashMap<>();

  private Dimension fLastPreferredSize = new Dimension();
  private boolean fAnimationDesired = false;

  public static AnimatedLayoutManager create(LayoutManager aDelegate) {
    if (aDelegate instanceof LayoutManager2) {
      LayoutManager2 layoutManager = (LayoutManager2) aDelegate;
      return new MyLayoutManager2(layoutManager);
    } else {
      return new AnimatedLayoutManager(aDelegate);
    }
  }

  private AnimatedLayoutManager(LayoutManager aDelegate) {
    fDelegate = aDelegate;
  }

  @Override
  public void addLayoutComponent(String name, Component comp) {
    fDelegate.addLayoutComponent(name, comp);
  }

  @Override
  public void removeLayoutComponent(Component comp) {
    fDelegate.removeLayoutComponent(comp);
    clearBookkeeping(comp);
  }

  private void clearBookkeeping(Component comp) {
    fAnimatedBounds.remove(comp);
    fLastDelegateBounds.remove(comp);
  }

  @Override
  public Dimension preferredLayoutSize(Container parent) {
    Dimension pref = fDelegate.preferredLayoutSize(parent);

    if ( !fLastPreferredSize.equals(pref) ) {
      // The delegate layout changed its preferred size, so start an animation to re-layout the children.
      // Size changes that are imposed upon us from outside are not animated. Examples for such size changes
      // are maximizing a frame or moving the divider of a split pane.
      fAnimationDesired = true;
      fLastPreferredSize = pref;
    }

    return pref;
  }

  @Override
  public Dimension minimumLayoutSize(Container parent) {
    return preferredLayoutSize(parent);
  }

  @Override
  public void layoutContainer(Container parent) {
    // This is not guaranteed to be called by Swing before calling this method, but we need it to set the
    // fAnimationDesired flag
    preferredLayoutSize(parent);

    // This updates the bounds of all children to their desired location/size, as defined by the delegate.
    // The bounds are read back by updateAnimatedBounds, and then overruled by the below for loop.
    fDelegate.layoutContainer(parent);

    updateAnimatedBounds(parent);

    for (Component c : parent.getComponents()) {
      if ( c.isVisible() ) {
        c.setBounds(fAnimatedBounds.get(c));
      }
    }
  }

  /**
   * Update animated bounds of all children, and queue animations if needed.
   */
  private void updateAnimatedBounds(Container parent) {
    boolean someAnimationStarted = false;
    List<Component> childrenNeedingAppearAnimation = new ArrayList<>();

    for (Component c : parent.getComponents()) {
      Rectangle target = c.getBounds();

      if ( !c.isVisible() ) {
        // Behave identical to a removed component so that it appears nicely when it is made visible again.
        clearBookkeeping(c);
        continue;
      }

      // Is there a change in the bounds of this component?
      if (!target.equals(fLastDelegateBounds.get(c))) {
        fLastDelegateBounds.put(c, target);

        // Only animate when on screen and desired.
        if (parent.isShowing() && fAnimationDesired) {
          // Start animation from the current size
          Rectangle current = fAnimatedBounds.get(c);
          if (current != null) {
            ALcdAnimationManager.getInstance().putAnimation(c, new ResizeAnimation(c, current, target));
            someAnimationStarted = true;
          } else {
            // Component was just added
            childrenNeedingAppearAnimation.add(c);
          }
        } else {
          ALcdAnimationManager.getInstance().removeAnimation(c);
          fAnimatedBounds.put(c, target);
        }
      }
    }

    for (Component c : childrenNeedingAppearAnimation) {
      Rectangle target = c.getBounds();
      Rectangle current = findBestInitialBounds(target, parent);
      fAnimatedBounds.put(c, current);
      ALcdAnimationManager.getInstance().putAnimation(c, new ResizeAnimation(c, current, target));
      someAnimationStarted = true;
    }

    // Whenever ResizeAnimation ends it has called revalidate() meaning lay out is triggered, so we get called again.
    // If we can get through without starting any animation, it means we've settled on a stable value, and no more
    // animations are needed.
    if (!someAnimationStarted) {
      fAnimationDesired = false;
    }
  }

  /**
   * Finds the best initial bounds to start animating from, using a heuristic of minimal overlap with the existing
   * components.
   * @param aChildBounds The target bounds of the component to place.
   * @param aParent The parent in which to place the given bounds.
   * @return The best initial bounds for the new child.
   */
  private Rectangle findBestInitialBounds(Rectangle aChildBounds, Container aParent) {
    Rectangle bestBounds = null;
    int maxSurfaceOverlap = Integer.MAX_VALUE;
    for (Side side : Side.values()) {
      Rectangle bounds = createLineRectangle(aChildBounds, side);
      int overlap = surfaceOverlap(bounds, aParent);
      if ( overlap < maxSurfaceOverlap) {
        maxSurfaceOverlap = overlap;
        bestBounds = bounds;
      }
    }
    return bestBounds;
  }

  /**
   * Calculates the overlap in square pixels between the given bounds and the visible components of the given parent.
   * It does not use the current bounds of the parent, but what it would be when half-way through the animation.
   * @param aBounds the bounds to calculate overlap for.
   * @param aParent  The parent.
   * @return The surface in square pixels.
   */
  private int surfaceOverlap(Rectangle aBounds, Container aParent) {
    int surface = 0;
    for (Component c : aParent.getComponents()) {
      if ( c.isVisible() ) {
        Rectangle target = c.getBounds();
        Rectangle current = fAnimatedBounds.get(c);
        if ( current != null ) {
          Rectangle halfWay = interpolate(current, target, 0.5);
          surface += surfaceOverlap(halfWay, aBounds);
        }
      }
    }
    return surface;
  }

  private int surfaceOverlap(Rectangle aOne, Rectangle aOther) {
    Rectangle intersection = aOne.intersection(aOther);
    return intersection.width * intersection.height;
  }

  /**
   * Creates a rectangle that is actually a line, a rectangle that has either width or height zero. It is positioned at
   * the given side.
   * @param aSurface The surface to work in.
   * @param aSide One of SwingConstants.LEFT, RIGHT, TOP or BOTTOM.
   * @return A line inside the given surface at the given side.
   */
  private Rectangle createLineRectangle(Rectangle aSurface, Side aSide) {
    switch ( aSide ) {
      case LEFT: return new Rectangle(aSurface.x, aSurface.y, 0, aSurface.height);
      case RIGHT: return new Rectangle(aSurface.x + aSurface.width -1, aSurface.y, 0, aSurface.height);
      case TOP: return new Rectangle(aSurface.x, aSurface.y, aSurface.width, 0);
      case BOTTOM: return new Rectangle(aSurface.x, aSurface.y + aSurface.height - 1, aSurface.width, 0);
      default: throw new RuntimeException("Invalid side " + aSide);
    }
  }

  private static enum Side {TOP, LEFT, RIGHT, BOTTOM} // in the desired order

  private static Rectangle interpolate(Rectangle aFrom, Rectangle aTo, double aFraction) {
    return new Rectangle(
        interpolate(aFrom.x, aTo.x, aFraction),
        interpolate(aFrom.y, aTo.y, aFraction),
        interpolate(aFrom.width, aTo.width, aFraction),
        interpolate(aFrom.height, aTo.height, aFraction));
  }

  private static int interpolate(double aStart, double aTarget, double aFraction) {
    return (int) Math.round((1 - aFraction) * aStart + aFraction * aTarget);
  }

  /**
   * Extends the public AnimatedLayoutManager class to also implement the additional methods from LayoutManager2.
   * It is important to return an instance that implements LayoutManager2 or not depending on what the delegate
   * implements, so that instanceof checks in calling code still work.
   */
  private static class MyLayoutManager2 extends AnimatedLayoutManager implements LayoutManager2 {
    public MyLayoutManager2(LayoutManager2 aLayoutManager) {
      super(aLayoutManager);
    }

    private LayoutManager2 getDelegate() {
      return (LayoutManager2) fDelegate;
    }

    @Override
    public void addLayoutComponent(Component comp, Object constraints) {
      getDelegate().addLayoutComponent(comp, constraints);
    }

    @Override
    public Dimension maximumLayoutSize(Container target) {
      return preferredLayoutSize(target);
    }

    @Override
    public float getLayoutAlignmentX(Container target) {
      return getDelegate().getLayoutAlignmentX(target);
    }

    @Override
    public void invalidateLayout(Container target) {
      getDelegate().invalidateLayout(target);
    }

    @Override
    public float getLayoutAlignmentY(Container target) {
      return getDelegate().getLayoutAlignmentY(target);
    }
  }

  /**
   * Animates between two given rectangles.
   */
  private class ResizeAnimation extends ALcdAnimation {
    private final Component fComponent;
    private final Rectangle fFrom;
    private final Rectangle fTo;

    private ResizeAnimation(Component aComponent, Rectangle aFrom, Rectangle aTo) {
      super(ANIMATION_DURATION);
      setInterpolator(Interpolator.SMOOTH_STEP);
      fComponent = aComponent;
      fFrom = aFrom;
      fTo = aTo;
    }

    @Override
    protected void setTimeImpl(double aTime) {
      double fraction = aTime / getDuration();
      fAnimatedBounds.put(fComponent, AnimatedLayoutManager.interpolate(fFrom, fTo, fraction));
      fComponent.revalidate();
    }
  }
}
