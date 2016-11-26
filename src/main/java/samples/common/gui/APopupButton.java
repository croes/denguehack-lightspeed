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

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.ref.WeakReference;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.UIManager;

import com.luciad.gui.ILcdAction;
import com.luciad.gui.ILcdIcon;
import com.luciad.gui.TLcdIconFactory;
import com.luciad.gui.swing.TLcdGreyIcon;
import com.luciad.gui.swing.TLcdSWIcon;
import samples.common.PopupTriangleIcon;
import samples.common.SwingUtil;

/**
 * A button that displays an arrow icon to show a popup panel or popup menu, check out the sub-classes.
 * The arrow icon is painted on top of the button, so that the button can still have its regular text and icon.
 */
abstract class APopupButton extends JButton {
  private static final int ARROW_ICON_HEIGHT = TLcdIconFactory.getDefaultSize().getSize() * 9 / 16; //px
  private static final int ARROW_TEXT_GAP = 3; //px
  private static final int MARGIN = 2; //px

  private static WeakReference<APopupButton> sLastOpenedPopup = new WeakReference<>(null);

  private ILcdIcon fArrowIcon = new PopupTriangleIcon(ARROW_ICON_HEIGHT);
  private Icon fArrowSwingIcon;
  private Icon fDisabledArrowSwingIcon;
  private boolean fButtonContentCentered;

  public APopupButton() {
    this(false);
  }

  /**
   * Constructor.
   *
   * @param aButtonContentCentered To display the arrow icon, extra space is added to the right side of the button.
   *                               This makes the button asymmetrical, which does not look nice when used on a vertical
   *                               tool bar for example. When setting this property to {@code true}, the padding to the
   *                               left and right sides is identical, meaning the button content (text + icon) appears
   *                               centered. The drawback is that the button becomes wider. Setting it to {@code false}
   *                               only adds the padding to the right side for the arrow icon. This property has
   *                               no effect if the arrow icon is {@code null}.
   */
  public APopupButton(boolean aButtonContentCentered) {
    super();
    fButtonContentCentered = aButtonContentCentered;
    addMouseListener(new ShowPopupController());
    updateUIImpl();
  }

  @Override
  public void setAction(Action a) {
    super.setAction(a);

    // Hide the button text if the action wants us to
    Boolean showActionName = (Boolean) a.getValue(ILcdAction.SHOW_ACTION_NAME);
    if (showActionName != null) {
      setHideActionText(!showActionName);
    }
  }

  @Override
  public void setIcon(Icon defaultIcon) {
    super.setIcon(defaultIcon);

    // Swing automatically derives disabled icons, but not for all icon types. Check if the look and feel
    // can do it, and if not, do it ourselves.
    Icon disabledIcon = UIManager.getLookAndFeel().getDisabledIcon(this, defaultIcon);
    if (disabledIcon == null) {
      setDisabledIcon(new TLcdSWIcon(new TLcdGreyIcon(defaultIcon)));
    } else {
      // Let Swing derive it automatically
      setDisabledIcon(null);
    }
  }

  public ILcdIcon getArrowIcon() {
    return fArrowIcon;
  }

  /**
   * Sets the icon used to indicate this button is a popup. It can be {@code null}.
   * @param aArrowIcon The arrow icon, usually a small triangle like icon.
   */
  public void setArrowIcon(ILcdIcon aArrowIcon) {
    fArrowIcon = aArrowIcon;
    updateUIImpl();
  }

  @Override
  public void updateUI() {
    super.updateUI();
    updateUIImpl();
  }

  private void updateUIImpl() {
    int iconWidth = fArrowIcon == null ? 0 : (ARROW_TEXT_GAP + fArrowIcon.getIconWidth());

    int right = iconWidth + MARGIN;
    int left = fButtonContentCentered ? right : MARGIN;
    Insets margin = new Insets(MARGIN, left, MARGIN, right);
    SwingUtil.makeSquare(this, margin);

    if (fArrowIcon != null) {
      fArrowSwingIcon = new TLcdSWIcon(fArrowIcon);
      fDisabledArrowSwingIcon = UIManager.getLookAndFeel().getDisabledIcon(this, fArrowSwingIcon);
      if (fDisabledArrowSwingIcon == null) {
        fDisabledArrowSwingIcon = fArrowSwingIcon;
      }
    } else {
      fArrowSwingIcon = null;
      fDisabledArrowSwingIcon = null;
    }
  }

  @Override
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);

    // Paint the arrow icon where the margin has left room for it.
    if (fArrowIcon != null) {
      Insets insets = getInsets();
      Icon icon = getModel().isEnabled() ? fArrowSwingIcon : fDisabledArrowSwingIcon;
      int y = Math.max(0, (getHeight() / 2) - (fArrowIcon.getIconHeight() / 2));
      icon.paintIcon(this, g, getWidth() - insets.right + ARROW_TEXT_GAP, y);
    }
  }

  /**
   * This MouseListener shows a given popup menu when:
   * - the mouse is pressed on the button
   * - the mouse is hovered over the button, when a sibling pop-up menu is already open. This simulates the
   *   behavior of a menu-bar, where you can explore all menu's by expanding one and hovering over the others.
   */
  private class ShowPopupController extends MouseAdapter {
    @Override
    public void mousePressed(MouseEvent e) {
      sLastOpenedPopup = new WeakReference<>(null);

      Component source = e.getComponent();
      if (!source.isEnabled()) {
        return;
      }
      if (isPopupShowing()) {
        hidePopup();
      } else {
        showPopupImpl();
      }
    }

    private void showPopupImpl() {
      showPopup();
      sLastOpenedPopup = new WeakReference<>(APopupButton.this);
    }

    @Override
    public void mouseEntered(MouseEvent e) {
      APopupButton lastOpened = sLastOpenedPopup.get();
      if (!isPopupShowing() &&
          lastOpened != null && lastOpened.isPopupShowing() &&
          siblings(APopupButton.this, lastOpened)) {
        lastOpened.hidePopup();
        showPopupImpl();
      }
    }

    private boolean siblings(JComponent aOne, JComponent aOther) {
      return aOne.getParent() == aOther.getParent();
    }
  }

  protected abstract void showPopup();

  protected abstract void hidePopup();

  protected abstract boolean isPopupShowing();
}
