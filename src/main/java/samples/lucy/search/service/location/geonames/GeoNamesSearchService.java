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

import java.util.Collections;
import java.util.List;

import javax.swing.table.TableCellRenderer;

import samples.lucy.search.ISearchTask;
import samples.lucy.search.service.location.AbstractLocationSearchService;
import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.lucy.map.ILcyGenericMapComponent;
import com.luciad.view.ILcdLayer;
import com.luciad.view.ILcdView;

/**
 * {@code AbstractLocationSearchService} extension which can search on place names and country names.
 * The place and country names are converted to a location by using the <a href="http://www.geonames.org/">GeoNames webservice</a>.
 */
public final class GeoNamesSearchService extends AbstractLocationSearchService {
  private final GeoNamesRenderer fGeoNamesRenderer = new GeoNamesRenderer();
  private final String fUserName;

  public GeoNamesSearchService(ILcyLucyEnv aLucyEnv, ILcyGenericMapComponent<? extends ILcdView, ? extends ILcdLayer> aMapComponent, String aUserName) {
    super(aLucyEnv, aMapComponent);
    fUserName = aUserName;
  }

  @Override
  public List<? extends ISearchTask> createSearchTasks() {
    return Collections.singletonList(new GeoNamesSearchTask(fUserName, this));
  }

  @Override
  public TableCellRenderer getSearchResultRenderer() {
    return fGeoNamesRenderer;
  }

  @Override
  public int getServicePriority() {
    return LOW_SERVICE_PRIORITY;
  }
}
