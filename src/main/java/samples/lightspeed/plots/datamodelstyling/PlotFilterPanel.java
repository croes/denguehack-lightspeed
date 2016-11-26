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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.luciad.datamodel.TLcdDataProperty;
import com.luciad.datamodel.TLcdDataType;
import com.luciad.gui.swing.TLcdRangeSlider;

/**
 * This panel provides GUI elements to configure filtering based on {@link com.luciad.datamodel.TLcdDataModel} properties:
 * <ul>
 *   <li>For properties that have a {@link EnumAnnotation}, a combobox is provided</li>
 *   <li>For properties that have a {@link RangeAnnotation}, a slider is provided</li>
 * </ul>
 */
public class PlotFilterPanel extends JPanel implements ChangeListener, ActionListener {

  private final GridBagConstraints fConstraints;
  private final List<ChangeListener> fChangeListeners = new ArrayList<ChangeListener>();
  private final List<Filter> fFilters = new ArrayList<Filter>();

  private DataTypeStyler fPlotStyler;

  public PlotFilterPanel() {
    super(new GridBagLayout());
    setOpaque(false);
    fConstraints = new GridBagConstraints();
    fConstraints.insets = new Insets(2, 5, 2, 5);
    fConstraints.weightx = 0.25;
    fConstraints.ipadx = 2;
    fConstraints.ipady = 2;
    fConstraints.gridx = 0;
    fConstraints.gridy = 0;
    fConstraints.fill = GridBagConstraints.HORIZONTAL;
  }

  public void addChangeListener(ChangeListener aChangeListener) {
    fChangeListeners.add(aChangeListener);
  }

  public void removeChangeListener(ChangeListener aChangeListener) {
    fChangeListeners.remove(aChangeListener);
  }

  /**
   * Applies the changes in to GUI to the {@link DataTypeStyler}.
   */
  protected void notifyChangeListeners() {

    Collection<TLcdDataProperty> properties = new HashSet<TLcdDataProperty>();

    for (Filter filter : fFilters) {
      boolean isActive;
      if (filter.fFilter instanceof TLcdRangeSlider) {
        TLcdRangeSlider rangeSlider = (TLcdRangeSlider) filter.fFilter;
        fPlotStyler.setRangeParameters(filter.fProperty, rangeSlider.getRangeMinimum(), rangeSlider.getRangeMaximum());
        isActive = (rangeSlider.getRangeMinimum() != rangeSlider.getMinimum() || rangeSlider.getRangeMaximum() != rangeSlider.getMaximum());
      } else if (filter.fFilter instanceof JComboBox) {
        JComboBox comboBox = (JComboBox) filter.fFilter;
        fPlotStyler.setEnumParameter(filter.fProperty, comboBox.getSelectedItem());
        isActive = (comboBox.getSelectedIndex() != 0);
      } else if (filter.fFilter instanceof JList) {
        JList comboBox = (JList) filter.fFilter;
        fPlotStyler.setEnumParameter(filter.fProperty, comboBox.getSelectedValue());
        isActive = (comboBox.getSelectedIndex() != 0);
      } else if (filter.fFilter instanceof JScrollPane) {
        JList comboBox = (JList) (((JScrollPane) filter.fFilter).getViewport().getView());
        fPlotStyler.setEnumParameter(filter.fProperty, comboBox.getSelectedValue());
        isActive = (comboBox.getSelectedIndex() != 0);
      } else if (filter.fFilter instanceof JSpinner) {
        JSpinner comboBox = (JSpinner) filter.fFilter;
        fPlotStyler.setEnumParameter(filter.fProperty, comboBox.getValue());
        isActive = (!comboBox.getValue().equals("-"));
      } else {
        throw new IllegalStateException("Unknown filter: " + filter.fFilter);
      }
      if (isActive) {
        properties.add(filter.fProperty);
      }
    }

    fPlotStyler.setVisibilityProperties(properties.toArray(new TLcdDataProperty[0]));

    for (ChangeListener changeListener : fChangeListeners) {
      changeListener.stateChanged(new ChangeEvent(this));
    }
  }

  /**
   * Creates slider and combobox GUI elements for the {@link TLcdDataProperty}s in the {@link TLcdDataType}
   * based on the {@link EnumAnnotation} and {@link RangeAnnotation}.
   */
  public void initialize(TLcdDataType aDataType, DataTypeStyler aPlotStyler) {
    fPlotStyler = aPlotStyler;

    for (TLcdDataProperty property : aDataType.getProperties()) {
      if (property.isAnnotationPresent(EnumAnnotation.class)) {
        EnumAnnotation annotation = property.getAnnotation(EnumAnnotation.class);
        Object[] values = new Object[annotation.size() + 1];
        values[0] = "-";
        for (int i = 1; i < values.length; i++) {
          values[i] = annotation.get(i - 1);
        }

        addFilter(property, createCombobox(values, 0));
      } else if (property.isAnnotationPresent(RangeAnnotation.class)) {
        addFilter(property, createSlider(property.getAnnotation(RangeAnnotation.class)));
      }
    }

    notifyChangeListeners();
  }

  protected JComponent createLabel(String aText) {
    return new JLabel(aText);
  }

  protected JComponent createSlider(RangeAnnotation aAnnotation) {
    TLcdRangeSlider slider = new TLcdRangeSlider(aAnnotation.getLowerBound().doubleValue(), aAnnotation.getUpperBound().doubleValue());
    slider.addChangeListener(this);
    slider.setOpaque(false);
    return slider;
  }

  protected JComponent createCombobox(Object[] aValues, int aSelectedIndex) {
    JComboBox comboBox = new JComboBox(aValues);
    comboBox.setSelectedIndex(aSelectedIndex);
    comboBox.setOpaque(false);
    comboBox.addActionListener(this);
    return comboBox;
  }

  private void addFilter(TLcdDataProperty aProperty, Component aFilter) {
    fConstraints.weightx = 0.25;
    fConstraints.gridx = 0;
    add(createLabel(aProperty.getDisplayName()), fConstraints);

    fConstraints.gridx = 1;
    fConstraints.weightx = 0.75;
    add(aFilter, fConstraints);

    fConstraints.gridy++;

    fFilters.add(new Filter(aProperty, aFilter));
  }

  @Override
  public void stateChanged(ChangeEvent e) {
    notifyChangeListeners();
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    notifyChangeListeners();
  }

  private static class Filter {
    public TLcdDataProperty fProperty;
    public Component fFilter;

    private Filter(TLcdDataProperty aProperty, Component aFilter) {
      fProperty = aProperty;
      fFilter = aFilter;
    }
  }
}
