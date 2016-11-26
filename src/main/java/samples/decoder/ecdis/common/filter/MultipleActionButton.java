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
package samples.decoder.ecdis.common.filter;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import com.luciad.gui.TLcdIconFactory;
import com.luciad.gui.swing.TLcdSWIcon;

/**
 * Button that shows a popup menu containing additional actions when clicked.
 */
public final class MultipleActionButton extends JPanel {
  private static final Icon ICON = new TLcdSWIcon(TLcdIconFactory.create(TLcdIconFactory.MOVE_DOWN_ICON ));
  private final JMenu fMenu;

  /**
   * Constructor.
   *
   * @param aButtonLabel the button label
   * @param aDelegateActions the delegate actions that should be shown in the popup
   */
  public MultipleActionButton(String aButtonLabel, AbstractAction... aDelegateActions) {
    fMenu = new JMenu(aButtonLabel);
    for (AbstractAction delegateAction : aDelegateActions) {
      JMenuItem menuItem = new JMenuItem(delegateAction);
      menuItem.setIcon(null);
      fMenu.add(menuItem);
    }

    fMenu.setHorizontalTextPosition(SwingConstants.LEFT);
    fMenu.setIcon(ICON);
    JMenuBar menuBar = new JMenuBar();
    menuBar.add(fMenu);
    add(menuBar);
  }

  @Override
  public void setToolTipText(String text) {
    fMenu.setToolTipText(text);
  }
}
