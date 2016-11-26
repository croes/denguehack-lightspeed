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
package samples.lucy.search.service.data;

import java.util.regex.Pattern;

import org.jdesktop.swingx.renderer.StringValue;

import samples.lucy.search.ISearchResult;
import samples.lucy.search.ISearchService;
import samples.lucy.tableview.ValueUtil;
import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.model.ILcdModelReference;
import com.luciad.reference.ILcdGeoReference;
import com.luciad.shape.ILcdBounds;
import com.luciad.view.ILcdLayer;

/**
 * {@code ISearchResult} implementation representing the result of a search through the data of a model.
 * It can be rendered using a {@link DataResultRenderer}.
 */
final class DataSearchResult implements ISearchResult {
  private final String[] fPropertyPath;
  private final Object fPropertyValue;
  private final Object fDomainObject;
  private final Pattern fSearchPattern;
  private final ILcdGeoReference fReference;
  private final ILcdBounds fBounds;
  private final ILcdLayer fLayer;
  private final ILcyLucyEnv fLucyEnv;
  private final ISearchService fSearchService;

  DataSearchResult(Object aDomainObject,
                   String[] aPropertyPath,
                   Object aPropertyValue,
                   ILcdBounds aBounds,
                   ILcdGeoReference aBoundsReference,
                   Pattern aSearchPattern,
                   ILcdLayer aLayer,
                   ILcyLucyEnv aLucyEnv,
                   ISearchService aSearchService) {
    fPropertyPath = aPropertyPath;
    fPropertyValue = aPropertyValue;
    fDomainObject = aDomainObject;
    fSearchPattern = aSearchPattern;
    fReference = aBoundsReference;
    fBounds = aBounds;
    fLayer = aLayer;
    fLucyEnv = aLucyEnv;
    fSearchService = aSearchService;
  }

  public String[] getPropertyPath() {
    return fPropertyPath;
  }

  public Object getPropertyValue() {
    return fPropertyValue;
  }

  @Override
  public Object getResult() {
    return fDomainObject;
  }

  @Override
  public ILcdGeoReference getReference() {
    return fReference;
  }

  @Override
  public Pattern getSearchPattern() {
    return fSearchPattern;
  }

  @Override
  public ILcdBounds getBounds() {
    return fBounds;
  }

  @Override
  public ISearchService getSearchService() {
    return fSearchService;
  }

  public ILcdLayer getLayer() {
    return fLayer;
  }

  @Override
  public String getStringRepresentation() {
    StringValue compositeStringValue = ValueUtil.createCompositeStringValue(fLucyEnv, (ILcdModelReference) fReference);
    return compositeStringValue.getString(getPropertyValue());
  }
}
