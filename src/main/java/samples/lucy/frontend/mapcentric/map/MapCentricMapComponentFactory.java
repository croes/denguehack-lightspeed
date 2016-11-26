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
package samples.lucy.frontend.mapcentric.map;

import java.awt.Component;
import java.awt.Container;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;

import com.luciad.gui.TLcdImageIcon;
import com.luciad.gui.swing.TLcdSWIcon;
import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.lucy.gui.ILcyToolBar;
import com.luciad.lucy.gui.TLcyGroupDescriptor;
import com.luciad.lucy.gui.TLcyToolBar;
import com.luciad.lucy.map.lightspeed.ILcyLspMapComponent;
import com.luciad.lucy.map.lightspeed.TLcyLspMapComponentFactory;
import com.luciad.lucy.util.properties.ALcyProperties;

/**
 * <p>
 *   Custom extension of the regular {@code TLcyLspMapComponentFactory}
 * </p>
 */
public class MapCentricMapComponentFactory extends TLcyLspMapComponentFactory {

  public MapCentricMapComponentFactory(ILcyLucyEnv aLucyEnv) {
    super(aLucyEnv);
  }

  @Override
  protected ILcyLspMapComponent createGUIContent(ALcyProperties aProperties) {
    ILcyLspMapComponent guiContent = super.createGUIContent(aProperties);
    addLogoComponent(aProperties);
    return guiContent;
  }

  private void addLogoComponent(ALcyProperties aProperties) {
    //instead of adding the logo on the map, add it in the right toolbar
    ILcyToolBar rightToolBar = getToolBar(RIGHT_TOOL_BAR);
    if (rightToolBar != null) {
      String logoPath = aProperties.getString("TLcyLspMapAddOn.logo.imageFileName", null);
      if (logoPath != null) {
        TLcdImageIcon imageIcon = new TLcdImageIcon(logoPath);
        JLabel label = new JLabel(new TLcdSWIcon(imageIcon));
        label.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
        rightToolBar.insertComponent(label, new TLcyGroupDescriptor("LogoGroup"));
      }
    }

  }

  @Override
  protected ILcyToolBar createToolBar(int aToolBarID, ALcyProperties aProperties) {
    if (aToolBarID == TOOL_BAR) {
      TLcyToolBar toolBar = new TLcyToolBar() {
        @Override
        protected Container createItemContainer(String[] aMenus, TLcyGroupDescriptor[] aMenuGroupDescriptors) {
          Container itemContainer = super.createItemContainer(aMenus, aMenuGroupDescriptors);
          if (aMenus.length == 1 && itemContainer instanceof JComponent) {
            // Provide extra margin around the top-level menu's
            ((JComponent) itemContainer).setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
          }
          return itemContainer;
        }
      };
      toolBar.setAutoHide(true);
      return toolBar;
    }
    return super.createToolBar(aToolBarID, aProperties);
  }

  @Override
  protected Component createComponent(int aComponentID, ALcyProperties aProperties) {
    if (aComponentID == LOGO_ICON_COMPONENT) {
      //remove the on-map logo
      return null;
    }
    return super.createComponent(aComponentID, aProperties);
  }
}
