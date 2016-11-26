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
package samples.lucy.treetableview;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import com.luciad.gui.TLcdIconFactory;
import com.luciad.lucy.gui.ALcyActiveSettable;
import com.luciad.lucy.util.language.TLcyLang;

/**
 * Active settable which enables/disables the filtering of empty and/or null
 * nodes
 */
class HideEmptyActiveSettable extends ALcyActiveSettable {

  private TreeTableViewCustomizerPanel fCustomizerPanel;

  private boolean fActive;

  public HideEmptyActiveSettable(TreeTableViewCustomizerPanel aCustomizerPanel) {
    fCustomizerPanel = aCustomizerPanel;
    aCustomizerPanel.addPropertyChangeListener("hideNullOrEmptyNodes", new CustomizerPanelListener());
    fActive = aCustomizerPanel.isHideNullOrEmptyNodes();
    setShortDescription(TLcyLang.getString("Hide empty read-only properties"));
    setIcon(TLcdIconFactory.create(TLcdIconFactory.FILTER_ICON));
  }

  @Override
  public boolean isActive() {
    return fActive;
  }

  @Override
  public void setActive(boolean aActive) {
    if (aActive != fActive) {
      fActive = aActive;
      fCustomizerPanel.setHideNullOrEmptyNodes(fActive);
      firePropertyChange("active", !fActive, fActive);
    }
  }

  private class CustomizerPanelListener implements PropertyChangeListener {
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
      setActive(((Boolean) evt.getNewValue()));
    }
  }
}
