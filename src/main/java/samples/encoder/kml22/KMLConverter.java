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
package samples.encoder.kml22;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import com.luciad.datamodel.ILcdDataObject;
import com.luciad.datamodel.TLcdDataProperty;
import com.luciad.format.kml22.model.TLcdKML22DataTypes;
import com.luciad.format.kml22.model.extendeddata.TLcdKML22Schema;
import com.luciad.format.kml22.model.feature.TLcdKML22AbstractFeature;
import com.luciad.format.kml22.model.feature.TLcdKML22Placemark;
import com.luciad.format.kml22.model.geometry.TLcdKML22AbstractGeometry;
import com.luciad.format.kml22.model.style.ELcdKML22ColorMode;
import com.luciad.format.kml22.model.style.TLcdKML22AbstractStyleSelector;
import com.luciad.format.kml22.model.style.TLcdKML22IconStyle;
import com.luciad.format.kml22.model.style.TLcdKML22LineStyle;
import com.luciad.format.kml22.model.style.TLcdKML22PolyStyle;
import com.luciad.format.kml22.model.style.TLcdKML22Style;
import com.luciad.format.kml22.model.style.TLcdKML22StyleMap;
import com.luciad.format.kml22.model.time.TLcdKML22TimeStamp;
import com.luciad.format.kml22.model.util.TLcdKML22BasicLink;
import com.luciad.format.kml22.xml.TLcdKML22ModelEncoder;
import com.luciad.format.shp.TLcdSHPModelDecoder;
import com.luciad.format.shp.TLcdSHPModelDescriptor;
import com.luciad.io.TLcdIOUtil;
import com.luciad.model.ILcdModel;
import com.luciad.shape.ILcdBounded;
import com.luciad.shape.ILcdBounds;
import com.luciad.shape.ILcdComplexPolygon;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.ILcdPolygon;
import com.luciad.shape.ILcdShape;
import com.luciad.shape.ILcdShapeList;
import com.luciad.shape.TLcdShapeList;
import com.luciad.shape.shape2D.ILcd2DEditablePoint;
import com.luciad.shape.shape2D.TLcdLonLatPolyline;
import com.luciad.util.ILcdFilter;

import samples.encoder.kml22.AirspaceModelFactory.AirspaceModelDescriptor;
import samples.encoder.kml22.converter.*;

/**
 * This sample demonstrates how to convert and encode models to KML.
 */
public class KMLConverter {

  /**
   * Shape files we want to convert
   */
  private static final String TRACK_FILE = "Data/Shp/Tracks/500k/tracks_500K.shp";
  private static final String STATES_FILE = "Data/Shp/Usa/states.shp";

  public static class TrackPlacemarkConverter implements IKML22PlacemarkConverter {

    private Set<String> fFlights = new HashSet<>();
    private static final int NB_FLIGHTS = 30;

    @Override
    public boolean canConvertIntoPlacemark(ILcdModel aModel, Object aObject) {
      if (aModel.getModelDescriptor() instanceof TLcdSHPModelDescriptor) {
        return aObject instanceof ILcd2DEditablePoint && aObject instanceof ILcdDataObject;
      }
      return false;
    }

    @Override
    public TLcdKML22Placemark convertIntoPlacemark(ILcdModel aModel, Object aObject, KML22ModelBuilder aKMLBuilder) throws IllegalArgumentException {
      if (!(aModel.getModelDescriptor() instanceof TLcdSHPModelDescriptor)) {
        throw new IllegalArgumentException("Model is not a SHP model.");
      }
      if (!(aObject instanceof ILcd2DEditablePoint && aObject instanceof ILcdDataObject)) {
        throw new IllegalArgumentException("Object isn't a track.");
      }

      ILcdDataObject dataObject = (ILcdDataObject) aObject;
      String flightNr = dataObject.getValue("Flight_nr").toString();
      if (!fFlights.contains(flightNr)) {
        if (fFlights.size() < NB_FLIGHTS) {
          fFlights.add(flightNr);
        } else {
          return null; // We convert only tracks from the first 30 flights.
        }
      }

      String name = flightNr + " " + dataObject.getValue("Time");
      TLcdKML22Placemark placemark = KML22FeatureUtil.createPlacemark(name);
      TLcdKML22Schema schema = aKMLBuilder.getSchema(dataObject.getDataType().getName());
      if (schema == null) {
        schema = KML22FeatureUtil.createSchema(dataObject.getDataType());
        aKMLBuilder.addSchema(schema);
      }
      placemark.setExtendedData(KML22FeatureUtil.createExtendedData(dataObject, "#" + dataObject.getDataType().getName()));
      return placemark;
    }
  }

  public static class TrackStyleConverter implements IKML22StyleConverter {

    private final static String STYLE_ID = "TRACK_STYLE";
    private final static String NORMAL_ID = "TRACK_STYLE_NORMAL";
    private final static String SELECTED_ID = "TRACK_STYLE_SELECTED";

    @Override
    public boolean canConvertStyle(TLcdKML22AbstractFeature aFeature, ILcdModel aModel, Object aObject) {
      return aObject instanceof ILcd2DEditablePoint;
    }

    @Override
    public void convertStyle(TLcdKML22AbstractFeature aFeature, ILcdModel aModel, Object aObject, KML22ModelBuilder aKMLBuilder) {
      if (!canConvertStyle(aFeature, aModel, aObject)) {
        throw new IllegalArgumentException("Cannot assign a style to this feature");
      }
      TLcdKML22AbstractStyleSelector style = aKMLBuilder.getStyle(STYLE_ID);
      if (style == null) {
        TLcdKML22IconStyle normalIconStyle = new TLcdKML22IconStyle(TLcdKML22DataTypes.IconStyleType);
        TLcdKML22BasicLink basicLink = new TLcdKML22BasicLink(TLcdKML22DataTypes.IconStyleBasicLinkType);
        basicLink.setHref("images/mif20_airplane.gif");
        normalIconStyle.setIconLink(basicLink);
        normalIconStyle.setColor(Color.YELLOW);
        normalIconStyle.setScale(0.5);
        normalIconStyle.setId(NORMAL_ID + "_ICON_STYLE");
        TLcdKML22Style normalStyle = KML22StyleUtil.createStyle(NORMAL_ID, normalIconStyle, null, null, null);

        TLcdKML22IconStyle selectedIconStyle = new TLcdKML22IconStyle(TLcdKML22DataTypes.IconStyleType);
        selectedIconStyle.setIconLink(basicLink);
        selectedIconStyle.setColor(Color.RED);
        selectedIconStyle.setId(SELECTED_ID + "_ICON_STYLE");
        TLcdKML22Style selectedStyle = KML22StyleUtil.createStyle(SELECTED_ID, selectedIconStyle, null, null, null);

        TLcdKML22StyleMap styleMap = KML22StyleUtil.createStyleMap(STYLE_ID, "#" + NORMAL_ID, "#" + SELECTED_ID);
        aKMLBuilder.addStyle(normalStyle);
        aKMLBuilder.addStyle(selectedStyle);
        aKMLBuilder.addStyle(styleMap);
      }
      aFeature.setStyleUrl("#" + STYLE_ID);
    }

  }

  public static class StateConverter implements IKML22ShapeConverter, IKML22PlacemarkConverter {

    private KML22ShapeConverter fDelegate;

    public StateConverter(KML22ShapeConverter aDelegate) {
      fDelegate = aDelegate;
    }

    @Override
    public boolean canConvertIntoPlacemark(ILcdModel aModel, Object aObject) {
      if (aModel.getModelDescriptor() instanceof TLcdSHPModelDescriptor) {
        return aObject instanceof ILcdShapeList && aObject instanceof ILcdDataObject;
      }
      return false;
    }

    @Override
    public boolean canConvertIntoShape(ILcdModel aModel, Object aObject) {
      return canConvertIntoPlacemark(aModel, aObject);
    }

    @Override
    public TLcdKML22Placemark convertIntoPlacemark(ILcdModel aModel, Object aObject, KML22ModelBuilder aKMLBuilder) throws IllegalArgumentException {
      if (!(aModel.getModelDescriptor() instanceof TLcdSHPModelDescriptor)) {
        throw new IllegalArgumentException("Model is not a SHP model.");
      }
      if (!(aObject instanceof ILcdShapeList && aObject instanceof ILcdDataObject)) {
        throw new IllegalArgumentException("Object isn't a state.");
      }

      ILcdDataObject dataObject = (ILcdDataObject) aObject;
      TLcdKML22Placemark placemark = KML22FeatureUtil.createPlacemark(dataObject.getValue("STATE_NAME").toString());
      TLcdKML22Schema schema = aKMLBuilder.getSchema(dataObject.getDataType().getName());
      if (schema == null) {
        schema = KML22FeatureUtil.createSchema(dataObject.getDataType());
        aKMLBuilder.addSchema(schema);
      }
      placemark.setExtendedData(KML22FeatureUtil.createExtendedData(dataObject, "#" + dataObject.getDataType().getName()));
      return placemark;
    }

    @Override
    public TLcdKML22AbstractGeometry convertIntoShape(ILcdModel aModel, Object aObject) {
      if (!canConvertIntoShape(aModel, aObject)) {
        throw new IllegalArgumentException("Object isn't a state.");
      }
      /**
       * The shapes from the shape files cannot be directly encoded into KML because
       * some are {@link ILcdComplexPolygon} with multiple outer rings. For the sample purpose,
       * we convert all the shapes into polylines.
       */
      return fDelegate.convertIntoShape(aModel, transformShapeIntoPolylines((ILcdShape) aObject));
    }

    private ILcdShape transformShapeIntoPolylines(ILcdShape aShape) {
      if (aShape instanceof ILcdShapeList) {
        TLcdShapeList shapeList = new TLcdShapeList();
        for (int i = 0; i < ((ILcdShapeList) aShape).getShapeCount(); i++) {
          shapeList.addShape(transformShapeIntoPolylines(((ILcdShapeList) aShape).getShape(i)));
        }
        return shapeList;
      }
      if (aShape instanceof ILcdComplexPolygon) {
        TLcdShapeList shapeList = new TLcdShapeList();
        for (int i = 0; i < ((ILcdComplexPolygon) aShape).getPolygonCount(); i++) {
          shapeList.addShape(transformShapeIntoPolylines(((ILcdComplexPolygon) aShape).getPolygon(i)));
        }
        return shapeList;
      }
      if (aShape instanceof ILcdPolygon) {
        TLcdLonLatPolyline polyline = new TLcdLonLatPolyline();
        for (int i = 0; i < ((ILcdPolygon) aShape).getPointCount(); i++) {
          ILcdPoint point = ((ILcdPolygon) aShape).getPoint(i);
          polyline.insert2DPoint(i, point.getX(), point.getY());
        }
        ILcdPoint firstPoint = polyline.getPoint(0);
        polyline.insert2DPoint(polyline.getPointCount(), firstPoint.getX(), firstPoint.getY());
        return polyline;
      }
      throw new IllegalArgumentException("The shape isn't a polygon.");
    }
  }

  public static class TrackTimeConverter implements IKML22TimeConverter {

    @Override
    public boolean canConvertTime(TLcdKML22AbstractFeature aFeature, ILcdModel aModel, Object aObject) {
      if (aObject instanceof ILcdDataObject && aObject instanceof ILcdPoint) {
        ILcdDataObject dataObject = (ILcdDataObject) aObject;
        return dataObject.getDataType().getProperty("Time") != null;
      }
      return false;
    }

    @Override
    public void convertTime(TLcdKML22AbstractFeature aFeature, ILcdModel aModel, Object aObject) {
      if (!canConvertTime(aFeature, aModel, aObject)) {
        throw new IllegalArgumentException("Cannot assign a time primitive to this feature");
      }

      ILcdDataObject dataObject = (ILcdDataObject) aObject;
      TLcdDataProperty timeProperty = dataObject.getDataType().getProperty("Time");

      long time = ((Number) dataObject.getValue(timeProperty)).longValue();
      TLcdKML22TimeStamp timeStamp = new TLcdKML22TimeStamp(TLcdKML22DataTypes.TimeStampType);
      timeStamp.setBeginTime(time);
      aFeature.setAbstractTimePrimitiveGroup(timeStamp);

    }
  }

  public static class AirspacePlacemarkConverter implements IKML22PlacemarkConverter {

    @Override
    public boolean canConvertIntoPlacemark(ILcdModel aModel, Object aObject) {
      return aModel.getModelDescriptor() instanceof AirspaceModelDescriptor;
    }

    @Override
    public TLcdKML22Placemark convertIntoPlacemark(ILcdModel aModel, Object aObject, KML22ModelBuilder aKMLBuilder) throws IllegalArgumentException {
      if (!canConvertIntoPlacemark(aModel, aObject)) {
        throw new IllegalArgumentException("Object isn't a airspace.");
      }
      ILcdDataObject airspace = (ILcdDataObject) aObject;
      TLcdKML22Placemark placemark = KML22FeatureUtil.createPlacemark(airspace.getValue("Name").toString());
      TLcdKML22Schema schema = aKMLBuilder.getSchema(airspace.getDataType().getName());
      if (schema == null) {
        schema = KML22FeatureUtil.createSchema(airspace.getDataType());
        aKMLBuilder.addSchema(schema);
      }
      placemark.setExtendedData(KML22FeatureUtil.createExtendedData(airspace, "#" + airspace.getDataType().getName()));
      return placemark;
    }
  }

  public static class AirspaceStyleConverter implements IKML22StyleConverter {

    private static final String STYLE_A = "CLASS_A";
    private static final String STYLE_A_NORMAL = "CLASS_A_NORMAL";

    private static final String STYLE_OTHER = "CLASS_OTHER";
    private static final String STYLE_OTHER_NORMAL = "CLASS_OTHER_NORMAL";

    @Override
    public boolean canConvertStyle(TLcdKML22AbstractFeature aFeature, ILcdModel aModel, Object aObject) {
      return aModel.getModelDescriptor() instanceof AirspaceModelDescriptor;
    }

    @Override
    public void convertStyle(TLcdKML22AbstractFeature aFeature, ILcdModel aModel, Object aObject, KML22ModelBuilder aKMLBuilder) {
      if (!canConvertStyle(aFeature, aModel, aObject)) {
        throw new IllegalArgumentException("Cannot assign a style to this feature");
      }
      ILcdDataObject airspace = (ILcdDataObject) aObject;

      if ("A".equals(airspace.getValue("Class"))) {
        TLcdKML22AbstractStyleSelector style = aKMLBuilder.getStyle(STYLE_A);
        if (style == null) {
          TLcdKML22PolyStyle polyStyle = KML22StyleUtil.createPolyStyle(STYLE_A_NORMAL + "-poly", new Color(176, 58, 80, 100), ELcdKML22ColorMode.NORMAL, true, true);
          TLcdKML22LineStyle lineStyle = KML22StyleUtil.createLineStyle(STYLE_A_NORMAL + "-line", new Color(176, 58, 80, 255), ELcdKML22ColorMode.NORMAL, 2);
          TLcdKML22Style normalStyle = KML22StyleUtil.createStyle(STYLE_A_NORMAL, null, lineStyle, polyStyle, null);

          TLcdKML22StyleMap styleMap = KML22StyleUtil.createStyleMap(STYLE_A, "#" + STYLE_A_NORMAL, KML22ModelBuilder.DEFAULT_HIGHLIGHT_URL);
          aKMLBuilder.addStyle(normalStyle);
          aKMLBuilder.addStyle(styleMap);
        }
        aFeature.setStyleUrl("#" + STYLE_A);
      } else {
        TLcdKML22AbstractStyleSelector style = aKMLBuilder.getStyle(STYLE_OTHER);
        if (style == null) {
          TLcdKML22PolyStyle polyStyle = KML22StyleUtil.createPolyStyle(STYLE_OTHER_NORMAL + "-poly", new Color(43, 71, 219, 100), ELcdKML22ColorMode.NORMAL, true, true);
          TLcdKML22LineStyle lineStyle = KML22StyleUtil.createLineStyle(STYLE_OTHER_NORMAL + "-line", new Color(43, 71, 219, 255), ELcdKML22ColorMode.NORMAL, 2);
          TLcdKML22Style normalStyle = KML22StyleUtil.createStyle(STYLE_OTHER_NORMAL, null, lineStyle, polyStyle, null);

          TLcdKML22StyleMap styleMap = KML22StyleUtil.createStyleMap(STYLE_OTHER, "#" + STYLE_OTHER_NORMAL, KML22ModelBuilder.DEFAULT_HIGHLIGHT_URL);
          aKMLBuilder.addStyle(normalStyle);
          aKMLBuilder.addStyle(styleMap);
        }
        aFeature.setStyleUrl("#" + STYLE_OTHER);
      }
    }

  }

  public static class BoundFilter implements ILcdFilter<Object> {

    private ILcdBounds fBounds;

    public BoundFilter(ILcdBounds aBounds) {
      fBounds = aBounds;
    }

    @Override
    public boolean accept(Object aObject) {
      if (aObject instanceof ILcdPoint) {
        ILcdPoint point = (ILcdPoint) aObject;
        return fBounds.contains2D(point);
      } else if (aObject instanceof ILcdBounded) {
        return fBounds.interacts2D(((ILcdBounded) aObject).getBounds());
      }
      return false;
    }
  }

  public static void exportAirspace(String aDestination) throws IOException {
    KML22Converter kml22Converter = new KML22Converter();
    kml22Converter.addShapeConverter(new KML22ShapeConverter());
    kml22Converter.addPlacemarkConverter(new AirspacePlacemarkConverter());
    kml22Converter.addTimeConverter(new TimeBoundsTimeConverter());
    kml22Converter.addStyleConverter(new AirspaceStyleConverter());

    ILcdModel kmlModel = kml22Converter.convert(AirspaceModelFactory.createModel());
    new TLcdKML22ModelEncoder().export(kmlModel, aDestination);
  }

  public static void exportTracks(ILcdModel aTrackModel, String aDestination, ILcdBounds aBounds) throws IOException {
    KML22Converter kml22Converter = new KML22Converter();
    KML22ShapeConverter shapeConverter = new KML22ShapeConverter();

    kml22Converter.addShapeConverter(shapeConverter);
    kml22Converter.addPlacemarkConverter(new TrackPlacemarkConverter());
    kml22Converter.addStyleConverter(new TrackStyleConverter());
    kml22Converter.addTimeConverter(new TrackTimeConverter());
    kml22Converter.setFilter(new BoundFilter(aBounds));

    ILcdModel kmlModel = kml22Converter.convert(aTrackModel);
    new TLcdKML22ModelEncoder().export(kmlModel, aDestination);
  }

  public static void exportStates(ILcdModel aStatesModel, String aDestination, ILcdBounds aBounds) throws IOException {
    KML22Converter kml22Converter = new KML22Converter();
    KML22ShapeConverter shapeConverter = new KML22ShapeConverter();
    StateConverter stateConverter = new StateConverter(shapeConverter);
    kml22Converter.addPlacemarkConverter(stateConverter);
    kml22Converter.addShapeConverter(stateConverter);
    kml22Converter.setFilter(new BoundFilter(aBounds));

    ILcdModel kmlModel = kml22Converter.convert(aStatesModel);
    new TLcdKML22ModelEncoder().export(kmlModel, aDestination);
  }

  public static void main(final String[] aArgs) throws Exception {

    System.out.println("This sample illustrates how to convert specific vector data into KML.");
    System.out.println("For more details, refer to the Developer's Guide or the sample readme file.");

    TLcdSHPModelDecoder decoder = new TLcdSHPModelDecoder();
    TLcdIOUtil ioUtil = new TLcdIOUtil();
    ioUtil.setSourceName("Data" + File.separator + "Kml");
    String destinationDirectory = ioUtil.getURL() != null ? ioUtil.getURL().toURI().getPath() : ioUtil.getFileName();
    ILcdBounds airspaceBounds = AirspaceModelFactory.createModel().getBounds();

    System.out.println("Decodes a \"tracks\" SHP file, and exports it as a KML file.");
    ioUtil.setSourceName(TRACK_FILE);
    String fileName = ioUtil.getURL() != null ? ioUtil.getURL().toURI().getPath() : ioUtil.getFileName();
    exportTracks(decoder.decode(fileName), destinationDirectory + File.separator + "tracks.kml", airspaceBounds);

    System.out.println("Decodes a \"USA States\" SHP file, and exports it as a KML file.");
    ioUtil.setSourceName(STATES_FILE);
    fileName = ioUtil.getURL() != null ? ioUtil.getURL().toURI().getPath() : ioUtil.getFileName();
    exportStates(decoder.decode(fileName), destinationDirectory + File.separator + "states.kml", airspaceBounds);

    System.out.println("Creates an in-memory airspaces model, and exports it as a KML file.");
    exportAirspace(destinationDirectory + File.separator + "airspaces.kml");

    System.out.println("The different files are now in the directory : " + destinationDirectory);
  }
}
