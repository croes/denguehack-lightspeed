package com.luciad.dengue.weather;

import java.util.Comparator;
import java.util.List;

/**
 * Created by luciad on 26.11.16.
 */
public class WeatherDomainObject {
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
