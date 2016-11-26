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
package samples.lucy.gxy.tea;

import static samples.lucy.gxy.tea.LOSCoverageBackEnd.PROPAGATION_CUSTOM_ID;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.lucy.addons.tea.loscoverage.TLcyLOSCoverageAddOn;
import com.luciad.lucy.addons.tea.loscoverage.TLcyLOSCoverageGUIFactory;
import com.luciad.lucy.util.properties.ALcyProperties;

/**
 * Extension of TLcyLOSCoverageGUIFactory, replacing the style panel.
 */
public class LOSCoverageGUIFactory extends TLcyLOSCoverageGUIFactory {

  public LOSCoverageGUIFactory(ILcyLucyEnv aLucyEnv) {
    super(aLucyEnv);
  }

  @Override
  protected Component createPanel(int aID, ALcyProperties aProperties) {
    if (aID == STYLE_PANEL) {
      return createStylePanel(super.createPanel(aID, aProperties), aProperties);
    }
    return super.createPanel(aID, aProperties);
  }

  private JPanel createStylePanel(Component aOriginalPanel, ALcyProperties aProperties) {
    final CardLayout layout = new CardLayout();
    final JPanel stylePanel = new JPanel(layout);

    stylePanel.add(createCustomPropagationStylePanel(), "customPropagation");
    stylePanel.add(aOriginalPanel, "default");

    //switch to the correct panel when the selected propagation function changes
    String propagationID = aProperties.getString(TLcyLOSCoverageAddOn.SELECTED_PROPAGATION_KEY, null);
    updateStylePanel(layout, stylePanel, propagationID);
    aProperties.addPropertyChangeListener(TLcyLOSCoverageAddOn.SELECTED_PROPAGATION_KEY, new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        updateStylePanel(layout, stylePanel, ((String) evt.getNewValue()));
      }
    });
    return stylePanel;
  }

  private void updateStylePanel(CardLayout aLayout, JPanel aStylePanel, String aPropagationID) {
    aLayout.show(aStylePanel, PROPAGATION_CUSTOM_ID.equals(aPropagationID) ? "customPropagation" : "default");
  }

  private JPanel createCustomPropagationStylePanel() {
    JPanel style_panel = new JPanel(new BorderLayout());
    style_panel.add(BorderLayout.NORTH, new JCheckBox("Custom style setting"));
    return style_panel;
  }

}
