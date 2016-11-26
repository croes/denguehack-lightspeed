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
package samples.symbology.common.gui.customizer;

import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

import samples.symbology.common.util.MilitarySymbolFacade;
import com.luciad.util.ILcdStringTranslator;

/**
 * Provides a text field to customize a text modifier value formatted as a military date.
 */
class SymbolModifierDateTimeCustomizer extends SymbolModifierCustomizer {

  /**
   * @param aFireModelChange true if model changes should be fired. Typically this is false if you're
   *                         customizing objects that are not (yet) contained in a model.
   * @param aStringTranslator translator to be used for tooltips, labels, etc
   * @param aModifier Related text modifier reference
   */
  public SymbolModifierDateTimeCustomizer(boolean aFireModelChange, ILcdStringTranslator aStringTranslator, MilitarySymbolFacade.Modifier aModifier) {
    this(aFireModelChange, aStringTranslator, aModifier.getName());
  }


  protected SymbolModifierDateTimeCustomizer(boolean aFireModelChange, ILcdStringTranslator aStringTranslator, String aName) {
    super(aFireModelChange, aStringTranslator, aName);

    setHint(translate("ddHHmmTMMMyy"));
    setToolTip(translate("<html>" +
                         "Enter a date formatted as ddHHmmTMMMyy:" +
                         "<ul>dd (day, 00-31)<br/>" +
                         "HH (hour, 00-23)<br/>" +
                         "mm (minutes, 00-59)<br/>" +
                         "T (timezone character)<br/>" +
                         "MMM (month, jan-dec)<br/>" +
                         "yy (year, 00-99)</ul>" +
                         "For example:" +
                         "<ul>311956ZOCT14</ul>" +
                         "Press enter to set the value." +
                         "</html>"));
  }

  @Override
  protected JTextField createTextField() {
    return new MilitaryDateField();
  }

  /**
   * Simple validating field for date time group values.
   */
  public class MilitaryDateField extends JTextField {

    // describes the maximum numerical or alphabetical value of each character
    private final static String DATE_PATTERN = "392959ZSVV99";

    public MilitaryDateField() {
      setColumns(DATE_PATTERN.length());
      setDocument(new DateDocument());
    }

    protected class DateDocument extends PlainDocument {

      public void insertString(int offs, String aToInsert, AttributeSet a)
          throws BadLocationException {

        char[] charsToInsert = aToInsert.toCharArray();
        char[] result = new char[charsToInsert.length];
        int j = 0;

        char[] curStr = MilitaryDateField.this.getText().toCharArray();
        if ((curStr.length + charsToInsert.length) > DATE_PATTERN.length()) {
          if (curStr.length < DATE_PATTERN.length()) {
            result = new char[DATE_PATTERN.length() - curStr.length];
          } else {
            result = null;
          }
        }

        for (int i = 0; (result != null) && (i < result.length); i++) {
          char charToInsert = charsToInsert[i];
          if (Character.isDigit(DATE_PATTERN.charAt(offs + i))) {
            if (Character.isDigit(charToInsert)) {
              int max = DATE_PATTERN.charAt(offs + i) - '0';
              int cur = charToInsert - '0';
              if ((cur >= 0) && (cur <= max)) {
                result[j++] = charToInsert;
              }
            }
          } else {
            charToInsert = Character.toUpperCase(charToInsert);
            if ((charToInsert >= 'A') && (charToInsert <= DATE_PATTERN.charAt(offs + i))) {
              result[j++] = charToInsert;
            }
          }
        }

        if (j > 0) {
          super.insertString(offs, new String(result, 0, j), a);
        }
      }
    }

  }
}
