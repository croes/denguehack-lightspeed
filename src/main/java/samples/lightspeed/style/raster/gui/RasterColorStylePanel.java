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
package samples.lightspeed.style.raster.gui;

import static java.awt.GridBagConstraints.HORIZONTAL;
import static java.awt.GridBagConstraints.LINE_START;

import static samples.lightspeed.style.raster.gui.ColorFilters.*;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.luciad.imaging.operator.util.ALcdColorLookupTable;
import com.luciad.imaging.operator.util.TLcdColorLookupTable;
import com.luciad.imaging.operator.util.TLcdComponentColorLookupTable;
import com.luciad.util.ELcdInterpolationType;

import samples.gxy.common.TitledPanel;
import samples.lightspeed.style.raster.RasterFilter;
import samples.lightspeed.style.raster.RasterStyler;

/**
 * Panel for customizing the raster color styling.
 */
public class RasterColorStylePanel extends JPanel {

  private RasterStyler fStyler;
  private JSlider fOpacitySlider;
  private JSlider fBrightnessSlider;
  private JSlider fContrastSlider;
  private JComboBox fFilterComboBox;
  private JSlider[] fFilterValueSliders;
  private JComboBox fInterpolationComboBox;

  private final RasterFilterFactory[] fRasterFilterFactories = new RasterFilterFactory[]{
      // No filter
      new RasterFilterFactory("None") {
        @Override
        protected ALcdColorLookupTable createLut(float[] aValues) {
          return null;
        }
      },
      // A filter that inverts the colors
      new RasterFilterFactory("Negative") {
        @Override
        protected ALcdColorLookupTable createLut(float[] aValues) {
          return TLcdComponentColorLookupTable.newBuilder().
              filter(negative()).
                                                  build();
        }
      },
      // A filter that changes the hue
      new RasterFilterFactory("Hue", new String[]{"Hue"}, new float[]{.5f}) {
        @Override
        protected ALcdColorLookupTable createLut(float[] aValues) {
          return TLcdColorLookupTable.newBuilder().
              filter(hueSaturationBrightness(aValues[0], 1f, 1f)).
                                         build();
        }
      },
      // A filter that reduces the number of distinct colors
      new RasterFilterFactory("Threshold", new String[]{"Threshold"}, new float[]{0f}) {
        @Override
        protected ALcdColorLookupTable createLut(float[] aValues) {
          int size = Math.round(2 + aValues[0] * 14);
          return TLcdComponentColorLookupTable.newBuilder().
              size(size).
                                                  interpolation(ELcdInterpolationType.NONE).
                                                  filter(noOp()).
                                                  build();
        }
      },
      // A filter that hides blue areas
      new RasterFilterFactory("Hide blue", new String[]{"Threshold"}, new float[]{.3f}) {
        @Override
        protected ALcdColorLookupTable createLut(float[] aValues) {
          return TLcdColorLookupTable.newBuilder().
              size(16, 16, 16).
                                         filter(hideColor(new Color(58, 112, 194), aValues[0])).
                                         build();
        }
      },
  };

  private boolean fUpdatingFilterGui;

  public RasterColorStylePanel() {
    fOpacitySlider = new JSlider(JSlider.HORIZONTAL, 0, 100, 80);
    fOpacitySlider.addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent e) {
        if (fStyler != null) {
          fStyler.setOpacity(fOpacitySlider.getValue() / 100.0f);
        }
      }
    });
    fOpacitySlider.setToolTipText("Change the opacity of the raster");

    fBrightnessSlider = new JSlider(JSlider.HORIZONTAL, 0, 200, 100);
    fBrightnessSlider.addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent e) {
        if (fStyler != null) {
          fStyler.setBrightness(fBrightnessSlider.getValue() / 100.0f);
        }
      }
    });
    fBrightnessSlider.setToolTipText("Change the brightness of the raster");

    fContrastSlider = new JSlider(JSlider.HORIZONTAL, 0, 200, 100);
    fContrastSlider.addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent e) {
        if (fStyler != null) {
          fStyler.setContrast(fContrastSlider.getValue() / 100.0f);
        }
      }
    });
    fContrastSlider.setToolTipText("Change the contrast of the raster");

    fFilterComboBox = new JComboBox(fRasterFilterFactories);
    fFilterComboBox.addItemListener(new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent e) {
        if (fStyler == null || fUpdatingFilterGui) {
          return;
        }
        updateValueSliders(fStyler);
        updateStyleFilter();
      }
    });
    fFilterValueSliders = new JSlider[1];
    for (int i = 0; i < fFilterValueSliders.length; i++) {
      JSlider slider = new JSlider(JSlider.HORIZONTAL, 0, 100, 50);
      slider.addChangeListener(new ChangeListener() {
        @Override
        public void stateChanged(ChangeEvent e) {
          if (fStyler == null || fUpdatingFilterGui) {
            return;
          }
          updateStyleFilter();
        }
      });
      fFilterValueSliders[i] = slider;
    }
    fFilterComboBox.setToolTipText("Change the color filter of the raster");

    fInterpolationComboBox = new JComboBox(Interpolation.values());
    fInterpolationComboBox.setRenderer(new DefaultListCellRenderer() {
      @Override
      public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        String displayName = value == null ? null : ((Interpolation) value).getDisplayName();
        return super.getListCellRendererComponent(list, displayName, index, isSelected, cellHasFocus);
      }
    });
    fInterpolationComboBox.addItemListener(new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent e) {
        if (fStyler != null) {
          fStyler.setInterpolationType(((Interpolation) fInterpolationComboBox.getSelectedItem()).getInterpolationType());
        }
      }
    });
    fInterpolationComboBox.setToolTipText("Change how pixels between raster values are computed");

    JPanel sliderPanel = new JPanel(new GridLayout(1, 4));
    JPanel panel = new JPanel();
    panel.setLayout(new GridBagLayout());
    int y = 0;
    panel.add(new JLabel("Brightness"), new GridBagConstraints(0, y, 1, 1, 0, 0, LINE_START, HORIZONTAL, new Insets(0, 0, 0, 5), 0, 0));
    panel.add(fBrightnessSlider, new GridBagConstraints(1, y++, 1, 1, 0, 0, LINE_START, HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
    panel.add(new JLabel("Contrast"), new GridBagConstraints(0, y, 1, 1, 0, 0, LINE_START, HORIZONTAL, new Insets(0, 0, 0, 5), 0, 0));
    panel.add(fContrastSlider, new GridBagConstraints(1, y++, 1, 1, 0, 0, LINE_START, HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
    panel.add(new JLabel("Opacity"), new GridBagConstraints(0, y, 1, 1, 0, 0, LINE_START, HORIZONTAL, new Insets(0, 0, 0, 5), 0, 0));
    panel.add(fOpacitySlider, new GridBagConstraints(1, y++, 1, 1, 0, 0, LINE_START, HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
    panel.add(new JLabel("Filter"), new GridBagConstraints(0, y, 1, 1, 0, 0, LINE_START, HORIZONTAL, new Insets(5, 0, 0, 5), 0, 0));
    panel.add(fFilterComboBox, new GridBagConstraints(1, y++, 1, 1, 0, 0, LINE_START, HORIZONTAL, new Insets(5, 0, 0, 0), 0, 0));
    for (JSlider valueSlider : fFilterValueSliders) {
      panel.add(valueSlider, new GridBagConstraints(1, y++, 1, 1, 0, 0, LINE_START, HORIZONTAL, new Insets(5, 0, 0, 0), 5, 0));
    }
    panel.add(new JLabel("Interpolation"), new GridBagConstraints(0, y, 1, 1, 0, 0, LINE_START, HORIZONTAL, new Insets(5, 0, 0, 5), 0, 0));
    panel.add(fInterpolationComboBox, new GridBagConstraints(1, y++, 1, 1, 0, 0, LINE_START, HORIZONTAL, new Insets(5, 0, 0, 0), 0, 0));
    sliderPanel.add(panel);

    add(TitledPanel.createTitledPanel("Raster Style", sliderPanel));
  }

  private void updateStyleFilter() {
    RasterFilterFactory factory = (RasterFilterFactory) fFilterComboBox.getSelectedItem();
    int n = factory.getParameterCount();
    float[] values = new float[n];
    for (int i = 0; i < n; i++) {
      values[i] = fFilterValueSliders[i].getValue() / 100.f;
    }
    fStyler.setFilter(factory.create(values));
  }

  private void updateValueSliders(RasterStyler aStyler) {
    fUpdatingFilterGui = true;
    try {
      RasterFilterFactory factory = (RasterFilterFactory) fFilterComboBox.getSelectedItem();
      int n = factory.getParameterCount();
      RasterFilter filter = aStyler.getFilter();
      float[] values;
      if (factory.isCompatible(filter)) {
        values = (float[]) filter.getProperty(RasterFilterFactory.VALUES_KEY);
      } else {
        values = factory.getDefaultValues();
      }
      for (int i = 0; i < fFilterValueSliders.length; i++) {
        JSlider valueSlider = fFilterValueSliders[i];
        if (i < n) {
          valueSlider.setEnabled(true);
          valueSlider.setValue(Math.round(values[i] * 100.f));
          valueSlider.setToolTipText(factory.getParameterNames()[i]);
        } else {
          valueSlider.setEnabled(false);
        }
      }
    } finally {
      fUpdatingFilterGui = false;
    }
  }

  public void setStyler(RasterStyler aStyler, boolean aInteractiveLayer) {
    fStyler = null;
    Util.setEnabledRecursive(this, aStyler != null);

    if (aStyler == null) {
      return;
    }

    fOpacitySlider.setValue((int) (aStyler.getOpacity() * 100f));
    fBrightnessSlider.setValue((int) (aStyler.getBrightness() * 100f));
    fContrastSlider.setValue((int) (aStyler.getContrast() * 100f));

    if (aInteractiveLayer && aStyler.getColorMap() == null) {
      RasterFilter filter = aStyler.getFilter();
      RasterFilterFactory filterFactory = null;
      for (RasterFilterFactory f : fRasterFilterFactories) {
        if (f.isCompatible(filter)) {
          filterFactory = f;
        }
      }
      if (filterFactory == null) {
        filterFactory = fRasterFilterFactories[0];
      }
      fFilterComboBox.setSelectedItem(filterFactory);
      updateValueSliders(aStyler);
    } else {
      fFilterComboBox.setEnabled(false);
      for (JSlider slider : fFilterValueSliders) {
        slider.setEnabled(false);
      }
    }

    if (aInteractiveLayer) {
      fInterpolationComboBox.setSelectedItem(Interpolation.get(aStyler.getInterpolationType()));
    } else {
      fInterpolationComboBox.setSelectedItem(null);
      fInterpolationComboBox.setEnabled(false);
    }

    fStyler = aStyler;
  }

  private static enum Interpolation {
    DEFAULT("Default", null),
    NONE("None", ELcdInterpolationType.NONE),
    LINEAR("Linear", ELcdInterpolationType.LINEAR);

    private final String fDisplayName;
    private final ELcdInterpolationType fInterpolationType;

    private Interpolation(String aDisplayName, ELcdInterpolationType aInterpolationType) {
      fDisplayName = aDisplayName;
      fInterpolationType = aInterpolationType;
    }

    public String getDisplayName() {
      return fDisplayName;
    }

    public ELcdInterpolationType getInterpolationType() {
      return fInterpolationType;
    }

    public static Interpolation get(ELcdInterpolationType aInterpolationType) {
      for (Interpolation interpolation : values()) {
        if (interpolation.getInterpolationType() == aInterpolationType) {
          return interpolation;
        }
      }
      return null;
    }
  }
}
