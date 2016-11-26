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
package samples.wms.client.ecdis.gxy.s52;

import static samples.wms.client.ecdis.gxy.s52.S52DataTypes.*;
import static samples.wms.client.ecdis.gxy.s52.S52Tooltips.*;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JPanel;

import samples.common.DataObjectOptionsPanelBuilder;

public class S52DisplaySettingsCustomizer extends JPanel {

  private DataObjectOptionsPanelBuilder.DataObjectHolder fObjectHolder;
  private S52DepthsPanel fDepthsPanel;

  public S52DisplaySettingsCustomizer(S52DisplaySettings aDisplaySettings) {

    // Synchronizes the radio button for 2/4 shades with the depths panel.
    PropertyChangeListener twoShadesUpdater = new PropertyChangeListener() {

      @Override
      public void propertyChange(PropertyChangeEvent aEvent) {
        fDepthsPanel.setTwoShades((Boolean) aEvent.getNewValue());
      }
    };

    // Creation of the UI panels
    fDepthsPanel = new S52DepthsPanel();

    DataObjectOptionsPanelBuilder panelBuilder = DataObjectOptionsPanelBuilder.newInstance(S52DataTypes.getDataModel().getDeclaredType("DisplaySettingsType"));

    panelBuilder.fixedPanel("Basic")
                .toggleButtonGroup(COLOR_SCHEME).tooltip(COLOR_TYPE_TOOLTIP)
                .toggleButtonGroup(DISPLAY_CATEGORY).tooltip(DISPLAY_CATEGORY_TOOLTIP).hint("Controls the amount of data on the map");

    panelBuilder.fixedPanel("Safety")
                .trueFalseRadioButtonGroup(USE_TWO_SHADES).trueValue("Two shades").falseValue("Four shades").tooltip(TWO_SHADES_TOOLTIP).listeners(twoShadesUpdater)
                .component(fDepthsPanel)
                .checkBox(DISPLAY_ISOLATED_DANGERS_IN_SHALLOW_WATER).tooltip(DISPLAY_ISOLATED_DANGERS_TOOLTIP)
                .checkBox(DISPLAY_SHALLOW_PATTERN).tooltip(DISPLAY_SHALLOW_PATTERN_TOOLTIP)
                .checkBox(DISPLAY_SOUNDINGS).tooltip(DISPLAY_SOUNDINGS_TOOLTIP).hint("Soundings of unsafe depth are darker");

    panelBuilder.fixedPanel("Text")
                .checkBox(DISPLAY_TEXT).tooltip(DISPLAY_TEXT_TOOLTIP)
                .checkBox(USE_ABBREVIATIONS).tooltip(USE_ABBREVIATIONS_TOOLTIP)
                .falseTrueRadioButtonGroup(USE_NATIONAL_LANGUAGE).trueValue("National").falseValue("International").tooltip(USE_NATIONAL_LANGUAGE_TOOLTIP);

    DataObjectOptionsPanelBuilder.PanelBuilder advancedPanelBuilder = panelBuilder.collapsiblePanel("Advanced");
    advancedPanelBuilder.radioButtonGroup(AREA_BOUNDARY_SYMBOL_TYPE).tooltip(AREA_SYMBOL_TOOLTIP)
                        .radioButtonGroup(POINT_SYMBOL_TYPE).tooltip(POINT_SYMBOL_TOOLTIP)
                        .falseTrueRadioButtonGroup(DISPLAY_FULL_LENGTH_LIGHT_SECTOR_LINES).falseValue("Short").trueValue("Real length").tooltip(LIGHT_SECTOR_LINES_TOOLTIP)
                        .checkBox(DISPLAY_CHART_BOUNDARIES).tooltip(DISPLAY_CHART_BOUNDARIES_TOOLTIP)
                        .checkBox(DISPLAY_OVERSCALE_INDICATION).tooltip(DISPLAY_OVERSCALE_INDICATOR_TOOLTIP)
                        .checkBox(DISPLAY_UNDERSCALE_INDICATION).tooltip(DISPLAY_UNDERSCALE_INDICATOR_TOOLTIP)
                        .checkBox(DISPLAY_LAND_AREAS).tooltip(DISPLAY_LAND_AREAS_TOOLTIP)
                        .checkBox(DISPLAY_METADATA).tooltip(DISPLAY_METADATA_TOOLTIP).hint("Only visible when display category is set to Other");

    fObjectHolder = panelBuilder.populate(this);
    setS52DisplaySettings(aDisplaySettings);
  }

  public void setS52DisplaySettings(S52DisplaySettings aDisplaySettings) {
    fObjectHolder.setDataObject(aDisplaySettings);
    fDepthsPanel.setS52DisplaySettings(aDisplaySettings);
  }

  @Override
  public void setEnabled(boolean aEnabled) {
    super.setEnabled(aEnabled);
    fObjectHolder.setEnabled(aEnabled);
    fDepthsPanel.setEnabled(aEnabled);
  }

}
