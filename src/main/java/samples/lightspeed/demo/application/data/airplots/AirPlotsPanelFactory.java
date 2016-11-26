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
package samples.lightspeed.demo.application.data.airplots;

import java.text.ParseException;
import java.util.Collections;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.ListCellRenderer;
import javax.swing.SpinnerListModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.FormLayout;
import com.luciad.datamodel.TLcdDataProperty;

import samples.common.HaloLabel;
import samples.lightspeed.demo.application.gui.PlotPanelFactory;
import samples.lightspeed.demo.framework.data.themes.AbstractTheme;
import samples.lightspeed.demo.framework.gui.DemoUIColors;
import samples.lightspeed.demo.framework.gui.RoundedBorder;
import samples.lightspeed.plots.datamodelstyling.PlotFilterPanel;
import samples.lightspeed.plots.datamodelstyling.PlotStylePanel;

/**
 * Panel factory for the EuroControl SASS-C data set.
 */
class AirPlotsPanelFactory extends PlotPanelFactory {

  @Override
  public List<JPanel> createThemePanels(AbstractTheme aTheme) {
    AirPlotsTheme theme = (AirPlotsTheme) aTheme;

    PlotFilterPanel filterPanel = new PlotFilterPanel() {
      @Override
      protected JComponent createLabel(String aText) {
        return new HaloLabel(aText);
      }

      @Override
      protected JComponent createCombobox(Object[] aValues, int aSelectedIndex) {
        return createJSpinner(aValues, aSelectedIndex, new ChangeListener() {
          @Override
          public void stateChanged(ChangeEvent e) {
            notifyChangeListeners();
          }
        });
      }
    };

    PlotStylePanel stylePanel = new PlotStylePanel() {
      @Override
      protected JComponent createLabel(String aText) {
        return new HaloLabel(aText);
      }

      @Override
      protected JComponent createCombobox(Object[] aValues, ListCellRenderer aCellRenderer, int aSelectedIndex) {
        return createJSpinner(aValues, aSelectedIndex, new ChangeListener() {
          @Override
          public void stateChanged(ChangeEvent e) {
            notifyChangeListeners();
          }
        });
      }

      @Override
      protected Object getSelectedItem(JComponent aComponent) {
        return AirPlotsPanelFactory.this.getSelectedItem(aComponent);
      }
    };

    filterPanel.initialize(theme.fDataType, theme.fDataTypeStyler);
    stylePanel.initialize(theme.fDataType, theme.fDataTypeStyler);

    final DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout("p, 5dlu, p"));
    builder.border(Borders.DIALOG);

    builder.append(createLabel("Filter", 15), 3);
    builder.nextLine();
    addSubPanel(filterPanel, builder);

    builder.append(createLabel("Style", 15), 3);
    builder.nextLine();
    addSubPanel(stylePanel, builder);

    return Collections.singletonList(getPanel(builder));
  }

  private void addSubPanel(JComponent aFilterPanel, DefaultFormBuilder aBuilder) {
    aFilterPanel.setBorder(new RoundedBorder(15, DemoUIColors.SUB_PANEL_COLOR, DemoUIColors.SUB_PANEL_COLOR));
    aBuilder.append(aFilterPanel);
    aBuilder.nextLine();
  }

  private Object getSelectedItem(JComponent aComponent) {
    JSpinner spinner = (JSpinner) aComponent;
    return spinner.getValue();
  }

  private JComponent createJSpinner(Object[] aValues, int aSelectedIndex, ChangeListener aChangeListener) {
    final JSpinner list = new JSpinner(new SpinnerListModel(aValues));
    JSpinner.ListEditor editor = (JSpinner.ListEditor) list.getEditor();
    editor.getTextField().setEditable(false);
    boolean dataProperties = true;
    for (Object value : aValues) {
      dataProperties &= value == null || value instanceof TLcdDataProperty;
    }
    if (dataProperties) {
      editor.getTextField().setFormatterFactory(new JFormattedTextField.AbstractFormatterFactory() {
        @Override
        public JFormattedTextField.AbstractFormatter getFormatter(JFormattedTextField tf) {
          return new JFormattedTextField.AbstractFormatter() {
            @Override
            public Object stringToValue(String text) throws ParseException {
              return text;
            }

            @Override
            public String valueToString(Object value) throws ParseException {
              return value != null ? ((TLcdDataProperty) value).getDisplayName() : "-";
            }
          };
        }
      });
    }
    if (aSelectedIndex > 0 && aSelectedIndex < aValues.length) {
      Object selected = aValues[aSelectedIndex];
      list.setValue(selected);
    }
    list.addChangeListener(aChangeListener);
    list.setOpaque(false);
    return list;
  }

}
