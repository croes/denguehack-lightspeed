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
package samples.lucy.util;

import java.awt.Color;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.text.Format;
import java.text.ParseException;

import javax.swing.JFormattedTextField;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.InternationalFormatter;

import samples.common.text.ParseAllFormat;
import com.luciad.lucy.ILcyLucyEnv;

/**
 * <p>Extension of <code>JFormattedTextField</code> that automatically colors the background of the
 * text field red if the entered text is invalid.  The text is invalid if it can't be parsed by the
 * given <code>java.text.Format</code>, or if the given format only parses a part of the entered
 * text. The validation is performed as you type. If focus is lost, and an invalid value is still in
 * the text field, it is reverted to what it was before.</p>
 *
 * <p>As with any JFormattedTextField, <code>setValue</code> should best be invoked before showing
 * this text field. Changes in the value can be observed by a "value" property change listener.</p>
 */
public class ValidatingTextField extends JFormattedTextField {

  public static Color ERROR_BACKGROUND;
  public static Color ERROR_FOREGROUND;

  private static final String ERROR_BACKGROUND_COLOR_KEY = "com.luciad.lucy.lookandfeel.validation.errorBackgroundColor";
  private static final String ERROR_FOREGROUND_COLOR_KEY = "com.luciad.lucy.lookandfeel.validation.errorForegroundColor";

  /**
   * Boolean indicating whether the colors have already been retrieved from the config file
   */
  private static boolean fColorInitialised = false;

  private Color fBackground, fForeground;

  private boolean fValidContent;

  public ValidatingTextField(Format aFormat, ILcyLucyEnv aLucyEnv) {
    super(new ParseAllFormat(aFormat));
    setFocusLostBehavior(JFormattedTextField.COMMIT_OR_REVERT);
    getDocument().addDocumentListener(new DocumentListener() {
      @Override
      public void insertUpdate(DocumentEvent e) {
        updateValidity();
      }

      @Override
      public void removeUpdate(DocumentEvent e) {
        updateValidity();
      }

      @Override
      public void changedUpdate(DocumentEvent e) {
        updateValidity();
      }
    });
    addFocusListener(new MousePositionCorrectorListener());
    initializeColors(aLucyEnv);
  }

  public static void initializeColors(ILcyLucyEnv aLucyEnv) {
    if (!(fColorInitialised)) {
      // UIManager gets precedence, so it's easy to configure all application colors in one place.
      ERROR_BACKGROUND = UIManager.getColor(ERROR_BACKGROUND_COLOR_KEY);
      ERROR_FOREGROUND = UIManager.getColor(ERROR_FOREGROUND_COLOR_KEY);

      // Fall back to the user preferences
      if (aLucyEnv != null) {
        if (ERROR_BACKGROUND == null) {
          ERROR_BACKGROUND = aLucyEnv.getPreferencesManager().getCurrentUserPreferences().
              getColor(ERROR_BACKGROUND_COLOR_KEY, null);
        }
        if (ERROR_FOREGROUND == null) {
          ERROR_FOREGROUND = aLucyEnv.getPreferencesManager().getCurrentUserPreferences().
              getColor(ERROR_FOREGROUND_COLOR_KEY, null);
        }
      } else {
        //set default values when LucyEnv is not specified
        ERROR_FOREGROUND = null;
        ERROR_BACKGROUND = new Color(255, 215, 215);
      }
      fColorInitialised = true;
    }
  }

  public ValidatingTextField(Format aFormat, Object aValue, ILcyLucyEnv aLucyEnv) {
    this(aFormat, aLucyEnv);
    setValue(aValue);
  }

  public boolean isValidContent() {
    return fValidContent;
  }

  public void setValidContent(boolean aValidContent) {
    if (fValidContent != aValidContent) {
      fValidContent = aValidContent;
      firePropertyChange("validContent", !fValidContent, fValidContent);
    }
  }

  @Override
  public void updateUI() {
    super.updateUI();
    fBackground = getBackground();
    fForeground = getForeground();
  }

  /**
   * Sets the given format for formatting and parsing.
   * @param aFormat The format for formatting and parsing.
   * @param aNewValue The new value which will be set after the format has been changed
   */
  public void setFormat(Format aFormat, Object aNewValue) {
    int old_caret_position = getCaretPosition();
    /*
    When changing the FormatterFactory, the old value of the JFormattedTextField is automatically
    formatted with the new format. Since this can cause exceptions, the FormatterFactory is first
     changed to the default one, then the new value is put on the JFormattedTextField. This new
     value will then be formatted with the DefaultFormatterFactory, after which the format is changed
     again to the requested format. The newly put format will then only format the new value and not
     the old one.
     */
    setFormatterFactory(new DefaultFormatterFactory());
    setValue(aNewValue);
    setFormatterFactory(new DefaultFormatterFactory(new InternationalFormatter(new ParseAllFormat(aFormat))));
    setCaretPosition(Math.min(old_caret_position, getText().length()));
  }

  private void updateValidity() {
    boolean valid = validContent();
    setValidContent(valid);
    if (ERROR_BACKGROUND != null) {
      setBackground(valid ? fBackground : ERROR_BACKGROUND);
    }
    if (ERROR_FOREGROUND != null) {
      setForeground(valid ? fForeground : ERROR_FOREGROUND);
    }
  }

  private boolean validContent() {
    AbstractFormatter formatter = getFormatter();
    if (formatter != null) {
      try {
        formatter.stringToValue(getText());
        return true;
      } catch (ParseException e) {
        return false;
      }
    }
    return true;
  }

  @Override
  public void setValue(Object value) {
    boolean validValue = true;
    //before setting the value, parse it by using the format
    try {
      AbstractFormatter formatter = getFormatter();
      if (formatter != null) {
        formatter.valueToString(value);
      }
    } catch (ParseException e) {
      validValue = false;
    }
    //only set the value when valid
    if (validValue) {
      int old_caret_position = getCaretPosition();
      super.setValue(value);
      setCaretPosition(Math.min(old_caret_position, getText().length()));
    }
    updateValidity();
  }

  @Override
  protected boolean processKeyBinding(KeyStroke ks, KeyEvent e, int condition, boolean pressed) {
    //do not let the formatted text field consume the enters
    if (validContent()) {
      return super.processKeyBinding(ks, e,
                                     condition, pressed) && ks != KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
    } else {
      return super.processKeyBinding(ks, e,
                                     condition, pressed);
    }
  }

  private static class MousePositionCorrectorListener extends FocusAdapter {
    @Override
    public void focusGained(FocusEvent e) {
      /* After a formatted text field gains focus, it replaces its text with its
       * current value, formatted appropriately of course. It does this after
       * any focus listeners are notified. We want to make sure that the caret
       * is placed in the correct position rather than the dumb default that is
        * before the 1st character ! */
      final JTextField field = (JTextField) e.getSource();
      final int dot = field.getCaret().getDot();
      final int mark = field.getCaret().getMark();
      if (field.isEnabled() && field.isEditable()) {
        SwingUtilities.invokeLater(new Runnable() {
          @Override
          public void run() {
            // Only set the caret if the textfield hasn't got a selection on it
            if (dot == mark) {
              field.getCaret().setDot(dot);
            }
          }
        });
      }
    }
  }

}
