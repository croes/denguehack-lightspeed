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
package samples.lucy.search.service.location.coordinate;

import java.text.Format;
import java.text.ParseException;
import java.util.regex.Pattern;

import samples.lucy.search.ISearchTask;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.reference.TLcdGridReference;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.shape2D.TLcdLonLatPoint;
import com.luciad.shape.shape2D.TLcdXYPoint;
import com.luciad.shape.shape3D.TLcdLonLatHeightPoint;
import com.luciad.shape.shape3D.TLcdXYZPoint;

/**
 * {@code ISearchTask} implementation for the {@link CoordinateSearchService}
 */
final class CoordinateSearchTask implements ISearchTask {

  private final CoordinateSearchService.PointFormatProvider fFormatProvider;
  private final CoordinateSearchService fSearchService;

  CoordinateSearchTask(CoordinateSearchService.PointFormatProvider aFormatProvider, CoordinateSearchService aSearchService) {
    fFormatProvider = aFormatProvider;
    fSearchService = aSearchService;
  }

  @Override
  public void search(Pattern aSearchPattern, ResultCollector aSearchResultCollector) {
    try {
      Format format = fFormatProvider.retrieveFormat();
      Object point = format.parseObject(aSearchPattern.toString());
      if (point instanceof TLcdLonLatPoint || point instanceof TLcdLonLatHeightPoint) {
        CoordinateSearchResult coordinateSearchResult = new CoordinateSearchResult((ILcdPoint) point,
                                                                                   new TLcdGeodeticReference(),
                                                                                   aSearchPattern,
                                                                                   fFormatProvider,
                                                                                   fSearchService);
        aSearchResultCollector.addResult(coordinateSearchResult);
      } else if (point instanceof TLcdXYPoint || point instanceof TLcdXYZPoint) {
        CoordinateSearchResult coordinateSearchResult = new CoordinateSearchResult((ILcdPoint) point,
                                                                                   new TLcdGridReference(),
                                                                                   aSearchPattern,
                                                                                   fFormatProvider,
                                                                                   fSearchService);
        aSearchResultCollector.addResult(coordinateSearchResult);
      }
    } catch (ParseException aExc) {
      //ignore
    }
  }

  @Override
  public Speed getSpeed() {
    return Speed.FAST;
  }
}
