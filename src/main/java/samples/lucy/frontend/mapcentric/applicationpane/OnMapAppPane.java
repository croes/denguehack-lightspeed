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

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.util.EventObject;
import java.util.HashMap;

import javax.swing.JPanel;

import com.luciad.gui.ILcdIcon;
import samples.lucy.frontend.mapcentric.gui.onmappanel.OnMapPanel;
import samples.lucy.frontend.mapcentric.gui.onmappanel.OnMapPanelContainer;
import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.lucy.gui.ILcyApplicationPane;
import com.luciad.lucy.gui.ILcyApplicationPaneOwner;

/**
 * Implementation of <code>ILcyApplicationPane</code> that is based on a <code>OnMapPanel</code>.
 */
class OnMapAppPane extends OnMapPanel implements ILcyApplicationPane {
  private final ILcyApplicationPaneOwner fOwner;
  private final ILcyLucyEnv fLucyEnv;
  private final OnMapPanelContainer fParent;
  private final int fOriginalLocationIndex;

  private final HashMap<String, Object> fProperties = new HashMap<String, Object>();
  private boolean fDisposable = true;

  public OnMapAppPane(OnMapPanelContainer aParent, ILcyApplicationPaneOwner aOwner, ILcyLucyEnv aLucyEnv, int aOriginalLocationIndex) {
    super(new JPanel(new BorderLayout()));
    fOwner = aOwner;
    fLucyEnv = aLucyEnv;
    fParent = aParent;
    fOriginalLocationIndex = aOriginalLocationIndex;
  }

  @Override
  public void putValue(String aKey, Object aValue) {
    Object oldValue = fProperties.put(aKey, aValue);
    firePropertyChange(aKey, oldValue, aValue);

    //Use the small description as tooltip.
    if (SHORT_DESCRIPTION.equals(aKey)) {
      setToolTipText((String) aValue);
      getUserContent().setToolTipText((String) aValue);
    } else if ( SMALL_ICON.equals(aKey)){
      setTitleIcon((ILcdIcon) aValue);
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

    removeWithAnimation();
    fParent.revalidate();
    fParent.repaint();
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
    boolean oldValue = isEnabled();
    setEnabled(aEnabled);
    firePropertyChange("appEnabled", oldValue, aEnabled);
  }

  @Override
  public boolean isAppVisible() {
    return isVisible();
  }

  @Override
  public void setAppVisible(boolean aVisible) {
    boolean oldValue = isAppVisible();
    setVisible(aVisible);
    firePropertyChange("appVisible", oldValue, aVisible);
  }

  @Override
  public void setAppSize(Dimension aDimension) {
    Dimension oldValue = getAppSize();
    setSize(aDimension);
    firePropertyChange("appSize", oldValue, aDimension);
  }

  @Override
  public Dimension getAppSize() {
    return getSize();
  }

  @Override
  public void setResizable(boolean aResizable) {
    //Do nothing, pane can't be resized
  }

  @Override
  public void setAppTitle(String aTitle) {
    String oldValue = getAppTitle();
    setTitle(aTitle);
    firePropertyChange("appTitle", oldValue, aTitle);
  }

  @Override
  public String getAppTitle() {
    return getTitle();
  }

  @Override
  public Container getAppContentPane() {
    return getUserContent();
  }

  @Override
  public void bringAppToFront() {
    setCollapsed(false);
  }

  public int getOriginalLocationIndex() {
    return fOriginalLocationIndex;
  }
}
