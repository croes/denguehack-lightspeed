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
package samples.symbology.nvg.gui.style;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Objects;

import javax.swing.JComponent;
import javax.swing.JTextField;

import samples.symbology.common.EMilitarySymbology;
import com.luciad.model.ILcdModel;
import com.luciad.util.ILcdStringTranslator;

abstract class AbstractNVGStyleCustomizer extends AbstractNVGSymbolCustomizer {

  private final JTextField fTextField;

  public AbstractNVGStyleCustomizer(boolean aFireModelChange, ILcdStringTranslator aStringTranslator) {
    super(aFireModelChange, aStringTranslator);
    fTextField = new JTextField();
    fTextField.addFocusListener(new FocusListener() {
      @Override
      public void focusGained(FocusEvent e) {
      }

      @Override
      public void focusLost(FocusEvent e) {
        updateFromTextField();
      }
    });

    fTextField.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        updateFromTextField();
      }
    });

    fTextField.setColumns(5);
    fTextField.setToolTipText(translate("Press enter to set the value"));
  }

  private void updateFromTextField() {
    final Object symbol = getSymbol();
    String selectedValue = fTextField.getText();
    // an empty string just means no value
    if ("".equals(selectedValue)) {
      selectedValue = null;
    }

    if (symbol != null) {
      String previousValue = getValue(symbol);
      if ((selectedValue == null && previousValue != null) ||
          (selectedValue != null && !selectedValue.equals(previousValue))) {
        final String finalSelectedValue = selectedValue;
        final String oldValue = getValue(symbol);
        applyChange(new Runnable() {
                      @Override
                      public void run() {
                        setValue(symbol, finalSelectedValue);
                      }
                    },
                    new Runnable() {
                      @Override
                      public void run() {
                        setValue(symbol, oldValue);
                      }
                    }
        );
      }
    }
  }

  @Override
  public void setSymbolImpl(EMilitarySymbology aMilitarySymbology, ILcdModel aModel, Object aSymbol) {
    if (aSymbol != null && hasValue(aSymbol)) {
      String newValue = getValue(aSymbol);
      String oldValue = getTextField().getText();
      if (!Objects.equals(oldValue, newValue)) {
        getTextField().setText(newValue);
      }
    }
    setEnabled(aSymbol != null && hasValue(aSymbol));
  }

  protected abstract boolean hasValue(Object aSymbol);

  protected abstract String getValue(Object aSymbol);

  protected abstract void setValue(Object aSymbol, String aValue);

  @Override
  public JComponent getComponent() {
    return fTextField;
  }

  @Override
  public void setEnabled(boolean aEnabled) {
    fTextField.setEnabled(aEnabled);
  }

  public JTextField getTextField() {
    return fTextField;
  }
}
