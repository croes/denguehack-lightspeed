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
package samples.lucy.tableview;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import com.luciad.gui.TLcdIconFactory;
import com.luciad.lucy.gui.ALcyActiveSettable;
import com.luciad.lucy.util.context.TLcyModelContext;
import com.luciad.lucy.util.language.TLcyLang;

/**
 * <code>ILcyActiveSettable</code> which will, when active, apply the selection from a table on a
 * layer
 */
class AutoSelectActiveSettable extends ALcyActiveSettable {

  private TableViewCustomizerPanel fCustomizerPanel;

  public AutoSelectActiveSettable(TableViewCustomizerPanel aCustomizerPanel) {
    super(TLcyLang.getString("Auto Select"));
    setIcon(TLcdIconFactory.create(TLcdIconFactory.SELECT_OBJECT_ON_MAP_ICON));
    fCustomizerPanel = aCustomizerPanel;
    setShortDescription(TLcyLang.getString("Automatically select table selection on map"));
    fCustomizerPanel.addPropertyChangeListener("autoSelect", new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        firePropertyChange("active", evt.getOldValue(), evt.getNewValue());
      }
    });
    // Adjust the enabled state based on the layer in the context
    // The customizer panel only accepts one object, and that object cannot be changed afterwards so no need
    // for listeners
    TLcyModelContext modelContext = (TLcyModelContext) fCustomizerPanel.getObject();
    if (modelContext != null && modelContext.getLayer() != null) {
      setEnabled(modelContext.getLayer().isSelectableSupported());
    }
  }

  @Override
  public boolean isActive() {
    return isEnabled() && fCustomizerPanel.isAutoSelect();
  }

  @Override
  public void setActive(boolean aActive) {
    if (isEnabled()) {
      if (aActive != isActive()) {
        fCustomizerPanel.setAutoSelect(aActive);
        firePropertyChange("active", !aActive, aActive);
      }
    }
  }
}
