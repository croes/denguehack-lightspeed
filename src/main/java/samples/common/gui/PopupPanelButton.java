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

import javax.swing.JComponent;

/**
 * A button that displays the given content in a popup when you click on it. There are no restrictions on the content,
 * it can for example contain nested popups, combo boxes or text fields.
 *
 * See also {@link PopupMenuButton} which shows a regular JPopupMenu.
 */
public class PopupPanelButton extends APopupButton {
  private final PopupPanel fPopup;
  private final JComponent fContent;

  public PopupPanelButton(JComponent aContent) {
    this(aContent, false, PopupLocation.DEFAULT);
  }

  /**
   * Creates a new button. Specify the text and/or icon after creating it.
   *
   * @param aContent the content to be displayed inside the popup.
   * @param aButtonContentCentered See {@link APopupButton#APopupButton(boolean)}.
   * @param aLocation The location for the popup.
   */
  public PopupPanelButton(JComponent aContent, boolean aButtonContentCentered, PopupLocation aLocation) {
    super(aButtonContentCentered);
    fPopup = PopupPanel.create(this, aContent, true, aLocation);
    fContent = aContent;
  }

  @Override
  protected void showPopup() {
    preparePopupContent(fContent);
    fPopup.setPopupVisible(true);
  }

  @Override
  protected void hidePopup() {
    fPopup.setPopupVisible(false);
  }

  @Override
  protected boolean isPopupShowing() {
    return fPopup.isPopupVisible();
  }

  public PopupPanel getPopup() {
    return fPopup;
  }

  /**
   * Called before showing the popup. Overwrite this method to update the popup content before showing it.
   * @param aContent The content, as it was provided to the constructor.
   */
  protected void preparePopupContent(JComponent aContent) {
  }
}
