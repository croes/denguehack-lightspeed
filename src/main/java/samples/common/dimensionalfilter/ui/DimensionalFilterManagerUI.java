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
package samples.common.dimensionalfilter.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.Date;
import java.util.List;

import javax.swing.Box;
import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import com.luciad.gui.swing.TLcdOverlayLayout;
import samples.common.dimensionalfilter.model.DimensionalFilterGroup;
import samples.common.dimensionalfilter.model.DimensionalFilterManager;
import com.luciad.util.ILcdStringTranslator;
import com.luciad.util.iso19103.ILcdISO19103Measure;
import com.luciad.util.measure.TLcdMeasureFormatUtil;

/**
 * A slider-based GUI component to edit one of the {@link samples.common.dimensionalfilter.model.DimensionalFilterGroup}
 * of the {@link samples.common.dimensionalfilter.model.DimensionalFilterManager}.
 *
 * @since 2015.0
 */
public class DimensionalFilterManagerUI extends JPanel {

  private DimensionalFilterManager fDimensionalFilterManager;
  private JComponent fOverlayPanel;

  private final DimensionalFilterComboBoxModel fComboBoxModel;
  private final DimensionalFilterBoundedRangeModel fSliderModel;

  //model for time filters
  private DimensionalFilterBoundedRangeModel fTimeSliderModel;
  //current filterGroup for time
  private DimensionalFilterGroup fCurrentTimeFilterGroup;

  private final DimensionalFilterSliderPanel fVerticalFilterPanel;
  private final DimensionalFilterSliderPanel fTimeFilterPanel;

  private static NumberFormat sNUMBER_FORMAT = NumberFormat.getInstance();
  private static DateFormat sDATE_FORMAT = DateFormat.getDateInstance();

  static {
    //same as TLcdMeasureFormatUtil
    sNUMBER_FORMAT.setMinimumFractionDigits(3);
    sNUMBER_FORMAT.setMaximumFractionDigits(3);
  }

  /**
   * Create a new instance.
   */
  public DimensionalFilterManagerUI(DimensionalFilterManager aDimensionalFilterManager, JComponent aOverlayPanel) {
    setOpaque(false);
    setLayout(new GridBagLayout());
    setMinimumSize(new Dimension(80, 200));

    fDimensionalFilterManager = aDimensionalFilterManager;
    fOverlayPanel = aOverlayPanel;

    //create combobox model with all filtergroups in the service
    //comboboxmodel will only use non-time filter models
    fComboBoxModel = new DimensionalFilterComboBoxModel(aDimensionalFilterManager);
    fSliderModel = new DimensionalFilterBoundedRangeModel(aDimensionalFilterManager, fComboBoxModel.getSelectedItem());

    //get time filter model from filter service
    fCurrentTimeFilterGroup = getCurrentTimeFilterGroup();
    fTimeSliderModel = new DimensionalFilterBoundedRangeModel(aDimensionalFilterManager, fCurrentTimeFilterGroup);

    final JComboBox<DimensionalFilterGroup> modelComboBox = new JComboBox<>(fComboBoxModel);
    fComboBoxModel.addListDataListener(new InitialSelectionHandler(fComboBoxModel));
    fComboBoxModel.addListDataListener(new ListDataListener() {
      @Override
      public void intervalAdded(ListDataEvent e) {
        updateComboBoxState();
      }

      @Override
      public void intervalRemoved(ListDataEvent e) {
        updateComboBoxState();
      }

      @Override
      public void contentsChanged(ListDataEvent e) {
        updateComboBoxState();
      }

      private void updateComboBoxState() {
        SwingUtilities.invokeLater(new Runnable() {
          @Override
          public void run() {
            modelComboBox.setVisible(fComboBoxModel.getSize() > 0);
            DimensionalFilterGroup selectedItem = fComboBoxModel.getSelectedItem();
            fSliderModel.setFilterGroup(selectedItem);
            fVerticalFilterPanel.setVisible(selectedItem != null);
            if (selectedItem != null) {
              fVerticalFilterPanel.getSlider().setInverted(!selectedItem.isPositive());
            }

            //update time filter model as well since modelcombobox is affected
            //from all the changes
            fCurrentTimeFilterGroup = getCurrentTimeFilterGroup();
            fTimeSliderModel.setFilterGroup(fCurrentTimeFilterGroup);
            fTimeFilterPanel.setVisible(fCurrentTimeFilterGroup != null);
          }
        });
      }
    });

    fSliderModel.addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent e) {
        updateVerticalLabels();
      }
    });

    fTimeSliderModel.addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent e) {
        if (fCurrentTimeFilterGroup != null) {
          fTimeFilterPanel.getMinLabel().setText(convertToString(fDimensionalFilterManager.getMinValue(fCurrentTimeFilterGroup)));
          fTimeFilterPanel.getMinLabel().repaint();
          fTimeFilterPanel.getMaxLabel().setText(convertToString(fDimensionalFilterManager.getMaxValue(fCurrentTimeFilterGroup)));
          fTimeFilterPanel.getMaxLabel().repaint();
        }
      }
    });

    fVerticalFilterPanel = new DimensionalFilterSliderPanel(JSlider.VERTICAL, fSliderModel, fDimensionalFilterManager);
    //a group presents initially with Lucy
    DimensionalFilterGroup selectedInitialGroup = fComboBoxModel.getSelectedItem();
    if (null != selectedInitialGroup) {
      fVerticalFilterPanel.getSlider().setInverted(!selectedInitialGroup.isPositive());
    }
    fTimeFilterPanel = new DimensionalFilterSliderPanel(JSlider.HORIZONTAL, fTimeSliderModel, fDimensionalFilterManager);
    fTimeFilterPanel.setVisible(false);

    GridBagConstraints constraints = new GridBagConstraints();
    constraints.gridx = 0;
    constraints.weightx = 1;
    constraints.gridy = GridBagConstraints.RELATIVE;
    constraints.fill = GridBagConstraints.NONE;
    constraints.anchor = GridBagConstraints.WEST;
    add(modelComboBox, constraints);
    add(Box.createVerticalStrut(5), constraints);
    add(fVerticalFilterPanel, constraints);

    //timefilter slider will be displayed on north center
    //of the panel, so the parent is aOverlayPanel iso this panel.
    //everytime this panel is added/removed to/from aOverlayPanel,
    //we should add/remove timefilter too
    aOverlayPanel.addContainerListener(new ContainerListener() {
      @Override
      public void componentAdded(ContainerEvent e) {
        if (e.getChild() == DimensionalFilterManagerUI.this) {
          fOverlayPanel.add(fTimeFilterPanel, TLcdOverlayLayout.Location.NORTH);
          fOverlayPanel.add(fTimeFilterPanel.getToolTip(), TLcdOverlayLayout.Location.NO_LAYOUT);
          fOverlayPanel.add(fVerticalFilterPanel.getToolTip(), TLcdOverlayLayout.Location.NO_LAYOUT);
          fOverlayPanel.setComponentZOrder(fTimeFilterPanel.getToolTip(), 0);
          fOverlayPanel.setComponentZOrder(fVerticalFilterPanel.getToolTip(), 0);
        }
      }

      @Override
      public void componentRemoved(ContainerEvent e) {
        if (e.getChild() == DimensionalFilterManagerUI.this) {
          fOverlayPanel.remove(fTimeFilterPanel);
          fOverlayPanel.remove(fTimeFilterPanel.getToolTip());
          fOverlayPanel.remove(fVerticalFilterPanel.getToolTip());
        }
      }
    });

    updateVerticalLabels();
  }

  private void updateVerticalLabels() {
    DimensionalFilterGroup selectedItem = fComboBoxModel.getSelectedItem();
    boolean positive = selectedItem != null && selectedItem.isPositive();
    fVerticalFilterPanel.getMinLabel().setText(convertToString(positive ? fDimensionalFilterManager.getMinValue(selectedItem) : fDimensionalFilterManager.getMaxValue(selectedItem)));
    fVerticalFilterPanel.getMinLabel().repaint();
    fVerticalFilterPanel.getMaxLabel().setText(convertToString(positive ? fDimensionalFilterManager.getMaxValue(selectedItem) : fDimensionalFilterManager.getMinValue(selectedItem)));
    fVerticalFilterPanel.getMaxLabel().repaint();
  }

  /**
   * Gets the timefiltergroup from filtermanager
   * @return current timefiltergroup
   */
  private DimensionalFilterGroup getCurrentTimeFilterGroup() {
    List<DimensionalFilterGroup> dimensionalFilterGroups = fDimensionalFilterManager.getFilterGroups();
    for (DimensionalFilterGroup dimensionalFilterGroup : dimensionalFilterGroups) {
      if (Date.class.isAssignableFrom(dimensionalFilterGroup.getType())) {
        return dimensionalFilterGroup;
      }
    }
    return null;
  }

  public void destroy() {
    fComboBoxModel.destroy();
    fSliderModel.destroy();
    fTimeSliderModel.destroy();
  }

  /**
   * Set the color used to paint the halo of the labels.
   *
   * @param aColor the color used to paint the halo of the labels.
   */
  public void setLabelHaloColor(Color aColor) {
    fVerticalFilterPanel.getMinLabel().setHaloColor(aColor);
    fVerticalFilterPanel.getMaxLabel().setHaloColor(aColor);
  }

  /**
   * Set the color used to paint the text of the labels.
   *
   * @param aColor the color used to paint the text of the labels.
   */
  public void setLabelTextColor(Color aColor) {
    fVerticalFilterPanel.getMinLabel().setTextColor(aColor);
    fVerticalFilterPanel.getMaxLabel().setTextColor(aColor);
  }

  static String convertToString(Object aSubject) {
    if (aSubject == null) {
      return "";
    } else if (aSubject instanceof ILcdISO19103Measure) {
      ILcdISO19103Measure measure = (ILcdISO19103Measure) aSubject;
      return TLcdMeasureFormatUtil.formatMeasures(new ILcdISO19103Measure[]{measure}, TLcdMeasureFormatUtil.MeasureTypeMode.NEVER).get(0);
    } else if (Date.class.isAssignableFrom(aSubject.getClass())) {
      return sDATE_FORMAT.format(aSubject);
    } else if (Number.class.isAssignableFrom(aSubject.getClass())) {
      return sNUMBER_FORMAT.format(aSubject);
    } else {
      return aSubject.toString();
    }
  }

  /**
   * Returns the dimensional filter manager.
   *
   * @return the dimensional filter manager
   */
  public DimensionalFilterManager getDimensionalFilterManager() {
    return fDimensionalFilterManager;
  }

  DimensionalFilterComboBoxModel getComboBoxModel() {
    return fComboBoxModel;
  }

  DimensionalFilterBoundedRangeModel getSliderModel() {
    return fSliderModel;
  }

  DimensionalFilterBoundedRangeModel getTimeSliderModel() {
    return fTimeSliderModel;
  }

  public void setStringTranslator(ILcdStringTranslator aStringTranslator) {
    fVerticalFilterPanel.setStringTranslator(aStringTranslator);
    fTimeFilterPanel.setStringTranslator(aStringTranslator);
  }

  DimensionalFilterSliderPanel getVerticalFilterPanel() {
    return fVerticalFilterPanel;
  }

  DimensionalFilterSliderPanel getTimeFilterPanel() {
    return fTimeFilterPanel;
  }

  public void setVerticalSliderInsets(String aVerticalSliderInsets) {
    String[] values = aVerticalSliderInsets.split(",");
    if (values.length != 4) {
      throw new IllegalArgumentException("Illegal input format for insets. " + aVerticalSliderInsets);
    }
    Insets insets = new Insets(Integer.parseInt(values[0]), Integer.parseInt(values[1]), Integer.parseInt(values[2]), Integer.parseInt(values[3]));
    fVerticalFilterPanel.setSliderInsets(insets);
  }

  /**
   * A listener that update the combox box selection when the combobox list changes.
   */
  private static class InitialSelectionHandler implements ListDataListener {
    private final ComboBoxModel<DimensionalFilterGroup> fComboBoxAdapter;

    public InitialSelectionHandler(ComboBoxModel<DimensionalFilterGroup> aComboBoxAdapter) {
      fComboBoxAdapter = aComboBoxAdapter;
    }

    @Override
    public void intervalAdded(ListDataEvent e) {
      maybeUpdateSelection();
    }

    @Override
    public void intervalRemoved(ListDataEvent e) {
      // do nothing.
    }

    @Override
    public void contentsChanged(ListDataEvent e) {
      maybeUpdateSelection();
    }

    private void maybeUpdateSelection() {
      Object selectedItem = fComboBoxAdapter.getSelectedItem();
      int size = fComboBoxAdapter.getSize();
      if (selectedItem == null && size > 0) {
        fComboBoxAdapter.setSelectedItem(fComboBoxAdapter.getElementAt(0));
      }
    }
  }
}
