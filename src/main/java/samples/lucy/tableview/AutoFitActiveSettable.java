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

import com.luciad.gui.TLcdCompositeIcon;
import com.luciad.gui.TLcdIconFactory;
import com.luciad.gui.TLcdResizeableIcon;
import com.luciad.gui.TLcdTranslatedIcon;
import com.luciad.lucy.gui.ALcyActiveSettable;
import com.luciad.lucy.util.language.TLcyLang;

/**
 * <code>ILcyActiveSettable</code> which will fit the view on the table selection when active
 */
class AutoFitActiveSettable extends ALcyActiveSettable {
  private TableViewCustomizerPanel fCustomizerPanel;

  public AutoFitActiveSettable(TableViewCustomizerPanel aCustomizerPanel) {
    super(TLcyLang.getString("Auto Fit"));
    TLcdCompositeIcon composite_icon = new TLcdCompositeIcon();
    composite_icon.addIcon(new TLcdResizeableIcon(new TLcdTranslatedIcon(TLcdIconFactory.create(TLcdIconFactory.SEARCH_ICON), -3, 0)));
    composite_icon.addIcon(new TLcdResizeableIcon(new TLcdTranslatedIcon(TLcdIconFactory.create(TLcdIconFactory.LINK_DECO_ICON), 3, -3)));
    composite_icon.setIconWidth(22);
    setIcon(composite_icon);
    fCustomizerPanel = aCustomizerPanel;
    setShortDescription(TLcyLang.getString("Automatically fit map to table selection"));
    fCustomizerPanel.addPropertyChangeListener("autoFit", new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        firePropertyChange("active", evt.getOldValue(), evt.getNewValue());
      }
    });
  }

  @Override
  public boolean isActive() {
    return fCustomizerPanel.isAutoFit();
  }

  @Override
  public void setActive(boolean aActive) {
    if (aActive != isActive()) {
      fCustomizerPanel.setAutoFit(aActive);
      firePropertyChange("active", !aActive, aActive);
    }
  }
}
