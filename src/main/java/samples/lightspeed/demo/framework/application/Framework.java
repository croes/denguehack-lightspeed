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
package samples.lightspeed.demo.framework.application;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.SwingUtilities;

import com.luciad.gui.TLcdUndoManager;
import com.luciad.io.TLcdInputStreamFactory;
import com.luciad.model.ILcdModel;
import com.luciad.util.logging.ILcdLogger;
import com.luciad.util.logging.TLcdLoggerFactory;
import com.luciad.view.lightspeed.ILspAWTView;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.ILspPaintableLayer;
import com.luciad.view.lightspeed.layer.TLspPaintRepresentation;

import samples.lightspeed.demo.framework.data.AbstractModelFactory;
import samples.lightspeed.demo.framework.data.DataSetManager;
import samples.lightspeed.demo.framework.data.themes.AbstractTheme;
import samples.lightspeed.demo.framework.data.themes.ThemeAnimation;
import samples.lightspeed.demo.framework.exception.DataSetException;
import samples.lightspeed.demo.framework.gui.menu.ActiveThemeMenuController;
import samples.lightspeed.demo.framework.gui.menu.InfoPanel;
import samples.lightspeed.demo.simulation.SimulationSupport;

/**
 * Framework that provides functionality to perform application-related tasks.
 * This class is meant as a facade interface between the user interface and the
 * underlying application data.
 */
public class Framework {

  private static final String SOURCE_PATH_PROPERTY = "luciad.demo.dataPaths";
  private static ILcdLogger sLogger = TLcdLoggerFactory.getLogger(Framework.class);

  // Singleton instance of this class (there can only be one application object)
  private static Framework sInstance;

  // Dataset attributes
  private DataSetManager fDataSetManager;

  // Application attributes
  private FrameworkContext fFrameworkContext;
  private Properties fProperties;
  private AbstractTheme fActiveTheme;

  // Utility attributes
  private PropertyChangeSupport fPropertyChangeSupport;

  private List<ActiveThemeMenuController> fActiveThemeMenuControllers;

  // Map containing all objects that are shared throughout the application
  private HashMap<String, Object> fSharedValues;

  // Keeps track of undoable actions
  private TLcdUndoManager fUndoManager;

  // Shows info information for a theme
  private InfoPanel fInfoPanel;

  /**
   * Returns the singleton instance of this class.
   *
   * @return the singleton instance of this class
   */
  public static Framework getInstance() {
    if (sInstance == null) {
      sInstance = new Framework();
    }
    return sInstance;
  }

  /**
   * Destroys the framework instance if it exists.
   */
  public static void destroyInstance() {
    if (sInstance != null) {
      sInstance.destroy();
      sInstance = null;
    }
  }

  private Framework() {
    fDataSetManager = new DataSetManager();
    fProperties = new Properties();
    fPropertyChangeSupport = new PropertyChangeSupport(this);
    fActiveThemeMenuControllers = new ArrayList<ActiveThemeMenuController>();
    fSharedValues = new HashMap<String, Object>();
    fUndoManager = new TLcdUndoManager();
  }

  /**
   * Stores the given object as a shared value.
   *
   * @param aKey    the key to reference the object
   * @param aObject the shared value
   */
  public void storeSharedValue(String aKey, Object aObject) {
    fSharedValues.put(aKey, aObject);
  }

  /**
   * Returns the shared value associated with the given key.
   *
   * @param aKey the key for which the shared value is to be retrieved
   *
   * @return the shared value associated to the key, <code>null</code> otherwise (NOTE:
   *         <code>null</code>
   *         could also be associated to the key, i.e. when <code>null</code> is returned, this
   *         does not necessarily mean that the key-value pair with given key does not exist)
   */
  public Object getSharedValue(String aKey) {
    return fSharedValues.get(aKey);
  }

  public FrameworkContext getFrameworkContext() {
    return fFrameworkContext;
  }

  public void setFrameworkContext(FrameworkContext aFrameworkContext) {
    FrameworkContext oldValue = fFrameworkContext;
    fFrameworkContext = aFrameworkContext;
    fPropertyChangeSupport.firePropertyChange("applicationContext", oldValue, fFrameworkContext);
  }

  public TLcdUndoManager getUndoManager() {
    return fUndoManager;
  }

  public void addPropertyChangeListener(PropertyChangeListener listener) {
    fPropertyChangeSupport.addPropertyChangeListener(listener);
  }

  public void removePropertyChangeListener(PropertyChangeListener listener) {
    fPropertyChangeSupport.removePropertyChangeListener(listener);
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////
  // Dataset related methods

  public void loadDataSet(String aName) {
    try {
      fDataSetManager.loadDataSet(aName);
      // Fire property change events for the delegated stuff
      fPropertyChangeSupport.firePropertyChange("themes", null, getThemes());
    } catch (DataSetException e) {
      sLogger.error("Could not load dataset [" + aName + "]" +
                    (e.getMessage() != null ? ", reason: " + e.getMessage() : ""), e);
      System.exit(-1);
    }
  }

  /**
   * Initializes the loaded themes for the given list of views.
   *
   * @param aViews the list of views that were created in the demo
   */
  public void initializeThemes(List<ILspView> aViews) {
    fDataSetManager.initializeThemes(aViews);
  }

  public List<String> getLayerIDs() {
    return fDataSetManager.getLayerIDs();
  }

  public void registerActiveThemeMenuController(ActiveThemeMenuController aActiveThemeMenuController) {
    fActiveThemeMenuControllers.add(aActiveThemeMenuController);
  }

  /**
   * Returns all the layers with the given ID.
   * <p/>
   * Note that there may be multiple layers with the same idea,
   * when they are in different views.
   *
   * @param aID   the idea for which the layer(s) should be retrieved
   * @param aView the view that the layer belongs to or {@code null} to get the layers of all views
   *
   * @return the layers with the given ID
   *
   * @throws IllegalArgumentException when there are no layers with the given ID
   */
  public List<ILspLayer> getLayersWithID(String aID, ILspView aView) {
    return fDataSetManager.getLayersWithID(aID, aView);
  }

  /**
   * Returns all the layers with the given ID.
   * <p/>
   * Note that there may be multiple layers with the same id,
   * when they are in different views.
   *
   * @param aID the idea for which the layer(s) should be retrieved
   *
   * @return the layers with the given ID
   *
   * @throws IllegalArgumentException when there are no layers with the given ID
   */
  public List<ILspLayer> getLayersWithID(String aID) {
    return fDataSetManager.getLayersWithID(aID, null);
  }

  public void addLayersToView(ILspView aView) {
    try {
      fDataSetManager.addLayersToView(aView);
    } catch (DataSetException e) {
      sLogger.error("Could not add layers to view" + (e.getMessage() != null ? ", reason: " + e
          .getMessage() : ""), e);
    }
  }

  /**
   * Registers layers to a data set.
   *
   * @param aID     the data set id
   * @param aView   the view or {@code null} if the layers do not belong to a specific view
   * @param aLayers the layers
   */
  public void registerLayers(String aID, ILspView aView, Collection<ILspLayer> aLayers) {
    fDataSetManager.registerLayers(aID, aView, aLayers);
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////
  // Model related methods

  public String getDataPath(String aFileName) {
    return fDataSetManager.getDataPath(aFileName);
  }

  public String getDataSetPath(String aDataSetName, String aFileName) {
    return fDataSetManager.getDataSetPath(aDataSetName, aFileName);
  }

  public AbstractModelFactory getRegisteredModelFactory(String aType) {
    return fDataSetManager.getRegisteredModelFactory(aType);
  }

  public ILcdModel getModelWithID(String aID) {
    return fDataSetManager.getModelWithID(aID);
  }

  /**
   * In case of concurrent model loading, this method waits until
   * all models are loaded.
   */
  public void ensureModelsAreLoaded() {
    fDataSetManager.ensureModelsAreLoaded();
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////
  // Theme related methods

  public AbstractTheme[] getThemes() {
    return fDataSetManager.getThemes();
  }

  public <T extends AbstractTheme> T getThemeByClass(Class<T> aThemeClass) {
    return fDataSetManager.getThemeByClass(aThemeClass);
  }

  public AbstractTheme getActiveTheme() {
    return fActiveTheme;
  }

  public void setActiveTheme(AbstractTheme aActiveTheme) {
    // Do nothing when theme is already active.
    if (aActiveTheme == getActiveTheme()) {
      return;
    }
    if (aActiveTheme == null) {
      throw new NullPointerException("Can not set active theme to [null]!");
    }

    // Deactivate old theme
    AbstractTheme oldTheme = fActiveTheme;
    if (oldTheme != null) {
      if (fInfoPanel != null) {
        fInfoPanel.setVisible(false);
      }

      deactivateActiveTheme();
    }

    Set<ILspLayer> themeLayers = new HashSet<ILspLayer>();
    Set<ILspLayer> baseLayers = new HashSet<ILspLayer>();
    retrieveVisibleLayers(baseLayers, themeLayers);

    // Hide all theme layers
    for (ILspLayer layer : themeLayers) {
      // Leave performance layer on
      if (!"Performance".equals(layer.getLabel())) {
        layer.setVisible(false);
      }
    }

    // Hide labels of base layer
    for (ILspLayer layer : baseLayers) {
      layer.setVisible(TLspPaintRepresentation.LABEL, false);
    }

    // Activate new theme
    activateThemeAnimated(aActiveTheme, oldTheme, baseLayers);
  }

  private void removeInVisible(Collection<ILspLayer> aLayers) {
    Set<ILspLayer> invisible = new HashSet<ILspLayer>();
    for (ILspLayer layer : aLayers) {
      if (!layer.isVisible()) {
        invisible.add(layer);
      }
    }
    aLayers.removeAll(invisible);
  }

  private void removeUnlabeled(Collection<ILspLayer> aLayers) {
    Set<ILspLayer> unlabeled = new HashSet<ILspLayer>();
    for (ILspLayer layer : aLayers) {
      if (!(layer instanceof ILspPaintableLayer && ((ILspPaintableLayer) layer).isVisible(TLspPaintRepresentation.LABEL))) {
        unlabeled.add(layer);
      }
    }
    aLayers.removeAll(unlabeled);

  }

  /**
   * Retrieves two sets of visible layers, for regular and labeled base layers respectively.
   */
  private void retrieveVisibleLayers(Set<ILspLayer> aBaseLayers, Set<ILspLayer> aThemeLayers) {
    aBaseLayers.clear();
    aThemeLayers.clear();
    List<String> layerIDs = fDataSetManager.getLayerIDs();
    for (String id : layerIDs) {
      try {
        if (fDataSetManager.isBaseLayer(id)) {
          aBaseLayers.addAll(fDataSetManager.getLayersWithID(id));
        } else {
          aThemeLayers.addAll(fDataSetManager.getLayersWithID(id));
        }
      } catch (IllegalArgumentException ignore) {
      }
    }
    removeInVisible(aThemeLayers);
    removeInVisible(aBaseLayers);
    removeUnlabeled(aBaseLayers);
  }

  public boolean isBaseLayer(ILspLayer aLayer) {
    return fDataSetManager.isBaseLayer(aLayer);
  }

  private void showInfoPanel(AbstractTheme aActiveTheme) {
    if (!aActiveTheme.isShowInfoMessage() ||
        aActiveTheme.getInfoMessage() == null ||
        aActiveTheme.getInfoMessage().isEmpty()) {
      return;
    }
    if (fFrameworkContext != null) {
      List<ILspView> views = fFrameworkContext.getViews();

      if (views != null && !views.isEmpty()) {
        // We only show the info message on one view,
        // so the GUI does not become to cluttered
        ILspView view = views.get(0);

        if (view instanceof ILspAWTView) {
          final ILspAWTView awtView = (ILspAWTView) view;

          if (fInfoPanel == null) {
            fInfoPanel = new InfoPanel(awtView);
          }
          fInfoPanel.setInfoMessage(aActiveTheme.getInfoMessage());
        }
      }
    }
    aActiveTheme.setShowInfoMessage(false);
  }

  private void deactivateActiveTheme() {
    for (ActiveThemeMenuController activeThemeMenuController : fActiveThemeMenuControllers) {
      activeThemeMenuController.removeAllThemePanels();
    }
    fActiveTheme.deactivate();
    if (fActiveTheme.isSimulated()) {
      SimulationSupport.getInstance().stopSimulator();
    }
  }

  private void activateThemeAnimated(final AbstractTheme aNextTheme,
                                     final AbstractTheme aPreviousTheme,
                                     final Set<ILspLayer> aBaseLayers) {
    fActiveTheme = aNextTheme;
    if (fActiveTheme.getAnimation() == null && fFrameworkContext != null) {
      fActiveTheme.setAnimation(new ThemeAnimation(fActiveTheme, fFrameworkContext
          .getViews()));
    }
    if (fActiveTheme.getAnimation() != null && aPreviousTheme != null) {
      final ThemeAnimation animation = fActiveTheme.getAnimation();
      animation.addAnimationListener(
          new ThemeAnimation.AnimationListener() {
            @Override
            public void animationStarted() {
            }

            @Override
            public void animationStopped() {
              animation.removeAnimationListener(this);
              activateTheme(aNextTheme, aPreviousTheme, aBaseLayers);

            }
          }
      );
      fPropertyChangeSupport.firePropertyChange("activeTheme", aPreviousTheme, aNextTheme);
      animation.doAnimation();
    } else {
      fPropertyChangeSupport.firePropertyChange("activeTheme", aPreviousTheme, aNextTheme);
      activateTheme(aNextTheme, aPreviousTheme, aBaseLayers);
    }
  }

  private void activateTheme(final AbstractTheme aNextTheme, AbstractTheme aPreviousTheme, Set<ILspLayer> aBaseLayers) {
    if (aNextTheme != fActiveTheme) {
      return;
    }
    aNextTheme.activate();
    // Make base layer labels visible again
    for (ILspLayer layer : aBaseLayers) {
      layer.setVisible(TLspPaintRepresentation.LABEL, true);
    }

    // Make sure all side panels are shown.
    for (ActiveThemeMenuController activeThemeMenuController : fActiveThemeMenuControllers) {
      activeThemeMenuController.addThemePanels(aNextTheme);
    }

    // Wait some time before showing the info panel, to give the view some time to render everything.
    new Timer().schedule(new TimerTask() {
      @Override
      public void run() {
        SwingUtilities.invokeLater(new Runnable() {
          @Override
          public void run() {
            showInfoPanel(aNextTheme);
          }
        });

      }
    }, 500);
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////
  // Properties related methods

  /**
   * Returns the value of the application property with given name (or <code>null</code> if no
   * property with given name exists).
   *
   * @param aName the name of the property whose value is to be retrieved
   *
   * @return the property's value, <code>null</code> if the property does not exist
   */
  public String getProperty(String aName) {
    return fProperties.getProperty(aName);
  }

  /**
   * Returns the value of the application property with given name (or <code>aDefault</code> if no
   * property with given name exists).
   *
   * @param aName    the name of the property whose value is to be retrieved
   * @param aDefault the default value that will be returned if property does not exist
   *
   * @return the property's value, <code>aDefault</code> if the property does not exist
   */
  public String getProperty(String aName, String aDefault) {
    return fProperties.getProperty(aName, aDefault);
  }

  /**
   * Loads the properties of the given file as application properties.
   *
   * @param aFile the file containing the properties
   *
   * @throws IOException
   */
  public void loadProperties(String aFile) throws IOException {
    if (sLogger.isTraceEnabled()) {
      sLogger.trace("Loading properties from file: " + aFile);
    }

    // Load properties
    TLcdInputStreamFactory factory = new TLcdInputStreamFactory();
    InputStream in = factory.createInputStream(aFile);
    fProperties.load(in);
    in.close();

    // Update data root directory
    String dataRoots = getProperty("data.paths");
    String pathSystemProperty = System.getProperty(SOURCE_PATH_PROPERTY);
    if (pathSystemProperty != null) {
      dataRoots = dataRoots.concat(";" + pathSystemProperty);
    }

    if (dataRoots != null) {
      fDataSetManager.setDataRoots(dataRoots.split(";"));
    }
  }

  public void setProperty(String aKey, String aValue) {
    if (aValue == null) {
      fProperties.remove(aKey);
    } else {
      fProperties.setProperty(aKey, aValue);
    }
  }

  /**
   * Adds a listener that receives events when layers are added or removed.
   *
   * @param aLayeredListener The listener that will receive the events.
   */
  public void addLayerListener(PropertyChangeListener aLayeredListener) {
    fDataSetManager.addLayeredListener(aLayeredListener);
  }

  /**
   * Destroys the framework.
   */
  private void destroy() {
    for (AbstractTheme theme : getThemes()) {
      theme.destroy();
    }
  }
}
