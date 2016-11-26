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
package samples.lucy.frontend.internalframes;

import java.awt.Container;
import java.awt.Dimension;
import java.util.EventObject;
import java.util.HashMap;

import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;

import com.luciad.gui.ILcdIcon;
import com.luciad.gui.swing.TLcdSWIcon;
import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.lucy.gui.ILcyApplicationPane;
import com.luciad.lucy.gui.ILcyApplicationPaneOwner;

/**
 * Implementation of <code>ILcyApplicationPane</code> that is based on a <code>JInternalFrame</code>.
 */
public class InternalFrameAppPane extends JInternalFrame implements ILcyApplicationPane {
  private HashMap fProperties = new HashMap();
  private ILcyApplicationPaneOwner fOwner;
  private final ILcyLucyEnv fLucyEnv;

  public InternalFrameAppPane(ILcyApplicationPaneOwner aOwner, ILcyLucyEnv aLucyEnv) {
    super("Title", true, true, true, true);
    fOwner = aOwner;
    fLucyEnv = aLucyEnv;
    setSize(100, 100); //make sure there is always some size
  }

  @Override
  public void putValue(String aKey, Object aValue) {
    Object old_value = fProperties.put(aKey, aValue);
    firePropertyChange(aKey, old_value, aValue);

    //Use the icon and small description as frame icon and tooltip.
    if (SMALL_ICON.equals(aKey)) {
      setFrameIcon(aValue == null ? null : new TLcdSWIcon((ILcdIcon) aValue));
    } else if (SHORT_DESCRIPTION.equals(aKey)) {
      setToolTipText((String) aValue);
      ((JComponent) getContentPane()).setToolTipText((String) aValue);
    }

  }

  @Override
  public Object getValue(String aKey) {
    return fProperties.get(aKey);
  }

  @Override
  public void packApp() {
    pack();
  }

  @Override
  public boolean canPackApp() {
    return true;
  }

  @Override
  public void dispose() {
    super.dispose();
    notifyInterestedParties();
  }

  @Override
  public void disposeApp() {
    notifyInterestedParties();

    JDesktopPane desktop = getDesktopPane();
    desktop.remove(this);
    desktop.revalidate();
    desktop.repaint();
  }

  private void notifyInterestedParties() {
    if (fOwner != null) {
      fOwner.applicationPaneDisposing(new EventObject(this));
    }
    fLucyEnv.getUserInterfaceManager().getApplicationPaneManager().applicationPaneRemoved(this);
  }

  @Override
  public boolean isDisposable() {
    return isClosable();
  }

  @Override
  public void setDisposable(boolean aDisposable) {
    boolean old_value = isDisposable();
    setClosable(aDisposable);
    firePropertyChange("disposable", old_value, aDisposable);
  }

  @Override
  public void setAppEnabled(boolean aEnabled) {
    boolean old_value = isEnabled();
    setEnabled(aEnabled);
    firePropertyChange("appEnabled", old_value, aEnabled);
  }

  @Override
  public boolean isAppVisible() {
    return isVisible();
  }

  @Override
  public void setAppVisible(boolean aVisible) {
    boolean old_value = isAppVisible();
    setVisible(aVisible);
    firePropertyChange("appVisible", old_value, aVisible);
  }

  @Override
  public void setAppSize(Dimension aDimension) {
    Dimension old_value = getAppSize();
    setSize(aDimension);
    firePropertyChange("appSize", old_value, aDimension);
  }

  @Override
  public Dimension getAppSize() {
    return getSize();
  }

  @Override
  public void setAppTitle(String aTitle) {
    String old_value = getAppTitle();
    setTitle(aTitle);
    firePropertyChange("appTitle", old_value, aTitle);
  }

  @Override
  public String getAppTitle() {
    return getTitle();
  }

  @Override
  public Container getAppContentPane() {
    return getContentPane();
  }

  @Override
  public void bringAppToFront() {
    toFront();
  }
}
