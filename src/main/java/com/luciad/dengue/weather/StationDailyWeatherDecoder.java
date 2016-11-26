package com.luciad.dengue.weather;

import com.luciad.io.ILcdInputStreamFactory;
import com.luciad.io.ILcdInputStreamFactoryCapable;
import com.luciad.io.TLcdInputStreamFactory;
import com.luciad.model.ILcdModel;
import com.luciad.model.TLcd2DBoundsIndexedModel;
import com.luciad.util.TLcdPair;
import com.luciad.dengue.util.DataSet;
import com.luciad.dengue.util.DateUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Thomas De Bodt
 */
public class StationDailyWeatherDecoder implements ILcdInputStreamFactoryCapable {

  private ILcdInputStreamFactory fInputStreamFactory = new TLcdInputStreamFactory();

  @Override
  public void setInputStreamFactory(ILcdInputStreamFactory aInputStreamFactory) {
    fInputStreamFactory = aInputStreamFactory;
  }

  @Override
  public ILcdInputStreamFactory getInputStreamFactory() {
    return fInputStreamFactory;
  }

  public ILcdModel decodeReports(String aSourceName, List<WeatherStation> aStations) throws IOException {
    Map<TLcdPair<Integer, String>, List<WeatherStation>> stationsById = aStations.stream().collect(Collectors.groupingBy(s -> new TLcdPair<>(s.USAF, s.WBAN)));

    TLcd2DBoundsIndexedModel model = new TLcd2DBoundsIndexedModel();
    try(BufferedReader reader = new BufferedReader(new InputStreamReader(fInputStreamFactory.createInputStream(aSourceName)))) {
      // stn, wban, yearmoda, temp, dewp, slp, stp, visib, wdsp, mxspd, gust, max, min, prcp, sndp, frshtt
      ArrayList<String> header = split(reader.readLine());
      int stnIdx = header.indexOf("stn");
      int wbanIdx = header.indexOf("wban");
      int yearmodaIdx = header.indexOf("yearmoda");
      int tempIdx = header.indexOf("temp");
      int dewpIdx = header.indexOf("dewp");
      int slpIdx = header.indexOf("slp");
      int stpIdx = header.indexOf("stp");
      int visibIdx = header.indexOf("visib");
      int wdspIdx = header.indexOf("wdsp");
      int mxspdIdx = header.indexOf("mxspd");
      int gustIdx = header.indexOf("gust");
      int maxIdx = header.indexOf("max");
      int minIdx = header.indexOf("min");
      int prcpIdx = header.indexOf("prcp");
      int sndpIdx = header.indexOf("sndp");
      int frshttIdx = header.indexOf("frshtt");

      String line;
      while((line = reader.readLine()) != null) {
        ArrayList<String> fields = split(line);
        DailyWeatherReport report = new DailyWeatherReport();
        report.stn = Integer.parseInt(fields.get(stnIdx));
        report.wban = fields.get(wbanIdx);
        report.yearmoda = parseYearModa(fields.get(yearmodaIdx));
        report.temp = parseDouble(fields, tempIdx);
        report.dewp = parseDouble(fields, dewpIdx);
        report.slp = parseDouble(fields, slpIdx);
        report.stp = parseDouble(fields, stpIdx);
        report.visib = parseDouble(fields, visibIdx);
        report.wdsp = parseDouble(fields, wdspIdx);
        report.mxspd = parseDouble(fields, mxspdIdx);
        report.gust = parseDouble(fields, gustIdx);
        report.max = parseDouble(fields, maxIdx);
        report.min = parseDouble(fields, minIdx);
        report.prcp = parseDouble(fields, prcpIdx);
        report.sndp = parseDouble(fields, sndpIdx);
        report.frshtt = parseDouble(fields, frshttIdx);

        report.station = getStation(report, stationsById);

        if(report.isValid()) {
          model.addElement(report, ILcdModel.NO_EVENT);
        }
      }
    }
    return model;
  }

  public List<WeatherStation> decodeStations(String aSourceName) throws IOException {
    ArrayList<WeatherStation> stations = new ArrayList<>();
    try(BufferedReader reader = new BufferedReader(new InputStreamReader(fInputStreamFactory.createInputStream(aSourceName)))) {
      // USAF,WBAN,STATION NAME,CTRY,ST,CALL,LAT,LON,ELEV(M),BEGIN,END
      String headerLine = reader.readLine();
      ArrayList<String> header = split(headerLine);
      int USAFIdx = header.indexOf("USAF");
      int WBANIdx = header.indexOf("WBAN");
      int STATION_NAMEIdx = header.indexOf("STATION NAME");
      int CTRYIdx = header.indexOf("CTRY");
      int STIdx = header.indexOf("ST");
      int CALLIdx = header.indexOf("CALL");
      int LATIdx = header.indexOf("LAT");
      int LONIdx = header.indexOf("LON");
      int ELEVIdx = header.indexOf("ELEV(M)");
      int BEGINIdx = header.indexOf("BEGIN");
      int ENDIdx = header.indexOf("END");

      String line;
      while((line = reader.readLine()) != null) {
        ArrayList<String> fields = split(line);

        WeatherStation station = new WeatherStation();
        station.USAF = Integer.parseInt(fields.get(USAFIdx));
        station.WBAN = fields.get(WBANIdx);
        station.STATION_NAME = fields.get(STATION_NAMEIdx);
        station.CTRY = fields.get(CTRYIdx);
        station.ST = fields.get(STIdx);
        station.CALL = fields.get(CALLIdx);
        station.move2D(
            parseDouble(fields, LONIdx),
            parseDouble(fields, LATIdx)
        );
        station.ELEV = fields.get(ELEVIdx);
        station.BEGIN = fields.get(BEGINIdx);
        station.END = fields.get(ENDIdx);

        stations.add(station);
      }
    }
    return stations;
  }

  private WeatherStation getStation(DailyWeatherReport aReport, Map<TLcdPair<Integer, String>, List<WeatherStation>> aStationsById) {
    List<WeatherStation> stations = aStationsById.get(new TLcdPair<>(aReport.stn, aReport.wban));
    if(stations == null || stations.isEmpty()) {
      return null;
    } else if(stations.size() == 1) {
      return stations.get(0);
    }
    return null;
  }

  private long parseYearModa(String aS) {
    int year = Integer.parseInt(aS.substring(0, 4));
    int month = Integer.parseInt(aS.substring(4, 6));
    int day = Integer.parseInt(aS.substring(6, 8));
    return DateUtils.dateToMillis(DateUtils.date(year, month, day));
  }

  private static double parseDouble(ArrayList<String> aFields, int aIdx) {
    String field = aFields.get(aIdx);
    if(field.isEmpty()) {
      return Double.NaN;
    }
    return Double.parseDouble(field);
  }

  private static ArrayList<String> split(String aLine) {
    ArrayList<String> tokens = new ArrayList<>();

    int i = 0;
    boolean quote = false;
    int start = 0;
    while(i < aLine.length()) {
      if(aLine.charAt(i) == '"') {
        quote = !quote;
      } else if(aLine.charAt(i) == ',') {
        if(!quote) {
          tokens.add(aLine.substring(start, i));
          start = i + 1;
        }
      }
      i++;
    }
    if(start < aLine.length()) {
      tokens.add(aLine.substring(start));
    }
    return tokens;
  }

  public static void main(String[] args) throws Exception {
    Map<WeatherStation, List<DailyWeatherReport>> reportsByStation = new StationDailyWeatherDecoder().decodeWeather();

    System.out.printf("Stations:%n");
    System.out.printf("  %d stations%n", reportsByStation.size());
    System.out.printf("  %.1f reports/station%n", reportsByStation.values().stream().mapToInt(List::size).average().orElse(Double.NaN));
  }

  public Map<WeatherStation, List<DailyWeatherReport>> decodeWeather() throws IOException {
    StationDailyWeatherDecoder md = new StationDailyWeatherDecoder();
    List<WeatherStation> stations = md
        .decodeStations(new File(DataSet.ROOT, "asia_daily_summaries/data/weatherstationsXY.csv").getPath()).stream()
        .filter(s -> s.CTRY.equals("MY"))
        .collect(Collectors.toList());

    ILcdModel model = md.decodeReports(new File(DataSet.ROOT, "asia_daily_summaries/data/daily_climate_asia.zip").getPath(), stations);
    //noinspection unchecked
    ArrayList<DailyWeatherReport> reports = Collections.list((Enumeration<DailyWeatherReport>)model.elements());

    int numReports = reports.size();

    System.out.printf("Reports:%n");
    System.out.printf("  %d reports%n", numReports);

    return reports.stream().collect(Collectors.groupingBy(r -> r.station));
  }
}
