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
package samples.decoder.ecdis.common;

import static com.luciad.format.s52.TLcdS52DisplaySettings.*;
import static samples.common.DataObjectOptionsPanelBuilder.ButtonUpdater;
import static samples.common.DataObjectOptionsPanelBuilder.newInstance;
import static samples.decoder.ecdis.common.S52Tooltips.*;

import java.awt.Component;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import com.luciad.format.s52.ELcdS52DisplayCategory;
import com.luciad.format.s52.ILcdS52ColorProvider;
import com.luciad.format.s52.TLcdS52DataTypes;
import com.luciad.format.s52.TLcdS52DisplaySettings;
import com.luciad.format.s52.TLcdS52ProductConfiguration;
import com.luciad.format.s57.ELcdS57ProductType;
import com.luciad.gui.TLcdIconFactory;
import com.luciad.gui.swing.TLcdSWIcon;
import samples.common.DataObjectOptionsPanelBuilder;
import samples.common.SwingUtil;
import samples.decoder.ecdis.common.filter.FilterPanel;

public class S52DisplaySettingsCustomizer extends JPanel {

  private static final String DISPLAY_CATEGORY_HINT = "Controls the amount of data on the map";

  private final DataObjectOptionsPanelBuilder.DataObjectHolder fObjectHolder;
  private final PanelState fPanelState = new PanelState();
  private final S52DepthsPanel fDepthsPanel;
  private final DisplayCategoryButtonUpdater fDisplayCategoryButtonUpdater = new DisplayCategoryButtonUpdater();

  public S52DisplaySettingsCustomizer() {
    this(new TLcdS52DisplaySettings(), false);
  }

  public S52DisplaySettingsCustomizer(boolean aCategoryIndependentFiltering) {
    this(new TLcdS52DisplaySettings(), aCategoryIndependentFiltering);
  }

  public S52DisplaySettingsCustomizer(TLcdS52DisplaySettings aDisplaySettings) {
    this(aDisplaySettings, false);
  }

  public S52DisplaySettingsCustomizer(TLcdS52DisplaySettings aDisplaySettings, boolean aCategoryIndependentFiltering) {
    this(TLcdS52ProductConfiguration.getInstance(ELcdS57ProductType.ENC).createSymbology(aDisplaySettings),
         aDisplaySettings,
         aCategoryIndependentFiltering);
  }

  /**
   * Creates a new instance.
   *
   * @param aColorProvider the color provider
   * @param aDisplaySettings the display settings if available
   * @param aCategoryIndependentFiltering whether or not to allow display category independent filtering. If
   * {@code false}, the customizer panel will only allow defining object class filtering for display category
   * {@link ELcdS52DisplayCategory#OTHER}. If {@code true}, object class filtering will be
   *                                      available for all display categories.
   */
  public S52DisplaySettingsCustomizer(ILcdS52ColorProvider aColorProvider,
                                      TLcdS52DisplaySettings aDisplaySettings,
                                      boolean aCategoryIndependentFiltering) {

    // Synchronizes the radio button for 2/4 shades with the depths panel.
    PropertyChangeListener twoShadesUpdater = new PropertyChangeListener() {

      @Override
      public void propertyChange(PropertyChangeEvent aEvent) {
        fDepthsPanel.setTwoShades((Boolean) aEvent.getNewValue());
      }
    };

    // Creation of the UI panels
    fDepthsPanel = new S52DepthsPanel(aColorProvider);

    DataObjectOptionsPanelBuilder panelBuilder = newInstance(TLcdS52DataTypes.getDataModel().getDeclaredType("DisplaySettingsType"));

    panelBuilder.fixedPanel("Basic")
                .toggleButtonGroup(COLOR_SCHEME_PROPERTY.getName()).tooltip(COLOR_TYPE_TOOLTIP)
                .toggleButtonGroup(DISPLAY_CATEGORY_PROPERTY.getName()).buttonEnabler(fDisplayCategoryButtonUpdater).appendButton(createObjectClassFilterButton()).tooltip(S52Tooltips.DISPLAY_CATEGORY_TOOLTIP).hint(DISPLAY_CATEGORY_HINT);

    panelBuilder.fixedPanel("Safety")
                .trueFalseRadioButtonGroup(USE_TWO_SHADES_PROPERTY.getName()).trueValue("Two shades").falseValue("Four shades").tooltip(TWO_SHADES_TOOLTIP).listeners(twoShadesUpdater)
                .component(fDepthsPanel)
                .checkBox(DISPLAY_ISOLATED_DANGERS_IN_SHALLOW_WATER_PROPERTY.getName()).tooltip(DISPLAY_ISOLATED_DANGERS_TOOLTIP)
                .checkBox(DISPLAY_SHALLOW_PATTERN_PROPERTY.getName()).tooltip(DISPLAY_SHALLOW_PATTERN_TOOLTIP)
                .checkBox(DISPLAY_SOUNDINGS_PROPERTY.getName()).tooltip(DISPLAY_SOUNDINGS_TOOLTIP).hint("Soundings of unsafe depth are darker");

    panelBuilder.collapsiblePanel("Text")
                .checkBox(DISPLAY_TEXT_PROPERTY.getName()).tooltip(DISPLAY_TEXT_TOOLTIP)
                .checkBox(USE_ABBREVIATIONS_PROPERTY.getName()).tooltip(USE_ABBREVIATIONS_TOOLTIP)
                .falseTrueRadioButtonGroup(USE_NATIONAL_LANGUAGE_PROPERTY.getName()).trueValue("National").falseValue("International").tooltip(USE_NATIONAL_LANGUAGE_TOOLTIP);

    DataObjectOptionsPanelBuilder.PanelBuilder advancedPanelBuilder = panelBuilder.collapsiblePanel("Advanced");
    advancedPanelBuilder
        .radioButtonGroup(AREA_BOUNDARY_SYMBOL_TYPE_PROPERTY.getName()).tooltip(AREA_SYMBOL_TOOLTIP)
        .radioButtonGroup(POINT_SYMBOL_TYPE_PROPERTY.getName()).tooltip(POINT_SYMBOL_TOOLTIP)
        .falseTrueRadioButtonGroup(DISPLAY_FULL_LENGTH_LIGHT_SECTOR_LINES_PROPERTY.getName()).falseValue("Short").trueValue("Real length").tooltip(LIGHT_SECTOR_LINES_TOOLTIP)
        .checkBox(DISPLAY_CHART_BOUNDARIES_PROPERTY.getName()).tooltip(DISPLAY_CHART_BOUNDARIES_TOOLTIP)
        .checkBox(DISPLAY_OVERSCALE_INDICATION_PROPERTY.getName()).tooltip(DISPLAY_OVERSCALE_INDICATOR_TOOLTIP)
        .checkBox(DISPLAY_UNDERSCALE_INDICATION_PROPERTY.getName()).tooltip(DISPLAY_UNDERSCALE_INDICATOR_TOOLTIP)
        .checkBox(DISPLAY_LAND_AREAS_PROPERTY.getName()).tooltip(DISPLAY_LAND_AREAS_TOOLTIP)
        .checkBox(DISPLAY_METADATA_PROPERTY.getName()).tooltip(DISPLAY_METADATA_TOOLTIP).hint("Only visible when display category is set to Other");

    fObjectHolder = panelBuilder.populate(this);
    setS52DisplaySettings(aDisplaySettings);
  }

  private JButton createObjectClassFilterButton() {
    final JButton objectClassFilter = new JButton(new ShowFilterAction());
    fPanelState.addListener(new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        if (PanelState.SELECTED_CATEGORY_PROPERTY.equals(evt.getPropertyName())) {
          boolean isOther = ELcdS52DisplayCategory.OTHER.equals(evt.getNewValue());
          objectClassFilter.setVisible(isOther);
          TLcdS52DisplaySettings displaySettings = fPanelState.getDisplaySettings();
          if (displaySettings != null) {
            displaySettings.setObjectClassSelectionEnabled(isOther);
          }
        }
      }
    });
    SwingUtil.makeSquare(objectClassFilter);
    objectClassFilter.setToolTipText(OBJECT_CLASS_FILTER_TOOLTIP);
    return objectClassFilter;
  }

  public void updateUIFromDisplaySettings() {
    fObjectHolder.updateUIFromDataObject();
    /*
        This call has been added here since the option panel builder internally holds a handle instance that updates the
        enabled state of ALL buttons that were ever made by the ButtonBuilder. This makes it impossible to specifically
        define the behavior of a given button since it's always following the enabled state of the panel.
     */
    fDisplayCategoryButtonUpdater.update();
    fDepthsPanel.updateFromDataObject();
  }

  public void setS52DisplaySettings(TLcdS52DisplaySettings aDisplaySettings) {
    fObjectHolder.setDataObject(aDisplaySettings);
    fPanelState.setDisplaySettings(aDisplaySettings);
    fDepthsPanel.setS52DisplaySettings(aDisplaySettings);
  }

  public TLcdS52DisplaySettings getS52DisplaySettings() {
    return (TLcdS52DisplaySettings) fObjectHolder.getDataObject();
  }

  @Override
  public void setEnabled(boolean aEnabled) {
    super.setEnabled(aEnabled);
    fObjectHolder.setEnabled(aEnabled);
    fDepthsPanel.setEnabled(aEnabled);
  }

  /**
   * Action that opens the dialog containing the filter settings.
   */
  private final class ShowFilterAction extends AbstractAction {

    private JDialog fCurrentDialog;

    public ShowFilterAction() {
      super(null, new TLcdSWIcon(TLcdIconFactory.create(TLcdIconFactory.FILTER_ICON)));
    }

    @Override
    public void actionPerformed(ActionEvent aEvent) {
      Component sourceComponent = (Component) aEvent.getSource();
      Window mainWindow = SwingUtilities.getWindowAncestor(sourceComponent);

      // Initialize with all possible object classes
      fPanelState.getDisplaySettings().setObjectClassSelectionEnabled(true);
      if (fPanelState.getDisplaySettings().getObjectClasses() == null) {
        int[] allObjectClasses = S57ObjectClassLookup.getLookup().getUniqueObjectClassCodes().toIntArray();
        fPanelState.getDisplaySettings().setObjectClasses(allObjectClasses);
      }

      // Set flag to indicate that the user activated custom filtering (i.e. filter dialog is active)
      fPanelState.setCustomFilterMode(true);

      // Show filter UI
      fCurrentDialog = FilterPanel.showDialog(fPanelState.getDisplaySettings(), mainWindow);
      setEnabled(false);

      S52DisplaySettingsCustomizer.this.addAncestorListener(new CloseFilterOnAncestorRemoved());
      fCurrentDialog.addWindowListener(new WindowAdapter() {
        @Override
        public void windowClosed(WindowEvent aEvent) {
          fCurrentDialog.removeWindowListener(this);
          fCurrentDialog = null;
          fPanelState.setCustomFilterMode(false);
          setEnabled(true);
        }
      });
    }

    private class CloseFilterOnAncestorRemoved implements AncestorListener {
      @Override
      public void ancestorAdded(AncestorEvent event) {
        // No-op
      }

      @Override
      public void ancestorRemoved(AncestorEvent event) {
        S52DisplaySettingsCustomizer.this.removeAncestorListener(this);
        if (fCurrentDialog != null) {
          fCurrentDialog.dispose();
        }
      }

      @Override
      public void ancestorMoved(AncestorEvent event) {
        // No-op
      }
    }
  }

  private final class PanelState implements PropertyChangeListener {
    private static final String SELECTED_CATEGORY_PROPERTY = "selectedCategory";
    private static final String CUSTOM_FILTER_MODE_PROPERTY = "customFilterMode";
    private static final String PROPERTY_DISPLAY_SETTINGS = "displaySettings";

    private final PropertyChangeSupport fSupport = new PropertyChangeSupport(this);
    private TLcdS52DisplaySettings fDisplaySettings;
    private ELcdS52DisplayCategory fSelectedCategory;
    private boolean fCustomFilterMode;

    public ELcdS52DisplayCategory getSelectedCategory() {
      return fSelectedCategory;
    }

    public void setSelectedCategory(ELcdS52DisplayCategory aSelectedCategory) {
      ELcdS52DisplayCategory oldValue = fSelectedCategory;
      fSelectedCategory = aSelectedCategory;
      fSupport.firePropertyChange(SELECTED_CATEGORY_PROPERTY, oldValue, fSelectedCategory);
    }

    public boolean isCustomFilterMode() {
      return fCustomFilterMode;
    }

    public void setCustomFilterMode(boolean aCustomFilterMode) {
      boolean oldValue = fCustomFilterMode;
      fCustomFilterMode = aCustomFilterMode;
      fSupport.firePropertyChange(CUSTOM_FILTER_MODE_PROPERTY, oldValue, fCustomFilterMode);
    }

    public TLcdS52DisplaySettings getDisplaySettings() {
      return fDisplaySettings;
    }

    /**
     * Sets a new display settings instance that needs to be managed by this UI.
     *
     * @param aDisplaySettings the new display settings
     */
    public void setDisplaySettings(TLcdS52DisplaySettings aDisplaySettings) {
      TLcdS52DisplaySettings oldValue = fDisplaySettings;
      if (oldValue != null) {
        oldValue.removePropertyChangeListener(this);
      }

      fDisplaySettings = aDisplaySettings;
      updatePanelState();
      fSupport.firePropertyChange(PROPERTY_DISPLAY_SETTINGS, oldValue, fDisplaySettings);

      if (fDisplaySettings != null) {
        fDisplaySettings.addPropertyChangeListener(this);
      }
    }

    private void updatePanelState() {
      setSelectedCategory(fDisplaySettings == null ?
                          null : ELcdS52DisplayCategory.values()[fDisplaySettings.getDisplayCategory()]);
    }

    public void addListener(PropertyChangeListener aListener) {
      if (aListener != null) {
        fSupport.addPropertyChangeListener(aListener);
      }
    }

    public void removeListener(PropertyChangeListener aListener) {
      fSupport.removePropertyChangeListener(aListener);
    }

    @Override
    public void propertyChange(PropertyChangeEvent aEvent) {
      updatePanelState();
    }
  }

  private class DisplayCategoryButtonUpdater extends ButtonUpdater {

    public DisplayCategoryButtonUpdater() {
      fPanelState.addListener(new PropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent aEvent) {
          if (PanelState.CUSTOM_FILTER_MODE_PROPERTY.equals(aEvent.getPropertyName())) {
            update();
          }
        }
      });
    }

    @Override
    public void updateButtons(AbstractButton[] aButtons) {
      TLcdS52DisplaySettings displaySettings = fPanelState.getDisplaySettings();
      if (displaySettings != null) {
        for (AbstractButton button : aButtons) {
          button.setEnabled(!fPanelState.isCustomFilterMode());
        }
      }
    }
  }
}
