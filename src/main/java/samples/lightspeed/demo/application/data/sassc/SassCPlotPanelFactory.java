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
package samples.lightspeed.demo.application.data.sassc;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.FormLayout;
import com.luciad.util.collections.ILcdList;
import com.luciad.view.lightspeed.layer.ILspEditableStyledLayer;
import com.luciad.view.lightspeed.layer.TLspPaintRepresentationState;

import samples.lightspeed.demo.application.gui.PlotPanelFactory;
import samples.lightspeed.demo.framework.data.themes.AbstractTheme;
import samples.lightspeed.internal.eurocontrol.sassc.model.sensors.ASasSensor;

/**
 * Panel factory for the EuroControl SASS-C data set.
 */
class SassCPlotPanelFactory extends PlotPanelFactory {

  private static final ASasSensor ANY_SENSOR = new ASasSensor(-1, 0, 0, "Any", null);

  @Override
  public List<JPanel> createThemePanels(AbstractTheme aTheme) {
    ILspEditableStyledLayer layer = (ILspEditableStyledLayer) aTheme.getLayers().iterator().next();
    final SassCStyler styler = (SassCStyler) layer.getStyler(TLspPaintRepresentationState.REGULAR_BODY);

    final DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout("p, 5dlu, p"));
    builder.border(Borders.DIALOG);

    createFilterPanel(builder, styler);
    createStylePanel(builder, styler);

    return Collections.singletonList(getPanel(builder));
  }

  private void createFilterPanel(DefaultFormBuilder builder, final SassCStyler aStyler) {
    builder.append(createLabel("Filter Controls", 15), 3);
    builder.nextLine();
    // Time-based filtering
    JComponent timeRangeSlider = createRangeSlider(new ARangeModel(aStyler) {
      @Override
      public double getMinimum() {
        return aStyler.getMinTime();
      }

      @Override
      public double getMaximum() {
        return aStyler.getMaxTime();
      }

      @Override
      public double getRangeMinimum() {
        return aStyler.getTimeRangeMinimum();
      }

      @Override
      public double getRangeMaximum() {
        return aStyler.getTimeRangeMaximum();
      }

      @Override
      protected void doSetRange(double aStart, double aEnd) {
        aStyler.setTimeRange(aStart, aEnd);
      }
    });
    addComponent("Time", timeRangeSlider, builder);
    // Height-based filtering
    JComponent heightRangeSlider = createRangeSlider(new ARangeModel(aStyler) {
      @Override
      public double getMinimum() {
        return aStyler.getMinHeight();
      }

      @Override
      public double getMaximum() {
        return aStyler.getMaxHeight();
      }

      @Override
      public double getRangeMinimum() {
        return aStyler.getHeightRangeMinimum();
      }

      @Override
      public double getRangeMaximum() {
        return aStyler.getHeightRangeMaximum();
      }

      @Override
      protected void doSetRange(double aStart, double aEnd) {
        aStyler.setHeightRange(aStart, aEnd);
      }
    });
    addComponent("Height", heightRangeSlider, builder);
    // Sensor-based filtering
    ILcdList<ASasSensor> sensors = aStyler.getSensors();
    JComponent sensorComboBox = createComboBox(
        new ListComboBoxModel<ASasSensor>(sensors, ANY_SENSOR) {
          @Override
          protected void setSelectedValue(ASasSensor aSensor) {
            aStyler.setSelectedSensor(aSensor);
          }

          @Override
          protected ASasSensor getSelectedValue() {
            return aStyler.getSelectedSensor();
          }
        },
        new DefaultListCellRenderer() {
          @Override
          public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            String string = value == null ? null : ((ASasSensor) value).getName();
            return super.getListCellRendererComponent(list, string, index, isSelected, cellHasFocus);
          }
        },
        10
    );
    addComponent("Sensor", sensorComboBox, builder);

  }

  private void createStylePanel(DefaultFormBuilder builder, final SassCStyler aStyler) {
    builder.append(createLabel("Style Controls", 15), 3);
    builder.nextLine();

    // Color styling
    if (!aStyler.getSupportedColorAttributes().isEmpty()) {
      JComponent colorComboBox = createComboBox(
          new DefaultComboBoxModel(new Vector<SassCAttribute>(aStyler.getSupportedColorAttributes())) {
            {
              setSelectedItem(aStyler.getColorAttribute());
            }

            @Override
            public void setSelectedItem(Object anObject) {
              super.setSelectedItem(anObject);
              aStyler.setColorAttribute((SassCAttribute) anObject);
              invalidateViews(aStyler);
            }
          },
          new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
              return super.getListCellRendererComponent(list, ((SassCAttribute) value).getName() + "-based", index, isSelected, cellHasFocus);
            }
          },
          aStyler.getSupportedColorAttributes().size()
      );
      addComponent("Color", colorComboBox, builder);
    }

    final JCheckBox paintDensityCheckBox = new JCheckBox();
    paintDensityCheckBox.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent aActionEvent) {
        aStyler.setPaintDensity(paintDensityCheckBox.getModel().isSelected());
      }
    });
    addComponent("Paint Density", paintDensityCheckBox, builder);
  }

}
