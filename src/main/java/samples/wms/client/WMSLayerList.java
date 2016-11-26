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
package samples.wms.client;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.luciad.model.ILcdModel;
import com.luciad.util.concurrent.TLcdLockUtil;
import com.luciad.wms.client.model.ALcdWMSNamedLayer;
import com.luciad.wms.client.model.ALcdWMSProxy;
import com.luciad.wms.client.model.TLcdWMSProxyModelDescriptor;
import com.luciad.wms.client.model.TLcdWMSStyledNamedLayerWrapper;

/**
 * Extension of <code>JList</code> that supports controlling the visible status of WMS layers in a
 * WMS model.
 * <p/>
 * A WMS model holds a single object of type <code>ALcdWMSProxy</code>, representing
 * the entry point towards a WMS server. Besides providing access to the WMS capabilities (see
 * {@link ALcdWMSProxy#getWMSCapabilities()}, it also holds the set of
 * WMS layers that need to be retrieved from the WMS server, via a list of
 * <code>TLcdWMSStyledNamedLayerWrapper</code> instances: see {@link
 * ALcdWMSProxy#addStyledNamedLayer(TLcdWMSStyledNamedLayerWrapper)} and
 * {@link ALcdWMSProxy#getStyledNamedLayer(int)}. This
 * <code>JList</code> provides a UI to control the list <code>TLcdWMSStyledNamedLayerWrapper</code>
 * instances:
 * <p/>
 * To use this class, set a WMS model through {@link #setWMSModel(ILcdModel)}.
 * Initially, it will show all <code>TLcdWMSStyledNamedLayerWrapper</code> instances registered
 * on the <code>ALcdWMSProxy</code> contained in the WMS model, together with a selected check box.
 * When a check box gets deselected, the corresponding <code>TLcdWMSStyledNamedLayerWrapper</code>
 * instance is removed from the <code>ALcdWMSProxy</code>, meaning that it will no longer be
 * retrieved from the WMS server. Selecting the checkbox again registers the instance back on the
 * <code>ALcdWMSProxy</code>.
 */
public class WMSLayerList extends JList<TLcdWMSStyledNamedLayerWrapper> implements ListCellRenderer<TLcdWMSStyledNamedLayerWrapper> {

  // Delay in ms to refresh the WMS model based on the selected WMS layers.
  // This delay avoids continuously refreshing the WMS model if the user is still changing the WMS layer selection.
  private static final int DATA_REFRESH_DELAY = 750;

  // The WMS model.
  private ILcdModel fWMSModel;

  // The version of the currently known WMS content.
  private String fUpdateSequence;

  // Data structure to keep track of the registered WMS layers in the WMS model.
  private LinkedHashMap<TLcdWMSStyledNamedLayerWrapper, Boolean> fWMSLayerMap;

  // Thread executor for data refresh tasks.
  private ExecutorService fExecutor = Executors.newSingleThreadExecutor();

  // Timer to incorporate a delay before starting data refresh tasks.
  private Timer fTimer;

  /**
   * Creates a new <code>WMSLayerList</code>.
   */
  public WMSLayerList() {
    super();

    // Behavior & UI initialization.
    setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    setCellRenderer(this);
    setOpaque(false);

    // Listener to check for selection updates.
    addListSelectionListener(new ListSelectionListener() {

      @Override
      public void valueChanged(ListSelectionEvent aEvent) {
        if (!aEvent.getValueIsAdjusting()) {
          // Retrieve the selected WMS layer.
          JList list = (JList) aEvent.getSource();
          TLcdWMSStyledNamedLayerWrapper updatedWmsLayer = (TLcdWMSStyledNamedLayerWrapper) list.getSelectedValue();

          if (updatedWmsLayer != null) {
            // We have an updated WMS layer state.
            // We first update the UI. Afterwards, we update the WMS model asynchronously.

            // 1. UI update
            // Loop over all WMS layers and check whether they need to be registered on the proxy,
            // taking into account the current selection status in the list.
            List<TLcdWMSStyledNamedLayerWrapper> newListOfWMSLayers = new ArrayList<>();
            for (Map.Entry<TLcdWMSStyledNamedLayerWrapper, Boolean> entry : fWMSLayerMap.entrySet()) {
              TLcdWMSStyledNamedLayerWrapper wmsLayer = entry.getKey();
              Boolean visibleStatus = entry.getValue();

              if (updatedWmsLayer.equals(wmsLayer)) {
                visibleStatus = !visibleStatus;
                entry.setValue(visibleStatus);
              }

              if (visibleStatus) {
                newListOfWMSLayers.add(wmsLayer);
              }
            }

            // Make sure to clear the selection in the list, so that the previously selected
            // item can be deselected again and vice versa.
            list.clearSelection();

            // Repaint the list component.
            list.revalidate();
            list.repaint();

            // 2. WMS model update
            updateWMSModel(newListOfWMSLayers);
          }
        }
      }
    });
  }

  /**
   * Selects all listed WMS layers.
   */
  public void selectAll() {
    if (fWMSLayerMap != null) {
      // Update the UI layer list selection.
      for (Map.Entry<TLcdWMSStyledNamedLayerWrapper, Boolean> entry : fWMSLayerMap.entrySet()) {
        entry.setValue(true);
      }

      // Update the WMS model.
      List<TLcdWMSStyledNamedLayerWrapper> newListOfWMSLayers = new ArrayList<>();
      newListOfWMSLayers.addAll(fWMSLayerMap.keySet());
      updateWMSModel(newListOfWMSLayers);

      // Repaint the list component.
      revalidate();
      repaint();
    }
  }

  /**
   * Deselects all listed WMS layers.
   */
  public void clearAll() {
    if (fWMSLayerMap != null) {
      // Update the UI layer list selection.
      for (Map.Entry<TLcdWMSStyledNamedLayerWrapper, Boolean> entry : fWMSLayerMap.entrySet()) {
        entry.setValue(false);
      }

      // Update the WMS model.
      updateWMSModel(Collections.<TLcdWMSStyledNamedLayerWrapper>emptyList());

      // Repaint the list component.
      revalidate();
      repaint();
    }
  }

  /**
   * Sets a WMS model. The model is expected to have a model descriptor of type
   * <code>TLcdWMSProxyModelDescriptor</code> and contain a single <code>ALcdWMSProxy</code>
   * object.
   *
   * @param aModel A WMS model
   */
  public void setWMSModel(ILcdModel aModel) {
    if (aModel.getModelDescriptor() instanceof TLcdWMSProxyModelDescriptor) {
      // We only apply the update if it is a completely new model or if the model has updated content.
      if (fWMSModel == null || fWMSModel != aModel ||
          (fUpdateSequence != null && !fUpdateSequence.equals(getUpdateSequence(aModel)))) {
        fWMSModel = aModel;
        fUpdateSequence = getUpdateSequence(aModel);

        // We are going to read from the WMS model: register a read lock.
        TLcdLockUtil.readLock(fWMSModel);

        try {
          // Get the WMS proxy object from the model. The proxy is the main entry point
          // towards the WMS server.
          final ALcdWMSProxy wmsProxy = getWMSProxy();

          // Define a data structure to keep track of the visible / invisible status
          // of the WMS layers.
          fWMSLayerMap = collectStyledWMSLayers(wmsProxy);

          // Populate this data structure and the UI list component based on the given proxy.
          DefaultListModel<TLcdWMSStyledNamedLayerWrapper> listModel = new DefaultListModel<>();
          for (TLcdWMSStyledNamedLayerWrapper layer : fWMSLayerMap.keySet()) {
            // We add the layers in reverse order to the list, to show the bottom layer
            // at the bottom of the list.
            listModel.add(0, layer);
          }
          setModel(listModel);
        } finally {
          // Release our read lock.
          TLcdLockUtil.readUnlock(fWMSModel);
        }
      }
    } else {
      throw new IllegalArgumentException("Only WMS models are supported");
    }
  }

  public void removeWMSModel() {
    fWMSModel = null;
    fUpdateSequence = null;
    fWMSLayerMap = null;
    setModel(new DefaultListModel<TLcdWMSStyledNamedLayerWrapper>());
  }

  private void updateWMSModel(final List<TLcdWMSStyledNamedLayerWrapper> aNewListOfWMSLayers) {
    if (fTimer != null) {
      fTimer.cancel();
    }
    fTimer = new Timer();
    fTimer.schedule(new TimerTask() {
      @Override
      public void run() {
        fExecutor.submit(new Runnable() {
          @Override
          public void run() {
            // We will update the list of registered WMS layers on the WMS proxy;
            // we therefore take a write lock on the WMS model (that contains the WMS proxy),
            // to avoid any threading issues with the rendering pipeline (that also accesses
            // the proxy).
            TLcdLockUtil.writeLock(fWMSModel);

            try {
              // Get the proxy.
              final ALcdWMSProxy wmsProxy = getWMSProxy();

              // Clear all registered WMS layers from the proxy.
              wmsProxy.clearStyledNamedLayers();

              // Loop over all WMS layers that should be registered on the proxy and register them.
              for (TLcdWMSStyledNamedLayerWrapper wmsLayer : aNewListOfWMSLayers) {
                wmsProxy.addStyledNamedLayer(wmsLayer);
              }

              // The proxy has been updated: clear its cache and fire a model changed
              // event to trigger a repaint.
              wmsProxy.clearCache();
              fWMSModel.elementChanged(wmsProxy, ILcdModel.FIRE_NOW);
            } finally {
              // Release the write lock on the model.
              TLcdLockUtil.writeUnlock(fWMSModel);
            }
          }
        });
      }
    }, DATA_REFRESH_DELAY);
  }

  private LinkedHashMap<TLcdWMSStyledNamedLayerWrapper, Boolean> collectStyledWMSLayers(ALcdWMSProxy aWmsProxy) {
    // Collect all available layers
    ArrayList<ALcdWMSNamedLayer> layers = new ArrayList<ALcdWMSNamedLayer>();
    for (int i = 0; i < aWmsProxy.getWMSRootNamedLayerCount(); i++) {
      collectWMSLayers(aWmsProxy.getWMSRootNamedLayer(i), layers);
    }
    // Collect selected layers
    HashMap<ALcdWMSNamedLayer, Integer> wmsLayerToIndex = new HashMap<ALcdWMSNamedLayer, Integer>();
    for (int i = 0; i < aWmsProxy.getStyledNamedLayerCount(); i++) {
      wmsLayerToIndex.put(aWmsProxy.getStyledNamedLayer(i).getNamedLayer(), i);
    }
    // Initialize the map
    LinkedHashMap<TLcdWMSStyledNamedLayerWrapper, Boolean> styledLayersMap = new LinkedHashMap<TLcdWMSStyledNamedLayerWrapper, Boolean>(layers.size());
    for (ALcdWMSNamedLayer layer : layers) {
      Integer selectedIdx = wmsLayerToIndex.get(layer);
      if (selectedIdx == null) {
        styledLayersMap.put(new TLcdWMSStyledNamedLayerWrapper(layer), Boolean.FALSE);
      } else {
        styledLayersMap.put(aWmsProxy.getStyledNamedLayer(selectedIdx), Boolean.TRUE);
      }
    }
    return styledLayersMap;
  }

  private void collectWMSLayers(ALcdWMSNamedLayer aWmsLayer, List<ALcdWMSNamedLayer> aLayersSFCT) {
    if (aWmsLayer.getNamedLayerName() != null) {
      aLayersSFCT.add(aWmsLayer);
    } else {
      for (int i = 0; i < aWmsLayer.getChildWMSNamedLayerCount(); i++) {
        ALcdWMSNamedLayer childLayer = aWmsLayer.getChildWMSNamedLayer(i);
        collectWMSLayers(childLayer, aLayersSFCT);
      }
    }
  }

  private String getUpdateSequence(ILcdModel aModel) {
    return ((ALcdWMSProxy) aModel.elements().nextElement()).getWMSCapabilities().getUpdateSequence();
  }

  @Override
  // Custom List cell renderer that shows the name of the WMS layer together with a check box.
  public Component getListCellRendererComponent(JList aList,
                                                TLcdWMSStyledNamedLayerWrapper aWMSLayer,
                                                int aIndex,
                                                boolean aSelected,
                                                boolean aHasFocus) {
    String wmsLayerName = aWMSLayer.getNamedLayer().getNamedLayerTitle();
    JCheckBox checkBox = new JCheckBox(wmsLayerName);
    checkBox.setSelected(fWMSLayerMap.get(aWMSLayer));
    return checkBox;
  }

  /**
   * Returns the <code>ALcdWMSProxy</code> instance contained in the WMS model registered through
   * {@link #setWMSModel(ILcdModel)}.
   *
   * @return the <code>ALcdWMSProxy</code> instance contained in the currently registered WMS model
   */
  private ALcdWMSProxy getWMSProxy() {
    return (ALcdWMSProxy) fWMSModel.elements().nextElement();
  }
}
