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
package samples.lightspeed.demo.framework.data;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.*;

import org.jdom.Element;
import org.jdom.Namespace;

import com.luciad.model.ILcdModel;
import com.luciad.util.logging.ILcdLogger;
import com.luciad.util.logging.TLcdLoggerFactory;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.ILspLayerFactory;

import samples.lightspeed.demo.framework.exception.DataSetException;
import samples.lightspeed.demo.framework.util.LogUtil;

/**
 * The layer manager is responsible for creating and managing
 * all ILspLayer objects that are used in the application.
 */
class LayerManager {

  private static ILcdLogger sLogger = TLcdLoggerFactory.getLogger(LayerManager.class);

  // List of layer factories that are loaded
  private List<ILspLayerFactory> fLayerFactories;

  // Mapping from layer ids to view-layer pairs (needed because there can be more than one view)
  private Map<String, WeakHashMap<ILspView, List<ILspLayer>>> fLayerMap;

  // List of ids that are associated to base layers (i.e. layers that are always visible)
  private ArrayList<String> fBaseLayerIDs;

  // Base layers
  private Set<ILspLayer> fBaseLayers;

  // List of XML elements that define the layers
  private List<Element> fLayerElements;

  // Link to dataset manager, needed to retrieve the models for which layers are created
  private DataSetManager fDataSetManager;

  // Used to support layer events without too much implementation work
  private PropertyChangeSupport fPropertyChangeSupport;

  /**
   * Creates a new layer manager that uses {@code aDataSetManager} to retrieve
   * models.
   *
   * @param aDataSetManager A valid {@code DataSetManager}
   */
  LayerManager(DataSetManager aDataSetManager) {
    fLayerFactories = new ArrayList<ILspLayerFactory>();
    fLayerMap = new HashMap<String, WeakHashMap<ILspView, List<ILspLayer>>>();
    fBaseLayerIDs = new ArrayList<String>();
    fBaseLayers = new HashSet<ILspLayer>();
    fLayerElements = new ArrayList<Element>();
    fDataSetManager = aDataSetManager;
    fPropertyChangeSupport = new PropertyChangeSupport(this);
  }

  /**
   * Disposes all loaded layers and layer factories.
   */
  public void disposeAll() {
    fLayerFactories.clear();
    fLayerMap.clear();
    fBaseLayerIDs.clear();
    fBaseLayers.clear();
    fLayerElements.clear();
  }

  /**
   * Registers the given layer factory with the layer manager.
   *
   * @param aLayerFactory the layer factory to be registered
   */
  public void registerLayerFactory(ILspLayerFactory aLayerFactory) {
    fLayerFactories.add(aLayerFactory);
  }

  /**
   * Checks whether this manager contains a layer with the given id.
   *
   * @param aID the id of the layer to be checked
   *
   * @return <code>true</code> if a layer with given id was loaded, <code>false</code> otherwise
   */
  public boolean hasLayerWithID(String aID) {
    return fLayerMap.containsKey(aID);
  }

  /**
   * Returns the layers with given ID that are associated to the given view.
   *
   * @param aID   the id associated to the layers
   * @param aView the view that the layer belongs to or {@code null} to get the layers of all views
   *
   * @return a list of layers that are associated to the given id
   *
   * @throws IllegalArgumentException when there are no layers with given id
   */
  public List<ILspLayer> getLayersWithID(String aID, ILspView aView) {
    Map<ILspView, List<ILspLayer>> viewMap = fLayerMap.get(aID);
    if (viewMap == null) {
      throw new IllegalArgumentException("No layers found with given id: " + aID);
    }

    if (aView != null) {
      return viewMap.get(aView);
    } else {
      ArrayList<ILspLayer> result = new ArrayList<ILspLayer>();
      for (List<ILspLayer> viewLayers : viewMap.values()) {
        result.addAll(viewLayers);
      }
      return result;
    }

  }

  /**
   * Returns the ids of the layers that were loaded.
   *
   * @return the ids of the loaded layers
   */
  public List<String> getLayerIDs() {
    return new ArrayList<String>(fLayerMap.keySet());
  }

  /**
   * Returns whether the layer with given id is a base layer or not.
   *
   * @param aLayerID the id of the layer to be checked
   *
   * @return <code>true</code> if the layer with given id is a base layer, <code>false</code>
   *         otherwise
   */
  public boolean isBaseLayer(String aLayerID) {
    return fBaseLayerIDs.contains(aLayerID);
  }

  /**
   * Returns whether the layeris a base layer or not.
   *
   * @param aLayer the layer to be checked
   *
   * @return <code>true</code> if the layer is a base layer, <code>false</code>
   *         otherwise
   */
  public boolean isBaseLayer(ILspLayer aLayer) {
    return fBaseLayers.contains(aLayer);
  }

  /**
   * Creates instances for the loaded layers and adds them to the given view.
   *
   * @param aView the view to which the created layers will be added
   */
  public void addLayersToView(ILspView aView) throws DataSetException {
    for (Element element : fLayerElements) {
      Collection<ILspLayer> layers = createLayers(element, aView);
      for (ILspLayer layer : layers) {
        aView.addLayer(layer);
      }
    }
  }

  private Collection<ILspLayer> createLayers(Element aElement, ILspView aView) throws DataSetException {
    Collection<ILspLayer> layers = null;
    final String id = AliasResolver.resolve(aElement, "id");
    String model = AliasResolver.resolve(aElement, "model");
    String label = AliasResolver.resolve(aElement, "label"); // optional
    String factory = AliasResolver.resolve(aElement, "factory"); // optional
    String baseStr = AliasResolver.resolve(aElement, "base"); // optional
    boolean optional = Boolean.parseBoolean(aElement.getAttributeValue("optional", "false")); // optional
    final LogUtil.LogLevel errLogLevel = optional ? LogUtil.LogLevel.DEBUG : LogUtil.LogLevel.ERROR;
    final LogUtil.LogLevel warnLogLevel = optional ? LogUtil.LogLevel.DEBUG : LogUtil.LogLevel.WARN;

    // Determine whether or not the decoded layer will be a base layer
    boolean base = (baseStr == null ? false : Boolean.parseBoolean(baseStr));

    ILcdModel layerModel;
    try {
      layerModel = fDataSetManager.getModelWithID(model);
    } catch (IllegalArgumentException e) {
      layerModel = null;
    }
    if (layerModel == null) {
      LogUtil.log(errLogLevel, sLogger, "Model with id [" + model + "] not available. Could not add layer [" + id + "] to the view.");
      return Collections.emptyList();
    }
    // If no layer factory was defined, check the loaded factories and try to create layer
    if (factory == null) {
      Iterator<ILspLayerFactory> iter = fLayerFactories.iterator();
      while (layers == null && iter.hasNext()) {
        ILspLayerFactory lf = iter.next();
        boolean canCreate;
        try {
          canCreate = lf.canCreateLayers(layerModel);
        } catch (Throwable t) {
          LogUtil.log(LogUtil.LogLevel.DEBUG, sLogger, "Skipping layer factory [" + lf + "] for layer with id [" + id + "]", t);
          continue;
        }
        if (canCreate) {
          try {
            layers = lf.createLayers(layerModel);
          } catch (Throwable t) {
            LogUtil.log(errLogLevel, sLogger, "Could not create layer with id [" + id + "]", t);
            return Collections.emptyList();
          }
          if (layers != null) {
            putLayers(id, aView, layers, label, base);
          }
        }
      }

      // Check if layer was decoded
      if (layers == null) {
        LogUtil.log(warnLogLevel, sLogger, "No compatible layer factory found for layer with id [" + id + "]. " +
                                           "Not adding layer to view.");
        return Collections.emptyList();
      }
    } else {
      // A layer factory was defined, try to find it
      ILspLayerFactory lf = null;
      for (ILspLayerFactory layerFactory : fLayerFactories) {
        if (factory.equals(layerFactory.getClass().getName())) {
          lf = layerFactory;
          break;
        }
      }

      // Check if a layer factory was found and try to create layer
      if (lf != null) {
        boolean canCreate;
        try {
          canCreate = lf.canCreateLayers(layerModel);
        } catch (Throwable t) {
          LogUtil.log(errLogLevel, sLogger, "Could not create layer with id [" + id + "]", t);
          return Collections.emptyList();
        }
        if (canCreate) {
          try {
            layers = lf.createLayers(layerModel);
          } catch (Throwable t) {
            LogUtil.log(errLogLevel, sLogger, "Could not create layer with id [" + id + "]", t);
            return Collections.emptyList();
          }
          if (layers != null) {
            putLayers(id, aView, layers, label, base);
          } else {
            LogUtil.log(errLogLevel, sLogger, "Could not create layer with id [" + id + "]");
          }
        } else {
          LogUtil.log(warnLogLevel, sLogger,
                      "Layer factory [" + factory + "], specified for layer [" + id + "], is not compatible with model [" + model + "]. " +
                      "Not adding layer to view."
          );
          return Collections.emptyList();
        }
      } else {
        LogUtil.log(warnLogLevel, sLogger,
                    "Layer factory [" + factory + "], specified for layer [" + id + "], not found. " +
                    "Not adding layer to view."
        );
        return Collections.emptyList();
      }
    }

    return layers;
  }

  private void putLayers(String aId, ILspView aView, Collection<ILspLayer> aLayers, String aLabel, boolean aBase) {
    putLayers(aId, aView, aLayers);
    for (ILspLayer layer : aLayers) {
      if (aLabel != null) {
        layer.setLabel(aLabel);
      }
      if (aBase) {
        fBaseLayerIDs.add(aId);
        fBaseLayers.add(layer);
        layer.setVisible(true);
      } else {
        layer.setVisible(false);
      }
    }
  }

  /**
   * Registers the given layers with the layer manager.
   *
   * @param aID     the id with which the given collection of layers is to be associated
   * @param aView   the view with which the given layers are to be associated
   * @param aLayers the layers to be registered
   */
  public void putLayers(String aID, ILspView aView, Collection<ILspLayer> aLayers) {
    WeakHashMap<ILspView, List<ILspLayer>> viewMap = fLayerMap.get(aID);
    if (viewMap == null) {
      viewMap = new WeakHashMap<ILspView, List<ILspLayer>>();
      fLayerMap.put(aID, viewMap);
    }

    List<ILspLayer> viewLayers = viewMap.get(aView);
    if (viewLayers == null) {
      viewLayers = new ArrayList<ILspLayer>();
      viewMap.put(aView, viewLayers);
    }
    viewLayers.addAll(aLayers);

    fPropertyChangeSupport.firePropertyChange("layers", null, aLayers);
  }

  /**
   * Loads the set of layers defined by the given XML hierarchy.
   *
   * @param aRoot      root element of the given XML hierarchy
   * @param aNamespace namespace in which the XML hierarchy is defined
   */
  public void loadLayerSet(Element aRoot, Namespace aNamespace) throws DataSetException {
    disposeAll();
    try {
      loadLayerFactories(aRoot, aNamespace);
      fLayerElements = aRoot.getChildren("Layer", aNamespace);
    } catch (Exception e) {
      throw new DataSetException(e.getMessage(), e);
    }
  }

  private void loadLayerFactories(Element aRoot, Namespace aNs) throws DataSetException {
    List<Element> factoryElements = aRoot.getChildren("LayerFactory", aNs);
    StringBuilder builder = new StringBuilder();
    String className = new String();

    for (Element factoryElement : factoryElements) {
      Class<?> factoryClass;
      try {
        className = AliasResolver.resolve(factoryElement, "class");
        factoryClass = Class.forName(className);
        ILspLayerFactory factory;
        if (ILspLayerFactory.class.isAssignableFrom(factoryClass)) {
          factory = (ILspLayerFactory) factoryClass.newInstance();
        } else {
          throw new DataSetException("Invalid layer factory class [" + className + "], layer factory must be a subclass of ILspLayerFactory");
        }

        // Extract properties and configure factory
        Properties props = new Properties();
        List<Element> propElements = factoryElement.getChildren("Property", aNs);
        for (Element propElement : propElements) {
          String key = AliasResolver.resolve(propElement, "key");
          String value = AliasResolver.resolve(propElement, "value");
          props.put(key, value);
        }

        if (factory instanceof AbstractLayerFactory) {
          ((AbstractLayerFactory) factory).configure(props);
        }

        registerLayerFactory(factory);
      } catch (Throwable t) {
        if (sLogger.isDebugEnabled()) {
          sLogger.debug("Could not load layer factory " + className, t);
        }
        builder.append("Error while creating layer factory [")
               .append(className)
               .append("], reason: ");

        if (t instanceof NoClassDefFoundError) {
          builder.append("missing dependencies, did you install all required components?");
        } else {
          builder.append("\n").append(t.toString());
        }

        builder.append("\n");
      }
    }

    String logMessage = builder.toString();
    if (!logMessage.isEmpty()) {
      sLogger.warn("Not all layer factories were loaded successfully:\n" + logMessage);
    }
  }

  /**
   * Adds a listener that receives events when layers are added or removed.
   *
   * @param aLayeredListener The listener that will receive the events.
   */
  public void addLayeredListener(PropertyChangeListener aLayeredListener) {
    fPropertyChangeSupport.addPropertyChangeListener(aLayeredListener);
  }

}
