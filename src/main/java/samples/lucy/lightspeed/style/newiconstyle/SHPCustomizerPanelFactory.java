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
package samples.lucy.lightspeed.style.newiconstyle;

import java.awt.Component;
import java.util.Collections;
import java.util.Map;

import javax.swing.BoxLayout;

import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.lucy.gui.customizer.ILcyCompositeCustomizerPanel;
import com.luciad.lucy.gui.customizer.ILcyCustomizerPanel;
import com.luciad.lucy.gui.customizer.ILcyCustomizerPanelFactory;
import com.luciad.lucy.gui.customizer.TLcyCompositeCustomizerPanel;
import com.luciad.lucy.gui.customizer.lightspeed.TLcyLspLayerCustomizerPanelFactory;
import com.luciad.lucy.gui.customizer.lightspeed.TLcyLspStyledLayerCustomizerPanelFactory;

/**
 * <p>Customizer panel factory for SHP point layers. It uses a regular {@link
 * TLcyLspLayerCustomizerPanelFactory} for the general
 * layer settings, and a {@link samples.lucy.lightspeed.style.newiconstyle.CustomizableIconStyleCustomizerPanelFactory}
 * for the icon customizer panel.</p>
 *
 * <p>The {@link #createCompositeCustomizerPanel(Map)} is overridden as well to combine all panels in
 * one panel.</p>
 */
class SHPCustomizerPanelFactory extends TLcyLspStyledLayerCustomizerPanelFactory {

  /**
   * Create a new customizer panel factory for SHP layers containing points
   *
   * @param aLucyEnv The Lucy back-end
   */
  public SHPCustomizerPanelFactory(ILcyLucyEnv aLucyEnv) {
    super(aLucyEnv,
          new TLcyLspLayerCustomizerPanelFactory(aLucyEnv),
          Collections.<ILcyCustomizerPanelFactory>singletonList(new CustomizableIconStyleCustomizerPanelFactory()));
  }

  @Override
  protected ILcyCompositeCustomizerPanel createCompositeCustomizerPanel(Map<ILcyCustomizerPanel, Object> aCustomizerPanelObjectMap) {
    TLcyCompositeCustomizerPanel result = new TLcyCompositeCustomizerPanel() {
      @Override
      public void addCustomizerPanel(ILcyCustomizerPanel aCustomizerPanel) {
        //do not add the panel to the UI since we want to combine all customizer panels on one panel
        super.addCustomizerPanel(aCustomizerPanel, false);
        //all customizer panels are components, so it is save to perform the cast
        add(((Component) aCustomizerPanel));
      }
    };
    result.setLayout(new BoxLayout(result, BoxLayout.Y_AXIS));
    return result;
  }
}
