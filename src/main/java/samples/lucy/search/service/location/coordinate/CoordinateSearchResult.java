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

import java.util.regex.Pattern;

import samples.lucy.search.ISearchService;
import samples.lucy.search.service.location.IPointSearchResult;
import com.luciad.reference.ILcdGeoReference;
import com.luciad.shape.ILcdBounds;
import com.luciad.shape.ILcdPoint;

final class CoordinateSearchResult implements IPointSearchResult {
  private final ILcdPoint fPoint;
  private final ILcdGeoReference fReference;
  private final Pattern fPattern;
  private final ILcdBounds fBounds;
  private final CoordinateSearchService fSearchService;
  private final CoordinateSearchService.PointFormatProvider fPointFormatProvider;

  public CoordinateSearchResult(ILcdPoint aPoint,
                                ILcdGeoReference aPointReference,
                                Pattern aSearchPattern,
                                CoordinateSearchService.PointFormatProvider aPointFormatProvider,
                                CoordinateSearchService aSearchService) {
    fPoint = aPoint;
    fReference = aPointReference;
    fPattern = aSearchPattern;
    fBounds = aPoint.getBounds();
    fPointFormatProvider = aPointFormatProvider;
    fSearchService = aSearchService;
  }

  @Override
  public ILcdPoint getResult() {
    return fPoint;
  }

  @Override
  public ILcdGeoReference getReference() {
    return fReference;
  }

  @Override
  public Pattern getSearchPattern() {
    return fPattern;
  }

  @Override
  public ILcdBounds getBounds() {
    return fBounds;
  }

  @Override
  public ISearchService getSearchService() {
    return fSearchService;
  }

  @Override
  public String getStringRepresentation() {
    return fPointFormatProvider.retrieveFormat().format(fPoint);
  }

  @Override
  public String getLocationLabel() {
    return null;
  }
}
