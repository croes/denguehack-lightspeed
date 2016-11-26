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
package samples.encoder.kml22.converter;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import com.luciad.format.kml22.model.TLcdKML22DataTypes;
import com.luciad.format.kml22.model.TLcdKML22Kml;
import com.luciad.format.kml22.model.TLcdKML22ModelDescriptor;
import com.luciad.format.kml22.model.extendeddata.TLcdKML22Schema;
import com.luciad.format.kml22.model.feature.TLcdKML22Document;
import com.luciad.format.kml22.model.feature.TLcdKML22Placemark;
import com.luciad.format.kml22.model.style.ELcdKML22ColorMode;
import com.luciad.format.kml22.model.style.TLcdKML22AbstractStyleSelector;
import com.luciad.format.kml22.model.style.TLcdKML22LabelStyle;
import com.luciad.format.kml22.model.style.TLcdKML22LineStyle;
import com.luciad.format.kml22.model.style.TLcdKML22PolyStyle;
import com.luciad.format.kml22.model.style.TLcdKML22Style;
import com.luciad.format.kml22.util.TLcdKML22ResourceProvider;
import com.luciad.format.kml22.xml.TLcdKML22ModelDecoder;
import com.luciad.io.TLcdInputStreamFactory;
import com.luciad.util.ILcdFireEventMode;

/**
 * A class to build KML models.
 */
public final class KML22ModelBuilder {

  private static final String SOURCE_NAME = "";

  private static final String DEFAULT_MAP = "Default";
  public static final String DEFAULT_MAP_URL = "#" + DEFAULT_MAP;
  private static final String DEFAULT_NORMAL = "DefaultNormal";
  public static final String DEFAULT_NORMAL_URL = "#" + DEFAULT_NORMAL;
  private static final String DEFAULT_HIGHLIGHT = "DefaultHighlight";
  public static final String DEFAULT_HIGHLIGHT_URL = "#" + DEFAULT_HIGHLIGHT;

  private final Map<String, TLcdKML22AbstractStyleSelector> fStyles = new HashMap<>();
  private final TLcdKML22Kml fKml;
  private final TLcdKML22Document fDocument;

  private final Map<String, TLcdKML22Schema> fSchemas = new HashMap<>();

  /**
   * Create a new KML builder instance.
   *
   * @param aName the name of the KML document.
   */
  public KML22ModelBuilder(String aName) {
    fDocument = KML22FeatureUtil.createDocument(aName);
    fDocument.setKMLSourceName(SOURCE_NAME);

    fKml = new TLcdKML22Kml(TLcdKML22DataTypes.KmlType);
    TLcdKML22ResourceProvider resourceProvider = new TLcdKML22ResourceProvider(new TLcdKML22ModelDecoder(), new TLcdInputStreamFactory());
    resourceProvider.getDocumentProvider().addDocument(fKml, SOURCE_NAME);
    fKml.setModelDescriptor(new TLcdKML22ModelDescriptor("Converted", resourceProvider));
    fKml.setAbstractFeatureGroup(fDocument);
    fKml.addModel(fDocument);

    addStyle(createDefaultNormalStyle());
    addStyle(createDefaultHighlightStyle());
    addStyle(KML22StyleUtil.createStyleMap(DEFAULT_MAP, DEFAULT_NORMAL_URL, DEFAULT_HIGHLIGHT_URL));
  }

  private TLcdKML22Style createDefaultNormalStyle() {
    TLcdKML22LineStyle rndLineStyle = KML22StyleUtil.createLineStyle(DEFAULT_NORMAL + "-line", new Color(0, 0, 0, 255), ELcdKML22ColorMode.RANDOM, 2);
    TLcdKML22PolyStyle rndPolyStyle = KML22StyleUtil.createPolyStyle(DEFAULT_NORMAL + "-poly", new Color(0, 0, 0, 160), ELcdKML22ColorMode.RANDOM, true, true);
    TLcdKML22LabelStyle labelStyle = KML22StyleUtil.createLabelStyle(DEFAULT_NORMAL + "-label", new Color(255, 255, 255, 255), ELcdKML22ColorMode.NORMAL, 1.0);
    TLcdKML22Style style = KML22StyleUtil.createStyle(DEFAULT_NORMAL, null, rndLineStyle, rndPolyStyle, labelStyle);
    return style;
  }

  private TLcdKML22Style createDefaultHighlightStyle() {
    TLcdKML22LineStyle lineStyle = KML22StyleUtil.createLineStyle(DEFAULT_HIGHLIGHT + "-line", new Color(240, 80, 20, 255), ELcdKML22ColorMode.NORMAL, 5);
    TLcdKML22PolyStyle polyStyle = KML22StyleUtil.createPolyStyle(DEFAULT_HIGHLIGHT + "-poly", new Color(240, 80, 20, 160), ELcdKML22ColorMode.NORMAL, true, true);
    TLcdKML22LabelStyle labelStyle = KML22StyleUtil.createLabelStyle(DEFAULT_NORMAL + "-label", new Color(255, 255, 255, 255), ELcdKML22ColorMode.NORMAL, 1.0);
    TLcdKML22Style style = KML22StyleUtil.createStyle(DEFAULT_HIGHLIGHT, null, lineStyle, polyStyle, labelStyle);
    return style;
  }

  /**
   * @return the {@link TLcdKML22Kml} instance.
   */
  public TLcdKML22Kml buildKMLModel() {
    return fKml;
  }

  /**
   * Register the given style selector as a shared style.
   * A shared style MUST HAVE a non-emtpy style id.
   *
   * @param aStyleSelector a style selector
   * @return this builder
   * @throws IllegalArgumentException if the given style selector doesn't have a valid style id.
   */
  public KML22ModelBuilder addStyle(TLcdKML22AbstractStyleSelector aStyleSelector) {
    String styleID = aStyleSelector.getId();
    if (styleID == null || styleID.isEmpty()) {
      throw new IllegalArgumentException("The style selector should have a non-empty style id.");
    }
    if (!fStyles.containsKey(styleID)) {
      fStyles.put(styleID, aStyleSelector);
      if (aStyleSelector.getKMLSourceName() == null) {
        aStyleSelector.setKMLSourceName(SOURCE_NAME);
      }
      fDocument.getAbstractStyleSelectorGroup().add(aStyleSelector);
    }
    return this;
  }

  /**
   * Get the registered shared style selector with the given id.
   *
   * @param aID a style selector id.
   * @return the registered shared style selector with the given id or {@code null} if none.
   */
  public TLcdKML22AbstractStyleSelector getStyle(String aID) {
    return fStyles.get(aID);
  }

  /**
   * Add a new {@link TLcdKML22Placemark KML feature} into this KML document.
   *
   * @param aPlacemark a {@link TLcdKML22Placemark} instance.
   * @return this builder
   */
  public KML22ModelBuilder addFeature(TLcdKML22Placemark aPlacemark) {
    if (aPlacemark.getKMLSourceName() == null) {
      aPlacemark.setKMLSourceName(SOURCE_NAME);
    }
    fDocument.addElement(aPlacemark, ILcdFireEventMode.NO_EVENT);
    return this;
  }

  /**
   * Add a new {@link TLcdKML22Schema KML schema} into this KML document.
   *
   * @param aSchema a KML schema
   * @return this builder
   */
  public KML22ModelBuilder addSchema(TLcdKML22Schema aSchema) {
    String schemaID = aSchema.getId();
    if (schemaID == null || schemaID.isEmpty()) {
      throw new IllegalArgumentException("The schema should have a non-empty style id.");
    }
    if (!fSchemas.containsKey(schemaID)) {
      fSchemas.put(schemaID, aSchema);
      fDocument.getSchema().add(aSchema);
    }
    return this;
  }

  /**
   * Get the registered schema with the given id.
   *
   * @param aID a schema id.
   * @return the registered schema with the given id or {@code null} if none.
   */
  public TLcdKML22Schema getSchema(String aID) {
    return fSchemas.get(aID);
  }
}
