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

import java.awt.Component;
import java.util.Arrays;
import java.util.List;

import javax.swing.JComponent;

import com.luciad.format.nvg.nvg20.model.TLcdNVG20Point;
import com.luciad.format.nvg.nvg20.model.TLcdNVG20SymbolizedContent;
import samples.common.TitledCollapsiblePane;
import samples.symbology.common.EMilitarySymbology;
import samples.symbology.common.gui.customizer.AbstractSymbolCustomizer;
import samples.common.TwoColumnPanel;
import com.luciad.model.ILcdModel;
import com.luciad.util.ILcdChangeListener;
import com.luciad.util.ILcdStringTranslator;
import com.luciad.util.TLcdChangeEvent;

public class NVGStyleCustomizer extends AbstractNVGSymbolCustomizer {

  private final TitledCollapsiblePane fContent;
  private final List<AbstractSymbolCustomizer> fCustomizers;

  public NVGStyleCustomizer(boolean aFireModelChange, ILcdStringTranslator aStringTranslator) {
    super(aFireModelChange, aStringTranslator);
    final AbstractSymbolCustomizer fillColorCustomizer = new NVGFillColorCustomizer(aFireModelChange, aStringTranslator);
    final AbstractSymbolCustomizer strokeColorCustomizer = new NVGStrokeColorCustomizer(aFireModelChange, aStringTranslator);
    final AbstractSymbolCustomizer strokeWidthCustomizer = new NVGStrokeWidthCustomizer(aFireModelChange, aStringTranslator);
    final AbstractSymbolCustomizer resetStyleCustomizer = new NVGResetStyleCustomizer(aFireModelChange, aStringTranslator);

    final ILcdChangeListener changeListener = new ILcdChangeListener() {
      @Override
      public void stateChanged(TLcdChangeEvent aChangeEvent) {
        fireChangeEvent(new TLcdChangeEvent(NVGStyleCustomizer.this));
      }
    };
    fillColorCustomizer.addChangeListener(changeListener);
    fillColorCustomizer.setUndoManager(getUndoManager());
    strokeColorCustomizer.addChangeListener(changeListener);
    strokeColorCustomizer.setUndoManager(getUndoManager());
    strokeWidthCustomizer.addChangeListener(changeListener);
    strokeWidthCustomizer.setUndoManager(getUndoManager());
    resetStyleCustomizer.addChangeListener(changeListener);
    resetStyleCustomizer.setUndoManager(getUndoManager());
    fCustomizers = Arrays.asList(fillColorCustomizer, strokeColorCustomizer, strokeWidthCustomizer, resetStyleCustomizer);

    fContent = new TitledCollapsiblePane(
        translate("Style"),
        new TwoColumnPanel()
            .contentBuilder()
            .row(translate("Fill color"), fillColorCustomizer.getComponent())
            .row(translate("Stroke color"), strokeColorCustomizer.getComponent())
            .row(translate("Stroke width"), strokeWidthCustomizer.getComponent())
            .row("", resetStyleCustomizer.getComponent())
            .build());
    fContent.setAlignmentX(Component.LEFT_ALIGNMENT);
  }

  @Override
  protected void setSymbolImpl(EMilitarySymbology aMilitarySymbology, ILcdModel aModel, Object aSymbol) {
    for (AbstractSymbolCustomizer customizer : fCustomizers) {
      customizer.setSymbol(aMilitarySymbology, aModel, aSymbol);
    }
    fContent.setCollapsed(!isStyledSymbolizedContent(aSymbol));
  }

  @Override
  public JComponent getComponent() {
    return fContent;
  }

  @Override
  public void setEnabled(boolean aEnabled) {
    for (AbstractSymbolCustomizer customizer : fCustomizers) {
      customizer.setEnabled(aEnabled);
    }
  }

  public static boolean isStyledSymbolizedContent(Object aSymbol) {
    return aSymbol instanceof TLcdNVG20SymbolizedContent && !(aSymbol instanceof TLcdNVG20Point);
  }
}
