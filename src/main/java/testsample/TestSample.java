package testsample;

import com.luciad.datamodel.ILcdDataObject;
import com.luciad.dengue.weather.DailyWeatherReport;
import com.luciad.dengue.weather.StationDailyWeatherDecoder;
import com.luciad.dengue.weather.WeatherStation;
import com.luciad.format.geojson.TLcdGeoJsonModelDecoder;
import com.luciad.geodesy.ILcdEllipsoid;
import com.luciad.model.ILcdModel;
import com.luciad.model.TLcdModelDescriptor;
import com.luciad.model.TLcdVectorModel;
import com.luciad.reference.ILcdGeoReference;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.shape2D.TLcdLonLatCircle;
import com.luciad.shape.shape2D.TLcdLonLatPoint;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.layer.ILspInteractivePaintableLayer;
import com.luciad.view.lightspeed.layer.TLspPaintState;
import com.luciad.view.lightspeed.layer.shape.TLspShapeLayerBuilder;
import com.luciad.view.lightspeed.style.TLspFillStyle;
import com.luciad.view.lightspeed.style.TLspIconStyle;
import com.luciad.view.lightspeed.style.TLspLineStyle;
import com.luciad.view.lightspeed.style.styler.ALspStyleCollector;
import com.luciad.view.lightspeed.style.styler.ALspStyler;
import samples.lightspeed.common.LightspeedSample;
import samples.lightspeed.common.LspDataUtil;
import samples.lightspeed.imaging.multispectral.general.GeneralOperationPanel;

import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.List;

/**
 * Created by luciad on 26.11.16.
 */
public class TestSample extends LightspeedSample {

    @Override
    protected void addData() throws IOException {
        LspDataUtil.instance().grid().addToView(getView());
        TLcdGeoJsonModelDecoder modelDecoder = new TLcdGeoJsonModelDecoder();
        ILcdModel malasya = modelDecoder.decode("malasia_cleaned.geojson");

        MalasyiaStyler malasyastyler = new MalasyiaStyler();
        ILspInteractivePaintableLayer layer = TLspShapeLayerBuilder.newBuilder().model(malasya).bodyStyler(TLspPaintState.REGULAR, malasyastyler).build();
        getView().addLayer(layer);


        //Station
        Map<WeatherStation, List<DailyWeatherReport>> weatherStationListMap = new StationDailyWeatherDecoder().decodeWeather();
        TLcdVectorModel dailyWeatherModel = new TLcdVectorModel(new TLcdGeodeticReference(), new TLcdModelDescriptor());
        for (Map.Entry<WeatherStation, List<DailyWeatherReport>> entry : weatherStationListMap.entrySet()) {
            dailyWeatherModel.addElement(new WeatherDomainObject(entry.getKey(), entry.getValue()), ILcdModel.NO_EVENT);
        }
        WeatherStationStyler weatherStationStyler = new WeatherStationStyler();
        ILspInteractivePaintableLayer weatherStationLayer = TLspShapeLayerBuilder.newBuilder().model(dailyWeatherModel).bodyStyler(TLspPaintState.REGULAR, weatherStationStyler).build();
        getView().addLayer(weatherStationLayer);

        //Time update
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                malasyastyler.setTime(malasyastyler.getTime() + (1000 * 60 * 60 * 24));
                weatherStationStyler.setTime(weatherStationStyler.getTime() + (1000 * 60 * 60 * 24));
            }
        }, 0, 200);
    }

    public static void main(String[] args) {
        LightspeedSample.startSample(TestSample.class, "test sample");
    }

    private static class WeatherDomainObject {
        private WeatherStation weatherStation;
        private List<DailyWeatherReport> weatherReports;

        public WeatherDomainObject(WeatherStation weatherStation, List<DailyWeatherReport> weatherReports) {
            this.weatherStation = weatherStation;
            this.weatherReports = weatherReports;
            this.weatherReports.sort(Comparator.comparing(DailyWeatherReport::getYearmoda));
        }

        public WeatherStation getWeatherStation() {
            return weatherStation;
        }

        public List<DailyWeatherReport> getWeatherReports() {
            return weatherReports;
        }
    }

    private static class MalasyiaStyler extends ALspStyler {

        private long time = 1357167600000l;

        public long getTime() {
            return time;
        }

        public void setTime(long time) {
            this.time = time;
            fireStyleChangeEvent();
        }

        @Override
        public void style(Collection<?> collection, ALspStyleCollector styleCollector, TLspContext context) {
            for (Object o : collection) {
                List<HashMap<String, Object>> countType = (List<HashMap<String, Object>>) ((ILcdDataObject) o).getValue("countType");
                long count = 0;
                for (HashMap<String, Object> dataObject : countType) {
                    Integer cumCount = (Integer) dataObject.get("cumCount");
                    Long startDate = (Long) dataObject.get("startDate");
                    Long endDate = (Long) dataObject.get("endDate");
                    if (time > startDate && time <= endDate) {
                        count += cumCount;
                    }

                }
                Color color = null;
                if (count < 5) {
                    color = new Color(255, 255, 178, 192);
                } else if (count < 10) {
                    color = new Color(254, 217, 118, 192);
                } else if (count < 20) {
                    color = new Color(254, 178, 76, 192);
                } else if (count < 50) {
                    color = new Color(253, 141, 60, 192);
                } else if (count < 100) {
                    color = new Color(252, 78, 42, 192);
                } else if (count < 500) {
                    color = new Color(227, 26, 28, 192);
                } else {
                    color = new Color(177, 0, 38, 192);
                }
                styleCollector.object(o).style(TLspFillStyle.newBuilder().color(color).build()).submit();
            }
        }
    }

    private static class WeatherStationStyler extends ALspStyler {

        private long time = 1357167600000l;

        public long getTime() {
            return time;
        }

        public void setTime(long time) {
            this.time = time;
            fireStyleChangeEvent();
        }

        @Override
        public void style(Collection<?> collection, ALspStyleCollector styleCollector, TLspContext tLspContext) {
            ILcdEllipsoid ellipsoid = ((ILcdGeoReference) tLspContext.getModelReference()).getGeodeticDatum().getEllipsoid();
            for (Object o : collection) {
                if (o instanceof WeatherDomainObject) {
                    double value = Double.NaN;
                    double factor = 20000;
                    List<DailyWeatherReport> weatherReports = ((WeatherDomainObject) o).getWeatherReports();
                    for (DailyWeatherReport weatherReport : weatherReports) {
                        if (weatherReport.getYearmoda() > time) {
                            value = weatherReport.getPrcp();
                            break;
                        }
                    }

                    ILcdPoint centerPoint = ((WeatherDomainObject) o).getWeatherStation().getLocation();
                    if (!Double.isNaN(value)) {
                        styleCollector.object(o).geometry(new TLcdLonLatCircle(centerPoint, value * factor, ellipsoid)).style(TLspLineStyle.newBuilder().color(Color.BLUE).build()).submit();
                    }
                }
            }
        }
    }
}
