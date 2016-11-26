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
package samples.lucy.lightspeed.common;

import java.io.IOException;
import java.util.Collection;

import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.lucy.addons.lspmap.TLcyLspMapAddOn;
import com.luciad.lucy.map.ILcyGenericMapComponent;
import com.luciad.lucy.map.TLcyCombinedMapManager;
import com.luciad.lucy.map.lightspeed.TLcyLspMapManager;
import com.luciad.lucy.model.TLcyCompositeModelDecoder;
import com.luciad.util.TLcdNoBoundsException;
import com.luciad.util.TLcdOutOfBoundsException;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.util.TLspViewNavigationUtil;

/**
 * Class containing some utility methods for Lightspeed related samples.
 */
public final class MapComponentUtil {
  /**
   * Source name for a model containing the rivers of the USA.
   */
  public static final String RIVERS_MODEL_SOURCE_NAME = "Data/Shp/Usa/rivers.shp";
  /**
   * Source name for a model containing the cities of the USA.
   */
  public static final String CITIES_MODEL_SOURCE_NAME = "Data/Shp/Usa/city_125.shp";
  /**
   * Source name for a model containing all countries of the world.
   */
  public static final String WORLD_MODEL_SOURCE_NAME = "Data/Shp/World/world.shp";

  /**
   * Private constructor, class only contains static methods.
   */
  private MapComponentUtil() {
  }

  /**
   * Method which makes sure the active map component of Lucy is a Lightspeed map component. When no
   * Lightspeed maps available, it will create a map component and make it the active one.
   *
   * @param aLucyEnv The Lucy back-end
   *
   * @return The Lightspeed map which has been made active
   */
  public static ILcyGenericMapComponent<ILspView, ILspLayer> activateLightspeedMap(ILcyLucyEnv aLucyEnv) {
    TLcyCombinedMapManager combinedMapManager = aLucyEnv.getCombinedMapManager();

    TLcyLspMapManager mapManager = aLucyEnv.getService(TLcyLspMapManager.class);
    if (mapManager == null) {
      throw new UnsupportedOperationException("No TLcyLspMapManager found. Please make sure the TLcyLspMapAddOn is loaded, or a TLcyLspMapManager is registered as a service on the back-end");
    }
    ILcyGenericMapComponent<ILspView, ILspLayer> mapComponent = null;

    //when a Lightspeed map is available, use that one
    if (mapManager.getMapComponentCount() > 0) {
      mapComponent = mapManager.getMapComponent(0);
    } else {
      //no Lightspeed map available so create one
      TLcyLspMapAddOn mapAddOn = aLucyEnv.retrieveAddOnByClass(TLcyLspMapAddOn.class);
      if (mapAddOn == null) {
        throw new UnsupportedOperationException("Please load the TLcyLspMapAddOn first");
      }
      mapComponent = mapAddOn.getMapBackEnd().createMapComponent();
    }

    //make the map active and return
    combinedMapManager.setActiveMapComponent(mapComponent);
    return mapComponent;
  }

  /**
   * Add <code>aModel</code> to <code>aMapComponent</code>, and fit the view on the added model.
   *
   * @param aMapComponent The map component to add the model to
   * @param aSourceName   Source name of the data source to add
   * @param aLucyEnv      The Lucy back-end
   * @param aFit          when <code>true</code>, a fit on the model will be performed
   *
   * @throws IOException when decoding <code>aSourceName</code> fails
   * @throws TLcdNoBoundsException
   *                             when no valid bounds to fit on could be calculated
   * @throws TLcdOutOfBoundsException
   *                             when fitting on the bounds was not possible
   */
  public static void addDataSourceAndFit(ILcyGenericMapComponent<ILspView, ILspLayer> aMapComponent, String aSourceName, ILcyLucyEnv aLucyEnv, boolean aFit) throws IOException, TLcdNoBoundsException, TLcdOutOfBoundsException {
    ILspView view = aMapComponent.getMainView();
    Collection<ILspLayer> addedLayers = view.addLayersFor(new TLcyCompositeModelDecoder(aLucyEnv)
                                                              .decode(aSourceName));
    if (aFit) {
      new TLspViewNavigationUtil(view).fit(addedLayers);
    }
  }
}
