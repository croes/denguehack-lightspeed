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

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;

import com.luciad.format.nvg.model.TLcdNVGStyle;
import com.luciad.format.nvg.nvg20.model.TLcdNVG20Content;
import samples.symbology.common.EMilitarySymbology;
import com.luciad.model.ILcdModel;
import com.luciad.util.ILcdStringTranslator;

public class NVGResetStyleCustomizer extends AbstractNVGSymbolCustomizer {

  private JPanel fContent = new JPanel();
  private JButton fButton;

  public NVGResetStyleCustomizer(boolean aFireModelChange, ILcdStringTranslator aStringTranslator) {
    super(aFireModelChange, aStringTranslator);
    fButton = new JButton();
    fButton.setAction(new ResetAction());
    fButton.setText(translate("Reset"));
    fButton.setToolTipText(translate("Reset to the default NVG style"));
    fContent.add(fButton);
  }

  @Override
  protected void setSymbolImpl(EMilitarySymbology aMilitarySymbology, ILcdModel aModel, Object aSymbol) {
    setEnabled(NVGStyleCustomizer.isStyledSymbolizedContent(aSymbol));
  }

  @Override
  public JComponent getComponent() {
    return fButton;
  }

  @Override
  public void setEnabled(boolean aEnabled) {
    fContent.setVisible(aEnabled);
  }

  private class ResetAction extends AbstractAction {

    @Override
    public void actionPerformed(ActionEvent e) {
      if (getSymbol() instanceof TLcdNVG20Content) {
        final TLcdNVG20Content nvg20Content = (TLcdNVG20Content) getSymbol();
        final TLcdNVGStyle oldStyle = nvg20Content.getStyle();
        applyChange(new Runnable() {
                      @Override
                      public void run() {
                        nvg20Content.setStyle(new TLcdNVGStyle());
                      }
                    },
                    new Runnable() {
                      @Override
                      public void run() {
                        nvg20Content.setStyle(oldStyle);
                      }
                    }
        );
      }
    }
  }
}
