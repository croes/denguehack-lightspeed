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
package samples.lucy.search.service.location.geonames;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.map.ObjectMapper;

import samples.lucy.search.ISearchService;
import samples.lucy.search.ISearchTask;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.shape.ILcdBounds;
import com.luciad.shape.shape2D.TLcdLonLatBounds;
import com.luciad.shape.shape2D.TLcdLonLatPoint;
import com.luciad.util.logging.ILcdLogger;
import com.luciad.util.logging.TLcdLoggerFactory;

/**
 * Search task to query the GeoNames web services.
 */
final class GeoNamesSearchTask implements ISearchTask {
  private static final ILcdLogger LOGGER = TLcdLoggerFactory.getLogger(GeoNamesSearchTask.class);
  private static final int MAX_RESULTS = 30;

  private final String fURL;
  private final ISearchService fSearchService;

  public GeoNamesSearchTask(String aUserName, ISearchService aSearchService) {
    fSearchService = aSearchService;
    String userLanguage = System.getProperty("user.language", "en");
    fURL = "http://api.geonames.org/searchJSON?maxRows=" + MAX_RESULTS + "&lang=" + userLanguage + "&searchlang=" + userLanguage + "&style=full&username=" + aUserName + "&q=";
  }

  @Override
  public void search(Pattern aSearchPattern, ResultCollector aSearchResultCollector) {
    try {
      String pattern = URLEncoder.encode(aSearchPattern.toString(), "UTF-8");
      HttpGet httpget = new HttpGet(fURL + pattern);

      try {
        DefaultHttpClient httpClient = new DefaultHttpClient();
        httpClient.execute(httpget, new GeoNamesResponseHandler(aSearchPattern, aSearchResultCollector));
      } finally {
        httpget.releaseConnection();
      }
    } catch (UnsupportedEncodingException exception) {
      LOGGER.warn("Could not encode search pattern to url.");
    } catch (IOException exception) {
      LOGGER.warn("Geonames server could not be reached.");
    }
  }

  @Override
  public Speed getSpeed() {
    return Speed.SLOW;
  }

  private class GeoNamesResponseHandler implements ResponseHandler<Void> {
    private final Pattern fSearchPattern;
    private final ResultCollector fSearchResultCollector;

    public GeoNamesResponseHandler(Pattern aSearchPattern, ResultCollector aSearchResultCollector) {
      fSearchPattern = aSearchPattern;
      fSearchResultCollector = aSearchResultCollector;
    }

    @Override
    public Void handleResponse(HttpResponse aHttpResponse) throws IOException {
      int status = aHttpResponse.getStatusLine().getStatusCode();
      if (status >= 200 && status < 300) {
        HttpEntity entity = aHttpResponse.getEntity();
        String content = EntityUtils.toString(entity);

        HashMap<String, Object> result = new ObjectMapper().readValue(content, HashMap.class);
        convertToSearchResultAndPublish(result);
      } else {
        LOGGER.warn("Unexpected response status from geonames server: " + status);
      }

      return null;
    }

    private void convertToSearchResultAndPublish(HashMap<String, Object> aResult) {
      List<Map<String, Object>> geonames = (List<Map<String, Object>>) aResult.get("geonames");
      for (Map<String, Object> geoname : geonames) {
        try {
          String name = (String) geoname.get("name");
          String countryName = (String) geoname.get("countryName");

          double lon = Double.parseDouble((String) geoname.get("lng"));
          double lat = Double.parseDouble((String) geoname.get("lat"));
          TLcdLonLatPoint point = new TLcdLonLatPoint(lon, lat);
          ILcdBounds bounds = calculateBounds(geoname, point);

          fSearchResultCollector.addResult(new GeoNamesSearchResult(name,
                                                                    countryName,
                                                                    point,
                                                                    new TLcdGeodeticReference(),
                                                                    bounds,
                                                                    fSearchPattern,
                                                                    fSearchService));
        } catch (Exception exception) {
          LOGGER.warn(exception.getMessage());
        }
      }
    }

    private ILcdBounds calculateBounds(Map<String, Object> aGeoname, TLcdLonLatPoint aPoint) {
      ILcdBounds bounds = aPoint.getBounds();

      Map<String, Object> bbox = (Map<String, Object>) aGeoname.get("bbox");
      if (bbox != null) {
        // if bounds are specified, use them to fit
        double south = getBoundsValue(bbox, "south");
        double north = getBoundsValue(bbox, "north");
        double east = getBoundsValue(bbox, "east");
        double west = getBoundsValue(bbox, "west");
        bounds = new TLcdLonLatBounds(west, south, east - west, north - south);
      }
      return bounds;
    }

    private double getBoundsValue(Map<String, Object> aBbox, String aSide) {
      Object value = aBbox.get(aSide);
      if (value instanceof Number) {
        return ((Number) value).doubleValue();
      }

      LOGGER.trace("Not a number, returning 0");
      return 0d;
    }
  }
}
