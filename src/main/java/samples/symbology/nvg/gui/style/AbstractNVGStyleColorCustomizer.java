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

import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Objects;

import javax.swing.JComponent;

import samples.symbology.common.EMilitarySymbology;
import com.luciad.model.ILcdModel;
import com.luciad.util.ILcdStringTranslator;

import samples.gxy.painterstyles.ColorPickPanel;

abstract class AbstractNVGStyleColorCustomizer extends AbstractNVGSymbolCustomizer {

  private final ColorPropertyChangeListener fListener = new ColorPropertyChangeListener();
  private ColorPickPanel fColorPickPanel;

  public AbstractNVGStyleColorCustomizer(boolean aFireModelChange, ILcdStringTranslator aStringTranslator) {
    super(aFireModelChange, aStringTranslator);

    fColorPickPanel = new ColorPickPanel();
  }

  @Override
  public JComponent getComponent() {
    return fColorPickPanel;
  }

  @Override
  public void setSymbolImpl(EMilitarySymbology aMilitarySymbology, ILcdModel aModel, Object aSymbol) {
    fColorPickPanel.removePropertyChangeListener(ColorPickPanel.COLOR_PROPERTY, fListener);
    if (aSymbol != null && hasValue(aSymbol)) {
      Color newValue = getValue(aSymbol);
      Color oldValue = fColorPickPanel.getColor();
      if (!Objects.equals(oldValue, newValue)) {
        fColorPickPanel.setColor(newValue);
      }
    }
    setEnabled(aSymbol != null && hasValue(aSymbol));
    fColorPickPanel.addPropertyChangeListener(ColorPickPanel.COLOR_PROPERTY, fListener);
  }

  protected abstract Color getValue(Object aSymbol);

  protected abstract boolean hasValue(Object aSymbol);

  protected abstract void setColor(Object aSymbol, Color aColor);

  @Override
  public void setEnabled(boolean aEnabled) {
    fColorPickPanel.setEnabled(aEnabled);
  }

  private class ColorPropertyChangeListener implements PropertyChangeListener {
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
      final Object symbol = getSymbol();
      final Color newColor = (Color) evt.getNewValue();
      final Color oldColor = getValue(symbol);

      applyChange(new Runnable() {
                    @Override
                    public void run() {
                      setColor(symbol, newColor);
                    }
                  },
                  new Runnable() {
                    @Override
                    public void run() {
                      setColor(symbol, oldColor);
                    }
                  }
      );
    }
  }
}
