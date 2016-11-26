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
package samples.lucy.cop.addons.cop;

import java.awt.Component;
import java.awt.Frame;
import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JOptionPane;

import com.luciad.gui.ALcdAction;
import com.luciad.gui.ILcdAction;
import com.luciad.gui.TLcdAWTUtil;
import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.lucy.gui.ILcyActionBar;
import com.luciad.lucy.gui.ILcyCustomizableRepresentationAction;
import com.luciad.lucy.gui.ILcyToolBar;
import com.luciad.lucy.gui.TLcyAlwaysFitJToolBar;

import samples.lucy.theme.ThemeManager;

/**
 * <p>Action which allows to change the active {@code Theme}. When the action is inserted in an
 * {@code ILcyToolBar} it will shown a combobox containing all the available themes.</p>
 */
class ThemeChooserAction extends ALcdAction implements ILcyCustomizableRepresentationAction {
  private final ThemeManager fThemeManager;
  private final ILcyLucyEnv fLucyEnv;

  private final ThemeChooserComboBoxModel fComboBoxModel;

  ThemeChooserAction(ThemeManager aThemeManager, ILcyLucyEnv aLucyEnv) {
    fThemeManager = aThemeManager;
    fLucyEnv = aLucyEnv;
    fComboBoxModel = new ThemeChooserComboBoxModel(fThemeManager);
  }

  private JComponent createComponent() {
    JComboBox comboBox = new JComboBox(fComboBoxModel);
    comboBox.setRenderer(new ThemeChooserRenderer());
    comboBox.setEditable(false);
    return comboBox;
  }

  @Override
  public Component customizeRepresentation(Component aDefaultComponent, ILcdAction aWrapperAction, ILcyActionBar aActionBar) {
    if (aActionBar instanceof ILcyToolBar) {
      //set the short description of the wrapper action as the tool tip for the spinner
      Object shortDescription = aWrapperAction.getValue(Action.SHORT_DESCRIPTION);
      JComponent component = createComponent();
      component.setToolTipText((String) shortDescription);
      return TLcyAlwaysFitJToolBar.createToolBarPanel(component);
    }
    return aDefaultComponent;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    //retrieve the parent frame to show a dialog
    Frame parentFrame = TLcdAWTUtil.findParentFrame(e);
    JOptionPane.showInputDialog(parentFrame, createComponent(), "Select theme", JOptionPane.PLAIN_MESSAGE, null, new String[]{"Close"}, "Close");
  }
}
