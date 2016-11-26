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

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.jgoodies.common.base.Strings;
import com.luciad.gui.TLcdIconFactory;
import com.luciad.gui.TLcdResizeableIcon;
import com.luciad.gui.swing.TLcdSWIcon;
import samples.common.SwingUtil;

/**
 * Textfield extension that allows searching a {@link SearchableTable}.
 */
class SearchTextField extends JTextField {

  private static final String PROMPT_TEXT = "Enter object class...";

  public SearchTextField() {
    super((int) (PROMPT_TEXT.length() * 1.5));
    setLayout(new BorderLayout());
    String promptText = PROMPT_TEXT;
    TextPrompt textOverlay = new TextPrompt(promptText, this, TextPrompt.Show.FOCUS_LOST, BorderLayout.WEST);
    textOverlay.changeAlpha(0.9f);
    textOverlay.changeStyle(Font.ITALIC);

    SwitchClearTextFieldButton searchClearButton = new SwitchClearTextFieldButton(this);
    add(centerVertically(searchClearButton), BorderLayout.EAST);

    AbstractAction clearAction = new AbstractAction("clear input") {
      @Override
      public void actionPerformed(ActionEvent e) {
        SearchTextField.this.setText("");
      }
    };
    getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), clearAction.getValue(Action.NAME));
    getActionMap().put(clearAction.getValue(Action.NAME), clearAction);
  }

  /**
   * Switches between search and clear mode. Clear mode means that a button will appear that allows the user to clear
   * the input field. If the input field is blank, a search icon appears.
   */
  private static JPanel centerVertically(SwitchClearTextFieldButton aSearchClearButton) {
    JPanel buttonContainer = new JPanel();
    buttonContainer.setOpaque(false);
    buttonContainer.setLayout(new BoxLayout(buttonContainer, BoxLayout.PAGE_AXIS));
    buttonContainer.add(Box.createVerticalGlue());
    buttonContainer.add(aSearchClearButton);
    buttonContainer.add(Box.createVerticalGlue());
    return buttonContainer;
  }

  private static final class SwitchClearTextFieldButton extends JPanel {
    private static final String SEARCH = "search";
    private static final String CLEAR = "clear";
    private static final TLcdSWIcon SEARCH_ICON = new TLcdSWIcon(new TLcdResizeableIcon(TLcdIconFactory.create(TLcdIconFactory.SEARCH_ICON), 12, 12));
    private static final TLcdSWIcon CLEAR_ICON = new TLcdSWIcon(new TLcdResizeableIcon(TLcdIconFactory.create(TLcdIconFactory.CLOSE_ICON), 12, 12));

    public SwitchClearTextFieldButton(final JTextField aTextField) {
      CardLayout cardLayout = new CardLayout();
      setLayout(cardLayout);
      add(createSearchButton(), SEARCH);
      add(createClearButton(aTextField), CLEAR);
      cardLayout.show(this, SEARCH);
      initBehavior(aTextField);
      setOpaque(false);
    }

    private void initBehavior(final JTextField aTextField) {
      aTextField.getDocument().addDocumentListener(new DocumentListener() {
        @Override
        public void insertUpdate(DocumentEvent aEvent) {
          update(aTextField.getText());
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
          update(aTextField.getText());
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
          update(aTextField.getText());
        }
      });
    }

    private Component createClearButton(final JTextField aTextField) {
      JButton button = new JButton(new AbstractAction(null, CLEAR_ICON) {
        @Override
        public void actionPerformed(ActionEvent e) {
          aTextField.setText("");
        }
      });
      button.setBorderPainted(false);
      button.setContentAreaFilled(false);
      button.setFocusPainted(false);
      button.setOpaque(false);
      button.setCursor(Cursor.getDefaultCursor());
      SwingUtil.makeSquare(button, new Insets(0, 0, 0, 0));
      return button;
    }

    private Component createSearchButton() {
      JLabel label = new JLabel(SEARCH_ICON);
      label.setOpaque(false);
      return label;
    }

    private void update(String aText) {
      String panelId = Strings.isBlank(aText) ? SEARCH : CLEAR;
      ((CardLayout) getLayout()).show(this, panelId);
    }
  }
}
