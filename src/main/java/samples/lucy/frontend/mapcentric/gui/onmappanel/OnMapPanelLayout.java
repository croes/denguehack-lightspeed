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
package samples.lucy.frontend.mapcentric.gui.onmappanel;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager2;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Layout manager that stacks OnMapPanel's vertically.
 *
 * When there is not enough room to fit all panels, panels will be collapsed. Least important ones
 * are collapsed first. The last panel is never collapsed.
 *
 * It uses animations to collapse/expand the panels.
 */
class OnMapPanelLayout implements LayoutManager2 {
  static final int GAP = 2; //px

  ImportantPanelTracker fTracker = new ImportantPanelTracker();

  private final boolean fAutoCollapseOnMapPanels;

  OnMapPanelLayout(boolean aAutoCollapseOnMapPanels) {
    fAutoCollapseOnMapPanels = aAutoCollapseOnMapPanels;
  }

  @Override
  public void addLayoutComponent(Component comp, Object constraints) {
    addLayoutComponent(comp);
  }

  @Override
  public void addLayoutComponent(String name, Component comp) {
    addLayoutComponent(comp);
  }

  @Override
  public Dimension maximumLayoutSize(Container target) {
    return preferredLayoutSize(target);
  }

  @Override
  public Dimension minimumLayoutSize(Container parent) {
    return preferredLayoutSize(parent);
  }

  @Override
  public float getLayoutAlignmentX(Container target) {
    return 0;
  }

  @Override
  public float getLayoutAlignmentY(Container target) {
    return 1;
  }

  @Override
  public void invalidateLayout(Container target) {
  }

  private void addLayoutComponent(Component comp) {
    if (!(comp instanceof OnMapPanel)) {
      throw new IllegalArgumentException("This layout manager can only accept OnMapPanel's, not " + comp);
    }

    fTracker.addLayoutComponent(comp);
  }

  @Override
  public void removeLayoutComponent(Component comp) {
    fTracker.removeLayoutComponent(comp);
  }

  private int getPreferredWidth(Container parent, boolean aAnimated) {
    int width = 0;
    for (OnMapPanel p : getVisibleChildren(parent)) {
      Dimension d = aAnimated ? p.getPreferredSize() : p.getPreferredSizeAfterAnimation();
      if (d.width > width) {
        width = d.width;
      }
    }
    Insets insets = parent.getInsets();
    return width + insets.left + insets.right;
  }

  private int getPreferredHeight(Container parent, boolean aAnimated) {
    int height = 0;

    for (OnMapPanel p : getVisibleChildren(parent)) {
      Dimension d = aAnimated ? p.getPreferredSize() : p.getPreferredSizeAfterAnimation();
      if (height > 0) {
        height += GAP;
      }
      height += d.height;
    }
    Insets insets = parent.getInsets();
    return height + insets.top + insets.bottom;
  }

  @Override
  public Dimension preferredLayoutSize(Container parent) {
    // Use the max of both animated and regular pref height, as collapsePanesIfNeeded relies on it
    // by calling parent.getHeight(). That height is assigned by the grandparent's layout mgr, so
    // it should have space for the regular pref height.
    int maxHeight = Math.max(getPreferredHeight(parent, true), getPreferredHeight(parent, false));

    return new Dimension(getPreferredWidth(parent, true), maxHeight);
  }

  @Override
  public void layoutContainer(Container parent) {
    fTracker.parent(parent);

    // If there is not enough space, collapse some panels
    if (fAutoCollapseOnMapPanels) {
      collapsePanesIfNeeded(parent);
    }

    // Dock the panels to the right-hand side if there is too much space (during animations)
    Insets insets = parent.getInsets();
    int width = parent.getWidth() - insets.left - insets.right;
    int animatedWidth = getPreferredWidth(parent, true);
    int leftMargin = Math.max(0, width - animatedWidth);

    Map<OnMapPanel, Integer> missingHeights = spreadOutMissingHeight(parent);

    int y = insets.top;
    for (OnMapPanel p : getVisibleChildren(parent)) {
      int height = p.getPreferredSize().height;

      Integer missing = missingHeights.get(p);
      if (missing == null) {
        missing = 0;
      }
      height -= missing;

      p.setBounds(insets.left + leftMargin, y, Math.min(animatedWidth, width), height);
      y += height;
      y += GAP;
    }
  }

  private void collapsePanesIfNeeded(Container parent) {
    List<OnMapPanel> ordered = fTracker.getPanelsInOrderOfImportance(parent);

    int i = 0;
    while (i < ordered.size() &&
           getPreferredHeight(parent, false) > parent.getHeight() &&
           getExpandedPaneCount(parent) > 1) {

      OnMapPanel p = ordered.get(i);
      if (!p.isCollapsed()) {
        p.setCollapsed(true);
      }
      i++;
    }
  }

  /**
   * If less than requested height is available, spread it over the panels.
   * Start with the bottom panels, and work upwards. A panel is never made smaller than its
   * collapsed size. If all panels have their collapsed size, and it still doesn't fit, panels
   * will overflow at the bottom.
   * @param parent The parent.
   * @return The heights per panel that are missing.
   */
  private Map<OnMapPanel, Integer> spreadOutMissingHeight(Container parent) {
    int neededHeight = getPreferredHeight(parent, true);
    int actualHeight = parent.getHeight();
    int missingHeight = Math.max(0, neededHeight - actualHeight);

    Map<OnMapPanel, Integer> missingHeights = new HashMap<OnMapPanel, Integer>();

    List<OnMapPanel> children = getVisibleChildren(parent);
    Collections.reverse(children);
    Iterator<OnMapPanel> childrenBackwards = children.iterator();

    while (childrenBackwards.hasNext() && missingHeight > 0) {
      OnMapPanel p = childrenBackwards.next();
      int height = p.getPreferredSize().height;
      int potential = height - p.getPrefCollapsedSize().height;
      int substract = Math.min(potential, missingHeight);
      missingHeights.put(p, substract);
      missingHeight -= substract;
    }
    return missingHeights;
  }

  static List<OnMapPanel> getVisibleChildren(Container parent) {
    ArrayList<OnMapPanel> children = new ArrayList<OnMapPanel>(parent.getComponentCount());
    for (Component component : parent.getComponents()) {
      if (component.isVisible()) {
        children.add((OnMapPanel) component);
      }
    }
    return children;
  }

  private int getExpandedPaneCount(Container parent) {
    int expandedCount = 0;
    for (OnMapPanel p : getVisibleChildren(parent)) {
      if (!p.isCollapsed()) {
        expandedCount++;
      }
    }
    return expandedCount;
  }
}
