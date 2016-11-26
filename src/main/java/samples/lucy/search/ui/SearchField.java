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
package samples.lucy.search.ui;

import javax.swing.Icon;
import javax.swing.JButton;

import org.jdesktop.swingx.JXSearchField;
import org.jdesktop.swingx.plaf.PromptTextFieldUI;

import com.luciad.gui.ILcdIcon;
import com.luciad.gui.TLcdIconFactory;
import com.luciad.gui.TLcdResizeableIcon;
import com.luciad.gui.swing.TLcdSWIcon;
import samples.gxy.common.PaddedIcon;
import samples.lucy.search.SearchManager;
import com.luciad.util.ILcdStatusListener;
import com.luciad.util.TLcdStatusEvent;

/**
 * Extension of a JXSearchField with different default buttons.
 * It will also listen to the SearchManager for events.
 * Depending on the event, the correct icons will be displayed.
 */
final class SearchField extends JXSearchField {
  private final Icon fLoopIcon = pad(TLcdIconFactory.create(TLcdIconFactory.SEARCH_ICON));
  private final Icon fBusyIcon = pad(TLcdIconFactory.create(TLcdIconFactory.BUSY_ANIMATED_ICON));

  /**
   * Constructor.
   *
   * @param aDefaultSearchText the initial search text that needs to be displayed.
   * @param aSearchManager the search manager
   */
  public SearchField(String aDefaultSearchText, SearchManager aSearchManager) {
    super(aDefaultSearchText);

    setColumns(15);
    setIconOnButton(fLoopIcon, getFindButton());
    setIconOnButton(pad(new TLcdResizeableIcon(TLcdIconFactory.create(TLcdIconFactory.CLOSE_ICON), 12, 12)),
                    getCancelButton());

    aSearchManager.addStatusListener(new IconChangeListener());

    setName("emphasizedTextField"); // On behalf of BlackLimeLookAndFeel, so it can style it differently
  }

  private void setIconOnButton(Icon aIcon, JButton aButtonSFCT) {
    aButtonSFCT.setIcon(aIcon);
    aButtonSFCT.setRolloverIcon(aIcon);
    aButtonSFCT.setPressedIcon(aIcon);
    aButtonSFCT.setSelectedIcon(aIcon);
    aButtonSFCT.setRolloverSelectedIcon(aIcon);
  }

  @Override
  public void updateUI() {
    super.updateUI();
    if (getUI() instanceof PromptTextFieldUI) {
      // Propagate our name to the inner text field used for the optional prompt, so that the UI can recognize it.
      ((PromptTextFieldUI) getUI()).getPromptComponent(this).setName(getName());
    }
  }

  private static Icon pad(ILcdIcon aIcon) {
    return new TLcdSWIcon(new PaddedIcon(aIcon, 3, 0));
  }

  private class IconChangeListener implements ILcdStatusListener<SearchManager> {
    @Override
    public void statusChanged(TLcdStatusEvent<SearchManager> aStatusEvent) {
      if (aStatusEvent.getID() == TLcdStatusEvent.START_BUSY) {
        setIconOnButton(fBusyIcon, getFindButton());
      } else if (aStatusEvent.getID() == TLcdStatusEvent.END_BUSY) {
        setIconOnButton(fLoopIcon, getFindButton());
      }
    }
  }
}
