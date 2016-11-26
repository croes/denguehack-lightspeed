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
package samples.lucy.search;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.Format;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

import javax.swing.Timer;

import samples.lucy.search.service.data.DataObjectSearchTaskFactory;
import samples.lucy.search.service.data.FeaturedSearchTaskFactory;
import samples.lucy.search.service.data.ModelDataSearchService;
import samples.lucy.search.service.location.GXYSearchLayerFactory;
import samples.lucy.search.service.location.LSPSearchLayerFactory;
import samples.lucy.search.service.location.coordinate.CoordinateSearchService;
import samples.lucy.search.service.location.geonames.GeoNamesSearchService;
import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.lucy.addons.ALcyPreferencesAddOn;
import com.luciad.lucy.gui.TLcyActionBarManager;
import com.luciad.lucy.gui.TLcyActionBarUtil;
import com.luciad.lucy.map.ILcyGenericMapComponent;
import com.luciad.lucy.map.ILcyGenericMapManagerListener;
import com.luciad.lucy.map.TLcyGenericMapManagerEvent;
import com.luciad.lucy.util.ALcyTool;
import com.luciad.view.ILcdLayer;
import com.luciad.view.ILcdView;

/**
 * <p>
 *   This is the search add-on for Lucy.
 *   It offers UI to the Lucy end-user allowing to perform a search on the map.
 *   By default, it can handle the following searches:
 * </p>
 *
 * <ul>
 *   <li>
 *     Searching and fitting on a location by providing the coordinates as search string.
 *   </li>
 *   <li>
 *     Searching on the name of a location.
 *     This is done using a geonames web-service.
 *     See the {@link GeoNamesSearchService} for more information.
 *   </li>
 *   <li>
 *     Searching through the properties of the loaded data by querying the {@link com.luciad.datamodel.ILcdDataObject} or
 *     the {@code com.luciad.util.ILcdFeatured} interfaces.
 *     This does not support all models: all {@link com.luciad.model.ILcdIntegerIndexedModel} instances are supported (which are the same instances as
 *     supported by the table view), and a few other specific data formats.
 *     An example of an unsupported model are all database models.
 *   </li>
 * </ul>
 *
 * <p>
 *   For each map component, this add-on will add a UI element on the map by using an {@link com.luciad.lucy.gui.ILcyCustomizableRepresentationAction}.
 *   The model behind the UI is provided by the {@link SearchManager}.
 *   Such a {@code SearchManager} instance is created for each map (see {@link #createSearchManager(ILcyGenericMapComponent)}),
 *   and it links the {@link ISearchService} instances to the UI element.
 * </p>
 *
 * <p>
 *   If you want to add your own search service, you need to create an implementation of {@link ISearchService}
 *   and override or adjust {@link #createSearchServices(ILcyGenericMapComponent)} to include your service.<br/>
 *   If you want to disable one of the default search services, you can simply disable it in the configuration file.
 * </p>
 *
 * <p>
 *   Consult the {@link samples.lucy.search package javadoc} for a high-level overview
 *   of how the search process uses the different interfaces.
 * </p>
 *
 * @since 2016.0
 */
public class SearchAddOn extends ALcyPreferencesAddOn {
  private ExecutorService fExecutorService;

  private boolean fSearchServicesInitialized = false;

  public SearchAddOn() {
    this(ALcyTool.getLongPrefix(SearchAddOn.class), ALcyTool.getShortPrefix(SearchAddOn.class));
  }

  public SearchAddOn(String aLongPrefix, String aShortPrefix) {
    super(aLongPrefix, aShortPrefix);
  }

  @Override
  public void plugInto(ILcyLucyEnv aLucyEnv) {
    super.plugInto(aLucyEnv);

    fExecutorService = SearchExecutorServiceFactory.createSearchExecutorService();
    addSearchBoxToCurrentAndFutureMaps(aLucyEnv);

    aLucyEnv.addService(new GXYSearchLayerFactory());
    aLucyEnv.addService(new LSPSearchLayerFactory());
  }

  @Override
  public void unplugFrom(ILcyLucyEnv aLucyEnv) {
    super.unplugFrom(aLucyEnv);
    fExecutorService.shutdown();
  }

  /**
   * This method will add a listener to the map manager.
   *
   * If a new map will be added, a search box will be added to the map component. This is done by registering an action in the action bar.
   * If a map is removed from the map manager, we delete the action from the action bar.
   *
   * @param aLucyEnv the Lucy back-end
   */
  private void addSearchBoxToCurrentAndFutureMaps(ILcyLucyEnv aLucyEnv) {
    aLucyEnv.getCombinedMapManager().addMapManagerListener(new ILcyGenericMapManagerListener<ILcdView, ILcdLayer>() {
      @Override
      public void mapManagerChanged(TLcyGenericMapManagerEvent<? extends ILcdView, ? extends ILcdLayer> aMapManagerEvent) {
        if (aMapManagerEvent.getId() == TLcyGenericMapManagerEvent.MAP_COMPONENT_ADDED) {
          ILcyGenericMapComponent<? extends ILcdView, ? extends ILcdLayer> mapComponent = aMapManagerEvent.getMapComponent();
          if (!fSearchServicesInitialized) {
            initializeSearchServices(mapComponent);
            fSearchServicesInitialized = true;
          }
          addSearchBoxToMap(mapComponent);
        }
      }
    }, true);
  }

  /**
   * Some of the search services trigger the loading of a lot of classes.
   * This can cause the application to stall when the first search is performed.
   * Therefore, we do a search in the background the first time search services are created
   *
   * @param aMapComponent The map component to create the search service for
   */
  private void initializeSearchServices(ILcyGenericMapComponent<? extends ILcdView, ? extends ILcdLayer> aMapComponent) {
    final SearchManager searchManager = createSearchManager(aMapComponent);
    try {
      searchManager.search("London");
    } finally {
      //give the search some time to run, then dispose the search manager
      Timer timer = new Timer(10000, new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          searchManager.dispose();
        }
      });
      timer.setRepeats(false);
      timer.start();
    }
  }

  private void addSearchBoxToMap(ILcyGenericMapComponent<? extends ILcdView, ? extends ILcdLayer> aMapComponent) {
    SearchBoxAction searchBoxAction = new SearchBoxAction(createSearchManager(aMapComponent));
    searchBoxAction.putValue(TLcyActionBarUtil.ID_KEY, getShortPrefix() + "searchAction");
    TLcyActionBarManager abm = getLucyEnv().getUserInterfaceManager().getActionBarManager();
    TLcyActionBarUtil.insertInConfiguredActionBars(searchBoxAction, aMapComponent, abm, getPreferences());
  }

  private SearchManager createSearchManager(ILcyGenericMapComponent<? extends ILcdView, ? extends ILcdLayer> aMapComponent) {
    return new SearchManager(fExecutorService, createSearchServices(aMapComponent));
  }

  /**
   * <p>
   *   Returns a list containing all search services instances which should be used on the specified map.
   * </p>
   *
   * <p>
   *   Note that the configuration file allows to disable any of the default search services.
   * </p>
   *
   * @param aMapComponent The map component
   *
   * @return The list of search services
   */
  protected List<ISearchService> createSearchServices(ILcyGenericMapComponent<? extends ILcdView, ? extends ILcdLayer> aMapComponent) {
    List<ISearchService> result = new ArrayList<>();
    if (getPreferences().getBoolean(getShortPrefix() + "searchService.coordinates", false)) {
      //The lon-lat format on the back-end can change, so use a format provider
      result.add(new CoordinateSearchService(getLucyEnv(), aMapComponent, new CoordinateSearchService.PointFormatProvider() {
        @Override
        public Format retrieveFormat() {
          return getLucyEnv().getDefaultLonLatPointFormat();
        }
      }));
    }
    if (getPreferences().getBoolean(getShortPrefix() + "searchService.dataObject", false)) {
      result.add(new ModelDataSearchService(getLucyEnv(), aMapComponent, new DataObjectSearchTaskFactory(getLucyEnv())));
    }
    if (getPreferences().getBoolean(getShortPrefix() + "searchService.featured", false)) {
      result.add(new ModelDataSearchService(getLucyEnv(), aMapComponent, new FeaturedSearchTaskFactory(getLucyEnv())));
    }
    if (getPreferences().getBoolean(getShortPrefix() + "searchService.geoNames", false)) {
      String userName = getPreferences().getString(getShortPrefix() + "searchService.geoNames.userName", "luciad");
      result.add(new GeoNamesSearchService(getLucyEnv(), aMapComponent, userName));
    }
    return result;
  }
}
