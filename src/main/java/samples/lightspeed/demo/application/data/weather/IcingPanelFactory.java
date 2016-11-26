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
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luciad.view.lightspeed.layer.ILspLayer;

import samples.common.HaloLabel;
import samples.lightspeed.demo.application.data.weather.IcingSeverityContour.Severity;

/**
 * Creates the icing panel for the extended weather theme.
 */
class IcingPanelFactory {

  private static final String SLD_TOOLTIP_TEXT = "<html>Supercooled water droplets, or freezing rain, do not freeze instantly on contact but gradually as they flow back across the surface.<br>"
                                                 + "This often forms clear, smooth and relatively difficult to remove ice called clear ice.<br>"
                                                 + "If the droplets are large the ice often extends to unprotected parts of the aircraft and forms larger ice shapes.<br>"
                                                 + "Supercooled large droplets (SLD) are defined as those with a diameter greater than 50 microns.</html>";

  private final IcingModel fIcingModel;
  private final List<ILspLayer> fIcingSeverityLayers;
  private final List<ILspLayer> fIcingSLDLayers;

  public IcingPanelFactory(IcingModel aIcingModel, List<ILspLayer> aIcingSeverityLayers, List<ILspLayer> aIcingSLDLayers) {
    fIcingModel = aIcingModel;
    fIcingSeverityLayers = aIcingSeverityLayers;
    fIcingSLDLayers = aIcingSLDLayers;
  }

  public IcingPanel createPanel() {
    DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout("l:p"));
    builder.border(BorderFactory.createEmptyBorder());

    builder.append(new HaloLabel("Time", 15, true));
    builder.nextLine();

    SliderPlaybackPanel timePanel = new SliderPlaybackPanel(new DimensionSlider<>(fIcingModel, fIcingModel.getTimeAxis(), getFilters()), 1000);
    builder.append(timePanel);
    builder.nextLine();

    builder.append(new HaloLabel("Altitude range", 15, true));
    final DimensionRangeSlider altitudeRangeSlider = new DimensionRangeSlider(fIcingModel, fIcingModel.getHeightAxis(), getMultiDimensionalRangeFilters());
    final HaloLabel altitudeRangeLabel = new HaloLabel(altitudeRangeSlider.getTextFormattedAxisValue(), 11, true);
    altitudeRangeSlider.addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent e) {
        altitudeRangeLabel.setText(altitudeRangeSlider.getTextFormattedAxisValue());
      }
    });
    JPanel altitudeRangeIndicator = new JPanel(new BorderLayout(0, 0));
    altitudeRangeIndicator.setOpaque(false);
    altitudeRangeIndicator.add(altitudeRangeLabel, BorderLayout.NORTH);
    altitudeRangeIndicator.add(altitudeRangeSlider, BorderLayout.CENTER);
    builder.append(altitudeRangeIndicator);

    final HaloLabel severityValueLabel = new HaloLabel("          >= " + Severity.fromCode(Severity.getLowestCode()).getMeaning(), 11, true);

    builder.append(new HaloLabel("Severity", 15, true));
    builder.append(severityValueLabel);
    builder.nextLine();

    final JSlider severitySlider = new JSlider(Severity.getLowestCode(), Severity.getHighestCode(), Severity.getLowestCode());
    severitySlider.setMajorTickSpacing(1);
    severitySlider.setPaintTicks(true);
    severitySlider.addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent e) {
        for (ILspLayer layer : fIcingSeverityLayers) {
          IcingSeverityFilter icingSeverityFilter = getSeverityFilterFromSeverityLayer(layer);
          Severity severity = Severity.fromCode(severitySlider.getValue());
          icingSeverityFilter.setSeverity(severity);
          severityValueLabel.setText("          >= " + severity.getMeaning());
        }
      }
    });
    builder.append(severitySlider);
    builder.nextLine();

    final HaloLabel probabilityValueLabel = new HaloLabel("          >= " + round(fIcingModel.getProbability(0) * 100, 0) + " %", 11, true);

    builder.append(new HaloLabel("Probability", 15, true));
    builder.append(probabilityValueLabel);
    builder.nextLine();

    final JSlider probabilitySlider = new JSlider(0, fIcingModel.getProbabilities().size() - 1, 0);
    probabilitySlider.setMajorTickSpacing(1);
    probabilitySlider.setPaintTicks(true);
    probabilitySlider.addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent e) {
        for (ILspLayer layer : fIcingSeverityLayers) {
          IcingProbabilityFilter icingProbabilityFilter = getProbabilityFilterFromSeverityLayer(layer);
          double probability = fIcingModel.getProbability(probabilitySlider.getValue());
          icingProbabilityFilter.setProbability(probability);
          probabilityValueLabel.setText("          >= " + round(probability * 100, 0) + " %");
        }
      }
    });
    builder.append(probabilitySlider);
    builder.nextLine();

    final JCheckBox sldButton = new JCheckBox("Supercooled Large Droplets");
    sldButton.setToolTipText(SLD_TOOLTIP_TEXT);
    sldButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        for (ILspLayer layer : fIcingSLDLayers) {
          if (sldButton.isSelected()) {
            layer.setVisible(true);
          } else {
            layer.setVisible(false);
          }
        }
      }
    });
    builder.append(sldButton);

    JPanel icingJPanel = builder.getPanel();
    icingJPanel.setSize(icingJPanel.getLayout().preferredLayoutSize(icingJPanel));
    icingJPanel.setCursor(Cursor.getDefaultCursor());
    icingJPanel.setOpaque(false);
    icingJPanel.setVisible(false);

    return new IcingPanel(fIcingSeverityLayers, fIcingSLDLayers, icingJPanel, timePanel, altitudeRangeSlider, severitySlider, probabilitySlider, sldButton);
  }

  private List<? extends DimensionalFilter> getFilters() {
    return new ArrayList<>(getMultiDimensionalRangeFilters());
  }

  private List<MultiDimensionalRangeFilter> getMultiDimensionalRangeFilters() {
    List<MultiDimensionalRangeFilter> filters = new ArrayList<>();
    for (ILspLayer layer : fIcingSeverityLayers) {
      MultiDimensionalRangeFilter filter = getMultiDimensionalRangeFilterFromSeverityLayer(layer);
      if (filter != null) {
        filters.add(filter);
      }
    }
    for (ILspLayer layer : fIcingSLDLayers) {
      MultiDimensionalRangeFilter filter = getMultiDimensionalRangeFilterFromSLDLayer(layer);
      if (filter != null) {
        filters.add(filter);
      }
    }
    return filters;
  }

  protected MultiDimensionalRangeFilter getMultiDimensionalRangeFilterFromSeverityLayer(ILspLayer layer) {
    return IcingSeverityContourLayerFactory.getMultiDimensionalRangeFilter(layer);
  }

  protected MultiDimensionalRangeFilter getMultiDimensionalRangeFilterFromSLDLayer(ILspLayer layer) {
    return IcingSLDContourLayerFactory.getMultiDimensionalRangeFilter(layer);
  }

  protected IcingSeverityFilter getSeverityFilterFromSeverityLayer(ILspLayer layer) {
    return IcingSeverityContourLayerFactory.getSeverityFilter(layer);
  }

  protected IcingProbabilityFilter getProbabilityFilterFromSeverityLayer(ILspLayer layer) {
    return IcingSeverityContourLayerFactory.getProbabilityFilter(layer);
  }

  static class IcingPanel {

    private final List<ILspLayer> fIcingSeverityLayers;
    private final List<ILspLayer> fIcingSLDLayers;
    private final JPanel fPanel;
    private final SliderPlaybackPanel fTimePanel;
    private final DimensionRangeSlider fHeightSlider;
    private final JSlider fSeveritySlider;
    private final JSlider fProbabilitySlider;
    private final JCheckBox fSLDButton;

    public IcingPanel(List<ILspLayer> aIcingSeverityLayers, List<ILspLayer> aIcingSLDLayers, JPanel aPanel, SliderPlaybackPanel aTimePanel, DimensionRangeSlider aHeightSlider, JSlider aSeveritySlider, JSlider aProbabilitySlider, JCheckBox aSLDButton) {
      fIcingSeverityLayers = aIcingSeverityLayers;
      fIcingSLDLayers = aIcingSLDLayers;
      fPanel = aPanel;
      fTimePanel = aTimePanel;
      fHeightSlider = aHeightSlider;
      fSeveritySlider = aSeveritySlider;
      fProbabilitySlider = aProbabilitySlider;
      fSLDButton = aSLDButton;
    }

    public JPanel getPanel() {
      return fPanel;
    }

    public boolean isActive() {
      return fIcingSeverityLayers.iterator().next().isVisible();
    }

    public void activate() {
      for (ILspLayer layer : fIcingSeverityLayers) {
        layer.setVisible(true);
      }
      for (ILspLayer layer : fIcingSLDLayers) {
        layer.setVisible(false);
      }
      if (fSLDButton.isSelected()) {
        fSLDButton.setSelected(false);
      }
      getPanel().setVisible(true);
    }

    public void activateWithSettingsOf(IcingPanel aIcingPanel) {
      for (ILspLayer layer : fIcingSeverityLayers) {
        layer.setVisible(true);
      }
      for (ILspLayer layer : fIcingSLDLayers) {
        layer.setVisible(false);
      }
      fTimePanel.setTimePosition(aIcingPanel.fTimePanel.getTimePosition());
      fHeightSlider.setRangeMinimum(aIcingPanel.fHeightSlider.getRangeMinimum());
      fHeightSlider.setRangeMaximum(aIcingPanel.fHeightSlider.getRangeMaximum());
      fSeveritySlider.setValue(aIcingPanel.fSeveritySlider.getValue());
      fProbabilitySlider.setValue(aIcingPanel.fProbabilitySlider.getValue());
      if (aIcingPanel.fSLDButton.isSelected()) {
        fSLDButton.doClick();
      }
      getPanel().setVisible(true);
    }

    public void deactivate() {
      for (ILspLayer layer : fIcingSeverityLayers) {
        layer.setVisible(false);
      }
      for (ILspLayer layer : fIcingSLDLayers) {
        layer.setVisible(false);
      }
      getPanel().setVisible(false);
      fTimePanel.deactivate();
      fHeightSlider.reset();
      fSeveritySlider.setValue(0);
      fProbabilitySlider.setValue(0);
      if (fSLDButton.isSelected()) {
        fSLDButton.setSelected(false);
      }
    }

  }

}
