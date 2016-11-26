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
package samples.lucy.search.service.location;

import java.util.Enumeration;
import java.util.List;

import samples.lucy.search.ISearchResult;
import samples.lucy.search.ISearchService;
import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.lucy.map.ILcyGenericMapComponent;
import com.luciad.lucy.map.TLcyGenericMapUtil;
import com.luciad.lucy.util.language.TLcyLang;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelReference;
import com.luciad.model.TLcd2DBoundsIndexedModel;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.shape3D.ILcd3DEditablePoint;
import com.luciad.util.ILcdFireEventMode;
import com.luciad.util.TLcdOutOfBoundsException;
import com.luciad.util.TLcdStatusEvent;
import com.luciad.util.concurrent.TLcdLockUtil;
import com.luciad.util.logging.ILcdLogger;
import com.luciad.util.logging.TLcdLoggerFactory;
import com.luciad.view.ILcdLayer;
import com.luciad.view.ILcdLayered;
import com.luciad.view.ILcdView;

/**
 * <p>
 *   This abstract implementation of the {@link ISearchService} interface can be used
 *   when searching for a point on the map.
 * </p>
 *
 * <p>
 *   This service assumes that the search tasks return {@link IPointSearchResult} instances.
 *   In return, this service will show the search result in a separate layer, mark it with a location icon
 *   and optionally a label, and fit the view on the location.
 * </p>
 */
public abstract class AbstractLocationSearchService implements ISearchService {
  private static final ILcdLogger LOGGER = TLcdLoggerFactory.getLogger(ISearchService.class);

  private final ILcyLucyEnv fLucyEnv;
  private final ILcyGenericMapComponent<? extends ILcdView, ? extends ILcdLayer> fMapComponent;

  protected AbstractLocationSearchService(ILcyLucyEnv aLucyEnv, ILcyGenericMapComponent<? extends ILcdView, ? extends ILcdLayer> aMapComponent) {
    fLucyEnv = aLucyEnv;
    fMapComponent = aMapComponent;
  }

  protected final ILcyLucyEnv getLucyEnv() {
    return fLucyEnv;
  }

  protected final ILcyGenericMapComponent<? extends ILcdView, ? extends ILcdLayer> getMapComponent() {
    return fMapComponent;
  }

  @Override
  public final void onResultChosen(ISearchResult aSearchResult) {
    ILcdModel model = getSearchResultModel(aSearchResult);

    ILcd3DEditablePoint point = model.getModelReference().makeModelPoint().cloneAs3DEditablePoint();
    ILcdPoint result = ((IPointSearchResult) aSearchResult).getResult();
    point.move3D(result.getX(), result.getY(), result.getZ());

    try (TLcdLockUtil.Lock lock = TLcdLockUtil.writeLock(model)) {
      model.removeAllElements(ILcdModel.FIRE_LATER);
      model.addElement(new SearchResultDomainObject(point, ((IPointSearchResult) aSearchResult).getLocationLabel()), ILcdFireEventMode.FIRE_LATER);
    }
    model.fireCollectedModelChanges();

    //The user might have switched the search layer to invisible
    ILcdLayer searchResultLayer = getSearchResultLayer();
    searchResultLayer.setVisible(true);

    fitOnSearchResult(aSearchResult);
  }

  private void fitOnSearchResult(ISearchResult aSearchResult) {
    ILcdView view = fMapComponent.getMainView();

    TLcyGenericMapUtil mapUtil = new TLcyGenericMapUtil(fLucyEnv);
    try {
      mapUtil.fitOnBounds(view, aSearchResult.getBounds(), aSearchResult.getReference());
    } catch (TLcdOutOfBoundsException exception) {
      if (LOGGER.isTraceEnabled()) {
        LOGGER.warn("Could not fit on search result [" + aSearchResult.getResult() + "]. Reason: " + exception.getMessage());
      }
      TLcdStatusEvent.sendMessage(fLucyEnv, this, TLcyLang.getString("Can't fit map, object(s) not visible in current projection"), TLcdStatusEvent.Severity.WARNING);
    }
  }

  private ILcdLayer getSearchResultLayer() {
    ILcdView mainView = fMapComponent.getMainView();
    if (mainView instanceof ILcdLayered) {
      Enumeration layers = ((ILcdLayered) mainView).layers();

      while (layers.hasMoreElements()) {
        ILcdLayer layer = (ILcdLayer) layers.nextElement();

        if (layer.getModel().getModelDescriptor() instanceof SearchResultModelDescriptor) {
          return layer;
        }
      }
    }

    return null;
  }

  /**
   * Checks if the given model is a model with search results.
   * @param aModel The model to test.
   * @return true if the given model is a model with search results, false otherwise.
   */
  public static boolean isSearchResultModel(ILcdModel aModel) {
    return aModel.getModelDescriptor() instanceof SearchResultModelDescriptor;
  }

  private ILcdModel getSearchResultModel(ISearchResult aSearchResult) {
    ILcdLayer searchResultLayer = getSearchResultLayer();
    if (searchResultLayer != null) {
      return searchResultLayer.getModel();
    }

    TLcd2DBoundsIndexedModel model = new TLcd2DBoundsIndexedModel();
    model.setModelReference((ILcdModelReference) aSearchResult.getReference());
    model.setModelDescriptor(new SearchResultModelDescriptor());

    ILcdView mainView = getMapComponent().getMainView();
    mainView.addModel(model);

    return model;
  }

  @Override
  public final void onClearSearchBox() {
    ILcdLayer searchResultLayer = getSearchResultLayer();
    if (searchResultLayer != null) {
      ILcdView mainView = getMapComponent().getMainView();
      mainView.removeModel(searchResultLayer.getModel());
    }
  }

  @Override
  public void sort(List<ISearchResult> aResultsSFCT) {
    //no sorting is needed
  }
}
