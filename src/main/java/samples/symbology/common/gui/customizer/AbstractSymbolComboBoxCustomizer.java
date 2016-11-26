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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.swing.JComponent;

import com.luciad.gui.ILcdIcon;
import com.luciad.gui.ILcdObjectIconProvider;
import samples.common.search.TextFieldComboBox;
import samples.symbology.common.EMilitarySymbology;
import com.luciad.model.ILcdModel;
import com.luciad.util.ILcdStringTranslator;

@SuppressWarnings("unchecked")
/**
 * Provides a combo box to customize an enumerated property of a military symbol.
 * To implement this class, implement these abstracts method:<br/>
 * - {@link #setValue} to update the symbol
 * - {@link #getValue(Object)} to retrieve the property value from the symbol
 * - {@link #retrieveValues} to check whether the property is applicable
 */
abstract class AbstractSymbolComboBoxCustomizer<V> extends AbstractSymbolCustomizer {

  private final SymbologyTextFieldComboBox fContent;

  private List<V> fPossibleValues = Collections.EMPTY_LIST;

  public AbstractSymbolComboBoxCustomizer(boolean aFireModelChange, ILcdStringTranslator aStringTranslator) {
    this(aFireModelChange, null, aStringTranslator);
  }

  /**
   * @param aFireModelChange true if model changes should be fired. Typically this is false if you're
   *                         customizing objects that are not (yet) contained in a model.
   * @param aStringTranslator translator to be used for tooltips, labels, etc
   */
  public AbstractSymbolComboBoxCustomizer(boolean aFireModelChange, ILcdObjectIconProvider aObjectIconProvider, ILcdStringTranslator aStringTranslator) {
    super(aFireModelChange, aStringTranslator);
    fContent = new SymbologyTextFieldComboBox(12, aObjectIconProvider == null ? null : new ListItemIconProvider(aObjectIconProvider));
    fContent.setWidthFactor(1.5);
  }

  /**
   * Stores the new value for the given symbol.
   * Before entering this method, a write lock is obtained for the model.
   */
  protected abstract void setValue(Object aSymbol, V aValue);

  /**
   * @return the symbol's current value.
   */
  protected abstract V getValue(Object aSymbol);

  /**
   * @return retrieves the list of possible values to choose from
   */
  public abstract List<V> retrieveValues(Object aSymbol);

  /**
   * Override to customize the String representation of the values.
   *
   * @param aValue the value to return the String representation for
   *
   * @return the String value displayed in this customizer
   */
  protected String getDisplayName(V aValue) {
    return Objects.toString(aValue);
  }

  /**
   * Override to replace a null value in the field (i.e. when clearing the field) with a particular default value.
   * The default implementation falls back on the first applicable possible value, if it exists.
   *
   * @return the default value to hide, if any
   * @param aSymbol the current symbol
   */
  protected V retrieveEmptyValue(Object aSymbol) {
    List<V> values = retrieveValues(aSymbol);
    return values.isEmpty() ? null : values.iterator().next();
  }

  @Override
  public void setSymbolImpl(final EMilitarySymbology aMilitarySymbology, ILcdModel aModel, Object aSymbol) {
    if (aMilitarySymbology != null) {
      String newTextValue;
      if (aSymbol != null) {
        fPossibleValues = retrieveValues(aSymbol);
        final ArrayList<String> items = new ArrayList<String>(fPossibleValues.size());
        for (V possibleValue : fPossibleValues) {
          items.add(getDisplayName(possibleValue));
        }
        fContent.setSearchContent(items.toArray(new String[items.size()]));

        V value = getValue(aSymbol);
        if (fPossibleValues.contains(value)) {
          newTextValue = getDisplayName(value);
        } else {
          newTextValue = null;
        }
      } else {
        newTextValue = null;
      }
      String oldTextValue = fContent.getText();
      if (!Objects.equals(oldTextValue, newTextValue)) {
        fContent.setText(newTextValue);
        fContent.setCaretPosition(newTextValue != null ? newTextValue.length() : 0);
      }
    } else if (aMilitarySymbology == null) {
      fContent.setText(null);
    }
    fContent.setEnabled(aSymbol != null && fPossibleValues.size() > 1);
  }

  @Override
  public JComponent getComponent() {
    return fContent;
  }

  @Override
  public void setEnabled(boolean aEnabled) {
    if (!aEnabled || fPossibleValues.size() > 1) {
      fContent.setEnabled(aEnabled);
    }
  }

  protected TextFieldComboBox getContent() {
    return fContent;
  }

  protected final void setBackgroundLocked(boolean aLocked) {
    fContent.setBackgroundLocked(aLocked);
  }

  private class SymbologyTextFieldComboBox extends TextFieldComboBox {

    protected SymbologyTextFieldComboBox(int columns, ILcdObjectIconProvider aObjectIconProvider) {
      super(columns, aObjectIconProvider);
    }

    @Override
    protected void valueSelected(String aOldValue, String aValue) {
      final V selectedValue;
      final Object symbol = getSymbol();
      if (aValue == null) {
        selectedValue = retrieveEmptyValue(symbol);
      } else {
        selectedValue = fPossibleValues.get(Arrays.asList(getSearchContent()).indexOf(aValue));
      }
      if (symbol != null) {
        V previousValue = getValue(symbol);
        if ((selectedValue == null && previousValue != null) ||
            (selectedValue != null && !selectedValue.equals(previousValue))) {
          final V oldValue = getValue(symbol);
          applyChange(new Runnable() {
                        @Override
                        public void run() {
                          setValue(symbol, selectedValue);
                        }
                      },
                      new Runnable() {
                        @Override
                        public void run() {
                          setValue(symbol, oldValue);
                        }
                      }
          );
        } else {
          // no symbol change is needed but the user did select a value,
          // so auto-complete the field to this value.
          fContent.setText(getDisplayName(getValue(symbol)));
        }
      }
    }
  }

  private class ListItemIconProvider implements ILcdObjectIconProvider {
    private final ILcdObjectIconProvider fDelegate;

    private ListItemIconProvider(ILcdObjectIconProvider aDelegate) {
      fDelegate = aDelegate;
    }

    @Override
    public ILcdIcon getIcon(Object aObject) throws IllegalArgumentException {
      if (aObject instanceof String) {
        int index = Arrays.asList(fContent.getSearchContent()).indexOf(aObject);
        if (index >= 0) {
          return fDelegate.getIcon(fPossibleValues.get(index));
        }
      }
      return null;
    }

    @Override
    public boolean canGetIcon(Object aObject) {
      if (aObject instanceof String) {
        int index = Arrays.asList(fContent.getSearchContent()).indexOf(aObject);
        if (index >= 0) {
          return fDelegate.canGetIcon(fPossibleValues.get(index));
        }
      }
      return false;
    }
  }
}
