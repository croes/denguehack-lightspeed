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
package samples.common.gui;

import java.awt.Point;

import javax.swing.JPopupMenu;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import com.luciad.gui.TLcdAWTUtil;
import com.luciad.gui.TLcdIconFactory;
import com.luciad.gui.TLcdResizeableIcon;
import com.luciad.gui.swing.TLcdSWIcon;

/**
 * A button that displays a popup menu when you click on it. The popup menu can be configured as desired,
 * for example with menu items, check box menu items, separators, sub-menus etc.
 *
 * The default constructors create a button that can have a regular icon and text associated to it. Next to that,
 * it has a little arrow icon to indicate that, when pressing the button, a popup appears.
 *
 * There is also a factory method that creates a button that is supposed to be used without any text on it, that
 * only shows the arrow icon, see {@link #createButtonWithoutText(JPopupMenu)}.
 *
 */
public class PopupMenuButton extends APopupButton {
  private final JPopupMenu fPopupMenu;
  private boolean fFirstTime = true;
  private boolean fMadeInvisibleExternally = false;

  /**
   * Creates a button that is supposed to be used without additional text or icon. Only the arrow icon is used.
   * @param aPopupMenu The popup menu.
   * @return The created button.
   */
  public static PopupMenuButton createButtonWithoutText(JPopupMenu aPopupMenu) {
    PopupMenuButton button = new PopupMenuButton(aPopupMenu);
    TLcdIconFactory.Size defaultSize = TLcdIconFactory.getDefaultSize();
    button.setIcon(new TLcdSWIcon(new TLcdResizeableIcon(button.getArrowIcon(), button.getArrowIcon().getIconWidth(), defaultSize.getSize())));
    button.setArrowIcon(null);
    return button;
  }

  /**
   * Creates a new button. Specify the text and/or icon after creating it.
   *
   * @param aPopup the popupmenu to be displayed.
   */
  public PopupMenuButton(JPopupMenu aPopup) {
    this(aPopup, false);
  }

  /**
   * Creates a new button. Specify the text and/or icon after creating it.
   *
   * @param aPopup the JPopupMenu to show when clicking this button.
   * @param aButtonContentCentered See {@link APopupButton#APopupButton(boolean)}.
   */
  public PopupMenuButton(JPopupMenu aPopup, boolean aButtonContentCentered) {
    super(aButtonContentCentered);
    fPopupMenu = aPopup;
    LookAndFeelChangeListener.install(aPopup);

    //we set the invoker prematurely so that it can be retrieved even
    //if the popup menu had never been shown. That way the invoker can
    //be retrieved and removed if this popup menu becomes empty
    aPopup.setInvoker(this);

    aPopup.addPopupMenuListener(new MyPopupMenuListener());
  }

  public JPopupMenu getPopupMenu() {
    return fPopupMenu;
  }

  @Override
  protected void showPopup() {
    if (fMadeInvisibleExternally) {
      // This case happens when we click on the button while the popup is visible. Clicking outside the popup
      // hides it, which is immediately followed by a mouse pressed which shows it again. But clicking on the
      // button while the popup is visible should hide the popup, therefore this workaround.
      return;
    }

    Point popupLocation = PopupLocation.DEFAULT.calculatePopupLocation(this, fPopupMenu);
    if (fFirstTime) {
      fPopupMenu.show(this, popupLocation.x, popupLocation.y);
      fPopupMenu.setVisible(false);
      fFirstTime = false;
    }
    fPopupMenu.show(this, popupLocation.x, popupLocation.y);
  }

  @Override
  protected void hidePopup() {
    fPopupMenu.setVisible(false);
  }

  @Override
  protected boolean isPopupShowing() {
    return fPopupMenu.isVisible();
  }

  /**
   * Selects the button when the popup menu is visible.
   */
  class MyPopupMenuListener implements PopupMenuListener {
    @Override
    public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
      PopupMenuButton.this.setSelected(true);
    }

    @Override
    public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
      PopupMenuButton.this.setSelected(false);

      //since this comes first, we mark this as an external invisibility.
      //if this is followed by a click on the button, we'll know that the button was pressed.
      fMadeInvisibleExternally = true;
      Runnable runnable = new Runnable() {
        @Override
        public void run() {
          //We set the external invisible flag false again on the next AWT event. (So that it is true if
          //we are clicking on the button, since this is part of the same AWT-event)
          fMadeInvisibleExternally = false;
        }
      };
      TLcdAWTUtil.invokeLater(runnable);
    }

    @Override
    public void popupMenuCanceled(PopupMenuEvent e) {
      PopupMenuButton.this.setSelected(false);
    }
  }
}
