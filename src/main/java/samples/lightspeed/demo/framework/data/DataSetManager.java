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
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;

import com.luciad.io.TLcdInputStreamFactory;
import com.luciad.model.ILcdModel;
import com.luciad.util.logging.ILcdLogger;
import com.luciad.util.logging.TLcdLoggerFactory;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.layer.ILspLayer;

import samples.lightspeed.demo.framework.data.themes.AbstractTheme;
import samples.lightspeed.demo.framework.exception.DataSetException;

/**
 * Loads and manages data sets.
 * <p/>
 * For loading datasets, the DataSetManager uses the following directory structure:
 * <p/>
 * <b>Data-root directories</b>
 * <ul>
 * <li> <b>[data-set-name].data</b> : directory defining a dataset
 * <ul>
 * <li> <b>index.xml</b> : specifies models, layers, themes, model factories and layer factories
 * that
 * are to be loaded (file must adhere to the demo xml schema)
 * <li> [data] :  any data that is required to load the models, layers or themes that are defined
 * in
 * index.xml
 * </ul>
 * </ul>
 */
public class DataSetManager {

  private static ILcdLogger sLogger = TLcdLoggerFactory.getLogger(DataSetManager.class);

  // The data root directories
  private String[] fDataRoots;

  // Factory used for creating input streams when data needs to be loaded
  private TLcdInputStreamFactory fInputStreamFactory;

  // Manager for models
  private ModelManager fModelManager;

  // Manager for layers
  private LayerManager fLayerManager;

  // Manager for themes
  private ThemeManager fThemeManager;

  public DataSetManager() {
    fInputStreamFactory = new TLcdInputStreamFactory();
    fModelManager = new ModelManager();
    fLayerManager = new LayerManager(this);
    fThemeManager = new ThemeManager();
  }

  /**
   * Returns the directories where the dataset manager will look for datasets and data.
   *
   * @return the directories
   */
  public String[] getDataRoots() {
    return fDataRoots;
  }

  /**
   * Sets the root directories of the dataset manager to the given directories.
   *
   * @param aDataRoot the new data root directory
   */
  public void setDataRoots(String[] aDataRoot) {
    fDataRoots = aDataRoot;
  }

  /**
   * Returns the datasets that are available under the current data-root directory. Note that this
   * method only returns dataset directories that end with the ".data" extension.
   *
   * @return the list of datasets that was found under the data-root directory
   */
  public String[] getAvailableDataSets() {
    List<String> result = new ArrayList<>();
    for (int i = 0; i < fDataRoots.length; i++) {
      String[] list = IOUtil.getFile(fDataRoots[i]).list(new FilenameFilter() {
        @Override
        public boolean accept(File dir, String name) {
          return name.endsWith(".data");
        }
      });
      result.addAll(Arrays.asList(list));
    }

    // Remove extensions
    for (int i = 0; i < result.size(); i++) {
      result.set(i, result.get(i).substring(0, result.get(i).indexOf(".data")));
    }

    return result.toArray(new String[result.size()]);
  }

  /**
   * Returns the model with given ID.
   *
   * @param aID the id associated to the model
   *
   * @return the model associated to the id
   *
   * @throws IllegalArgumentException when no model with given id exists
   */
  public ILcdModel getModelWithID(String aID) {
    return fModelManager.getModelWithID(aID);
  }

  /**
   * In case of concurrent model loading, this method waits until
   * all models are loaded.
   */
  public void ensureModelsAreLoaded() {
    fModelManager.ensureModelsAreLoaded();
  }

  /**
   * Returns a registered model factory from this model manager, or
   * null if no model factory with the same model type was present.
   *
   * @param aType A model type
   *
   * @return a model factory, or null if no model factory was registered for the given type
   */
  public AbstractModelFactory getRegisteredModelFactory(String aType) {
    return fModelManager.getRegisteredModelFactory(aType);
  }

  /**
   * Checks whether a layer with with given id was loaded.
   *
   * @param aID the id of the layer to be checked
   *
   * @return <code>true</code> if a layer with given id was loaded, <code>false</code> otherwise
   */
  public boolean hasLayerWithID(String aID) {
    return fLayerManager.hasLayerWithID(aID);
  }

  /**
   * Returns the layers with given ID.
   *
   * @param aID   the id associated to the layers
   * @param aView the view that the layer belongs to or {@code null} to get the layers of all views
   *
   * @return a list of layers that are associated to the given id
   *
   * @throws IllegalArgumentException when there are no layers with given id
   */
  public List<ILspLayer> getLayersWithID(String aID, ILspView aView) {
    return fLayerManager.getLayersWithID(aID, aView);
  }

  /**
   * Returns the layers with given ID.
   *
   * @param aID the id associated to the layers
   *
   * @return a list of layers that are associated to the given id
   *
   * @throws IllegalArgumentException when there are no layers with given id
   */
  public List<ILspLayer> getLayersWithID(String aID) {
    return fLayerManager.getLayersWithID(aID, null);
  }

  /**
   * Creates instances for the loaded layers and adds them to the given view.
   *
   * @param aView the view to which the created layers will be added
   */
  public void addLayersToView(ILspView aView) throws DataSetException {
    fLayerManager.addLayersToView(aView);
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
    return fLayerManager.isBaseLayer(aLayerID);
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
    return fLayerManager.isBaseLayer(aLayer);
  }

  /**
   * Returns the ids of the layers that were loaded.
   *
   * @return the ids of the layers that were loaded
   */
  public List<String> getLayerIDs() {
    return fLayerManager.getLayerIDs();
  }

  /**
   * Returns the loaded themes.
   *
   * @return the themes that were loaded
   */
  public AbstractTheme[] getThemes() {
    return fThemeManager.getThemes();
  }

  /**
   * Returns the loaded theme of the specified class
   *
   * @param aThemeClass The class of the theme
   * @param <T>         The type of theme
   *
   * @return the loaded theme of the specified class, or {@code null} when no such theme is loaded
   */
  public <T extends AbstractTheme> T getThemeByClass(Class<T> aThemeClass) {
    return fThemeManager.getThemeByClass(aThemeClass);
  }

  /**
   * Loads the dataset with given name.
   * <p/>
   * Note that a dataset can only be properly loaded when there is a "name.data" directory in the
   * data-root directory that contains at least the index.xml file.
   * <p/>
   * Aside from the index files, the dataset directory will most likely also contain data
   * files/directories
   * that contain the actual data to be loaded.
   *
   * @param aName the name of the dataset to be loaded
   *
   * @throws IllegalArgumentException when an error occurred during the loading of the dataset with
   *                                  given name
   */
  public void loadDataSet(String aName) throws DataSetException {
    try {
      String sourceName = getDataSetPath(aName, "index.xml");
      InputStream index = fInputStreamFactory.createInputStream(sourceName);

      try {
        Document doc = new SAXBuilder().build(index);
        Element root = doc.getRootElement();
        Namespace ns = Namespace.getNamespace("http://www.luciad.com/demo");

        // We must load all aliases before loading anything else,
        // because the other loaders might use the aliases.
        AliasResolver.load(root, ns);

        fModelManager.loadModelSet(root, ns);
        fLayerManager.loadLayerSet(root, ns);
        fThemeManager.loadThemeSet(root, ns);
      } catch (Exception e) {
        if (e instanceof DataSetException) {
          throw (DataSetException) e;
        } else {
          throw new DataSetException(e.getMessage(), e);
        }
      }
    } catch (IOException e) {
      throw new DataSetException(e);
    }
  }

  /**
   * Initializes the loaded themes for the given list of views.
   *
   * @param aViews the list of views that were created in the demo
   */
  public void initializeThemes(List<ILspView> aViews) {
    fThemeManager.initializeThemes(aViews);
  }

  /**
   * Convenience method that returns the correct path to the dataset file with given file name.
   *
   * @param aFileName    the name of the file for which the dataset path is to be returned,
   *                     the file is assumed to be relative ot the dataset directory
   *
   * @return the correct path to the dataset file with given file name
   */
  public String getDataPath(String aFileName) {
    if (aFileName == null) {
      return null;
    }
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < fDataRoots.length; i++) {
      builder.append(fDataRoots[i] + ";");
      File f = IOUtil.getFile(fDataRoots[i], aFileName);
      if (f.exists()) {
        return f.getAbsolutePath();
      }
    }

    sLogger.warn("Could not find '" + aFileName + "' in defined paths: " + builder.toString() + "\n" +
                 "Extra paths can be configured in the 'demo.internal.properties' file via the 'data.paths' property\n" +
                 "or by using the system property 'luciad.demo.dataPaths'. The system property can be defined by \n"
                 + "adding it as a value to the JVM_ARGS environment variable.");
    return aFileName;
  }

  /**
   * Convenience method that returns the correct path to the dataset file with given file name.
   *
   * @param aDataSetName the name of the dataset to which the file with given name belongs
   * @param aFileName    the name of the file for which the dataset path is to be returned,
   *                     the file is assumed to be relative ot the dataset directory
   *
   * @return the correct path to the dataset file with given file name
   */
  public String getDataSetPath(String aDataSetName, String aFileName) {
    return getDataPath(aDataSetName + "/" + aFileName);
  }

  /**
   * Adds a listener that receives events when layers are added or removed.
   *
   * @param aLayeredListener The listener that will receive the events.
   */
  public void addLayeredListener(PropertyChangeListener aLayeredListener) {
    fLayerManager.addLayeredListener(aLayeredListener);
  }

  /**
   * Registers layers to a data set.
   *
   * @param aID     the data set id
   * @param aView   the view or {@code null} if the layers do not belong to a specific view
   * @param aLayers the layers
   */
  public void registerLayers(String aID, ILspView aView, Collection<ILspLayer> aLayers) {
    fLayerManager.putLayers(aID, aView, aLayers);
  }

}
