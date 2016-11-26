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
package samples.lucy.applicationpane;

import java.awt.Component;
import java.beans.PropertyChangeEvent;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import com.luciad.lucy.gui.ALcyApplicationPaneTool;
import com.luciad.lucy.gui.TLcyTwoColumnLayoutBuilder;
import com.luciad.lucy.util.preferences.TLcyPreferencesTool;
import com.luciad.lucy.util.properties.ALcyProperties;
import com.luciad.util.ALcdWeakPropertyChangeListener;

/**
 * <p>
 *   An extension of {@code ALcyApplicationPaneTool} which add a panel containing a single combobox to the
 *   Lucy UI.
 * </p>
 *
 * <p>
 *   The combobox allows to choose a value from {@link ComboBoxValues}, and the selected value is stored in the
 *   workspace.
 *   This is achieved by keeping the selection of the combobox in sync with a key-value pair in the
 *   {@link TLcyPreferencesTool#getWorkspacePreferences() workspace preferences} of the {@link com.luciad.lucy.addons.ALcyPreferencesAddOn}.
 * </p>
 *
 * <p>
 *   Consult the Lucy Fundamentals developer guide for more information about this sample.
 * </p>
 */
final class CustomApplicationPaneTool extends ALcyApplicationPaneTool {

  private static final String SELECTED_VALUE_KEY = "selectedValue";

  private final ALcyProperties fWorkspacePreferences;

  CustomApplicationPaneTool(ShowApplicationPaneAddOn aShowApplicationPaneAddOn) {
    super(aShowApplicationPaneAddOn.getPreferences(), aShowApplicationPaneAddOn.getLongPrefix(), aShowApplicationPaneAddOn.getShortPrefix());
    //We use the workspace preferences to store the selected value, ensuring that the value is stored in the workspace
    fWorkspacePreferences = aShowApplicationPaneAddOn.getPreferencesTool().getWorkspacePreferences();
  }

  @Override
  protected Component createContent() {
    JPanel contents = new JPanel();
    TLcyTwoColumnLayoutBuilder.newBuilder()
                              .addTitledSeparator("Custom UI")
                              .row()
                              .columnOne(new JLabel("Select a value"), createComboBox())
                              .build()
                              .populate(contents);
    return contents;
  }

  /**
   * <p>
   *   This method creates a combo box, where the selected value of the combo box
   *   is stored in the preferences.
   * </p>
   * <p>
   *   The returned combo box will also ensure that the selected value in the combo box
   *   and in the preferences remain in sync:
   * </p>
   * <ul>
   *   <li>
   *     When the selected value in the preferences changes, the combo box will update its selected value to match.
   *   </li>
   *   <li>
   *     When the user alters the selected value in the combo box in the UI, the preferences will be updated to match this
   *     state.
   *   </li>
   * </ul>
   *
   * @return the combo box
   */
  private JComboBox<ComboBoxValues> createComboBox() {
    ComboBoxModel<ComboBoxValues> model = createPreferencesBackedComboBoxModel();
    return new JComboBox<>(model);
  }

  private ComboBoxModel<ComboBoxValues> createPreferencesBackedComboBoxModel() {
    final DefaultComboBoxModel<ComboBoxValues> comboBoxModel = new DefaultComboBoxModel<>(ComboBoxValues.values());
    final String propertyKey = getShortPrefix() + SELECTED_VALUE_KEY;
    updateSelectedValueFromPreferences(comboBoxModel, fWorkspacePreferences, propertyKey);
    fWorkspacePreferences.addPropertyChangeListener(new UpdateComboFromPreferencesListener(comboBoxModel, propertyKey, fWorkspacePreferences));

    comboBoxModel.addListDataListener(new ListDataListener() {
      @Override
      public void intervalAdded(ListDataEvent e) {
        //do nothing
      }

      @Override
      public void intervalRemoved(ListDataEvent e) {
        //do nothing
      }

      @Override
      public void contentsChanged(ListDataEvent e) {
        //this method is called when the selection is changed in the combobox
        fWorkspacePreferences.putString(propertyKey, comboBoxModel.getSelectedItem().toString());
      }
    });
    return comboBoxModel;
  }

  private static void updateSelectedValueFromPreferences(ComboBoxModel<ComboBoxValues> aComboBoxModel, ALcyProperties aProperties, String aPropertyKey) {
    String defaultValue = ComboBoxValues.FIRST.toString();
    String selectedAsString = aProperties.getString(aPropertyKey, defaultValue);
    aComboBoxModel.setSelectedItem(ComboBoxValues.valueOf(selectedAsString));
  }

  private static class UpdateComboFromPreferencesListener extends
                                                          ALcdWeakPropertyChangeListener<ComboBoxModel<ComboBoxValues>> {

    private final String fPropertyKey;
    private final ALcyProperties fProperties;

    private UpdateComboFromPreferencesListener(ComboBoxModel<ComboBoxValues> aObjectToModify, String aPropertyKey, ALcyProperties aProperties) {
      super(aObjectToModify);
      fPropertyKey = aPropertyKey;
      fProperties = aProperties;
    }

    @Override
    protected void propertyChangeImpl(ComboBoxModel<ComboBoxValues> aComboBoxModel, PropertyChangeEvent aPropertyChangeEvent) {
      if (fPropertyKey.equals(aPropertyChangeEvent.getPropertyName())) {
        updateSelectedValueFromPreferences(aComboBoxModel, fProperties, fPropertyKey);
      }
    }

  }

  /**
   * Enumeration of the possible values in the combo box
   */
  enum ComboBoxValues {
    FIRST,
    SECOND,
    THIRD
  }
}
