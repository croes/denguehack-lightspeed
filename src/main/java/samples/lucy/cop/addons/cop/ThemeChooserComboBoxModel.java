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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;

import samples.lucy.theme.Theme;
import samples.lucy.theme.ThemeManager;
import samples.lucy.theme.ThemeManagerEvent;
import samples.lucy.theme.ThemeManagerListener;

/**
 * <p>{@code ComboBoxModel} implementation for the themes stored in a theme manager. The model
 * is kept in sync with the state of the theme manager. When the selected item changes on this
 * model, the active theme in the theme manager will be updated.</p>
 */
final class ThemeChooserComboBoxModel extends AbstractListModel implements ComboBoxModel {
  private final ThemeManager fThemeManager;
  /**
   * Store a copy of the themes as we need them in a specific order, which is not
   * provided by the theme manager
   */
  private final List<Theme> fThemes = new ArrayList<Theme>();

  ThemeChooserComboBoxModel(ThemeManager aThemeManager) {
    fThemeManager = aThemeManager;
    fThemes.addAll(fThemeManager.getThemes());
    startSynchronizationWithThemeManager();
  }

  private void startSynchronizationWithThemeManager() {
    ThemeManagerChangesListener themeManagerChangesListener = new ThemeManagerChangesListener(this, fThemeManager);
    fThemeManager.addPropertyChangeListener(themeManagerChangesListener);
    fThemeManager.addThemeManagerListener(themeManagerChangesListener);
  }

  @Override
  public int getSize() {
    return fThemes.size();
  }

  @Override
  public Object getElementAt(int index) {
    return fThemes.get(index);
  }

  @Override
  public void setSelectedItem(Object anItem) {
    fThemeManager.setActiveTheme(((Theme) anItem));
  }

  @Override
  public Object getSelectedItem() {
    return fThemeManager.getActiveTheme();
  }

  private static class ThemeManagerChangesListener implements PropertyChangeListener, ThemeManagerListener {
    private final WeakReference<ThemeChooserComboBoxModel> fComboBoxModel;
    private final ThemeManager fThemeManager;

    private ThemeManagerChangesListener(ThemeChooserComboBoxModel aComboBoxModel, ThemeManager aThemeManager) {
      fThemeManager = aThemeManager;
      fComboBoxModel = new WeakReference<ThemeChooserComboBoxModel>(aComboBoxModel);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
      ThemeChooserComboBoxModel comboBoxModel = getComboBoxModel();
      if (comboBoxModel == null) {
        return;
      }

      if ("activeTheme".equals(evt.getPropertyName())) {
        comboBoxModel.fireContentsChanged(comboBoxModel, -1, -1);
      }
    }

    @Override
    public void themeManagerChanged(ThemeManagerEvent aEvent) {
      ThemeChooserComboBoxModel comboBoxModel = getComboBoxModel();
      if (comboBoxModel == null) {
        return;
      }

      switch (aEvent.getType()) {
      case THEME_ADDED:
        comboBoxModel.fThemes.add(aEvent.getTheme());
        int index = comboBoxModel.fThemes.size() - 1;
        comboBoxModel.fireIntervalAdded(comboBoxModel, index, index);
        break;
      case THEME_REMOVED:
        index = comboBoxModel.fThemes.indexOf(aEvent.getTheme());
        comboBoxModel.fThemes.remove(aEvent.getTheme());
        comboBoxModel.fireIntervalRemoved(comboBoxModel, index, index);
        break;
      }
    }

    private ThemeChooserComboBoxModel getComboBoxModel() {
      ThemeChooserComboBoxModel result = fComboBoxModel.get();
      if (result == null) {
        fThemeManager.removePropertyChangeListener(this);
        fThemeManager.removeThemeManagerListener(this);
      }
      return result;
    }
  }
}
