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

import java.awt.Cursor;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JToolBar;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luciad.view.lightspeed.layer.ILspLayer;

import samples.common.OptionsPanelBuilder;
import samples.lightspeed.demo.application.data.weather.IcingPanelFactory.IcingPanel;
import samples.lightspeed.demo.application.data.weather.WeatherPanelFactory.WeatherPanel;
import samples.lightspeed.demo.framework.data.themes.AbstractTheme;

/**
 * Creates panels for the extended weather theme.
 */
class ExtendedWeatherPanelFactory {

  private final WeatherModel fModel;
  private final List<ILspLayer> fTemperatureLayers;
  private final List<ILspLayer> fTemperatureContourLayers;
  private final List<ILspLayer> fWindLayers;
  private final IcingModel fIcingModel;
  private final List<ILspLayer> fIcingSeverityLayers;
  private final List<ILspLayer> fIcingSeverityExtrudedContourLayers;
  private final List<ILspLayer> fIcingSLDLayers;
  private final List<ILspLayer> fIcingSLDExtrudedContourLayers;

  private AbstractButton fTemperatureButton;
  private AbstractButton fIcingButton;
  private AbstractButton f2DButton;
  private AbstractButton f3DButton;
  private JPanel fIcePanel;

  public ExtendedWeatherPanelFactory(WeatherModel aModel, List<ILspLayer> aTemperatureLayers,
                                                          List<ILspLayer> aTemperatureContourLayers,
                                                          List<ILspLayer> aWindLayers,
                                     IcingModel aIcingModel, List<ILspLayer> aIcingSeverityLayers,
                                                             List<ILspLayer> aIcingSeverityExtrudedContourLayers,
                                                             List<ILspLayer> aIcingSLDLayers,
                                                             List<ILspLayer> aIcingSLDExtrudedContourLayers) {
    fModel = aModel;
    fTemperatureLayers = aTemperatureLayers;
    fTemperatureContourLayers = aTemperatureContourLayers;
    fWindLayers = aWindLayers;
    fIcingModel = aIcingModel;
    fIcingSeverityLayers = aIcingSeverityLayers;
    fIcingSeverityExtrudedContourLayers = aIcingSeverityExtrudedContourLayers;
    fIcingSLDLayers = aIcingSLDLayers;
    fIcingSLDExtrudedContourLayers = aIcingSLDExtrudedContourLayers;
  }

  public ExtendedWeatherPanel createPanel(AbstractTheme aTheme) {
    if (!(aTheme instanceof ExtendedWeatherTheme)) {
      return null;
    }

    //Standard panel
    WeatherPanel weatherPanel = new WeatherPanelFactory(fModel, fTemperatureLayers, fTemperatureContourLayers, fWindLayers).createPanel(aTheme);

    if (weatherPanel == null) {
      return null;
    }
    final JPanel standardPanel = weatherPanel.getPanel();
    standardPanel.setOpaque(false);
    standardPanel.setBorder(BorderFactory.createEmptyBorder());

    //Icing panel
    IcingPanel icingPanel = new IcingPanelFactory(fIcingModel, fIcingSeverityLayers, fIcingSLDLayers).createPanel();
    JPanel icingJPanel = icingPanel.getPanel();

    //Extruded icing panel
    IcingPanel extrudedIcingPanel = new ExtrudedIcingPanelFactory(fIcingModel, fIcingSeverityExtrudedContourLayers, fIcingSLDExtrudedContourLayers).createPanel();

    //Main panel
    fTemperatureButton = createTemperatureButton();
    fIcingButton = createIcingButton();
    f2DButton = create2DButton(weatherPanel, icingPanel, extrudedIcingPanel);
    f3DButton = create3DButton(weatherPanel, icingPanel, extrudedIcingPanel);

    ButtonGroup weatherTypeButtonGroup = new ButtonGroup();
    weatherTypeButtonGroup.add(fTemperatureButton);
    weatherTypeButtonGroup.add(fIcingButton);

    ButtonGroup dimensionButtonGroup = new ButtonGroup();
    dimensionButtonGroup.add(f2DButton);
    dimensionButtonGroup.add(f3DButton);

    DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout("l:p"));
    builder.border(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    JToolBar dimensionToolbar = new JToolBar();
    dimensionToolbar.setFloatable(false);
    dimensionToolbar.add(f2DButton);
    dimensionToolbar.add(f3DButton);

    JToolBar weatherTypeToolbar = new JToolBar();

    weatherTypeToolbar.add(fTemperatureButton);
    weatherTypeToolbar.add(fIcingButton);
    weatherTypeToolbar.setFloatable(false);

    builder.append(weatherTypeToolbar);
    builder.append(dimensionToolbar);
    builder.append(standardPanel);

    fIcePanel = new JPanel();
    fIcePanel.setOpaque(false);
    fIcePanel.add(icingJPanel);
    builder.append(fIcePanel);

    builder.appendSeparator();
    JPanel mainPanel = builder.getPanel();
    mainPanel.setSize(mainPanel.getLayout().preferredLayoutSize(mainPanel));
    mainPanel.setCursor(Cursor.getDefaultCursor());

    return new ExtendedWeatherPanel(weatherPanel, icingPanel, extrudedIcingPanel, mainPanel);
  }

  private AbstractButton createTemperatureButton() {
    AbstractButton button = createButton("Temperature", 3);
    button.setSelected(true);
    button.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        f2DButton.doClick();
        f2DButton.setVisible(false);
        f3DButton.setVisible(false);
      }
    });
    return button;
  }

  private AbstractButton createIcingButton() {
    AbstractButton button = createButton("Icing", 3);
    button.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        if (!f3DButton.isSelected()) {
          f2DButton.doClick();
          f2DButton.setVisible(true);
          f3DButton.setVisible(true);
        }
      }
    });
    return button;
  }

  private AbstractButton create2DButton(final WeatherPanel aWeatherPanel, final IcingPanel aIcingPanel, final IcingPanel aExtrudedIcingPanel) {
    AbstractButton button = createButton("2D", 1);
    button.setVisible(false);
    button.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        if (fTemperatureButton.isSelected()) {
          if (!aWeatherPanel.isActive()) {
            aWeatherPanel.activate();
            aIcingPanel.deactivate();
            aExtrudedIcingPanel.deactivate();
          }
        } else if (fIcingButton.isSelected()) {
          if (!aIcingPanel.isActive()) {
            aWeatherPanel.deactivate();
            aIcingPanel.activateWithSettingsOf(aExtrudedIcingPanel);
            aExtrudedIcingPanel.deactivate();
            fIcePanel.removeAll();
            fIcePanel.add(aIcingPanel.getPanel());
          }
        }
      }
    });
    return button;
  }

  private AbstractButton create3DButton(final WeatherPanel aWeatherPanel, final IcingPanel aIcingPanel, final IcingPanel aExtrudedIcingPanel) {
    AbstractButton button = createButton("3D", 1);
    button.setVisible(false);
    button.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        if (!aExtrudedIcingPanel.isActive() && fIcingButton.isSelected()) {
          aWeatherPanel.deactivate();
          aExtrudedIcingPanel.activateWithSettingsOf(aIcingPanel);
          aIcingPanel.deactivate();
          fIcePanel.removeAll();
          fIcePanel.add(aExtrudedIcingPanel.getPanel());
        }
      }
    });
    return button;
  }

  private static AbstractButton createButton(String aText, int aFontSizeOffset) {
    AbstractButton button = OptionsPanelBuilder.createUnderlinedButton(aText);
    Font font = button.getFont();
    button.setFont(font.deriveFont((float) font.getSize() + aFontSizeOffset));
    return button;
  }

  static class ExtendedWeatherPanel {

    private final WeatherPanel fWeatherPanel;
    private final IcingPanel fIcingPanel;
    private final IcingPanel fExtrudedIcingPanel;
    private final JPanel fPanel;

    public ExtendedWeatherPanel(WeatherPanel aWeatherPanel, IcingPanel aIcingPanel, IcingPanel aExtrudedIcingPanel, JPanel aPanel) {
      fWeatherPanel = aWeatherPanel;
      fIcingPanel = aIcingPanel;
      fExtrudedIcingPanel = aExtrudedIcingPanel;
      fPanel = aPanel;
    }

    public JPanel getPanel() {
      return fPanel;
    }

    public void deactivate() {
      fWeatherPanel.deactivate();
      fIcingPanel.deactivate();
      fExtrudedIcingPanel.deactivate();
    }

  }

}
