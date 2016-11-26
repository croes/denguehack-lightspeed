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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.table.TableCellRenderer;

import samples.lucy.search.ISearchResult;
import samples.lucy.search.ISearchService;
import samples.lucy.search.ISearchTask;
import samples.lucy.search.service.location.AbstractLocationSearchService;
import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.lucy.map.ILcyGenericMapComponent;
import com.luciad.lucy.map.TLcyGenericMapUtil;
import com.luciad.lucy.util.language.TLcyLang;
import com.luciad.model.ILcdIntegerIndexedModel;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelDescriptor;
import com.luciad.model.transformation.ALcdTransformingModel;
import com.luciad.util.ILcdFilter;
import com.luciad.util.ILcdFireEventMode;
import com.luciad.util.TLcdNoBoundsException;
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
 *   This {@code ISearchService} implementation allows to search through the data of
 *   all {@code ILcdModel} instances present in a map component.
 * </p>
 *
 * <p>
 *   The rendered results will indicate to which layer the result belongs, and show the
 *   property name and value which matched the search query.
 * </p>
 *
 * <p>
 *   When the user selects the result, the map will be fitted on the domain object.
 *   The domain object will be selected as well.
 * </p>
 *
 * <p>
 *   The actual search is performed by the search tasks created by the {@link ModelDataSearchTaskFactory}
 *   instance passed in the constructor.
 * </p>
 */
public final class ModelDataSearchService implements ISearchService {

  private static final ILcdLogger LOGGER = TLcdLoggerFactory.getLogger(ISearchService.class);

  /**
   * Filter which only let models pass on which it is safe to call {@link ILcdModel#elements()}
   */
  private static final ILcdFilter<ILcdModel> IS_ENUMERABLE_MODEL = new ILcdFilter<ILcdModel>() {
    @Override
    public boolean accept(ILcdModel aModel) {
      ILcdModelDescriptor modelDescriptor = aModel.getModelDescriptor();
      return aModel instanceof ILcdIntegerIndexedModel ||
             "KML22".equals(modelDescriptor.getTypeName());
    }
  };

  private final ILcyLucyEnv fLucyEnv;
  private final ILcyGenericMapComponent<? extends ILcdView, ? extends ILcdLayer> fMapComponent;
  private final TableCellRenderer fRenderer;
  private final ModelDataSearchTaskFactory fModelDataSearchTaskFactory;

  public ModelDataSearchService(ILcyLucyEnv aLucyEnv,
                                ILcyGenericMapComponent<? extends ILcdView, ? extends ILcdLayer> aMapComponent,
                                ModelDataSearchTaskFactory aModelDataSearchTaskFactory) {
    fLucyEnv = aLucyEnv;
    fModelDataSearchTaskFactory = aModelDataSearchTaskFactory;
    fRenderer = new DataResultRenderer(fLucyEnv);
    fMapComponent = aMapComponent;
  }

  @Override
  public final List<? extends ISearchTask> createSearchTasks() {
    ILcdView mainView = fMapComponent.getMainView();

    if (mainView instanceof ILcdLayered) {
      List<ISearchTask> tasks = new ArrayList<>(((ILcdLayered) mainView).layerCount());

      Enumeration layers = ((ILcdLayered) mainView).layers();
      while (layers.hasMoreElements()) {
        Object o = layers.nextElement();
        if (o instanceof ILcdLayer) {
          ILcdLayer layer = (ILcdLayer) o;
          ILcdModel model = layer.getModel();
          if (IS_ENUMERABLE_MODEL.accept(model) &&
              !AbstractLocationSearchService.isSearchResultModel(model)) {
            ModelDataSearchTask task = fModelDataSearchTaskFactory.createSearchTask(this, model, layer);
            if (task != null) {
              tasks.add(task);
            }
          }
        }
      }
      return tasks;
    }

    return Collections.emptyList();
  }

  @Override
  public final void onResultChosen(ISearchResult aSearchResult) {
    DataSearchResult searchResult = (DataSearchResult) aSearchResult;

    ILcdView view = fMapComponent.getMainView();
    TLcyGenericMapUtil mapUtil = new TLcyGenericMapUtil(fLucyEnv);
    try {
      mapUtil.fitOnObjects(view, searchResult.getLayer(), searchResult.getResult());
    } catch (TLcdOutOfBoundsException | TLcdNoBoundsException exception) {
      if (LOGGER.isTraceEnabled()) {
        LOGGER.warn("Could not fit on search result [" + aSearchResult.getResult() + "]. Reason: " + exception.getMessage());
      }
      TLcdStatusEvent.sendMessage(fLucyEnv, this, TLcyLang.getString("Can't fit map, object(s) not visible in current projection"), TLcdStatusEvent.Severity.WARNING);
    }

    ILcdLayer layer = ((DataSearchResult) aSearchResult).getLayer();

    ILcdView mainView = fMapComponent.getMainView();
    clearSelectionInView(mainView);

    layer.setSelectable(true);
    if (layer.getModel() instanceof ALcdTransformingModel) {
      Collection<Object> objectsToSelect = null;
      try (TLcdLockUtil.Lock autoUnlock = TLcdLockUtil.readLock(layer.getModel())) {
        objectsToSelect = ((ALcdTransformingModel) layer.getModel()).originalToTransformed(Collections.singleton(aSearchResult.getResult()));
      }
      for (Object o : objectsToSelect) {
        layer.selectObject(o, true, ILcdFireEventMode.FIRE_LATER);
      }
      layer.fireCollectedSelectionChanges();
    }
    layer.selectObject(aSearchResult.getResult(), true, ILcdFireEventMode.FIRE_NOW);
  }

  private void clearSelectionInView(ILcdView aMainView) {
    if (aMainView instanceof ILcdLayered) {
      Enumeration layers = ((ILcdLayered) aMainView).layers();
      while (layers.hasMoreElements()) {
        ILcdLayer layer = (ILcdLayer) layers.nextElement();
        layer.clearSelection(ILcdFireEventMode.FIRE_NOW);
      }
    }
  }

  @Override
  public final TableCellRenderer getSearchResultRenderer() {
    return fRenderer;
  }

  @Override
  public final void onClearSearchBox() {
    // Don't do anything
  }

  @Override
  public void sort(List<ISearchResult> aResultsSFCT) {
    // Build look-up map from layer to index.
    final Map<ILcdLayer, Integer> layerIndices = new HashMap<>();
    if (fMapComponent.getMainView() instanceof ILcdLayered) {
      ILcdLayered layered = (ILcdLayered) fMapComponent.getMainView();
      for ( int i = 0; i < layered.layerCount(); i++ ) {
        layerIndices.put(layered.getLayer(i), i);
      }
    }

    Map<ISearchResult, Integer> scores = scoreOnUniqueness(aResultsSFCT);

    sort(aResultsSFCT, scores, layerIndices);
  }

  /**
   * Count how many times a certain property name occurs in the search results, and prefer
   * those occurring less often. Those are more distinctive, so likely more relevant.
   * @param aResults The search results.
   * @return The scores of the search results. Lower is better.
   */
  private Map<ISearchResult, Integer> scoreOnUniqueness(List<ISearchResult> aResults) {
    // Build up a map: Layer -> PropertyPath[] -> Count
    Map<ILcdLayer, Map<List<String>, Integer>> layerPropertyPathCount = new HashMap<>();
    for (ISearchResult r : aResults) {
      DataSearchResult result = (DataSearchResult) r;
      ILcdLayer layer = result.getLayer();
      Map<List<String>, Integer> propertyPathCount = layerPropertyPathCount.get(layer);
      if (propertyPathCount == null) {
        propertyPathCount = new HashMap<>();
        layerPropertyPathCount.put(layer, propertyPathCount);
      }

      List<String> propertyPath =  Arrays.asList(result.getPropertyPath()); //using List as it has decent hashCode/equals methods
      Integer count = propertyPathCount.get(propertyPath);
      if (count == null) {
        count = 0;
      }
      propertyPathCount.put(propertyPath, count + 1);
    }

    // Score the results: the less often a property occurs, the more distinctive it is, the better the score.
    final Map<ISearchResult, Integer> scores = new HashMap<>(); //lower is better
    for (ISearchResult r : aResults) {
      DataSearchResult result = (DataSearchResult) r;
      ILcdLayer layer = result.getLayer();
      List<String> propertyPath = Arrays.asList(result.getPropertyPath());
      scores.put(r, layerPropertyPathCount.get(layer).get(propertyPath));
    }
    return scores;
  }

  /**
   *  Sort according to
   *    1) Uniqueness score
   *    2) prefer top-level properties over deeply buried ones
   *    3) respect layer order, prefer topmost layers
   *    4) on the string representation value, to get deterministic behavior
   * @param aResultsSFCT The results to sort in place.
   * @param aUniquenessScores The scores for the search results.
   * @param aLayerIndices The layer indices.
   */
  private void sort(List<ISearchResult> aResultsSFCT,
                    final Map<ISearchResult, Integer> aUniquenessScores,
                    final Map<ILcdLayer, Integer> aLayerIndices) {
    Collections.sort(aResultsSFCT, new Comparator<ISearchResult>() {
      @Override
      public int compare(ISearchResult o1, ISearchResult o2) {
        Integer s1 = aUniquenessScores.get(o1);
        Integer s2 = aUniquenessScores.get(o2);
        int compare = s1.compareTo(s2);

        if (compare == 0) {
          String[] path1 = ((DataSearchResult) o1).getPropertyPath();
          String[] path2 = ((DataSearchResult) o2).getPropertyPath();
          compare = Integer.compare(path1.length, path2.length);
        }

        if (compare == 0) {
          Integer i1 = aLayerIndices.get(((DataSearchResult) o1).getLayer());
          Integer i2 = aLayerIndices.get(((DataSearchResult) o2).getLayer());
          if (i1 != null && i2 != null) {
            compare = i2.compareTo(i1); //higher index == top layer == better
          }
        }

        if (compare == 0) {
          // Some arbitrary but deterministic ordering
          String value1 = o1.getStringRepresentation();
          String value2 = o2.getStringRepresentation();
          compare = value1.compareTo(value2);
        }

        return compare;
      }
    });
  }

  @Override
  public int getServicePriority() {
    return HIGH_SERVICE_PRIORITY;
  }

  /**
   * Interface defining a factory method for {@link ModelDataSearchTask} instance
   */
  public interface ModelDataSearchTaskFactory {
    /**
     * Create a new {@code ModelDataSearchTask} instance for the specified model
     *
     * @param aModelDataSearchService The {@code ModelDataSearchService} instance who wants to create the search task
     * @param aModel The model for which a task must be created
     * @param aLayer The layer containing the model
     *
     * @return a {@code ModelDataSearchTask} for the specified model, or {@code null} when the model is not supported by this factory
     */
    ModelDataSearchTask createSearchTask(ModelDataSearchService aModelDataSearchService, ILcdModel aModel, ILcdLayer aLayer);
  }
}
