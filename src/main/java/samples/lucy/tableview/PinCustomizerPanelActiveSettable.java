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

import com.luciad.gui.ILcdIcon;
import com.luciad.gui.TLcdIconFactory;
import com.luciad.lucy.gui.ALcyActiveSettable;
import com.luciad.lucy.gui.customizer.ILcyCustomizerPanel;
import com.luciad.lucy.util.language.TLcyLang;

/**
 * Active settable which adjusts the {@link ILcyCustomizerPanel#HINT_PINNED}
 * of an <code>ILcyCustomizerPanel</code>
 */
class PinCustomizerPanelActiveSettable extends ALcyActiveSettable {

  private static final ILcdIcon LOCK_ACTIVE_ICON = TLcdIconFactory.create(TLcdIconFactory.LOCKED_ICON);
  private static final ILcdIcon LOCK_DEACTIVE_ICON = TLcdIconFactory.create(TLcdIconFactory.UNLOCKED_ICON);

  private ILcyCustomizerPanel fPanel;

  public PinCustomizerPanelActiveSettable(ILcyCustomizerPanel aPanel) {
    super(TLcyLang.getString("Lock"));
    fPanel = aPanel;
    aPanel.addPropertyChangeListener(new HintPinnedPropertyChangeListener());

    setShortDescription("Lock on the current layer");
    setIcon(isActive() ? LOCK_ACTIVE_ICON : LOCK_DEACTIVE_ICON);
  }

  @Override
  public boolean isActive() {
    Object value = fPanel.getValue(ILcyCustomizerPanel.HINT_PINNED);
    return value instanceof Boolean && ((Boolean) value);
  }

  @Override
  public void setActive(boolean aActive) {
    fPanel.putValue(ILcyCustomizerPanel.HINT_PINNED, aActive);
  }

  /**
   * Listener which converts the HINT_PINNED property change event of an ILcyCustomizerPanel to
   * an 'active' property change event of this ILcyActiveSettable.
   */
  private class HintPinnedPropertyChangeListener implements PropertyChangeListener {
    @Override
    public void propertyChange(PropertyChangeEvent aEvent) {
      if (ILcyCustomizerPanel.HINT_PINNED.equals(aEvent.getPropertyName())) {
        PinCustomizerPanelActiveSettable.this.firePropertyChange("active", aEvent.getOldValue(), aEvent.getNewValue());
      }
    }
  }
}
