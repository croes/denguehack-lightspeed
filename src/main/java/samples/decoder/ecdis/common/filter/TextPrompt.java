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

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

class TextPrompt extends JLabel implements FocusListener, DocumentListener {

  private JTextComponent component;
  private Document document;

  private Show show;
  private boolean showPromptOnce;
  private int focusLost;

  public TextPrompt(String text, JTextComponent aComponent, Show aShow, String aLayoutConstraint) {
    component = aComponent;
    setShow(aShow);
    document = aComponent.getDocument();

    setText(text);
    setFont(aComponent.getFont());
    Insets insets = aComponent.getInsets();
    setBorder(BorderFactory.createEmptyBorder(insets.top, insets.left, insets.bottom, insets.right));
    setHorizontalAlignment(JLabel.LEADING);

    if (aShow != Show.ALWAYS) {
      aComponent.addFocusListener(this);
      document.addDocumentListener(this);
    }

    aComponent.add(this, aLayoutConstraint);
    checkForPrompt();
  }

  @Override
  protected void paintComponent(Graphics g) {
    Graphics2D graphics = (Graphics2D) g.create();
    graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    super.paintComponent(graphics);
    graphics.dispose();
  }

  /**
   *  Convenience method to change the alpha value of the current foreground
   *  Color to the specific value.
   *
   *  @param alpha value in the range of 0 - 1.0.
   */
  public void changeAlpha(float alpha) {
    changeAlpha((int) (alpha * 255));
  }

  /**
   *  Convenience method to change the alpha value of the current foreground
   *  Color to the specific value.
   *
   *  @param alpha value in the range of 0 - 255.
   */
  public void changeAlpha(int alpha) {
    alpha = alpha > 255 ? 255 : alpha < 0 ? 0 : alpha;

    Color foreground = getForeground();
    int red = foreground.getRed();
    int green = foreground.getGreen();
    int blue = foreground.getBlue();

    Color withAlpha = new Color(red, green, blue, alpha);
    super.setForeground(withAlpha);
  }

  /**
   *  Convenience method to change the style of the current Font. The style
   *  values are found in the Font class. Common values might be:
   *  Font.BOLD, Font.ITALIC and Font.BOLD + Font.ITALIC.
   *
   *  @param style value representing the the new style of the Font.
   */
  public void changeStyle(int style) {
    setFont(getFont().deriveFont(style));
  }

  /**
   *  Get the Show property
   *
   *  @return the Show property.
   */
  public Show getShow() {
    return show;
  }

  /**
   *  Set the prompt Show property to control when the promt is shown.
   *  Valid values are:
   *
   *  Show.ALWAYS (default) - always show the prompt
   *  Show.Focus_GAINED - show the prompt when the component gains focus
   *      (and hide the prompt when focus is lost)
   *  Show.Focus_LOST - show the prompt when the component loses focus
   *      (and hide the prompt when focus is gained)
   *
   *  @param show a valid Show enum
   */
  public void setShow(Show show) {
    this.show = show;
  }

  /**
   *  Get the showPromptOnce property
   *
   *  @return the showPromptOnce property.
   */
  public boolean getShowPromptOnce() {
    return showPromptOnce;
  }

  /**
   *  Show the prompt once. Once the component has gained/lost focus
   *  once, the prompt will not be shown again.
   *
   *  @param showPromptOnce  when true the prompt will only be shown once,
   *                         otherwise it will be shown repeatedly.
   */
  public void setShowPromptOnce(boolean showPromptOnce) {
    this.showPromptOnce = showPromptOnce;
  }

  /**
   *	Check whether the prompt should be visible or not. The visibility
   *  will change on updates to the Document and on focus changes.
   */
  private void checkForPrompt() {
    //  Text has been entered, remove the prompt

    if (document.getLength() > 0) {
      setVisible(false);
      return;
    }

    //  Prompt has already been shown once, remove it

    if (showPromptOnce && focusLost > 0) {
      setVisible(false);
      return;
    }

    //  Check the Show property and component focus to determine if the
    //  prompt should be displayed.

    if (component.hasFocus()) {
      setVisible(show == Show.ALWAYS || show == Show.FOCUS_GAINED);
    } else {
      setVisible(show == Show.ALWAYS || show == Show.FOCUS_LOST);
    }
  }

  //  Implement FocusListener
  public void focusGained(FocusEvent aEvent) {
    checkForPrompt();
  }

  public void focusLost(FocusEvent e) {
    focusLost++;
    checkForPrompt();
  }

  //  Implement DocumentListener
  public void insertUpdate(DocumentEvent aEvent) {
    checkForPrompt();
  }

  public void removeUpdate(DocumentEvent aEvent) {
    checkForPrompt();
  }

  public void changedUpdate(DocumentEvent aEvent) {
    // Ignore
  }

  public enum Show {
    ALWAYS,
    FOCUS_GAINED,
    FOCUS_LOST
  }
}
