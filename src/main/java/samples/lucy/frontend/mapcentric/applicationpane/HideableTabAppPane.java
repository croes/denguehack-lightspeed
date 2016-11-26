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
package samples.lucy.frontend.mapcentric.applicationpane;

import java.awt.Container;
import java.awt.Dimension;
import java.util.EventObject;
import java.util.HashMap;

import javax.swing.JComponent;

import com.luciad.gui.ILcdIcon;
import samples.lucy.frontend.mapcentric.gui.HideableTabbedPane;
import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.lucy.gui.ILcyApplicationPane;
import com.luciad.lucy.gui.ILcyApplicationPaneOwner;
import com.luciad.lucy.gui.TLcyGroupDescriptor;

/**
 * Application pane implementation that integrates with a HideableTabbedPane. Every application
 * pane is a tab of a given HideableTabbedPane.
 *
 * The application pane name, icon and content are used as the tab name, icon and content. When
 * application panes are disposed, the corresponding tab is removed. The application pane is
 * considered visible when the corresponding tab is the selected tab.
 */
class HideableTabAppPane extends HideableTabbedPane.Tab implements ILcyApplicationPane {
  private final ILcyApplicationPaneOwner fOwner;
  private final HideableTabbedPane fParent;
  private final ILcyLucyEnv fLucyEnv;

  private final HashMap<String, Object> fProperties = new HashMap<String, Object>();
  private boolean fDisposable = true;

  public HideableTabAppPane(HideableTabbedPane aParent, ILcyApplicationPaneOwner aOwner, ILcyLucyEnv aLucyEnv) {
    super("noNameYet");
    fParent = aParent;
    fOwner = aOwner;
    fLucyEnv = aLucyEnv;
  }

  @Override
  public void putValue(String aKey, Object aValue) {
    Object oldValue = fProperties.put(aKey, aValue);
    firePropertyChange(aKey, oldValue, aValue);

    //Use the icon and small description
    if (SMALL_ICON.equals(aKey)) {
      setTabIcon((ILcdIcon) aValue);
    } else if (SHORT_DESCRIPTION.equals(aKey)) {
      ((JComponent) getAppContentPane()).setToolTipText((String) aValue);
      setShortDescription((String) aValue);
    } else if (MapCentricAppPaneFactory.TAB_GROUP_DESCRIPTOR_KEY.equals(aKey)) {
      setGroupDescriptor((TLcyGroupDescriptor) aValue);
    }
  }

  @Override
  public Object getValue(String aKey) {
    return fProperties.get(aKey);
  }

  @Override
  public void packApp() {
  }

  @Override
  public boolean canPackApp() {
    return false;
  }

  @Override
  public void disposeApp() {
    notifyInterestedParties();

    fParent.removeTab(this);
  }

  private void notifyInterestedParties() {
    if (fOwner != null) {
      fOwner.applicationPaneDisposing(new EventObject(this));
    }
    fLucyEnv.getUserInterfaceManager().getApplicationPaneManager().applicationPaneRemoved(this);
  }

  @Override
  public boolean isDisposable() {
    return fDisposable;
  }

  @Override
  public void setDisposable(boolean aDisposable) {
    boolean oldValue = isDisposable();
    fDisposable = aDisposable;
    firePropertyChange("disposable", oldValue, aDisposable);
  }

  @Override
  public void setAppEnabled(boolean aEnabled) {
    boolean oldValue = getAppContentPane().isEnabled();
    getAppContentPane().setEnabled(aEnabled);
    firePropertyChange("appEnabled", oldValue, aEnabled);
  }

  @Override
  public boolean isAppVisible() {
    return fParent.getSelectedTab() == this;
  }

  @Override
  public void setAppVisible(boolean aVisible) {
    fParent.setSelectedTab(aVisible ? this : null);
  }

  @Override
  public void setAppSize(Dimension aDimension) {
    // ignore
  }

  @Override
  public Dimension getAppSize() {
    return getAppContentPane().getSize();
  }

  @Override
  public void setResizable(boolean aResizable) {
    //Do nothing, pane can't be resized
  }

  @Override
  public void setAppTitle(String aTitle) {
    String oldValue = getAppTitle();
    setTabName(aTitle);
    firePropertyChange("appTitle", oldValue, aTitle);
  }

  @Override
  public String getAppTitle() {
    return getTabName();
  }

  @Override
  public Container getAppContentPane() {
    return this;
  }

  @Override
  public void bringAppToFront() {
    fParent.setSelectedTab(this);
  }
}
