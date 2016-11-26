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
package samples.common;

import java.awt.Dimension;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.OverlayLayout;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

/**
 * Displays a prompt over top of a text component when the Document of the text field is empty. The
 * Show property is used to determine the visibility of the prompt.
 *
 * The Font and foreground Color of the prompt will default to those properties of the parent text
 * component. You are free to change the properties after class construction.
 */
public class TextComponentHint extends JLabel {

  public enum Show {
    ALWAYS,
    FOCUS_GAINED,
    FOCUS_LOST
  }

  private JTextComponent fComponent;
  private Document fDocument;

  private Show fShow;

  public static JComponent overlay(final JTextComponent aComponent, TextComponentHint aText) {
    // Overlay the search label on the search text field
    JLayeredPane lp = new JLayeredPane() {
      @Override
      public Dimension getPreferredSize() {
        Dimension size = super.getPreferredSize();
        return new Dimension(aComponent.getPreferredSize().width, size.height); // no horizontal stretching
      }
    };
    lp.setLayout(new OverlayLayout(lp));
    lp.add(aComponent, new Integer(0));
    lp.add(aText, new Integer(1));
    return lp;
  }

  /**
   * Creates a text prompt for the given component.
   * After creation, the prompt still needs to be overlayed on top of the component.
   * @param text      the text to display
   * @param component the component for which the text should be shown
   * @see #overlay(JTextComponent, TextComponentHint)
   */
  public TextComponentHint(String text, JTextComponent component) {
    this(text, component, Show.ALWAYS);
  }

  public TextComponentHint(String text, JTextComponent component, Show show) {
    fComponent = component;
    setShow(show);
    fDocument = component.getDocument();

    setText(text);
    setFont(component.getFont());
    setForeground(UIColors.fgHint());
    setBorder(new EmptyBorder(component.getInsets()));
    setHorizontalAlignment(JLabel.LEADING);
    setCursor(component.getCursor());

    component.addFocusListener(new MyFocusListener());
    fDocument.addDocumentListener(new MyDocumentListener());

    checkForPrompt();
  }

  /**
   * Get the Show property
   *
   * @return the Show property.
   */
  public Show getShow() {
    return fShow;
  }

  /**
   * Set the prompt Show property to control when the prom[t is shown. Valid values are:
   *
   * Show.ALWAYS(default) - always show the prompt
   * Show.Focus_GAINED - show the prompt when the component gains focus (and hide the prompt when focus is lost)
   * Show.Focus_LOST - show the prompt when the component loses focus (and hide the prompt when focus is gained)
   *
   * @param show a valid Show enum
   */
  public void setShow(Show show) {
    fShow = show;
  }

  /**
   * Check whether the prompt should be visible or not. The visibility will change on updates to the
   * Document and on focus changes.
   */
  private void checkForPrompt() {
    //  Text has been entered, remove the prompt

    if (fDocument.getLength() > 0) {
      setVisible(false);
      return;
    }

    //  Check the Show property and component focus to determine if the
    //  prompt should be displayed.

    if (fComponent.hasFocus()) {
      if (fShow == Show.ALWAYS
          || fShow == Show.FOCUS_GAINED) {
        setVisible(true);
      } else {
        setVisible(false);
      }
    } else {
      if (fShow == Show.ALWAYS
          || fShow == Show.FOCUS_LOST) {
        setVisible(true);
      } else {
        setVisible(false);
      }
    }
  }

  private class MyFocusListener implements FocusListener {
    @Override
    public void focusGained(FocusEvent e) {
      checkForPrompt();
    }

    @Override
    public void focusLost(FocusEvent e) {
      checkForPrompt();
    }
  }

  private class MyDocumentListener implements DocumentListener {
    @Override
    public void insertUpdate(DocumentEvent e) {
      checkForPrompt();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
      checkForPrompt();
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
    }

  }
}
