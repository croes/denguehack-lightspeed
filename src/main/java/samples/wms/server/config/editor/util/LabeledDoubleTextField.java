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
package samples.wms.server.config.editor.util;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

import com.luciad.util.ILcdFilter;

/**
 * Convenience class to display a labeled double text field.
 * Text field does not allow input that is not a double.
 */
public class LabeledDoubleTextField extends LabeledTextField {

  public LabeledDoubleTextField(String aLabel, String aDefaultValue) {
    super(aLabel, aDefaultValue);
    getTextField().setDocument(new DoubleDocument());
    getTextField().setText(aDefaultValue);
  }

  private static class FilterDocument extends PlainDocument {

    private ILcdFilter fFilter;

    public FilterDocument(ILcdFilter aFilter) {
      fFilter = aFilter;
    }

    public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
      StringBuffer testString = new StringBuffer(getText(0, getLength()));
      testString.insert(offs, str);
      if (fFilter.accept(testString.toString())) {
        super.insertString(offs, str, a);
      }
    }
  }

  private static class DoubleDocument extends FilterDocument {

    public DoubleDocument() {
      super(new ILcdFilter() {
        public boolean accept(Object aObject) {
          try {
            Double.parseDouble((String) aObject);
            return true;
          } catch (NumberFormatException e) {
            return false;
          }
        }
      });
    }
  }
}
