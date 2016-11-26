package com.luciad.dengue.lucy;

import com.luciad.dengue.weather.DailyWeatherReport;
import com.luciad.dengue.weather.WeatherDomainObject;
import com.luciad.geodesy.ILcdEllipsoid;
import com.luciad.reference.ILcdGeoReference;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.shape2D.TLcdLonLatCircle;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.style.TLspLineStyle;
import com.luciad.view.lightspeed.style.styler.ALspStyleCollector;
import com.luciad.view.lightspeed.style.styler.ALspStyler;
import testsample.TestSample;

import java.awt.*;
import java.util.Collection;
import java.util.List;

/**
 * Created by luciad on 26.11.16.
 */
public class WeatherStationStyler extends ALspStyler {



    private long time = 1357167600000l;
    private String keyToGet = "prcp";

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
        fireStyleChangeEvent();
    }

    public String getKeyToGet() {
        return keyToGet;
    }

    public void setKeyToGet(String keyToGet) {
        this.keyToGet = keyToGet;
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
                        value = weatherReport.get(keyToGet);
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