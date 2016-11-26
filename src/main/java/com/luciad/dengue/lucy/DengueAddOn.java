package com.luciad.dengue.lucy;

import com.luciad.contour.TLcdComplexPolygonContourFinder;
import com.luciad.contour.TLcdIntervalContour;
import com.luciad.contour.TLcdLonLatComplexPolygonContourBuilder;
import com.luciad.contour.TLcdValuedContour;
import com.luciad.dengue.util.DateUtils;
import com.luciad.dengue.util.RasterStyler;
import com.luciad.dengue.util.TimeBasedModel;
import com.luciad.dengue.view.DengueFilter;
import com.luciad.dengue.weather.*;
import com.luciad.format.geojson.TLcdGeoJsonModelDecoder;
import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.lucy.ILcyLucyEnvListener;
import com.luciad.lucy.TLcyLucyEnvEvent;
import com.luciad.lucy.addons.ALcyPreferencesAddOn;
import com.luciad.lucy.map.ILcyGenericMapComponent;
import com.luciad.lucy.map.ILcyGenericMapManagerListener;
import com.luciad.lucy.map.TLcyGenericMapManagerEvent;
import com.luciad.lucy.map.lightspeed.TLcyLspMapManager;
import com.luciad.lucy.util.ALcyTool;
import com.luciad.lucy.util.TLcyVetoException;
import com.luciad.model.ILcdModel;
import com.luciad.model.TLcd2DBoundsIndexedModel;
import com.luciad.model.TLcdModelDescriptor;
import com.luciad.model.TLcdVectorModel;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.shape.ILcdShape;
import com.luciad.util.ILcdFunction;
import com.luciad.util.TLcdColorMap;
import com.luciad.util.TLcdInterval;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.layer.ILspInteractivePaintableLayer;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.TLspPaintRepresentationState;
import com.luciad.view.lightspeed.layer.TLspPaintState;
import com.luciad.view.lightspeed.layer.raster.TLspRasterLayerBuilder;
import com.luciad.view.lightspeed.layer.shape.TLspShapeLayerBuilder;
import samples.gxy.contour.ContourLevels;
import samples.lightspeed.timeview.TimeSlider;

import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Created by tomc on 25/11/2016.
 */
public class DengueAddOn extends ALcyPreferencesAddOn {

  private TimeViewPanelTool timeViewPanelTool;

  public DengueAddOn() {
    super(ALcyTool.getLongPrefixWithClassName(DengueAddOn.class),
          ALcyTool.getShortPrefix(DengueAddOn.class));
  }

  @Override
  public void plugInto(ILcyLucyEnv aLucyEnv) {
    super.plugInto(aLucyEnv);

    timeViewPanelTool = new TimeViewPanelTool(getPreferences(), getLongPrefix(), getShortPrefix());
    timeViewPanelTool.plugInto(getLucyEnv());

    DengueFilter dengueFilter = new DengueFilter();
    FilterPanelTool filterTool = new FilterPanelTool(getPreferences(), getLongPrefix(), getShortPrefix(), dengueFilter);
    filterTool.plugInto(aLucyEnv);

    System.out.println("Loading data loader");

    TLcyLspMapManager mapManager = aLucyEnv.getService(TLcyLspMapManager.class);
    mapManager.addMapManagerListener(new DataLoader() {
      @Override
      public void viewLoaded(ILspView aView) throws IOException {
        loadData(aView);
      }
    }, true);
  }

  private void loadData(ILspView aView) throws IOException {
    //Weather
    System.out.println("Loading weather data");
    addWeatherData(new WeatherModelFactory(), WeatherModelFactory.PRECIPITATION,
                   new double[]{
                       Short.MIN_VALUE,
                       0.0,
                       1000.0,
                       Short.MAX_VALUE
                   },
                   new Color[]{
                       new Color(0, true),
                       new Color(0, true),
                       new Color(0, 13, 52, 128),
                       new Color(0, true),
                       },
                   aView,
                   timeViewPanelTool.getTimeSlider());

      //Contours
    TLcd2DBoundsIndexedModel contourModel = new TLcd2DBoundsIndexedModel();
    contourModel.setModelDescriptor(new TLcdModelDescriptor("Contour", "Contour", "Contour"));
    contourModel.setModelReference(new TLcdGeodeticReference());
    ILspLayer contourLayer = TLspShapeLayerBuilder
      .newBuilder()
      .model(contourModel)
      .build();
    aView.addLayer(contourLayer);

    ContourLevels contourLevels = new ContourLevels();
    double[] specialValues = contourLevels.getSpecialValues();
    double[] levelValues = contourLevels.getLevelValues(true);

    TLcdComplexPolygonContourFinder contourFinder = new TLcdComplexPolygonContourFinder() {
      @Override
      protected boolean isSpecialValue(double aValue) {
        for (int i = 0; i < specialValues.length; i++) {
          double specialValue = specialValues[i];
          if (aValue == specialValue) {
            return true;
          }
        }
        return false;
      }
    };
    TLcdLonLatComplexPolygonContourBuilder contourBuilder = new TLcdLonLatComplexPolygonContourBuilder(new ILcdFunction() {
      @Override
      public boolean applyOn(Object aObject) throws IllegalArgumentException {
        if (aObject instanceof TLcdIntervalContour) {
          TLcdIntervalContour intervalContour = (TLcdIntervalContour) aObject;
          ILcdShape shape = intervalContour.getShape();
          contourModel.addElement(shape, ILcdModel.FIRE_NOW);
        }
        if (aObject instanceof TLcdValuedContour) {
          TLcdValuedContour valuedContour = (TLcdValuedContour) aObject;
          ILcdShape shape = valuedContour.getShape();
          contourModel.addElement(shape, ILcdModel.FIRE_NOW);
        }
        return true;
      }
    });

    // Contours for weather..
    /*Object element = weatherModel.elements().nextElement();
    if(element instanceof ILcdRaster) {
      ILcdRaster raster = (ILcdRaster)element;

      ILcdMatrixView matrixView = new RasterMatrixView(raster, raster.getBounds());

      // INTERVAL: each complex polygon is the area in which the height is inside an interval, so the result will be disjoint complex polygons
      // HIGHER: each complex polygon is the area in which the height is higher than the value, so the result will be overlapping complex polygons
      TLcdComplexPolygonContourFinder.IntervalMode mode = TLcdComplexPolygonContourFinder.IntervalMode.INTERVAL;

      contourFinder.findContours(contourBuilder, matrixView, mode, levelValues, specialValues);
    }*/

    //Dengue Malaysia
    TLcdGeoJsonModelDecoder modelDecoder = new TLcdGeoJsonModelDecoder();
    ILcdModel malasya = modelDecoder.decode("malasia_cleaned.geojson");

    MalaysiaDengueStyler malasyastyler = new MalaysiaDengueStyler();
    timeViewPanelTool.getTimeSlider().addChangeListener(tLcdChangeEvent -> malasyastyler.setTime(timeViewPanelTool.getTimeSlider().getTime()));
    ILspInteractivePaintableLayer dengueLayer = TLspShapeLayerBuilder.newBuilder().model(malasya).bodyStyler(TLspPaintState.REGULAR, malasyastyler).build();
    aView.addLayer(dengueLayer);

    //Daily weather stations
    Map<WeatherStation, List<DailyWeatherReport>> weatherStationListMap = new StationDailyWeatherDecoder().decodeWeather();
    TLcdVectorModel dailyWeatherModel = new TLcdVectorModel(new TLcdGeodeticReference(), new TLcdModelDescriptor());
    for (Map.Entry<WeatherStation, List<DailyWeatherReport>> entry : weatherStationListMap.entrySet()) {
      dailyWeatherModel.addElement(new WeatherDomainObject(entry.getKey(), entry.getValue()), ILcdModel.NO_EVENT);
    }
    WeatherStationStyler weatherStationStyler = new WeatherStationStyler();
    timeViewPanelTool.getTimeSlider().addChangeListener(tLcdChangeEvent -> weatherStationStyler.setTime(timeViewPanelTool.getTimeSlider().getTime()));
    ILspInteractivePaintableLayer weatherStationLayer = TLspShapeLayerBuilder.newBuilder().model(dailyWeatherModel).bodyStyler(TLspPaintState.REGULAR, weatherStationStyler).build();
    aView.addLayer(weatherStationLayer);
  }

  private TimeBasedModel addWeatherData(WeatherModelFactory aWeatherModelFactory, WeatherModelFactory.MonthlyData aData,
                                        double[] aLevels, Color[] aColors,
                                        ILspView aView, TimeSlider aTimeSlider) throws IOException {
    int FIRST_YEAR = 2005;
    int LAST_YEAR = 2009;
    TimeBasedModel model = aWeatherModelFactory.createMonthlyModel(
        aData, FIRST_YEAR, LAST_YEAR
    );
    aView.addLayer(
        TLspRasterLayerBuilder
            .newBuilder()
            .model(model)
            .styler(
                TLspPaintRepresentationState.REGULAR_BODY,
                new RasterStyler(new TLcdColorMap(new TLcdInterval(Short.MIN_VALUE, Short.MAX_VALUE), aLevels, aColors))
            )
            .build()
    );

    EventQueue.invokeLater(() -> {
      aTimeSlider.addChangeListener(e -> model.setTime(aTimeSlider.getTime()));
      aTimeSlider.setValidRange(DateUtils.date(FIRST_YEAR, 1).toEpochSecond() * 1000,
                                DateUtils.date(LAST_YEAR, 12).toEpochSecond() * 1000,
                                0, 1000);
    });
    return model;
  }

  @Override
  public void unplugFrom(ILcyLucyEnv aLucyEnv) {
    super.unplugFrom(aLucyEnv);
  }

  public abstract class DataLoader implements ILcyGenericMapManagerListener<ILspView, ILspLayer> {

    @Override
    public void mapManagerChanged(TLcyGenericMapManagerEvent<? extends ILspView, ? extends ILspLayer> aEvent) {
      if (TLcyGenericMapManagerEvent.MAP_COMPONENT_ADDED == aEvent.getId()) {
        final ILcyGenericMapComponent<? extends ILspView, ? extends ILspLayer> mapComponent = aEvent.getMapComponent();
        getLucyEnv().addLucyEnvListener(new ILcyLucyEnvListener() {
          @Override
          public void lucyEnvStatusChanged(TLcyLucyEnvEvent aTLcyLucyEnvEvent) throws TLcyVetoException {
            if (aTLcyLucyEnvEvent.getID() == TLcyLucyEnvEvent.INITIALIZED) {
              ILspView view = mapComponent.getMainView();
              try {
                viewLoaded(view);
              } catch (IOException e) {
                e.printStackTrace();
              }
            }
          }
        });
      }

//      ILspView view = tLcyGenericMapManagerEvent.getMapComponent().getMainView();
//      try {
//        viewLoaded(view);
//      } catch (IOException e) {
//        e.printStackTrace();
//      }
    }

    public abstract void viewLoaded(ILspView aView) throws IOException;
  }
}
