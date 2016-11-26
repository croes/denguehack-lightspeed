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

import java.util.regex.Pattern;

import samples.lucy.search.ISearchService;
import samples.lucy.search.service.location.IPointSearchResult;
import com.luciad.reference.ILcdGeoReference;
import com.luciad.shape.ILcdBounds;
import com.luciad.shape.ILcdPoint;

final class GeoNamesSearchResult implements IPointSearchResult {
  private final String fName;
  private final String fCountryName;
  private final ILcdPoint fSearchResult;
  private final ILcdGeoReference fReference;
  private final ILcdBounds fBounds;
  private final Pattern fPattern;
  private final ISearchService fSearchService;

  GeoNamesSearchResult(String aName,
                       String aCountryName,
                       ILcdPoint aSearchResult,
                       ILcdGeoReference aReference,
                       ILcdBounds aBounds,
                       Pattern aPattern,
                       ISearchService aSearchService) {
    fName = aName;
    fCountryName = aCountryName;
    fSearchResult = aSearchResult;
    fReference = aReference;
    fBounds = aBounds;
    fPattern = aPattern;
    fSearchService = aSearchService;
  }

  @Override
  public ILcdPoint getResult() {
    return fSearchResult;
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

  public String getName() {
    return fName;
  }

  public String getCountryName() {
    return fCountryName;
  }

  @Override
  public String getStringRepresentation() {
    return fName;
  }

  @Override
  public String getLocationLabel() {
    return fName;
  }
}
