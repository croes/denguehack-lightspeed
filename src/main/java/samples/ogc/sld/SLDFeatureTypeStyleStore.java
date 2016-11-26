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
package samples.ogc.sld;

import static com.luciad.ogc.filter.model.TLcdOGCFilterFactory.property;
import static com.luciad.ogc.filter.model.TLcdOGCFilterFactory.sub;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;

import com.luciad.datamodel.ILcdDataObject;
import com.luciad.datamodel.TLcdDataType;
import com.luciad.format.xml.TLcdXMLName;
import com.luciad.imaging.ALcdBandMeasurementSemantics;
import com.luciad.imaging.ALcdBandSemantics;
import com.luciad.imaging.ALcdImage;
import com.luciad.imaging.ILcdImageModelDescriptor;
import com.luciad.model.ILcdDataModelDescriptor;
import com.luciad.model.ILcdModel;
import com.luciad.ogc.filter.ILcdOGCFeatureIDRetriever;
import com.luciad.ogc.filter.evaluator.ILcdEvaluatorFunction;
import com.luciad.ogc.filter.evaluator.ILcdOGCFilterEvaluator;
import com.luciad.ogc.filter.evaluator.ILcdPropertyRetrieverProvider;
import com.luciad.ogc.filter.evaluator.TLcdOGCFilterContext;
import com.luciad.ogc.filter.evaluator.TLcdOGCFilterEvaluator;
import com.luciad.ogc.filter.evaluator.TLcdPropertyRetrieverUtil;
import com.luciad.ogc.filter.model.TLcdOGCBinaryOperator;
import com.luciad.ogc.filter.model.TLcdOGCFunction;
import com.luciad.ogc.filter.model.TLcdOGCPropertyName;
import com.luciad.ogc.sld.model.TLcdSLDFeatureTypeStyle;
import com.luciad.ogc.sld.model.TLcdSLDParameterValue;
import com.luciad.ogc.sld.model.functions.TLcdSLDCategorize;
import com.luciad.ogc.sld.model.functions.TLcdSLDInterpolate;
import com.luciad.ogc.sld.model.functions.TLcdSLDInterpolate.Method;
import com.luciad.ogc.sld.model.functions.TLcdSLDInterpolate.Mode;
import com.luciad.ogc.sld.model.functions.TLcdSLDInterpolationPoint;
import com.luciad.ogc.sld.view.gxy.TLcdSLDContext;
import com.luciad.ogc.sld.xml.TLcdSLDFeatureTypeStyleDecoder;
import com.luciad.reference.ILcdGeoReference;

/**
 * A class that creates and manages a number of {@link TLcdSLDFeatureTypeStyle}
 * instances. Styles can be retrieved for a given {@code ILcdModel} using the
 * {@link #getStylesForModel(ILcdModel)} method.
 * <p/>
 * The styles are either loaded from file, using a
 * {@link TLcdSLDFeatureTypeStyleDecoder}, or created from scratch using
 * {@link TLcdSLDFeatureTypeStyle.Builder}.
 *
 */
public class SLDFeatureTypeStyleStore {

  private static final String POPULATION_1990_PROPERTY_NAME = "POP1990";
  private static final String POPULATION_1996_PROPERTY_NAME = "POP1996";
  private static final String AREA_PROPERTY_NAME = "AREA";

  private final String fBaseDirectory;
  private TLcdSLDFeatureTypeStyleDecoder fSLDDecoder = new TLcdSLDFeatureTypeStyleDecoder();


  private String[] fVectorStyleFiles = new String[]{
      "Data/SLD/simple_line_sld.xml",
      "Data/SLD/simple_point_sld.xml",
      "Data/SLD/simple_polygon_sld.xml",
      "Data/SLD/polyline_text_sld.xml",
      "Data/SLD/rivers_sld.xml",
      "Data/SLD/graphic_stroke.xml",
      "Data/SLD/rivers_mississippi_sld.xml",
      "Data/SLD/scale_range_point_sld.xml",
      "Data/SLD/sized_point_sld.xml",
      "Data/SLD/man_woman.xml",
      "Data/SLD/states_population.xml",
  };

  private String[] fMeasurementRasterStyleFiles = new String[]{
      "Data/SLD/raster_color_map_sld.xml",
      "Data/SLD/raster_outline.xml",
  };

  private String[] fMultiBandRasterStyleFiles = new String[]{
      "Data/SLD/raster_band_selection.xml",
      };

  private TLcdSLDFeatureTypeStyle[] fVectorStyles;
  private List<TLcdSLDFeatureTypeStyle> fMeasurementRasterStyles;
  private List<TLcdSLDFeatureTypeStyle> fMultiBandRasterStyles;

  public SLDFeatureTypeStyleStore(String aBaseDirectory) {
    fBaseDirectory = aBaseDirectory;
    fVectorStyles = createVectorStyles();
    fMeasurementRasterStyles = decodeStyles(fMeasurementRasterStyleFiles);
    fMultiBandRasterStyles = decodeStyles(fMultiBandRasterStyleFiles);
  }


  public static TLcdSLDContext createSLDContext(ILcdModel aModel) {
    ILcdOGCFilterEvaluator filter_evaluator = createFilterEvaluator();
    ILcdOGCFeatureIDRetriever feature_id_retriever = createFeatureIdRetriever(aModel);
    ILcdPropertyRetrieverProvider property_retriever_provider = createPropertyRetrieverProvider(aModel);

    return new TLcdSLDContext(filter_evaluator,
                              (ILcdGeoReference) aModel.getModelReference(),
                              feature_id_retriever,
                              property_retriever_provider);
  }

  /**
   * This implementation returns an ILcdOGCFeatureIDRetriever that returns the first feature of an object, if model
   * objects implement ILcdDataObject, or the object itself converted to a string, otherwise.
   *
   * @param aModel the model to create a feature ID retriever for.
   * @return The ILcdOGCFeatureIDRetriever to be used for evaluating SLD painting instructions.
   */
  protected static ILcdOGCFeatureIDRetriever createFeatureIdRetriever(ILcdModel aModel) {
    return new FeatureIdRetriever();
  }

  // An ID retriever that expects the unique ID of an object to be in the
  // property at position 0, if the object implements ILcdDataObject.
  // Otherwise the result of the toString method is assumed unique.
  private static class FeatureIdRetriever implements ILcdOGCFeatureIDRetriever {
    public String retrieveFeatureID(Object aObject) {
      if (aObject instanceof ILcdDataObject) {
        return ((ILcdDataObject) aObject).getValue(((ILcdDataObject) aObject).getDataType().getDeclaredProperties().get(0)).toString();
      }
      return aObject.toString();
    }
  }


  /**
   * This implementation returns the default ILcdPropertyRetrieverProvider created by {@link TLcdPropertyRetrieverUtil}.
   *
   * @param aModel the model to create a property retriever provider for.
   * @return the ILcdPropertyRetrieverProvider to be used for evaluating SLD painting instructions.
   */
  protected static ILcdPropertyRetrieverProvider createPropertyRetrieverProvider(ILcdModel aModel) {
    return TLcdPropertyRetrieverUtil.getDefaultPropertyRetrieverProvider(aModel);
  }

  /**
   * Creates a default filter evaluator. Override this method if you want a
   * filter evaluator which can evaluate advanced spatial filtering or if you
   * want to add your own function.
   *
   * @return a default filter evaluator.
   */
  protected static ILcdOGCFilterEvaluator createFilterEvaluator() {
    final TLcdOGCFilterEvaluator evaluator = new TLcdOGCFilterEvaluator();
    evaluator.registerFunction(TLcdXMLName.getInstance(new QName("PopulationDensity")), new PopulationDensityEvaluator());
    return evaluator;
  }

  /**
   * Creates and returns a new set of styles, suitable for vector layers.
   */
  private TLcdSLDFeatureTypeStyle[] createVectorStyles() {
    final String[] styleFiles = fVectorStyleFiles;
    List<TLcdSLDFeatureTypeStyle> styles = decodeStyles(styleFiles);

    styles.add(createPopulationDensityStyle());
    styles.add(createCheckerBoardPolygonStyle());
    return styles.toArray(new TLcdSLDFeatureTypeStyle[styles.size()]);
  }

  private List<TLcdSLDFeatureTypeStyle> decodeStyles(final String[] styleFiles) {
    List<TLcdSLDFeatureTypeStyle> styles = new ArrayList<TLcdSLDFeatureTypeStyle>();
    for (int style_index = 0; style_index < styleFiles.length; style_index++) {
      String styleFile = fBaseDirectory + styleFiles[style_index];
      try {
        TLcdSLDFeatureTypeStyle style = loadSLDStyle(styleFile);
        styles.add(style);
      } catch (IOException e) {
        System.out.println("Could not decode " + styleFile);
        e.printStackTrace();
      }
    }
    return styles;
  }

  private static TLcdSLDFeatureTypeStyle createCheckerBoardPolygonStyle() {
    TLcdSLDFeatureTypeStyle style = TLcdSLDFeatureTypeStyle.newBuilder()
                                                           .name("Checkerboard polygons")
                                                           .featureTypeName("statesType")
                                                           .descriptionTitle("Checkerboard polygons")
                                                           .descriptionAbstract("A style that uses an inline SVG image as a graphic fill for polygons.")
                                                           .addRule()
                                                           .addPolygonSymbolizer()
                                                           .fillGraphic().addInlineSVGMark("samples/svg/checkerboard.svg").buildGraphic()
                                                           .strokeColor(Color.black)
                                                           .buildSymbolizer()
                                                           .buildRule()
                                                           .buildStyle();
    return style;
  }


  // decode a XML file to a TLcdSLDFeatureTypeStyle
  private TLcdSLDFeatureTypeStyle loadSLDStyle(String aSLDFile) throws IOException {
    return fSLDDecoder.decodeFeatureTypeStyle(aSLDFile);
  }

  private static TLcdSLDFeatureTypeStyle createStatePopulationGrowthStyle(TLcdDataType aStateType) {

    final TLcdSLDInterpolate interpolate = new TLcdSLDInterpolate();
    final TLcdOGCBinaryOperator subtract = sub(property(aStateType, aStateType.getDeclaredProperty(POPULATION_1996_PROPERTY_NAME)),
                                               property(aStateType, aStateType.getDeclaredProperty(POPULATION_1990_PROPERTY_NAME)));

    interpolate.setLookupValue(new TLcdSLDParameterValue(subtract));
    interpolate.setMode(Mode.LINEAR);

    interpolate.setMethod(Method.COLOR);
    interpolate.getInterpolationPoint().add(new TLcdSLDInterpolationPoint(-100000, new TLcdSLDParameterValue("#FF0000")));
    interpolate.getInterpolationPoint().add(new TLcdSLDInterpolationPoint(0., new TLcdSLDParameterValue("#AAAAAA")));
    interpolate.getInterpolationPoint().add(new TLcdSLDInterpolationPoint(800000, new TLcdSLDParameterValue("#00FF00")));
    return TLcdSLDFeatureTypeStyle.newBuilder(aStateType)
                                  .name("Population growth")
                                  .descriptionTitle("State Population Growth")
                                  .descriptionAbstract("A style that uses a color gradient to indicate the population growth in a state.")
                                  .addRule()
                                  .addPolygonSymbolizer()
                                  .fillColor(interpolate).fillOpacity(0.7)
                                  .strokeColor(Color.black).strokeWidth(2)
                                  .buildSymbolizer()
                                  .buildRule()
                                  .buildStyle();
  }

  /**
   * Returns the styles that can by applied to the data in a given model.
   *
   * @param aModel A model
   * @return A list of styles, or null if no styles apply.
   */
  public List<TLcdSLDFeatureTypeStyle> getStylesForModel(ILcdModel aModel) {
    if (aModel == null) {
      return null;
    }
    final ArrayList<TLcdSLDFeatureTypeStyle> result = new ArrayList<TLcdSLDFeatureTypeStyle>();
    if (aModel.getModelDescriptor() instanceof ILcdDataModelDescriptor) {
      Set<TLcdDataType> types = ((ILcdDataModelDescriptor) aModel.getModelDescriptor()).getModelElementTypes();
      if (types != null && types.size() == 1) {
        final TLcdDataType modelType = types.iterator().next();
        for (TLcdSLDFeatureTypeStyle style : fVectorStyles) {
          if (modelType.getName().equals(style.getFeatureTypeName())) {
            result.add(style);
          }
        }
        if ("statesType".equals(modelType.getName())) {
          result.add(createStatePopulationGrowthStyle(modelType));
        }
      }
      addDefaultStyles(result);
    } else if (aModel.getModelDescriptor() instanceof ILcdImageModelDescriptor ) {
      ALcdImage image = ALcdImage.fromDomainObject(aModel.elements().nextElement());
      List<ALcdBandSemantics> bandSemantics = image.getConfiguration().getSemantics();
      if(bandSemantics.size() == 1 && bandSemantics.get(0) instanceof ALcdBandMeasurementSemantics) {
        for (TLcdSLDFeatureTypeStyle rasterStyle : fMeasurementRasterStyles) {
          result.add(rasterStyle);
        }
      }
      else if(bandSemantics.size() > 3) {
        for (TLcdSLDFeatureTypeStyle rasterStyle : fMultiBandRasterStyles) {
          result.add(rasterStyle);
        }
      }

    } else {
      return null;
    }
    return result;

  }

  private void addDefaultStyles(final ArrayList<TLcdSLDFeatureTypeStyle> result) {
    for (TLcdSLDFeatureTypeStyle style : fVectorStyles) {
      if (style.getFeatureTypeName() == null) {
        result.add(style);
      }
    }
  }

  /**
   * Creates a style that colors states in 10 different categories based on
   * their population density. A 'Categorize' expression is used to efficiently
   * map a density onto a category. A custom function is used to compute the
   * population density.
   *
   * @return a feature type style
   */
  private TLcdSLDFeatureTypeStyle createPopulationDensityStyle() {

    TLcdSLDFeatureTypeStyle.Builder<TLcdSLDFeatureTypeStyle> styleBuilder = TLcdSLDFeatureTypeStyle.newBuilder();
    styleBuilder.descriptionTitle("Population density");
    styleBuilder.descriptionAbstract("A style that colors states in 10 different categories based on" +
                                     " their population density. A 'Categorize' expression is used to efficiently" +
                                     " map a density onto a category. A custom function is used to compute the" +
                                     " population density.");
    styleBuilder.featureTypeName("statesType");
    // nr of different range-code pairs
    int nr_of_range_code_pairs = 10;

    final TLcdSLDCategorize populationDensityCategories = new TLcdSLDCategorize();
    populationDensityCategories.setLookupValue(new TLcdSLDParameterValue(createPopulationDensityFunction()));
    populationDensityCategories.setFirstValue(new TLcdSLDParameterValue("#FFFFFF"));

    for (int i = 2; i < nr_of_range_code_pairs; i++) {
      Color color = colorForFactor((double) i / (double) nr_of_range_code_pairs);
      final String colorString = "#" + Integer.toHexString(color.getRGB()).substring(2).toUpperCase();
      populationDensityCategories.addThreshold(new TLcdSLDParameterValue(Double.toString((double) i / (double) nr_of_range_code_pairs)), new TLcdSLDParameterValue(colorString));
    }

    final TLcdSLDFeatureTypeStyle style = styleBuilder.addRule()
                                                      .addPolygonSymbolizer()
                                                      .fillColor(populationDensityCategories)
                                                      .buildSymbolizer().buildRule().buildStyle();
    return style;
  }

  private static Color colorForFactor(double aFactor) {
    int gray = 55 + (200 - (int) (200 * (aFactor * aFactor)));

    int rgb = 0xff000000 | ((gray << 16) & 0x00ff0000) | ((gray << 8) & 0x0000ff00) | (gray & 0x000000ff);
    return new Color(rgb);
  }

  private TLcdOGCFunction createPopulationDensityFunction() {
    TLcdOGCFunction function = new TLcdOGCFunction();
    function.setName("PopulationDensity");
    function.addArgument(new TLcdOGCPropertyName(TLcdXMLName.getInstance(new QName(POPULATION_1996_PROPERTY_NAME))));
    function.addArgument(new TLcdOGCPropertyName(TLcdXMLName.getInstance(new QName(AREA_PROPERTY_NAME))));

    return function;
  }

  private static class PopulationDensityEvaluator implements ILcdEvaluatorFunction {

    private static final double DENSITY_MAX = 2.5;

    public Object apply(Object[] aObjects, Object aObject, TLcdOGCFilterContext aFilterContext) {
      double population = Double.parseDouble(aObjects[0].toString());
      double area = Double.parseDouble(aObjects[1].toString());

      double value = Math.log(1 + population / area) / (Math.log(10) * DENSITY_MAX);

      if (value > 1.0) {
        value = 1.0;
      }

      return value;
    }

    public int getArgumentCount() {
      return 2;
    }
  }
}
