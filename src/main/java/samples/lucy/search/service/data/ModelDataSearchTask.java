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

import java.util.Enumeration;
import java.util.regex.Pattern;

import samples.lucy.search.ISearchTask;
import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.model.ILcdModel;
import com.luciad.reference.ILcdGeoReference;
import com.luciad.shape.ILcdBounds;
import com.luciad.util.concurrent.TLcdLockUtil;
import com.luciad.view.ILcdLayer;

/**
 * <p>
 *   An abstract {@code ISearchTask} which will loop over all model elements, and search for the pattern in each of
 *   those model elements.
 *   Implementations of this class need to provide the search logic inside a model element by implementing the
 *   {@link #searchDomainObject(Pattern, Object)} method.
 * </p>
 */
public abstract class ModelDataSearchTask implements ISearchTask {

  private static final long MAX_SEARCH_TIME_IN_MILLI_SECONDS = 10000;
  private static final int MAX_RESULTS = 100;

  private final ModelDataSearchService fSearchService;
  private final ILcyLucyEnv fLucyEnv;
  private final ILcdModel fModel;
  private final ILcdLayer fLayer;

  private ResultCollector fResultCollector;
  private int fResultCounter;

  protected ModelDataSearchTask(ModelDataSearchService aSearchService,
                                ILcdModel aModel,
                                ILcdLayer aLayer,
                                ILcyLucyEnv aLucyEnv) {
    fSearchService = aSearchService;
    fModel = aModel;
    fLayer = aLayer;
    fLucyEnv = aLucyEnv;
  }

  protected final ILcdModel getModel() {
    return fModel;
  }

  protected final ILcdLayer getLayer() {
    return fLayer;
  }

  protected final ILcyLucyEnv getLucyEnv() {
    return fLucyEnv;
  }

  protected final ModelDataSearchService getSearchService() {
    return fSearchService;
  }

  @Override
  public final void search(Pattern aSearchPattern, ResultCollector aSearchResultCollector) {
    fResultCounter = 0;
    fResultCollector = aSearchResultCollector;
    try {
      try (TLcdLockUtil.Lock autoClose = TLcdLockUtil.readLock(fModel)) {
        Enumeration elements = fModel.elements();
        long startTime = System.currentTimeMillis();
        while (elements.hasMoreElements() &&
               !aSearchResultCollector.isCancelled() &&
               fResultCounter < MAX_RESULTS &&
               !timeoutReached(startTime)
            ) {
          Object domainObject = elements.nextElement();
          searchDomainObject(aSearchPattern, domainObject);
        }
      }
    } finally {
      fResultCollector = null;
      fResultCounter = 0;
    }
  }

  private boolean timeoutReached(long aStartTime) {
    return System.currentTimeMillis() - aStartTime >= MAX_SEARCH_TIME_IN_MILLI_SECONDS;
  }

  /**
   * Search for {@code aSearchPattern} in the data of the domain object {@code aDomainObject}.
   * Each time a result is found, it should be published by calling the
   * {@link #publishSearchResult(Object, String[], Object, ILcdBounds, ILcdGeoReference, Pattern)} method.
   *
   * @param aSearchPattern The pattern to search for
   * @param aDomainObject The domain object in which to search
   */
  protected abstract void searchDomainObject(Pattern aSearchPattern, Object aDomainObject);

  /**
   * <p>
   *   Method which allows to publish a search result.
   *   This method should be called from the {@link #searchDomainObject(Pattern, Object)} method
   *   each time a match has been found.
   * </p>
   *
   * @param aDomainObject The domain object which has a property matching the search query
   * @param aPropertyPath The path to the data property
   * @param aPropertyValue The value of the data property
   * @param aBounds The bounds of the result.
   *                When the result is selected, the map will be fitted on these bounds
   * @param aBoundsReference The reference in which {@code aBounds} is expressed
   * @param aSearchPattern The search pattern
   */
  protected final void publishSearchResult(Object aDomainObject,
                                           String[] aPropertyPath,
                                           Object aPropertyValue,
                                           ILcdBounds aBounds,
                                           ILcdGeoReference aBoundsReference,
                                           Pattern aSearchPattern) {
    if (fResultCollector == null || fResultCounter >= MAX_RESULTS) {
      return;
    }
    fResultCollector.addResult(new DataSearchResult(aDomainObject,
                                                    aPropertyPath,
                                                    aPropertyValue,
                                                    aBounds,
                                                    aBoundsReference,
                                                    aSearchPattern,
                                                    fLayer,
                                                    fLucyEnv,
                                                    fSearchService));
    fResultCounter++;
  }

  @Override
  public final Speed getSpeed() {
    return Speed.SLOW;
  }
}
