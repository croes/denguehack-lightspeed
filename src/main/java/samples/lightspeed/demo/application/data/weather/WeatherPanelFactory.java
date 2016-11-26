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
package samples.lightspeed.demo.application.data.weather;

import static samples.lightspeed.demo.application.data.weather.WeatherUtil.round;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luciad.multidimensional.TLcdDimensionAxis;
import com.luciad.util.TLcdColorMap;
import com.luciad.util.iso19103.TLcdISO19103Measure;
import com.luciad.view.lightspeed.layer.ILspEditableStyledLayer;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.TLspPaintRepresentationState;
import com.luciad.view.lightspeed.style.styler.ILspStyler;

import samples.common.HaloLabel;
import samples.lightspeed.demo.application.gui.ColorMapLegend;
import samples.lightspeed.demo.framework.data.themes.AbstractTheme;

/**
 * Creates theme panels for the weather theme.
 */
class WeatherPanelFactory {

  private final WeatherModel fModel;
  private final List<ILspLayer> fTemperatureLayers;
  private final List<ILspLayer> fTemperatureContourLayers;
  private final List<ILspLayer> fWindLayers;
  private DimensionInterpolationSlider<TLcdISO19103Measure> fPressureSlider;
  private RangeSliderPanel<TemperatureUnit, RasterValueRangeSlider> fTemperatureRangeSliderPanel;
  private JToggleButton fWindButton;

  public WeatherPanelFactory(WeatherModel aModel, List<ILspLayer> aTemperatureLayers, List<ILspLayer> aTemperatureContourLayers, List<ILspLayer> aWindLayers) {
    fModel = aModel;
    fTemperatureLayers = aTemperatureLayers;
    fTemperatureContourLayers = aTemperatureContourLayers;
    fWindLayers = aWindLayers;
  }

  public WeatherPanel createPanel(AbstractTheme aTheme) {
    if (!(aTheme instanceof WeatherTheme)) {
      return null;
    }

    DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout("left:pref"));
    builder.border(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    HaloLabel timeLabel = new HaloLabel("Time", 15, true);
    builder.append(timeLabel);
    builder.nextLine();

    final DimensionInterpolationSlider<Date> timeSlider = new DimensionInterpolationSlider<>(fModel, getTimeAxis(), fTemperatureContourLayers);
    SliderPlaybackPanel timePanel = new SliderPlaybackPanel(timeSlider, 60);
    builder.append(timePanel);
    builder.nextLine();

    final HaloLabel altitudeLabel = new HaloLabel("Pressure altitude", 15, true);
    builder.append(altitudeLabel);
    builder.nextLine();

    fPressureSlider = new DimensionInterpolationSlider<>(fModel, getPressureAxis(), DimensionInterpolationSlider.OperationMode.INVERTED, fTemperatureContourLayers);
    final HaloLabel altitudeValueLabel = new HaloLabel(fPressureSlider.getTextFormattedAxisValue(), 11, true);
    fPressureSlider.addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent e) {
        altitudeValueLabel.setText(fPressureSlider.getTextFormattedAxisValue());
      }
    });
    JPanel altitudeValueIndicator = new JPanel(new BorderLayout(0, 0));
    altitudeValueIndicator.setOpaque(false);
    altitudeValueIndicator.add(altitudeValueLabel, BorderLayout.NORTH);
    altitudeValueIndicator.add(fPressureSlider, BorderLayout.CENTER);

    builder.append(altitudeValueIndicator);
    builder.nextLine();
    new DimensionInterpolationSliderGroup(timeSlider, fPressureSlider);

    JPanel temperaturePanel = createTemperaturePanel();
    builder.appendSeparator();
    builder.append(temperaturePanel);
    builder.nextLine();
    builder.appendSeparator();

    fWindButton = new JCheckBox("Wind");
    fWindButton.setSelected(fWindLayers.iterator().next().isVisible());
    fWindButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        for (ILspLayer layer : fWindLayers) {
          if (fWindButton.isSelected()) {
            layer.setVisible(true);
          } else {
            layer.setVisible(false);
          }
        }
      }
    });
    builder.append(fWindButton);

    JPanel mainPanel = builder.getPanel();
    mainPanel.setSize(mainPanel.getLayout().preferredLayoutSize(mainPanel));
    mainPanel.setCursor(Cursor.getDefaultCursor());

    return new WeatherPanel(fTemperatureLayers, fTemperatureContourLayers, fWindLayers, mainPanel, timePanel, fPressureSlider, fTemperatureRangeSliderPanel, fWindButton);
  }

  private JPanel createTemperaturePanel() {
    DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout("l:p"));

    RasterValueRangeSlider temperatureRangeSlider = new RasterValueRangeSlider(fModel, fTemperatureLayers, fTemperatureContourLayers);
    fTemperatureRangeSliderPanel = new RangeSliderPanel<>("Temperature range", temperatureRangeSlider, TemperatureUnit.values());
    temperatureRangeSlider.addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent e) {
        updateTemperatureLabels(fTemperatureRangeSliderPanel.getSelectedItem());
      }
    });
    for (final AbstractButton button : fTemperatureRangeSliderPanel.getUnitButtons()) {
      button.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          fTemperatureRangeSliderPanel.setButtonPressed(button);
          TemperatureUnit temperatureUnit = fTemperatureRangeSliderPanel.getSelectedItem();
          for (ILspLayer layer : fTemperatureContourLayers) {
            if (layer instanceof ILspEditableStyledLayer) {
              ILspEditableStyledLayer editableLayer = (ILspEditableStyledLayer) layer;
              ILspStyler styler = editableLayer.getStyler(TLspPaintRepresentationState.REGULAR_BODY);
              if (styler instanceof ContourBodyStyler) {
                ContourBodyStyler contourBodyStyler = (ContourBodyStyler) styler;
                contourBodyStyler.setTemperatureUnit(temperatureUnit);
              }
            }
          }
          updateTemperatureLabels(temperatureUnit);
        }
      });
    }


    builder.append(fTemperatureRangeSliderPanel);
    ColorMapLegend colorMapLegend = new ColorMapLegend((TLcdColorMap) fModel.getContourModel().getColorMap().clone(), ColorMapLegend.Orientation.LEFT_TO_RIGHT);
    colorMapLegend.setPreferredSize(temperatureRangeSlider.getPreferredSize());
    builder.append(colorMapLegend);
    updateTemperatureLabels(fTemperatureRangeSliderPanel.getSelectedItem());
    builder.nextLine();

    JPanel contentPanel = builder.getPanel();
    contentPanel.setSize(contentPanel.getLayout().preferredLayoutSize(contentPanel));
    contentPanel.setOpaque(false);

    contentPanel.setCursor(Cursor.getDefaultCursor());

    return contentPanel;
  }

  private void updateTemperatureLabels(TemperatureUnit aTemperatureUnit) {
    double from = fTemperatureRangeSliderPanel.getRangeSlider().getRangeMinimum();
    fTemperatureRangeSliderPanel.setFrom(Integer.toString(round(aTemperatureUnit.fromKelvin(from), 0)));
    double to = fTemperatureRangeSliderPanel.getRangeSlider().getRangeMaximum();
    fTemperatureRangeSliderPanel.setTo(Integer.toString(round(aTemperatureUnit.fromKelvin(to), 0)));
  }

  private TLcdDimensionAxis<Date> getTimeAxis() {
    return fModel.getTimeAxis();
  }

  private TLcdDimensionAxis<TLcdISO19103Measure> getPressureAxis() {
    return fModel.getPressureAxis();
  }

  static class WeatherPanel {

    private final List<ILspLayer> fTemperatureLayers;
    private final List<ILspLayer> fTemperatureContourLayers;
    private final List<ILspLayer> fWindLayers;
    private final JPanel fPanel;
    private final SliderPlaybackPanel fTimePanel;
    private final DimensionInterpolationSlider<TLcdISO19103Measure> fPressureSlider;
    private final RangeSliderPanel<TemperatureUnit, RasterValueRangeSlider> fTemperatureRangeSliderPanel;
    private final JToggleButton fWindButton;

    public WeatherPanel(List<ILspLayer> aTemperatureLayers, List<ILspLayer> aTemperatureContourLayers, List<ILspLayer> aWindLayers, JPanel aPanel, SliderPlaybackPanel aTimePanel, DimensionInterpolationSlider<TLcdISO19103Measure> aPressureSlider, RangeSliderPanel<TemperatureUnit, RasterValueRangeSlider> aTemperatureRangeSliderPanel, JToggleButton aWindButton) {
      fTemperatureLayers = aTemperatureLayers;
      fTemperatureContourLayers = aTemperatureContourLayers;
      fWindLayers = aWindLayers;
      fPanel = aPanel;
      fTimePanel = aTimePanel;
      fPressureSlider = aPressureSlider;
      fTemperatureRangeSliderPanel = aTemperatureRangeSliderPanel;
      fWindButton = aWindButton;
    }

    public JPanel getPanel() {
      return fPanel;
    }

    public boolean isActive() {
      return fTemperatureLayers.iterator().next().isVisible();
    }

    public void activate() {
      for (ILspLayer layer : fTemperatureLayers) {
        layer.setVisible(true);
      }
      for (ILspLayer layer : fTemperatureContourLayers) {
        layer.setVisible(true);
      }
      for (ILspLayer windLayer : fWindLayers) {
        windLayer.setVisible(false);
      }
      getPanel().setVisible(true);
    }

    public void deactivate() {
      for (ILspLayer layer : fTemperatureLayers) {
        layer.setVisible(false);
      }
      for (ILspLayer layer : fTemperatureContourLayers) {
        layer.setVisible(false);
      }
      for (ILspLayer layer : fWindLayers) {
        layer.setVisible(false);
      }
      getPanel().setVisible(false);
      fTimePanel.deactivate();
      fPressureSlider.setValue(0);
      fTemperatureRangeSliderPanel.reset();
      if (fWindButton.isSelected()) {
        fWindButton.doClick();
      }
    }

  }

}
