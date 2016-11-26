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

import static com.luciad.util.concurrent.TLcdLockUtil.readLock;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import com.luciad.format.kml22.model.TLcdKML22Kml;
import com.luciad.format.kml22.model.feature.TLcdKML22Placemark;
import com.luciad.format.kml22.model.geometry.TLcdKML22AbstractGeometry;
import com.luciad.model.ILcdModel;
import com.luciad.util.ILcdFilter;
import com.luciad.util.concurrent.TLcdLockUtil;
import com.luciad.util.logging.ILcdLogger;
import com.luciad.util.logging.TLcdLoggerFactory;

/**
 * Class to convert a {@link ILcdModel} into a {@link TLcdKML22Kml KML Model}.
 * KML22Converter uses different converters that need to be registered  to convert a model element into
 * a {@link TLcdKML22Placemark}. Each converter is responsible to do the actual conversion of a specific aspect of KML.
 * <p> KML22Converter uses the following converters :
 * <li>
 *   <lu>instances of {@link IKML22PlacemarkConverter} to convert objects into placemarks.</lu>
 *   <lu>instances of {@link IKML22ShapeConverter} to convert objects into KML geometries</lu>
 *   <lu>instances of {@link IKML22StyleConverter} to retrieve KML style data from objects</lu>
 *   <lu>instances of {@link IKML22StyleConverter} to retrieve KML time data from objects</lu>
 * </li>
 * </p>
 */
public final class KML22Converter {

  private final static ILcdLogger sLogger = TLcdLoggerFactory.getLogger(KML22Converter.class);

  private ILcdFilter<Object> fFilter;
  private final List<IKML22ShapeConverter> fShapeConverters = new ArrayList<>();
  private final List<IKML22PlacemarkConverter> fFeatureConverters = new ArrayList<>();
  private final List<IKML22StyleConverter> fStyleConverters = new ArrayList<>();
  private final List<IKML22TimeConverter> fTimeConverters = new ArrayList<>();

  /**
   * Creates a new KML Converter instance.
   */
  public KML22Converter() {
    super();
  }

  /**
   * Returns the filter set to that is used.
   *
   * @return the filter, or <code>null</code> if none
   * @see #setFilter(ILcdFilter)
   */
  public ILcdFilter getFilter() {
    return fFilter;
  }

  /**
   * Sets the filter to be used. Model elements that are accepted by the filter will be converted into KML.
   *
   * @param aFilter to be set, or <code>null</code> for no filtering
   */
  public void setFilter(ILcdFilter<Object> aFilter) {
    fFilter = aFilter;
  }

  /**
   * Adds a feature converter to this instance.
   *
   * @param aPlacemarkConverter to be added
   */
  public void addPlacemarkConverter(IKML22PlacemarkConverter aPlacemarkConverter) {
    fFeatureConverters.add(aPlacemarkConverter);
  }

  /**
   * Remove a feature converter to this instance.
   *
   * @param aPlacemarkConverter to be remove
   */
  public void removePlacemarkConverter(IKML22PlacemarkConverter aPlacemarkConverter) {
    fFeatureConverters.remove(aPlacemarkConverter);
  }

  /**
   * Adds a shape converter to this instance.
   *
   * @param aShapeConverter to be added
   */
  public void addShapeConverter(IKML22ShapeConverter aShapeConverter) {
    fShapeConverters.add(aShapeConverter);
  }

  /**
   * Remove a shape converter to this instance.
   *
   * @param aShapeConverter to be remove
   */
  public void removeShapeConverter(IKML22ShapeConverter aShapeConverter) {
    fShapeConverters.remove(aShapeConverter);
  }

  /**
   * Adds a style converter to this instance.
   *
   * @param aStyleConverter to be added
   */
  public void addStyleConverter(IKML22StyleConverter aStyleConverter) {
    fStyleConverters.add(aStyleConverter);
  }

  /**
   * Remove a style converter to this instance.
   *
   * @param aStyleConverter to be remove
   */
  public void removeStyleConverter(IKML22StyleConverter aStyleConverter) {
    fStyleConverters.remove(aStyleConverter);
  }

  /**
   * Adds a time converter to this instance.
   *
   * @param aTimeConverter to be added
   */
  public void addTimeConverter(IKML22TimeConverter aTimeConverter) {
    fTimeConverters.add(aTimeConverter);
  }

  /**
   * Remove a time converter to this instance.
   *
   * @param aTimeConverter to be remove
   */
  public void removeTimeConverter(IKML22TimeConverter aTimeConverter) {
    fTimeConverters.remove(aTimeConverter);
  }

  /**
   * Converts a {@link ILcdModel} into a KML model. For each <code>aModel</code>'s element, the KML converter tries to convert the element
   * into a placemark and assign a geometry to the placemark. If the converter succeeds, it tries then to assign time
   * and style information to the newly created placemark.
   *
   * @param aModel model to be converted
   * @return a KML model
   */
  public TLcdKML22Kml convert(ILcdModel aModel) {
    KML22ModelBuilder kml22ModelBuilder = new KML22ModelBuilder(aModel.getModelDescriptor().getDisplayName());
    try (TLcdLockUtil.Lock autoUnlock = readLock(aModel)) {
      Enumeration elements = aModel.elements();
      while (elements.hasMoreElements()) {
        Object object = elements.nextElement();
        if (fFilter != null && !fFilter.accept(object)) {
          continue;
        }
        try {
          TLcdKML22Placemark placemark = convertIntoPlacemark(aModel, object, kml22ModelBuilder);
          TLcdKML22AbstractGeometry geometry = convertIntoShape(aModel, object);
          if (placemark == null || geometry == null) {
            continue;
          }
          placemark.setAbstractGeometryGroup(geometry);
          convertStyle(placemark, aModel, object, kml22ModelBuilder);
          convertTime(placemark, aModel, object);

          kml22ModelBuilder.addFeature(placemark);
        } catch (Exception e) {
          sLogger.error("Couldn't convert a" + object.getClass().getSimpleName() + "into a feature", e);
        }
      }
    }
    return kml22ModelBuilder.buildKMLModel();
  }

  private TLcdKML22Placemark convertIntoPlacemark(ILcdModel aModel, Object aObject, KML22ModelBuilder aKMLBuilder) {
    for (IKML22PlacemarkConverter shapeConverter : fFeatureConverters) {
      if (shapeConverter.canConvertIntoPlacemark(aModel, aObject)) {
        return shapeConverter.convertIntoPlacemark(aModel, aObject, aKMLBuilder);
      }
    }
    return null;
  }

  private TLcdKML22AbstractGeometry convertIntoShape(ILcdModel aModel, Object aObject) {
    for (IKML22ShapeConverter shapeConverter : fShapeConverters) {
      if (shapeConverter.canConvertIntoShape(aModel, aObject)) {
        return shapeConverter.convertIntoShape(aModel, aObject);
      }
    }
    return null;
  }

  private void convertStyle(TLcdKML22Placemark aFeature, ILcdModel aModel, Object aObject, KML22ModelBuilder aKml22ModelBuilder) {
    for (IKML22StyleConverter styleConverter : fStyleConverters) {
      if (styleConverter.canConvertStyle(aFeature, aModel, aObject)) {
        styleConverter.convertStyle(aFeature, aModel, aObject, aKml22ModelBuilder);
        return;
      }
    }
    aFeature.setStyleUrl(KML22ModelBuilder.DEFAULT_MAP_URL);
    sLogger.debug("No style could be converted for an instance of " + aObject.getClass().getSimpleName() + " , default style used.");
  }

  private void convertTime(TLcdKML22Placemark aFeature, ILcdModel aModel, Object aObject) {
    for (IKML22TimeConverter timeConverter : fTimeConverters) {
      if (timeConverter.canConvertTime(aFeature, aModel, aObject)) {
        timeConverter.convertTime(aFeature, aModel, aObject);
        return;
      }
    }
    sLogger.debug("No time info could be retrieved for an instance of " + aObject.getClass().getSimpleName() + ".");
  }
}
