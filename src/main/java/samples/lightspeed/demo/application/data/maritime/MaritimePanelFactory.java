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
package samples.lightspeed.demo.application.data.maritime;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luciad.format.s52.ILcdS52Symbology;
import com.luciad.format.s52.TLcdS52DisplaySettings;
import com.luciad.format.s52.TLcdS52Style;
import com.luciad.gui.TLcdAWTUtil;
import com.luciad.gui.swing.TLcdRangeSlider;
import com.luciad.util.ILcdFunction;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.ILspStyledLayer;
import com.luciad.view.lightspeed.layer.TLspPaintRepresentationState;

import samples.common.HaloLabel;
import samples.lightspeed.demo.application.gui.PlotPanelFactory;
import samples.lightspeed.demo.framework.application.Framework;
import samples.lightspeed.demo.framework.data.themes.AbstractTheme;
import samples.lightspeed.demo.framework.gui.DemoUIColors;
import samples.lightspeed.demo.framework.gui.RoundedBorder;

/**
 * Panel factory for the ExactEarth ExactAIS data set.
 */
public class MaritimePanelFactory extends PlotPanelFactory {
  private static final NumberFormat SAFETY_DEPTH_FORMAT;

  static {
    SAFETY_DEPTH_FORMAT = NumberFormat.getInstance();
    SAFETY_DEPTH_FORMAT.setMaximumFractionDigits(2);
    SAFETY_DEPTH_FORMAT.setMinimumFractionDigits(2);
  }

  private final SafetyDepthPropertyChangeListener fSafetyDepthPropertyChangeListener = new SafetyDepthPropertyChangeListener();

  @Override
  public List<JPanel> createThemePanels(AbstractTheme aTheme) {
    return Collections.singletonList(createFilterPanel((MaritimeTheme) aTheme));
  }

  private JPanel createFilterPanel(final MaritimeTheme aTheme) {
    DefaultFormBuilder totalBuilder = new DefaultFormBuilder(new FormLayout("p:grow"));
    totalBuilder.border(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    if (!aTheme.getAISLayers().isEmpty()) {

      totalBuilder.append(new HaloLabel("Vessel Settings", 15, true));
      totalBuilder.nextLine();

      DefaultFormBuilder vesselBuilder = createSubPanelBuilder(1, 1);
      // Time-based filtering
      final ARangeModel timeRangeModel = new ARangeModel(null) {
        @Override
        public double getMinimum() {
          long tmin = Long.MAX_VALUE;
          for (ILspLayer layer : aTheme.getAISLayers()) {
            ExactAISStyler styler = (ExactAISStyler) ((ILspStyledLayer) layer).getStyler(TLspPaintRepresentationState.REGULAR_BODY);
            tmin = Math.min(tmin, styler.getMinTime());
          }
          return tmin;
        }

        @Override
        public double getMaximum() {
          long tmax = Long.MIN_VALUE;
          for (ILspLayer layer : aTheme.getAISLayers()) {
            ExactAISStyler styler = (ExactAISStyler) ((ILspStyledLayer) layer).getStyler(TLspPaintRepresentationState.REGULAR_BODY);
            tmax = Math.max(tmax, styler.getMaxTime());
          }
          return tmax;
        }

        @Override
        public double getRangeMinimum() {
          double tStart = Long.MAX_VALUE;
          for (ILspLayer layer : aTheme.getAISLayers()) {
            ExactAISStyler styler = (ExactAISStyler) ((ILspStyledLayer) layer).getStyler(TLspPaintRepresentationState.REGULAR_BODY);
            tStart = Math.min(tStart, styler.getTimeRangeMin());
          }
          return tStart;
        }

        @Override
        public double getRangeMaximum() {
          double tEnd = Long.MIN_VALUE;
          for (ILspLayer layer : aTheme.getAISLayers()) {
            ExactAISStyler styler = (ExactAISStyler) ((ILspStyledLayer) layer).getStyler(TLspPaintRepresentationState.REGULAR_BODY);
            tEnd = Math.max(tEnd, styler.getTimeRangeMax());
          }
          return tEnd;
        }

        @Override
        protected void doSetRange(double aStart, double aEnd) {
          for (ILspLayer layer : aTheme.getAISLayers()) {
            ExactAISStyler styler = (ExactAISStyler) ((ILspStyledLayer) layer).getStyler(TLspPaintRepresentationState.REGULAR_BODY);
            styler.setTimeRange(aStart, aEnd);
          }
        }
      };
      for (ILspLayer layer : aTheme.getAISLayers()) {
        final ExactAISStyler styler = (ExactAISStyler) ((ILspStyledLayer) layer).getStyler(TLspPaintRepresentationState.REGULAR_BODY);
        styler.addPropertyChangeListener(new PropertyChangeListener() {
          @Override
          public void propertyChange(final PropertyChangeEvent evt) {
            TLcdAWTUtil.invokeLater(new Runnable() {
              @Override
              public void run() {
                timeRangeModel.fireChanged();
              }
            });
          }
        });
      }

      long tmin = Long.MAX_VALUE;
      long tmax = Long.MIN_VALUE;
      for (ILspLayer layer : aTheme.getAISLayers()) {
        ExactAISStyler styler = (ExactAISStyler) ((ILspStyledLayer) layer).getStyler(TLspPaintRepresentationState.REGULAR_BODY);
        tmin = Math.min(tmin, styler.getMinTime());
        tmax = Math.max(tmax, styler.getMaxTime());
      }

      final TLcdRangeSlider timeRangeSlider = (TLcdRangeSlider) createRangeSlider(timeRangeModel);
      timeRangeSlider.setRange(0, (tmax - tmin));
      addComponent("Time filter", timeRangeSlider, vesselBuilder);

      final JSlider speed = new JSlider(0, 300, 0);

      final Timer timer = new Timer(50, new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          double incrementMS = 360000.0 * speed.getValue();
          // Compensate for the timer interval
          double increment = incrementMS / 20.0;
          double min = timeRangeSlider.getRangeMinimum() + increment;
          double max = timeRangeSlider.getRangeMaximum() + increment;
          if (max > timeRangeSlider.getMaximum()) {
            max -= min;
            min = 0;
          }
          timeRangeSlider.setRange(min, max);
        }
      });

      speed.addChangeListener(new ChangeListener() {
        @Override
        public void stateChanged(ChangeEvent e) {
          int v = speed.getValue();
          if (v == 0) {
            timer.stop();
          } else {
            timer.start();
          }
        }
      });
      addComponent("Speed", speed, vesselBuilder);
      final JCheckBox paintDensityCheckBox = new JCheckBox();
      ExactAISStyler styler = aTheme.getLayerFactory().getStyler();
      if (styler != null) {
        paintDensityCheckBox.setSelected(styler.isPaintDensity());
      }
      paintDensityCheckBox.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent aActionEvent) {
          ExactAISStyler styler = aTheme.getLayerFactory().getStyler();
          if (styler == null) {
            return;
          }
          styler.setPaintDensity(paintDensityCheckBox.getModel().isSelected());
        }
      });
      addComponent("Paint Density", paintDensityCheckBox, vesselBuilder);

      final JCheckBox oceanDepthStylerCheckBox = new JCheckBox();
      if (styler != null) {
        oceanDepthStylerCheckBox.setSelected(styler.isStyleBasedOnOceanDepth());
      }
      oceanDepthStylerCheckBox.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent aActionEvent) {
          ExactAISStyler styler = aTheme.getLayerFactory().getStyler();
          if (styler == null) {
            return;
          }
          styler.setStyleBasedOnOceanDepth(oceanDepthStylerCheckBox.getModel().isSelected());
        }
      });
      addComponent("Validate Dataset", oceanDepthStylerCheckBox, vesselBuilder);

      JPanel vesselPanel = vesselBuilder.getPanel();
      vesselPanel.setOpaque(false);
      totalBuilder.append(vesselPanel);
    }

    // ECDIS

    if (!aTheme.getNOAALayers().isEmpty()) {

      final TLcdS52DisplaySettings displaySettings = ECDISConfigurationProvider.getS52DisplaySettings();
      displaySettings.removePropertyChangeListener(fSafetyDepthPropertyChangeListener);
      displaySettings.addPropertyChangeListener(fSafetyDepthPropertyChangeListener);

      totalBuilder.append(new HaloLabel("S52 Settings", 15, true));
      totalBuilder.nextLine();

      List<Integer> colorTypes = Arrays.asList(ILcdS52Symbology.DAY_BRIGHT_COLORS, ILcdS52Symbology.DUSK_COLORS, ILcdS52Symbology.NIGHT_COLORS);
      List<String> colorTypesStrings = Arrays.asList("Day", "Dusk", "Night");
      addRadioButtonsForEnumeration(totalBuilder,
                                    colorTypes,
                                    colorTypesStrings,
                                    new ILcdFunction() {
                                      @Override
                                      public boolean applyOn(Object aObject) throws IllegalArgumentException {
                                        displaySettings.setColorType((Integer) aObject);
                                        return true;
                                      }
                                    },
                                    colorTypes.indexOf(displaySettings.getColorType()),
                                    "Color type"
      );

      List<Integer> displayCategories = Arrays.asList(TLcdS52Style.DISPLAYBASE, TLcdS52Style.STANDARD, TLcdS52Style.OTHER);
      List<String> displayCategoriesStrings = Arrays.asList("Display Base", "Standard", "Other");
      addRadioButtonsForEnumeration(totalBuilder,
                                    displayCategories,
                                    displayCategoriesStrings,
                                    new ILcdFunction() {
                                      @Override
                                      public boolean applyOn(Object aObject) throws IllegalArgumentException {
                                        displaySettings.setDisplayCategory(((Integer) aObject));
                                        return true;
                                      }
                                    },
                                    displayCategories.indexOf(displaySettings.getDisplayCategory()),
                                    "Display category"
      );

      addLeftAlignedTitle(totalBuilder, "Options");
      DefaultFormBuilder filtersBuilder = createSubPanelBuilder(1, 1);

      final JCheckBox masklandareasCheckBox = new JCheckBox();
      masklandareasCheckBox.setSelected(!displaySettings.isDisplayLandAreas());
      masklandareasCheckBox.addItemListener(new ItemListener() {
        @Override
        public void itemStateChanged(ItemEvent e) {
          boolean display = !masklandareasCheckBox.isSelected();
          aTheme.setDisplayLandAreas(display);
        }
      });
      filtersBuilder.append(masklandareasCheckBox);
      filtersBuilder.append(new HaloLabel("Mask Land Areas"));
      filtersBuilder.nextLine();

      final JCheckBox soundingsCheckBox = new JCheckBox();
      soundingsCheckBox.setSelected(displaySettings.isDisplaySoundings());
      soundingsCheckBox.addItemListener(new ItemListener() {
        @Override
        public void itemStateChanged(ItemEvent e) {
          displaySettings.setDisplaySoundings(soundingsCheckBox.isSelected());
        }
      });
      filtersBuilder.append(soundingsCheckBox);
      filtersBuilder.append(new HaloLabel("Soundings"));
      filtersBuilder.nextLine();

      final JCheckBox overscaleCheckBox = new JCheckBox();
      overscaleCheckBox.setSelected(displaySettings.isDisplaySoundings());
      overscaleCheckBox.addItemListener(new ItemListener() {
        @Override
        public void itemStateChanged(ItemEvent e) {
          displaySettings.setDisplayOverscaleIndication(overscaleCheckBox.isSelected());
        }
      });
      filtersBuilder.append(overscaleCheckBox);
      filtersBuilder.append(new HaloLabel("Overscale"));
      filtersBuilder.nextLine();

      JPanel filtersPanel = filtersBuilder.getPanel();
      filtersPanel.setOpaque(false);

      totalBuilder.append(filtersPanel);
      totalBuilder.nextLine();

      addLeftAlignedTitle(totalBuilder, "Safety Depth");
      DefaultFormBuilder depthSettingsBuilder = createSubPanelBuilder(1, 1);

      JPanel safetyDepthPanel = new JPanel();
      safetyDepthPanel.setOpaque(false);
      final JLabel depthLabel = new HaloLabel("", 12, true);
      final JSlider depthSlider = new JSlider(2, 50);
      depthSlider.addChangeListener(new ChangeListener() {
        @Override
        public void stateChanged(ChangeEvent e) {
          int safetyDepth = depthSlider.getValue();
          int currentSafetyDepth = (int) ECDISConfigurationProvider.getS52DisplaySettings().getSafetyDepth();

          if (Math.abs(safetyDepth - currentSafetyDepth) < 5) {
            return;
          }

          MaritimeTheme.updateSafetyDepth(safetyDepth);
        }
      });
      safetyDepthPanel.add(depthSlider);
      safetyDepthPanel.add(depthLabel);
      fSafetyDepthPropertyChangeListener.setSafetyDepthSlider(depthSlider, depthLabel);

      depthSettingsBuilder.append(safetyDepthPanel, 3);

      JPanel depthSettingsPanel = depthSettingsBuilder.getPanel();
      depthSettingsPanel.setOpaque(false);

      totalBuilder.append(depthSettingsPanel);
      totalBuilder.nextLine();

    }

    return getPanel(totalBuilder);
  }

  /**
   * Adds buttons to the builder for each value in {@code aValues} with description {@code
   * aStringRepresentations}. When the
   * selected button changes the {@code aCallBackFunctionOnSelectionChange} is called with the new
   * value retrieved from {@code aValues}.
   *
   * @param aBuilder               The builder
   * @param aValues                The values. For each value a button will be created
   * @param aStringRepresentations Matching descriptions for each of the values. List should have
   *                               the same length as {@code aValues},
   *                               and the indices should match
   * @param aCallBackFunctionOnSelectionChange
   *                               When another button is selected, this function will be called.
   *                               The parameter passed to the
   *                               function is the value from {@code aValues} corresponding to the
   *                               new selected button
   * @param aDefaultSelectedIndex  The default selected index (index in {@code aValues}
   * @param aTitle                 The title for the panel
   */
  private <T> void addRadioButtonsForEnumeration(DefaultFormBuilder aBuilder,
                                                 List<T> aValues,
                                                 List<String> aStringRepresentations,
                                                 final ILcdFunction aCallBackFunctionOnSelectionChange,
                                                 int aDefaultSelectedIndex,
                                                 String aTitle) {
    boolean isTouchUI = Boolean.parseBoolean(Framework.getInstance().getProperty("ui.touch", "false"));

    addLeftAlignedTitle(aBuilder, aTitle);

    DefaultFormBuilder subPanelBuilder = createSubPanelBuilder(isTouchUI ? 1 : aValues.size(), isTouchUI ? aValues.size() : 1);
    ButtonGroup buttonGroup = new ButtonGroup();
    for (int i = 0; i < aValues.size(); i++) {
      final T value = aValues.get(i);
      String valueAsString = aStringRepresentations.get(i);
      final AbstractButton button = isTouchUI ? new JButton(valueAsString) : new JRadioButton();
      button.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          aCallBackFunctionOnSelectionChange.applyOn(value);
        }
      });
      if (!isTouchUI) {
        button.setSelected(i == aDefaultSelectedIndex);
        buttonGroup.add(button);
      }
      subPanelBuilder.append(button);
      if (!isTouchUI) {
        HaloLabel label = new HaloLabel(valueAsString);
        subPanelBuilder.append(label);
        label.addMouseListener(new MouseAdapter() {
          @Override
          public void mouseClicked(MouseEvent e) {
            button.doClick();
          }
        });
      }
      if (!isTouchUI) {
        subPanelBuilder.nextLine();
      }
    }
    JPanel subPanel = subPanelBuilder.getPanel();
    subPanel.setOpaque(false);
    aBuilder.append(subPanel);
    aBuilder.nextLine();
  }

  private DefaultFormBuilder createSubPanelBuilder(int aNumberOfRows, int aNumberOfColumns) {
    String layoutString = "";
    for (int i = 0; i < aNumberOfColumns; i++) {
      layoutString += "p, 5dlu, ";
    }
    layoutString += "p:grow";
    DefaultFormBuilder result = new DefaultFormBuilder(new FormLayout(layoutString));
    final RoundedBorder roundedBorder = new RoundedBorder(15, DemoUIColors.SUB_PANEL_COLOR, DemoUIColors.SUB_PANEL_COLOR);
    roundedBorder.setTotalItems(aNumberOfRows);
    result.border(roundedBorder);
    return result;
  }

  private void addLeftAlignedTitle(DefaultFormBuilder aBuilder, String aTitle) {
    aBuilder.append(new HaloLabel(aTitle, 12, false));
    aBuilder.nextLine();
  }

  private static class SafetyDepthPropertyChangeListener implements PropertyChangeListener {
    private final TLcdS52DisplaySettings fS52DisplaySettings = ECDISConfigurationProvider.getS52DisplaySettings();
    private JSlider fSafetyDepthSlider = null;
    private JLabel fDepthLabel;

    private void setSafetyDepthSlider(JSlider aSafetyDepthSlider, JLabel aDepthLabel) {
      fSafetyDepthSlider = aSafetyDepthSlider;
      fDepthLabel = aDepthLabel;
      updateSliderValue();
    }

    private void updateSliderValue() {
      fSafetyDepthSlider.setValue((int) Math.round(fS52DisplaySettings.getSafetyDepth()));
      fDepthLabel.setText(convertSafetyDepthToString(fS52DisplaySettings.getSafetyDepth()));
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
      if (fSafetyDepthSlider != null) {
        if ("Safety depth".equals(evt.getPropertyName()) ||
            "all".equals(evt.getPropertyName())) {
          updateSliderValue();
        }
      }
    }
  }

  private static String convertSafetyDepthToString(double aSafetyDepth) {
    return SAFETY_DEPTH_FORMAT.format(aSafetyDepth) + " m";
  }
}
