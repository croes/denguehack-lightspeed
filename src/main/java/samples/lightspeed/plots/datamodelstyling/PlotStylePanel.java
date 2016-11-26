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
package samples.lightspeed.plots.datamodelstyling;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.luciad.datamodel.TLcdDataProperty;
import com.luciad.datamodel.TLcdDataType;

/**
 * This panel provides GUI elements to configure plot styling based on {@link com.luciad.datamodel.TLcdDataModel} properties:
 * <ul>
 *   <li>The plot's icon can be determined based on any property that has a {@link EnumAnnotation}</li>
 *   <li>The plot's color can be determined based on any property that has a {@link EnumAnnotation} or a {@link RangeAnnotation}</li>
 * </ul>
 */
public class PlotStylePanel extends JPanel {

  private DataTypeStyler fDataTypeStyler;
  private JComponent fColorStylingComboBox;
  private JComponent fIconStylingComboBox;
  private List<ChangeListener> fChangeListeners = new ArrayList<ChangeListener>();
  private JCheckBox fDensityCheckBox = new JCheckBox("", false);

  public PlotStylePanel() {
    super(new GridBagLayout());
    setOpaque(false);

    //Add listeners
    fDensityCheckBox.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent aActionEvent) {
        notifyChangeListeners();
      }
    });
  }

  public void addChangeListener(ChangeListener aChangeListener) {
    fChangeListeners.add(aChangeListener);
  }

  public void removeChangeListener(ChangeListener aChangeListener) {
    fChangeListeners.remove(aChangeListener);
  }

  protected void notifyChangeListeners() {
    fColorStylingComboBox.setEnabled(!fDensityCheckBox.getModel().isSelected());
    fIconStylingComboBox.setEnabled(!fDensityCheckBox.getModel().isSelected());
    fDataTypeStyler.setUseDensity(fDensityCheckBox.getModel().isSelected());
    fDataTypeStyler.setColorProperty((TLcdDataProperty) getSelectedItem(fColorStylingComboBox));
    fDataTypeStyler.setIconProperty((TLcdDataProperty) getSelectedItem(fIconStylingComboBox));

    for (ChangeListener changeListener : fChangeListeners) {
      changeListener.stateChanged(new ChangeEvent(this));
    }
  }

  /**
   * Populates the color and icon combobox with the {@link TLcdDataProperty}s that have
   * {@link EnumAnnotation} or {@link RangeAnnotation}.
   */
  public void initialize(TLcdDataType aDataType, DataTypeStyler aDataTypeStyler) {
    fDataTypeStyler = aDataTypeStyler;

    List<TLcdDataProperty> iconValues = new ArrayList<TLcdDataProperty>();
    List<TLcdDataProperty> colorValues = new ArrayList<TLcdDataProperty>();

    iconValues.add(null);
    colorValues.add(null);

    for (TLcdDataProperty property : aDataType.getProperties()) {
      if (property.isAnnotationPresent(EnumAnnotation.class)) {
        iconValues.add(property);
        colorValues.add(property);
      }
      if (property.isAnnotationPresent(RangeAnnotation.class)) {
        colorValues.add(property);
      }
    }

    DefaultListCellRenderer cellRenderer = new DefaultListCellRenderer() {
      @Override
      public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        return super.getListCellRendererComponent(list, value != null ? ((TLcdDataProperty) value).getDisplayName() : "-", index, isSelected, cellHasFocus);
      }
    };

    fColorStylingComboBox = createCombobox(colorValues.toArray(), cellRenderer, 6);
    fIconStylingComboBox = createCombobox(iconValues.toArray(), cellRenderer, 2);

    initializePanel();

    notifyChangeListeners();
  }

  protected JComponent createLabel(String aText) {
    return new JLabel(aText);
  }

  protected JComponent createCombobox(Object[] aValues, ListCellRenderer aCellRenderer, int aSelectedIndex) {
    JComboBox comboBox = new JComboBox();
    comboBox.setRenderer(aCellRenderer);
    comboBox.setModel(new DefaultComboBoxModel(aValues));
    comboBox.setSelectedIndex(aSelectedIndex);
    comboBox.addItemListener(new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent e) {
        notifyChangeListeners();
      }
    });
    return comboBox;
  }

  protected Object getSelectedItem(JComponent aComponent) {
    return ((JComboBox) aComponent).getSelectedItem();
  }

  private void initializePanel() {
    GridBagConstraints c = new GridBagConstraints();
    c.insets = new Insets(2, 5, 2, 5);
    c.weightx = 0.25;
    c.ipadx = 2;
    c.ipady = 2;
    c.gridx = 0;
    c.gridy = 0;
    c.fill = GridBagConstraints.HORIZONTAL;
    add(createLabel("Color"), c);

    c.weightx = 0.75;
    c.gridx = 1;
    c.gridy = 0;
    add(fColorStylingComboBox, c);

    c.weightx = 0.25;
    c.gridx = 0;
    c.gridy = 2;
    add(createLabel("Icon Symbol"), c);

    c.weightx = 0.75;
    c.gridx = 1;
    c.gridy = 2;
    add(fIconStylingComboBox, c);

    c.weightx = 0.25;
    c.gridx = 0;
    c.gridy = 4;
    add(createLabel("Density"), c);

    c.weightx = 0.75;
    c.gridx = 1;
    c.gridy = 4;
    add(fDensityCheckBox, c);

    revalidate();
  }

}
