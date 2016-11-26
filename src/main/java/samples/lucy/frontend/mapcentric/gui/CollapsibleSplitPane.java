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
package samples.lucy.frontend.mapcentric.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.WeakHashMap;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.luciad.view.animation.ALcdAnimation;
import com.luciad.view.animation.ALcdAnimationManager;

/**
 * Split pane that contains a regular component at the top, and the content area of a
 * HideableTabbedPane in the bottom. When the HideableTabbedPane has no tabs selected,
 * the split pane fully collapses.
 *
 * When the divider is moved, it does so animatedly.
 */
public class CollapsibleSplitPane extends JPanel {
  private final JComponent fBottomComponent;
  private final Divider fDivider;
  private final Component fTopComponent;
  private final HideableTabbedPane fBottomPane;

  /**
   * Percentage of space usable for the bottom component. Both the current value is stored, and
   * also per tab. The per-tab value allows to restore the slider when switching tabs.
   */
  private double fDividerProportion = Double.NaN;
  private WeakHashMap<HideableTabbedPane.Tab, Double> fPerTabDividerProportions = new WeakHashMap<>();

  public CollapsibleSplitPane(Component aTopComponent, final HideableTabbedPane aBottomComponent) {
    setLayout(new SplitLayout());

    fTopComponent = aTopComponent;
    fDivider = new Divider();
    fBottomPane = aBottomComponent;
    fBottomComponent = fBottomPane.getContentPanel();

    add(aTopComponent);
    add(fDivider);
    add(fBottomComponent);

    aBottomComponent.setSupportAnimatedSplit(true);

    updateSplitLayout();
    aBottomComponent.addPropertyChangeListener("selectedTab", new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        updateSplitLayout();
      }
    });
  }

  private void updateSplitLayout() {
    HideableTabbedPane.Tab selected = fBottomPane.getSelectedTab();
    fDivider.setEnabled(selected != null);
    double dividerProportion = getDividerProportion(selected);
    if (!Double.isNaN(dividerProportion)) {
      changeDivider(dividerProportion);
    }
  }

  public JComponent getDivider() {
    return fDivider;
  }

  public double getDividerProportion() {
    return fDividerProportion;
  }

  public void setDividerProportion(double aDividerProportion) {
    setDividerProportionDoNotStore(aDividerProportion);
    storeDividerProportion(aDividerProportion);
  }

  private void setDividerProportionDoNotStore(double aDividerProportion) {
    fDividerProportion = aDividerProportion;
    revalidate();
  }

  private void storeDividerProportion(double aDividerProportion) {
    HideableTabbedPane.Tab selectedTab = fBottomPane.getSelectedTab();
    if (selectedTab != null) {
      fPerTabDividerProportions.put(selectedTab, aDividerProportion);
    }
  }

  private double getDividerProportion(HideableTabbedPane.Tab aTab) {
    double proportion;
    if (aTab != null) {
      Double stored = fPerTabDividerProportions.get(aTab);
      proportion = stored == null ? getDefaultDividerProportion(aTab) : stored;
    } else {
      proportion = 0; // when collapsed, bottom part is invisible
    }
    return proportion;
  }

  private double getDefaultDividerProportion(HideableTabbedPane.Tab aTab) {
    if (getHeight() <= 0) {
      return Double.NaN;
    }

    double defaultValue = (double) aTab.getPreferredSize().height / getHeight();
    defaultValue = Math.max(0.20, Math.min(defaultValue, 0.80)); //clamp
    return defaultValue;
  }

  private void changeDivider(double aTo) {
    // Avoid animating when not on screen
    if (isShowing()) {
      storeDividerProportion(aTo);
      ALcdAnimationManager.getInstance().putAnimation(getAnimationKey(), new MoveDividerAnimation(this, aTo));
    } else {
      setDividerProportion(aTo);
    }
  }

  Object getAnimationKey() {
    return this;
  }

  /**
   * Animation that moves the divider from its current location to a target location.
   */
  private static class MoveDividerAnimation extends ALcdAnimation {
    private final CollapsibleSplitPane fSplit;
    private final double fFrom;
    private final double fTo;

    private MoveDividerAnimation(CollapsibleSplitPane aSplit, double aTo) {
      super(0.3);
      fSplit = aSplit;
      fFrom = aSplit.getDividerProportion();
      fTo = aTo;
      setInterpolator(Interpolator.SMOOTH_STEP);
    }

    @Override
    protected void setTimeImpl(double aTime) {
      double fraction = aTime / getDuration();
      fSplit.setDividerProportionDoNotStore(interpolate(fFrom, fTo, fraction));
    }
  }

  /**
   * The draggable area that separates the top and bottom components.
   */
  private class Divider extends JPanel {
    private Divider() {
      super(new BorderLayout());
      int size = MapCentricUtil.getDividerSize();
      setBorder(BorderFactory.createMatteBorder(size, 0, 0, 0, MapCentricUtil.getDividerColor()));
      add(Box.createVerticalStrut(Math.max(0, 4-size))); //at least 4 pixels to grab the divider to assure it's usable
      setOpaque(false);
      setCursor(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));
      addMouseMotionListener(new MouseMotionAdapter() {
        @Override
        public void mouseDragged(MouseEvent e) {
          if (isEnabled()) {
            Point pointInSplit = SwingUtilities.convertPoint((Component) e.getSource(), e.getX(), e.getY(), CollapsibleSplitPane.this);
            double h = CollapsibleSplitPane.this.getHeight();
            int halfDiv = getPreferredSize().height / 2;
            double dividerProportion = (h - pointInSplit.y - halfDiv) / h;
            setDividerProportion(dividerProportion);
          }
        }
      });
    }
  }

  /**
   * Layout manager that lays out these components, one below the other:
   * - top component
   * - divider
   * - bottom component
   *
   * The preferred width is the max of the preferred widths, the preferred height is the sum of the
   * preferred heights of those components.
   */
  private class SplitLayout implements LayoutManager {
    private static final int GAP = 20; //px, at least keep the slider this far from the top/bottom

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
      Dimension topPref = fTopComponent.getPreferredSize();
      Dimension divPref = fDivider.getPreferredSize();
      Dimension bottomPref = fBottomComponent.getPreferredSize();

      Insets i = parent.getInsets();
      int w = i.left + i.right;
      int h = i.top + i.bottom;

      w += Math.max(Math.max(topPref.width, divPref.width), bottomPref.width);
      h += topPref.height + divPref.height + bottomPref.height;

      return new Dimension(w, h);
    }

    @Override
    public void layoutContainer(Container parent) {
      // This happens if we're being showed for the first time
      if (Double.isNaN(fDividerProportion)) {
        updateSplitLayout();
      }

      Insets i = parent.getInsets();
      int width = parent.getWidth() - i.left - i.right;
      int height = parent.getHeight() - i.top - i.bottom;

      int y = height;

      int bottomHeight = clampBottomHeight((int) Math.round(fDividerProportion * height));
      y -= bottomHeight;
      fBottomComponent.setBounds(i.left, y, width, bottomHeight);

      int dividerHeight = fDivider.isEnabled() ? fDivider.getPreferredSize().height : 0;
      y -= dividerHeight;
      fDivider.setBounds(i.left, y, width, dividerHeight);

      fTopComponent.setBounds(i.left, i.top, width, y - i.top);
    }

    private int clampBottomHeight(int aBottomHeight) {
      int min = fDivider.isEnabled() ? GAP : 0;
      int max = getHeight() - GAP;
      return Math.max(min, Math.min(aBottomHeight, max));
    }
  }
}
