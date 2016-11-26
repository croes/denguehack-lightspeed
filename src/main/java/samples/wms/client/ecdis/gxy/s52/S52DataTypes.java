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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ResourceBundle;

import com.luciad.datamodel.ILcdDataModelDisplayNameProvider;
import com.luciad.datamodel.TLcdDataModel;
import com.luciad.datamodel.TLcdDataModelBuilder;
import com.luciad.datamodel.TLcdDataObject;
import com.luciad.datamodel.TLcdDataProperty;
import com.luciad.datamodel.TLcdDataType;
import com.luciad.datamodel.TLcdResourceBundleDataModelDisplayNameProvider;
import com.luciad.format.xml.bind.schema.dataobject.TLcdXMLDataModelBuilder;

/**
 * Data model for the S-52 XML Schema.
 */
public class S52DataTypes {

  static final String COLOR_SCHEME = "colorScheme";
  static final String POINT_SYMBOL_TYPE = "pointSymbolType";
  static final String AREA_BOUNDARY_SYMBOL_TYPE = "areaBoundarySymbolType";
  static final String DISPLAY_FULL_LENGTH_LIGHT_SECTOR_LINES = "displayFullLengthLightSectorLines";
  static final String DISPLAY_CATEGORY = "displayCategory";
  static final String DISPLAY_SOUNDINGS = "displaySoundings";
  static final String DISPLAY_LAND_AREAS = "displayLandAreas";
  static final String DISPLAY_METADATA = "displayMetadata";
  static final String DISPLAY_TEXT = "displayText";
  static final String DISPLAY_TEXT_GROUPS = "displayTextGroups";
  static final String USE_ABBREVIATIONS = "useAbbreviations";
  static final String USE_NATIONAL_LANGUAGE = "useNationalLanguage";
  static final String SAFETY_DEPTH = "safetyDepth";
  static final String SHALLOW_CONTOUR = "shallowContour";
  static final String SAFETY_CONTOUR = "safetyContour";
  static final String DEEP_CONTOUR = "deepContour";
  static final String USE_TWO_SHADES = "useTwoShades";
  static final String DISPLAY_SHALLOW_PATTERN = "displayShallowPattern";
  static final String DISPLAY_ISOLATED_DANGERS_IN_SHALLOW_WATER = "displayIsolatedDangersInShallowWater";
  static final String DISPLAY_CHART_BOUNDARIES = "displayChartBoundaries";
  static final String DISPLAY_OVERSCALE_INDICATION = "displayOverscaleIndication";
  static final String DISPLAY_UNDERSCALE_INDICATION = "displayUnderscaleIndication";
  static final String UNDERSCALE_INDICATION_COLOR = "underscaleIndicationColor";
  static final String DISPLAY_OBJECTS_OUTSIDE_VIEW = "displayObjectsOutsideView";
  static final String SCALE_DENOMINATORS = "scaleDenominators";
  static final String OBJECT_CLASSES = "objectClasses";

  public static final String NAMESPACE = "http://www.luciad.com/ecdis/s52/1.0";

  private static final String SCHEMA_LOCATION = S52DataTypes.class.getClassLoader().getResource("samples/wms/client/ecdis/s52.xsd").toString();

  private static final String DISPLAY_SETTINGS_TYPE_STRING = "DisplaySettingsType";

  private static final TLcdDataModel DATA_MODEL;
  public static final TLcdDataType S52_DISPLAY_SETTINGS_TYPE;

  static {
    TLcdDataModelBuilder dataModelBuilder = new TLcdDataModelBuilder(NAMESPACE);

    // Initialize the model using the XML Schema information
    TLcdXMLDataModelBuilder xmlDataModelBuilder = new TLcdXMLDataModelBuilder();
    xmlDataModelBuilder.buildDataModel(dataModelBuilder, NAMESPACE, SCHEMA_LOCATION);

    dataModelBuilder.typeBuilder(DISPLAY_SETTINGS_TYPE_STRING).instanceClass(S52DisplaySettings.class);

    ILcdDataModelDisplayNameProvider displayNameProvider = new TLcdResourceBundleDataModelDisplayNameProvider(ResourceBundle.getBundle("samples/wms/client/ecdis/s52"));
    dataModelBuilder.displayNameProvider(displayNameProvider);

    DATA_MODEL = dataModelBuilder.createDataModel();
    S52_DISPLAY_SETTINGS_TYPE = DATA_MODEL.getDeclaredType(DISPLAY_SETTINGS_TYPE_STRING);
  }

  public static TLcdDataModel getDataModel() {
    return DATA_MODEL;
  }

  /**
   * S-52 configuration object, containing all display settings available in the S-52 rendering
   * library.
   */
  public static class S52DisplaySettings extends TLcdDataObject {

    private PropertyChangeSupport fPropertyChangeSupport = new PropertyChangeSupport(this);

    /**
     * Creates a new {@code TLcdS52DisplaySettings} instance with default display settings.
     */
    public S52DisplaySettings() {
      super(S52_DISPLAY_SETTINGS_TYPE);
      setValue(COLOR_SCHEME, "day");
      setValue(POINT_SYMBOL_TYPE, "simplified");
      setValue(AREA_BOUNDARY_SYMBOL_TYPE, "plain");
      setValue(DISPLAY_FULL_LENGTH_LIGHT_SECTOR_LINES, false);
      setValue(DISPLAY_CATEGORY, "standard");
      setValue(DISPLAY_SOUNDINGS, false);
      setValue(DISPLAY_LAND_AREAS, true);
      setValue(DISPLAY_METADATA, false);
      setValue(DISPLAY_TEXT, false);
      setValue(USE_ABBREVIATIONS, false);
      setValue(USE_NATIONAL_LANGUAGE, false);
      setValue(SAFETY_DEPTH, 30.0);
      setValue(SHALLOW_CONTOUR, 2.0);
      setValue(SAFETY_CONTOUR, 30.0);
      setValue(DEEP_CONTOUR, 30.0);
      setValue(USE_TWO_SHADES, true);
      setValue(DISPLAY_SHALLOW_PATTERN, false);
      setValue(DISPLAY_ISOLATED_DANGERS_IN_SHALLOW_WATER, false);
      setValue(DISPLAY_CHART_BOUNDARIES, true);
      setValue(DISPLAY_OVERSCALE_INDICATION, true);
      setValue(DISPLAY_UNDERSCALE_INDICATION, true);
    }

    @Override
    public void setValue(TLcdDataProperty aProperty, Object aValue) {
      Object oldValue = getValue(aProperty);
      if ((oldValue == null && aValue != null) || (oldValue != null && aValue == null) || !oldValue.equals(aValue)) {
        super.setValue(aProperty, aValue);
        fPropertyChangeSupport.firePropertyChange(aProperty.getName(), oldValue, aValue);
      }
    }

    // Property change support

    /**
     * Registers a property change listener on this object. The listener will be notified each time
     * one of the rendering
     * settings has been changed.
     *
     * @param aPropertyChangeListener a property change listener to be registered on this object.
     */
    public void addPropertyChangeListener(PropertyChangeListener aPropertyChangeListener) {
      fPropertyChangeSupport.addPropertyChangeListener(aPropertyChangeListener);
    }

    /**
     * Unregisters a property change listener from this object. The listener will no longer be
     * notified of changes of this
     * object.
     * <p/>
     * If the listener was added more than once, it will be notified one less time after being
     * removed.
     * If the listener is null, or was never added, no exception is thrown and no action is taken.
     *
     * @param aPropertyChangeListener the property change listener to be unregistered from this
     *                                object.
     */
    public void removePropertyChangeListener(PropertyChangeListener aPropertyChangeListener) {
      fPropertyChangeSupport.removePropertyChangeListener(aPropertyChangeListener);
    }

  }
}
