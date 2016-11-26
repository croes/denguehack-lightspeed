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

import java.util.Iterator;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import samples.symbology.common.EMilitarySymbology;
import samples.symbology.common.util.MilitarySymbolFacade;
import com.luciad.model.ILcdModel;
import com.luciad.symbology.app6a.model.ILcdEditableAPP6ACoded;
import com.luciad.symbology.milstd2525b.model.ILcdEditableMS2525bCoded;
import com.luciad.util.ILcdStringTranslator;

/**
 * Provides a check box to customize a boolean modifier of a military symbol.
 */
class SymbolModifierBooleanCustomizer extends AbstractSymbolCustomizer {

  private final JCheckBox fContent = new JCheckBox();

  private final String fModifier;
  private final String[] fTrueFalseValues = new String[2];

  /**
   * @param aFireModelChange true if model changes should be fired. Typically this is false if you're
   *                         customizing objects that are not (yet) contained in a model.
   * @param aStringTranslator translator to be used for tooltips, labels, etc
   */
  public SymbolModifierBooleanCustomizer(boolean aFireModelChange, ILcdStringTranslator aStringTranslator, MilitarySymbolFacade.Modifier aModifier) {
    super(aFireModelChange, aStringTranslator);
    fModifier = aModifier.getName();
    Iterator iterator = aModifier.getPossibleValues().iterator();
    fTrueFalseValues[0] = (String) iterator.next();
    fTrueFalseValues[1] = (String) iterator.next();

    fContent.getModel().addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent e) {
        final boolean selectedValue = fContent.getModel().isSelected();
        if (getSymbol() != null) {
          final boolean previousValue = getValue(getSymbol());
          if ((selectedValue != previousValue)) {
            applyChange(new Runnable() {
                          @Override
                          public void run() {
                            setValue(getSymbol(), selectedValue);
                          }
                        }, new Runnable() {
                          @Override
                          public void run() {
                            setValue(getSymbol(), previousValue);
                          }
                        }
            );
          }
        }
      }
    });
  }

  /**
   * Stores the new value for the given symbol. Before entering this method, a write lock is
   * obtained for the model.
   */
  protected void setValue(Object aSymbol, boolean aValue) {
    if (aSymbol instanceof ILcdEditableAPP6ACoded) {
      ((ILcdEditableAPP6ACoded) aSymbol).putTextModifier(fModifier, aValue ? fTrueFalseValues[0] : fTrueFalseValues[1]);
    } else {
      ((ILcdEditableMS2525bCoded) aSymbol).putTextModifier(fModifier, aValue ? fTrueFalseValues[0] : fTrueFalseValues[1]);
    }
  }

  /**
   * @return the symbol's current value.
   */
  protected boolean getValue(Object aSymbol) {
    String modifierValue;
    if (aSymbol instanceof ILcdEditableAPP6ACoded) {
      modifierValue = ((ILcdEditableAPP6ACoded) aSymbol).getTextModifierValue(fModifier);
    } else {
      modifierValue = ((ILcdEditableMS2525bCoded) aSymbol).getTextModifierValue(fModifier);
    }
    return fTrueFalseValues[0].equals(modifierValue);
  }

  @Override
  public void setSymbolImpl(final EMilitarySymbology aMilitarySymbology, ILcdModel aModel, Object aSymbol) {
    if (aMilitarySymbology != null) {
      if (aSymbol != null) {
        fContent.getModel().setSelected(getValue(aSymbol));
      }
    }
    fContent.setEnabled(aSymbol != null);
  }

  @Override
  public JComponent getComponent() {
    return fContent;
  }

  @Override
  public void setEnabled(boolean aEnabled) {
    fContent.setEnabled(aEnabled);
  }
}
