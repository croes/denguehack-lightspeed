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
package samples.wms.client.common;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.swing.SwingUtilities;

import com.luciad.datamodel.ILcdDataObject;
import com.luciad.gui.TLcdAWTUtil;
import samples.wms.common.WMSGetFeatureInfoModelDataTypes;
import samples.wms.common.WMSGetFeatureInfoModelFactory;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelDescriptor;
import com.luciad.model.ILcdModelReference;
import com.luciad.shape.ILcdPoint;
import com.luciad.util.ILcdFireEventMode;
import com.luciad.util.ILcdSelectionListener;
import com.luciad.util.TLcdSelectionChangedEvent;
import com.luciad.util.concurrent.TLcdLockUtil;
import com.luciad.view.ALcdWeakLayeredListener;
import com.luciad.view.ILcdLayer;
import com.luciad.view.ILcdLayered;
import com.luciad.view.ILcdView;
import com.luciad.view.TLcdLayeredEvent;
import com.luciad.wms.client.model.ALcdOGCWMSCapabilities;
import com.luciad.wms.client.model.ALcdWMSNamedLayer;
import com.luciad.wms.client.model.ALcdWMSProxy;
import com.luciad.wms.client.model.TLcdWMSGetFeatureInfoContext;
import com.luciad.wms.client.model.TLcdWMSGetFeatureInfoParameters;
import com.luciad.wms.client.model.TLcdWMSProxyModelDescriptor;

/**
 * <p>Mouse listener that triggers GetFeatureInfo requests on mouse click. GetFeatureInfo responses are decoded and
 * visualized in a dedicated layer, making them work similarly to selection. Requests are performed asynchronously.</p>
 *
 * <p>It is possible to customize the following settings:</p>
 * <ul>
 *   <li>Modify the used mime types: {@link #setSupportedMimeTypes(Collection)}</li>
 *   <li>Modify the used mime types per WMS layer: {@link #setSupportedMimeTypesForLayer(String, Collection)}</li>
 * </ul>
 */
public abstract class WMSGetFeatureInfoMouseListener<V extends ILcdLayered & ILcdView, L extends ILcdLayer> extends
                                                                                                            MouseAdapter {

  private static final String MIME_TYPE_JSON = "application/json";
  private static final String MIME_TYPE_GML31 = "application/vnd.ogc.gml/3.1.1";
  private static final String MIME_TYPE_GML2 = "application/vnd.ogc.gml";
  public static final String MIME_TYPE_HTML = "text/html";
  public static final String MIME_TYPE_PLAIN_TEXT = "text/plain";

  // Global and per-layer settings
  private Collection<String> fSupportedMimeTypes;
  private Map<String, Collection<String>> fSupportedMimeTypesForLayer = new HashMap<>();

  private final ThreadPoolExecutor fExecutor;

  private final V fView;
  private L fFeatureInfoLayer = null;
  private L fOriginalLayer = null;
  private boolean fRemovingFeatureInfoLayer; // Used to prevent re-entrant removing of feature layers

  public WMSGetFeatureInfoMouseListener(V aView) {
    fExecutor = new ThreadPoolExecutor(1, 1, 10, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), new ThreadFactory() {
      @Override
      public Thread newThread(Runnable r) {
        Thread thread = new Thread(r, "WMSGetFeatureInfoThread");
        thread.setDaemon(true);
        return thread;
      }
    });
    fExecutor.allowCoreThreadTimeOut(true);
    fView = aView;
    fView.addLayeredListener(new LayeredListener<>(this));
    fSupportedMimeTypes = Arrays.asList(
        MIME_TYPE_JSON,
        MIME_TYPE_GML31,
        MIME_TYPE_GML2,
        MIME_TYPE_HTML,
        MIME_TYPE_PLAIN_TEXT
    );
  }

  /**
   * Sets the mime types to use for GetFeatureInfo requests.
   * @param aSupportedMimeTypes the mime types to use for GetFeatureInfo requests.
   */
  public synchronized void setSupportedMimeTypes(Collection<String> aSupportedMimeTypes) {
    if (aSupportedMimeTypes == null) {
      throw new NullPointerException("Supported mime types should not be null, use an empty collection instead");
    }
    fSupportedMimeTypes = aSupportedMimeTypes;
  }

  /**
   * Returns the supported mime types.
   * @return the supported mime types.
   */
  public synchronized Collection<String> getSupportedMimeTypes() {
    return new ArrayList<>(fSupportedMimeTypes);
  }

  /**
   * Sets the mime types to use for GetFeatureInfo requests for the given layer. If not set, the default
   * {@linkplain #setSupportedMimeTypes(Collection) mime types} will be used instead.
   * @param aWMSLayerName       a WMS layer name
   * @param aSupportedMimeTypes the mime types to use for GetFeatureInfo requests.
   */
  public synchronized void setSupportedMimeTypesForLayer(String aWMSLayerName, Collection<String> aSupportedMimeTypes) {
    if (aSupportedMimeTypes == null || aSupportedMimeTypes.isEmpty()) {
      throw new NullPointerException("Supported mime types for layer (" + aWMSLayerName + ") should not be null or empty");
    }
    fSupportedMimeTypesForLayer.put(aWMSLayerName, new ArrayList<>(aSupportedMimeTypes));
  }

  /**
   * Returns the mime types to use for GetFeatureInfo requests for specific layers.
   * @return the mime types to use for GetFeatureInfo requests for specific layers.
   */
  public synchronized Map<String, Collection<String>> getSupportedMimeTypesForLayers() {
    return new HashMap<>(fSupportedMimeTypesForLayer);
  }

  public synchronized Collection<String> getSupportedMimeTypesForLayer(String aWMSLayerName) {
    Collection<String> mimeTypes = fSupportedMimeTypesForLayer.get(aWMSLayerName);
    if (mimeTypes == null) {
      mimeTypes = fSupportedMimeTypes;
    }
    if (mimeTypes == null || mimeTypes.isEmpty()) {
      throw new IllegalStateException("No supported mime types defined for WMS layer: " + aWMSLayerName);
    }
    return mimeTypes;
  }

  public boolean isBusy() {
    return fExecutor.getActiveCount() > 0 || !fExecutor.getQueue().isEmpty();
  }

  protected abstract TLcdWMSGetFeatureInfoContext getFeatureInfoContext(ALcdWMSProxy aProxy, L aLayer, V aView, int aX, int aY);

  protected abstract ILcdPoint getQueryLocationInModelCoordinates(L aLayer, V aView, int aX, int aY);

  protected abstract void addFeatureInfoLayer(V aView, ILcdModel aModel);

  protected abstract boolean isSelectControllerActive(V aView);

  /**
   * <p>Handles the given mouse event. This method will do a GetFeatureInfo request on a background thread
   * and process the result by creating a GetFeatureInfo layer that contains the response (as a selected
   * ILcdDataObject).</p>
   *
   * <p><strong>Note:</strong> This method calls invokeLater() before doing anything to make sure that the select
   * controller handles the event first. Custom code that calls this method should make sure that it is only called
   * when the select controller has been invoked, or will be invoked immediately after.</p>
   * @param e a mouse event
   */
  protected final void handleEvent(final MouseEvent e) {
    // Call invokeLater to make sure that the select controller has the opportunity to perform selection first.
    // The result of selection is used (among others) to determine if a GetFeatureInfo request should be made.
    TLcdAWTUtil.invokeLater(new Runnable() {
      @Override
      public void run() {
        if (shouldRemoveFeatureInfo(e, fView)) {
          removeCurrentFeatureInfoLayer();
          return;
        }
        if (shouldIgnoreEvent(e, fView)) {
          return;
        }

        queryGetFeatureInfo(e.getX(), e.getY());
      }
    });
  }

  private boolean shouldIgnoreEvent(MouseEvent e, V aView) {
    if (!isSelectControllerActive(aView)) {
      return true;
    }
    if (!SwingUtilities.isLeftMouseButton(e)) {
      return true;
    }
    return someObjectsAreSelected(aView, false);
  }

  private boolean shouldRemoveFeatureInfo(MouseEvent e, V aView) {
    if ((e.getModifiersEx() & MouseEvent.ALT_DOWN_MASK) != 0 ||
        (e.getModifiersEx() & MouseEvent.SHIFT_DOWN_MASK) != 0 ||
        (e.getModifiersEx() & MouseEvent.CTRL_DOWN_MASK) != 0) {
      return true;
    }
    if (someObjectsAreSelected(aView, true)) {
      return true;
    }
    return false;
  }

  private boolean someObjectsAreSelected(V aView, boolean aIgnoreGetFeatureInfoSelection) {
    Enumeration layers = aView.layers();
    while (layers.hasMoreElements()) {
      ILcdLayer layer = (ILcdLayer) layers.nextElement();
      if (aIgnoreGetFeatureInfoSelection && isGetFeatureInfoLayer(layer)) {
        continue;
      }
      if (layer.getSelectionCount() > 0) {
        return true;
      }
    }
    return false;
  }

  private boolean isGetFeatureInfoLayer(ILcdLayer aLayer) {
    return WMSGetFeatureInfoModelFactory.isGetFeatureInfoModel(aLayer.getModel());
  }

  private void removeCurrentFeatureInfoLayer() {
    if (fRemovingFeatureInfoLayer) {
      // This method can be triggered by this method call itself (e.g. by clearing the selection).
      // this check prevents problems in that case.
      return;
    }
    if (fFeatureInfoLayer != null) {
      fRemovingFeatureInfoLayer = true;
      try {
        fFeatureInfoLayer.clearSelection(ILcdFireEventMode.FIRE_NOW);
        fView.removeLayer(fFeatureInfoLayer);
      } finally {
        fRemovingFeatureInfoLayer = false;
      }
    }
    fFeatureInfoLayer = null;
    fOriginalLayer = null;
  }

  public void queryGetFeatureInfo(int aX, int aY) {
    final List<FeatureInfoContext<L>> featureInfoContexts = retrieveFeatureInfoContexts(aX, aY);
    if (featureInfoContexts.isEmpty()) {
      removeCurrentFeatureInfoLayer();
      return;
    }
    // Execute the GetFeatureInfo request in a background thread. Otherwise the EDT is potentially blocked for long time.
    fExecutor.execute(new Runnable() {
      @Override
      public void run() {
        for (final FeatureInfoContext<L> featureInfoContext : featureInfoContexts) {
          final ILcdModel featureInfoModel = doGetFeatureInfoRequest(featureInfoContext);
          if (featureInfoModel != null) {
            TLcdAWTUtil.invokeLater(new Runnable() {
              @Override
              public void run() {
                handleGetFeatureInfoObject(featureInfoModel, featureInfoContext.fLayer);
              }
            });
            return;
          }
        }
        TLcdAWTUtil.invokeLater(new Runnable() {
          @Override
          public void run() {
            removeCurrentFeatureInfoLayer();
          }
        });
      }
    });
  }

  @SuppressWarnings("unchecked")
  private List<FeatureInfoContext<L>> retrieveFeatureInfoContexts(int aX, int aY) {
    List<FeatureInfoContext<L>> contexts = new ArrayList<>();

    Enumeration layers = fView.layersBackwards();
    while (layers.hasMoreElements()) {
      L layer = (L) layers.nextElement();
      FeatureInfoContext<L> featureInfoContext = getFeatureInfoContextForLayer(layer, aX, aY);
      if (featureInfoContext != null) {
        contexts.add(featureInfoContext);
      }
    }
    return contexts;
  }

  private FeatureInfoContext<L> getFeatureInfoContextForLayer(L aLayer, int aX, int aY) {
    if (!aLayer.isVisible()) {
      return null;
    }
    ILcdModel model = aLayer.getModel();
    ILcdModelDescriptor modelDescriptor = model.getModelDescriptor();
    if (!(modelDescriptor instanceof TLcdWMSProxyModelDescriptor)) {
      return null;
    }

    try (TLcdLockUtil.Lock ignored = TLcdLockUtil.readLock(model)) {
      // Get the WMS proxy object.
      Enumeration elements = model.elements();
      if (!elements.hasMoreElements()) {
        return null;
      }
      ALcdWMSProxy proxy = (ALcdWMSProxy) elements.nextElement();

      Set<String> mimeTypesFromCapabilities = getMimeTypesFromCapabilities(proxy);
      if (mimeTypesFromCapabilities.isEmpty()) {
        return null;
      }

      List<ALcdWMSNamedLayer> queryableLayers = proxy.getNamedLayers(true, true);
      List<String> enabledQueryableLayers = getNamesOfEnabledLayers(queryableLayers);
      if (enabledQueryableLayers.isEmpty()) {
        return null;
      }
      boolean useSingleQuery = canUseSingleQuery(enabledQueryableLayers);
      TLcdWMSGetFeatureInfoContext featureInfoContext = getFeatureInfoContext(proxy, aLayer, fView, aX, aY);
      return featureInfoContext == null ? null : new FeatureInfoContext<>(aLayer,
                                                                          aLayer.getModel(),
                                                                          proxy,
                                                                          useSingleQuery,
                                                                          mimeTypesFromCapabilities,
                                                                          enabledQueryableLayers,
                                                                          featureInfoContext,
                                                                          getQueryLocationInModelCoordinates(aLayer, fView, aX, aY));
    }
  }

  private List<String> getNamesOfEnabledLayers(List<ALcdWMSNamedLayer> aLayers) {
    List<String> enabledLayers = new ArrayList<>();
    for (ALcdWMSNamedLayer layer : aLayers) {
      enabledLayers.add(layer.getNamedLayerName());
    }
    return enabledLayers;
  }

  private Set<String> getMimeTypesFromCapabilities(ALcdWMSProxy aProxy) {
    Set<String> mimeTypesFromCapabilities = new HashSet<>();
    ALcdOGCWMSCapabilities capabilities = aProxy.getWMSCapabilities();
    int count = capabilities.getSupportedFeatureInfoFormatCount();
    for (int i = 0; i < count; i++) {
      String supportedFormat = capabilities.getSupportedFeatureInfoFormat(i);
      mimeTypesFromCapabilities.add(supportedFormat);
    }
    return mimeTypesFromCapabilities;
  }

  private boolean canUseSingleQuery(List<String> aLayersToQuery) {
    Collection<String> supportedMimeTypes = getSupportedMimeTypes();
    for (String layerName : aLayersToQuery) {
      Collection<String> mimeTypes = getSupportedMimeTypesForLayers().get(layerName);
      if (mimeTypes != null && (!mimeTypes.containsAll(supportedMimeTypes) || !supportedMimeTypes.containsAll(mimeTypes))) {
        return false;
      }
    }
    return true;
  }

  private ILcdModel doGetFeatureInfoRequest(FeatureInfoContext<L> aFeatureInfoContext) {
    if (aFeatureInfoContext.fCanUseSingleQuery) {
      // Optimization: use a single query if the WMS layers all have the same supported mime types
      String mimeType = getFirstSupportedMimeType(getSupportedMimeTypes(), aFeatureInfoContext);
      if (mimeType == null) {
        return null;
      }
      return createFeatureInfoModel(mimeType, aFeatureInfoContext.fLayersToQuery, aFeatureInfoContext);
    } else {
      for (int i = aFeatureInfoContext.fLayersToQuery.size() - 1; i >= 0; i--) {
        String layerToQuery = aFeatureInfoContext.fLayersToQuery.get(i);
        String mimeType = getFirstSupportedMimeType(getSupportedMimeTypesForLayer(layerToQuery), aFeatureInfoContext);
        if (mimeType == null) {
          return null;
        }
        ILcdModel featureInfoModel = createFeatureInfoModel(mimeType, Collections.singletonList(layerToQuery), aFeatureInfoContext);
        if (featureInfoModel != null) {
          return featureInfoModel;
        }
      }
    }
    return null;
  }

  private String getFirstSupportedMimeType(Collection<String> aMimeTypes, FeatureInfoContext aFeatureInfoContext) {
    for (String mimeType : aMimeTypes) {
      if (aFeatureInfoContext.fMimeTypesFromCapabilities.contains(mimeType)) {
        return mimeType;
      }
    }
    return null;
  }

  private ILcdModel createFeatureInfoModel(String aMimeType, Collection<String> aLayersToQuery, FeatureInfoContext<L> aContext) {
    TLcdWMSGetFeatureInfoContext featureInfoContext = aContext.fGetFeatureInfoContext;
    if (featureInfoContext == null) {
      return null;
    }
    TLcdWMSGetFeatureInfoParameters parameters = TLcdWMSGetFeatureInfoParameters.newBuilder().featureInfoFormat(aMimeType).layersToQuery(aLayersToQuery).build();
    try {
      InputStream featureInfoInputStream;
      try (TLcdLockUtil.Lock ignored = TLcdLockUtil.readLock(aContext.fModel)) {
        featureInfoInputStream = aContext.fProxy.createFeatureInfoInputStream(featureInfoContext, parameters);
      }
      if (featureInfoInputStream == null) {
        return null;
      }
      try {
        ILcdPoint queryLocation = aContext.fQueryLocationInModelCoordinates;
        ILcdModelReference queryLocationReference = aContext.fLayer.getModel().getModelReference();
        if (queryLocation != null) {
          String displayName = aContext.fLayer.getLabel() + " Info";
          return WMSGetFeatureInfoModelFactory.convertToModel(featureInfoInputStream, aMimeType, queryLocation, queryLocationReference, displayName);
        }
      } finally {
        try {
          featureInfoInputStream.close();
        } catch (IOException ignored) {
        }
      }
    } catch (IOException ignored) {
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  private void handleGetFeatureInfoObject(ILcdModel aFeatureInfoModel, L aWMSLayer) {
    // Add a feature info layer that shows the data
    // Note: it's not easy to reuse the same feature info layer because the data model
    // may change for each GetFeatureInfo request, so we just create a new layer each time
    removeCurrentFeatureInfoLayer();
    addFeatureInfoLayer(fView, aFeatureInfoModel);
    fFeatureInfoLayer = (L) fView.layerOf(aFeatureInfoModel);
    if (fFeatureInfoLayer != null) {
      fOriginalLayer = aWMSLayer;

      int wmsLayerIndex = fView.indexOf(fOriginalLayer);
      fView.moveLayerAt(wmsLayerIndex + 1, fFeatureInfoLayer);

      Collection<Object> objectsToSelect = new ArrayList<>();
      try (TLcdLockUtil.Lock ignored = TLcdLockUtil.readLock(aFeatureInfoModel)) {
        Enumeration elements = aFeatureInfoModel.elements();
        while (elements.hasMoreElements()) {
          objectsToSelect.add(elements.nextElement());
        }
      }
      for (Object objectToSelect : objectsToSelect) {
        fFeatureInfoLayer.selectObject(objectToSelect, true, ILcdFireEventMode.FIRE_LATER);
      }
      fFeatureInfoLayer.fireCollectedSelectionChanges();
      fFeatureInfoLayer.addSelectionListener(new SelectionListener());
    }
  }

  public static boolean shouldPaintLabel(Object aObject) {
    if (aObject instanceof ILcdDataObject) {
      ILcdDataObject dataObject = (ILcdDataObject) aObject;
      if (dataObject.getDataType() == WMSGetFeatureInfoModelDataTypes.DATA_TYPE) {
        return false;
      }
    }
    return true;
  }

  private static class FeatureInfoContext<L extends ILcdLayer> {

    private final L fLayer;
    private final ILcdModel fModel;
    private final ALcdWMSProxy fProxy;
    private final boolean fCanUseSingleQuery;
    private final Set<String> fMimeTypesFromCapabilities;
    private final List<String> fLayersToQuery;
    private final TLcdWMSGetFeatureInfoContext fGetFeatureInfoContext;
    private final ILcdPoint fQueryLocationInModelCoordinates;

    FeatureInfoContext(L aLayer,
                       ILcdModel aModel, ALcdWMSProxy aProxy,
                       boolean aCanUseSingleQuery,
                       Set<String> aMimeTypesFromCapabilities,
                       List<String> aLayersToQuery,
                       TLcdWMSGetFeatureInfoContext aGetFeatureInfoContext, ILcdPoint aQueryLocationInModelCoordinates) {
      fLayer = aLayer;
      fModel = aModel;
      fProxy = aProxy;
      fCanUseSingleQuery = aCanUseSingleQuery;
      fMimeTypesFromCapabilities = aMimeTypesFromCapabilities;
      fLayersToQuery = aLayersToQuery;
      fGetFeatureInfoContext = aGetFeatureInfoContext;
      fQueryLocationInModelCoordinates = aQueryLocationInModelCoordinates;
    }
  }

  // Used to clean up the GetFeatureInfo layer when the original WMS layer is removed.
  private static class LayeredListener<V extends ILcdLayered & ILcdView, L extends ILcdLayer> extends
                                                                                              ALcdWeakLayeredListener<WMSGetFeatureInfoMouseListener> {

    LayeredListener(WMSGetFeatureInfoMouseListener<V, L> aListener) {
      super(aListener);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void layeredStateChangeImpl(WMSGetFeatureInfoMouseListener aListener, TLcdLayeredEvent aLayeredEvent) {
      if (aLayeredEvent.getID() == TLcdLayeredEvent.LAYER_REMOVED) {
        ILcdLayer removedLayer = aLayeredEvent.getLayer();
        ILcdLayer originalLayer = aListener.fOriginalLayer;
        ILcdLayer featureInfoLayer = aListener.fFeatureInfoLayer;
        if (removedLayer == originalLayer) {
          aListener.removeCurrentFeatureInfoLayer();
        } else if (removedLayer == featureInfoLayer) {
          aListener.removeCurrentFeatureInfoLayer();
        }
      }
    }
  }

  private class SelectionListener implements ILcdSelectionListener {
    @Override
    public void selectionChanged(TLcdSelectionChangedEvent aSelectionEvent) {
      if (aSelectionEvent.deselectedElements().hasMoreElements()) {
        removeCurrentFeatureInfoLayer();
      }
    }
  }
}
