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
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JPanel;

import com.luciad.gui.ILcdIcon;
import com.luciad.lucy.gui.ALcyActiveSettable;
import com.luciad.lucy.gui.ILcyActionBar;
import com.luciad.lucy.gui.TLcyGroupDescriptor;
import com.luciad.lucy.gui.TLcyToolBar;

/**
 * Class that behaves somewhat similar to a tabbed pane: it contains a number of panes, of which
 * only one is visible at a time. Each pane has an associated tab, clicking the tab shows the pane.
 *
 * These are the differences:
 * - Clicking on a tab that is already visible hides the entire content area, freeing up space
 *   for other parts of the UI.
 * - It updates its own preferred size based on the currently active tab. In contrary, a regular
 *   tabbed pane accommodates for the largest content panel.
 * - The area with the tabs and the area with the content are separate. This allows to integrate
 *   the tabs in an already existing action bar, such as a status bar.
 */
public class HideableTabbedPane {
  private final PropertyChangeSupport fPropertyChangeSupport = new PropertyChangeSupport(this);
  private final List<Tab> fTabs = new ArrayList<Tab>();
  private final List<ShowTabContentActiveSettable> fShowTabActiveSettables = new ArrayList<ShowTabContentActiveSettable>();

  private ILcyActionBar fTabsPanel;
  private boolean fSupportAnimatedSplit = false;

  private final JComponent fContentPanel;

  private final PropertyChangeListener fTabListener = new PropertyChangeListener() {
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
      if (evt.getPropertyName().equals("tabName") ||
          evt.getPropertyName().equals("tabIcon")) {
        updateUI();
        updateUITabSelection();
      }
    }
  };

  private Tab fSelectedTab = null;

  public HideableTabbedPane() {
    this(new TLcyToolBar());
  }

  public HideableTabbedPane(ILcyActionBar aTabsBarSFCT) {
    fTabsPanel = aTabsBarSFCT;

    fContentPanel = new JPanel();
    fContentPanel.setLayout(new MyOverlayLayout());
  }

  public void addPropertyChangeListener(PropertyChangeListener aListener) {
    fPropertyChangeSupport.addPropertyChangeListener(aListener);
  }

  public void addPropertyChangeListener(String aPropertyName, PropertyChangeListener aListener) {
    fPropertyChangeSupport.addPropertyChangeListener(aPropertyName, aListener);
  }

  public void removePropertyChangeListener(PropertyChangeListener aListener) {
    fPropertyChangeSupport.removePropertyChangeListener(aListener);
  }

  public void removePropertyChangeListener(String aPropertyName, PropertyChangeListener aListener) {
    fPropertyChangeSupport.removePropertyChangeListener(aPropertyName, aListener);
  }

  public void addTab(Tab aTab) {
    fTabs.add(aTab);
    aTab.addPropertyChangeListener(fTabListener);
    updateUI();
    updateUITabSelection();
  }

  public void removeTab(Tab aTab) {
    fTabs.remove(aTab);
    aTab.removePropertyChangeListener(fTabListener);
    if (fSelectedTab == aTab) {
      setSelectedTab(null);
    }
    updateUI();
    updateUITabSelection();
  }

  public List<Tab> getTabs() {
    return Collections.unmodifiableList(fTabs);
  }

  public JComponent getContentPanel() {
    return fContentPanel;
  }

  public ILcyActionBar getActionBar() {
    return fTabsPanel;
  }

  public void setActionBar(ILcyActionBar aActionBar) {
    ILcyActionBar oldBar = fTabsPanel;
    fTabsPanel = aActionBar;
    updateUI(oldBar);
    updateUITabSelection();
  }

  public Tab getSelectedTab() {
    return fSelectedTab;
  }

  public void setSelectedTab(Tab aSelectedTab) {
    Tab oldValue = fSelectedTab;
    fSelectedTab = aSelectedTab;
    updateUITabSelection(oldValue);
    fPropertyChangeSupport.firePropertyChange("selectedTab", oldValue, aSelectedTab);
  }

  public boolean isSupportAnimatedSplit() {
    return fSupportAnimatedSplit;
  }

  public void setSupportAnimatedSplit(boolean aSupportAnimatedSplit) {
    fSupportAnimatedSplit = aSupportAnimatedSplit;
  }

  private void updateUI() {
    updateUI(fTabsPanel);
  }

  private void updateUI(ILcyActionBar aOldActionBar) {
    // Remove all active settables and content panes
    for (ShowTabContentActiveSettable showTabActiveSettable : fShowTabActiveSettables) {
      aOldActionBar.removeActiveSettable(showTabActiveSettable);
      showTabActiveSettable.cleanup();
    }
    fShowTabActiveSettables.clear();

    fContentPanel.removeAll();

    // Re-add based on new situation
    for (Tab tab : fTabs) {
      ShowTabContentActiveSettable activeSettable = new ShowTabContentActiveSettable(this, tab);
      fShowTabActiveSettables.add(activeSettable);
      fTabsPanel.insertActiveSettable(activeSettable, tab.getGroupDescriptor());
      fContentPanel.add(tab);
    }
    fContentPanel.revalidate();
  }

  private void updateUITabSelection() {
    updateUITabSelection(null);
  }

  private void updateUITabSelection(Tab previouslySelectedTab) {
    for (Tab tab : fTabs) {
      tab.setVisible(fSelectedTab == tab);
    }

    // If the last tab is de-selected, keep it visible anyway. This is to be compatible with
    // animations of CollapsibleSplitPane: you want to still see the content during
    // the animation that is hiding it.
    if (isSupportAnimatedSplit() && fSelectedTab == null && previouslySelectedTab != null) {
      previouslySelectedTab.setVisible(true);
    }

    // Hide the entire content if no tabs remain visible. This also hides possible borders on the
    // content pane.
    fContentPanel.setVisible(isAtLeastOneTabVisible());
  }

  private boolean isAtLeastOneTabVisible() {
    for (Tab tab : fTabs) {
      if (tab.isVisible()) {
        return true;
      }
    }
    return false;
  }

  private static class ShowTabContentActiveSettable extends ALcyActiveSettable implements PropertyChangeListener {
    private final HideableTabbedPane fTabbedPane;
    private final Tab fTab;

    public ShowTabContentActiveSettable(HideableTabbedPane aPane, Tab aTab) {
      fTabbedPane = aPane;
      fTab = aTab;
      putValue(NAME, aTab.getTabName());
      putValue(SMALL_ICON, aTab.getTabIcon());
      putValue(SHORT_DESCRIPTION, aTab.getShortDescription());
      putValue(SHOW_ACTION_NAME, false);

      fTabbedPane.addPropertyChangeListener("selectedTab", this);
    }

    public void cleanup() {
      fTabbedPane.removePropertyChangeListener("selectedTab", this);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
      this.firePropertyChange("active", isActiveImpl((Tab) evt.getOldValue()), isActive());
    }

    @Override
    public boolean isActive() {
      return isActiveImpl(fTabbedPane.getSelectedTab());
    }

    private boolean isActiveImpl(Tab aSelectedTab) {
      return fTab == aSelectedTab;
    }

    @Override
    public void setActive(boolean aActive) {
      fTabbedPane.setSelectedTab(aActive ? fTab : null);
    }
  }

  /**
   * Similar to OverlayLayout: the children get all the available space. It is intended to only
   * contain one visible component at a time, from which the preferred size is used.
   */
  private class MyOverlayLayout implements LayoutManager {
    @Override
    public void addLayoutComponent(String name, Component comp) {
    }

    @Override
    public void removeLayoutComponent(Component comp) {
    }

    @Override
    public Dimension preferredLayoutSize(Container parent) {
      for (Component component : parent.getComponents()) {
        if (component.isVisible()) {
          return addInsets(parent, component.getPreferredSize());
        }
      }
      return addInsets(parent, new Dimension(0, 0));
    }

    @Override
    public Dimension minimumLayoutSize(Container parent) {
      for (Component component : parent.getComponents()) {
        if (component.isVisible()) {
          return addInsets(parent, component.getMinimumSize());
        }
      }
      return addInsets(parent, new Dimension(0, 0));
    }

    @Override
    public void layoutContainer(Container parent) {
      for (Component component : parent.getComponents()) {
        if (component.isVisible()) {
          Insets i = parent.getInsets();
          component.setBounds(i.left, i.top,
                              parent.getWidth() - i.left - i.right,
                              parent.getHeight() - i.top - i.bottom);
        }
      }
    }

    private Dimension addInsets(Container parent, Dimension aDim) {
      Insets i = parent.getInsets();
      return new Dimension(aDim.width + i.left + i.right, aDim.height + i.top + i.bottom);
    }
  }

  /**
   * Tab of a HideableTabbedPane. It has a name, icon and a content. The content can be any Swing
   * component.
   */
  public static class Tab extends JPanel {
    private String fTabName;
    private ILcdIcon fTabIcon;
    private String fShortDescription;
    private TLcyGroupDescriptor fGroupDescriptor = TLcyGroupDescriptor.DEFAULT;

    public Tab(String aTabName) {
      this(aTabName, null);
    }

    public Tab(String aTabName, ILcdIcon aTabIcon) {
      super(new BorderLayout());
      fTabName = aTabName;
      fTabIcon = aTabIcon;
    }

    public String getTabName() {
      return fTabName;
    }

    public void setTabName(String aTabName) {
      String oldValue = fTabName;
      fTabName = aTabName;
      firePropertyChange("tabName", oldValue, fTabName);
    }

    public ILcdIcon getTabIcon() {
      return fTabIcon;
    }

    public void setTabIcon(ILcdIcon aTabIcon) {
      ILcdIcon oldValue = fTabIcon;
      fTabIcon = aTabIcon;
      firePropertyChange("tabIcon", oldValue, fTabIcon);
    }

    public String getShortDescription() {
      return fShortDescription;
    }

    public void setShortDescription(String aShortDescription) {
      Object oldValue = fShortDescription;
      fShortDescription = aShortDescription;
      firePropertyChange("shortDescription", oldValue, aShortDescription);
    }

    public TLcyGroupDescriptor getGroupDescriptor() {
      return fGroupDescriptor;
    }

    /**
     * Sets the group descriptor that is used to insert the button (to show/hide this tab) into the
     * {@linkplain #setActionBar(ILcyActionBar) action bar}.
     * @param aGroupDescriptor The group descriptor.
     */
    public void setGroupDescriptor(TLcyGroupDescriptor aGroupDescriptor) {
      TLcyGroupDescriptor oldValue = fGroupDescriptor;
      fGroupDescriptor = aGroupDescriptor;
      firePropertyChange("groupDescriptor", oldValue, fGroupDescriptor);
    }
  }
}
