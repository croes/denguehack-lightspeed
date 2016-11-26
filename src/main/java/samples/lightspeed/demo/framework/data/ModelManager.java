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

import java.io.FileNotFoundException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.jdom.Element;
import org.jdom.Namespace;

import com.luciad.model.ILcdModel;
import com.luciad.util.logging.ILcdLogger;
import com.luciad.util.logging.TLcdLoggerFactory;

import samples.lightspeed.demo.framework.application.Framework;
import samples.lightspeed.demo.framework.exception.DataSetException;
import samples.lightspeed.demo.framework.util.LogUtil;

/**
 * The model manager is responsible for creating and managing all ILcdModels that are used in the
 * application.
 */
class ModelManager {

  private static final ILcdLogger sLogger = TLcdLoggerFactory.getLogger(ModelManager.class);

  // Mapping from model types to model factories
  private HashMap<String, AbstractModelFactory> fModelFactoryMap;

  // Mapping from model ids to models
  private HashMap<String, Future<ILcdModel>> fModelMap;

  // Used to schedule threads for loading models
  private ExecutorService fExecutorService;

  ModelManager() {
    fModelFactoryMap = new HashMap<String, AbstractModelFactory>();
    fModelMap = new HashMap<String, Future<ILcdModel>>();
    fExecutorService = Executors.newSingleThreadExecutor();
  }

  /**
   * Disposes all registered model factories and loaded models.
   */
  public void disposeAll() {
    fModelFactoryMap.clear();
    Collection<Future<ILcdModel>> models = fModelMap.values();
    for (Future<ILcdModel> model : models) {
      try {
        model.get().dispose();
      } catch (InterruptedException e) {
        sLogger.warn(e.getMessage(), e);
      } catch (ExecutionException e) {
        sLogger.warn(e.getMessage(), e);
      }
    }
    fModelMap.clear();
  }

  /**
   * In case of concurrent model loading, this method waits until
   * all models are loaded.
   */
  public void ensureModelsAreLoaded() {
    for (Future<ILcdModel> future : fModelMap.values()) {
      try {
        future.get();
      } catch (InterruptedException ignored) {
      } catch (ExecutionException ignored) {
      }
    }
  }

  /**
   * Registers the given model factory with the model manager. If a model factory with the same model
   * type was present, the given factory will replace the old one.
   *
   * @param aModelFactory the model factory to be registered
   */
  public void registerModelFactory(AbstractModelFactory aModelFactory) {
    fModelFactoryMap.put(aModelFactory.getType(), aModelFactory);
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
    return fModelFactoryMap.get(aType);
  }

  /**
   * Returns the model with given ID.
   *
   * @param aID the id associated to the model
   *
   * @return the model associated to the given id
   *
   * @throws IllegalArgumentException when there is no model with given id
   */
  public ILcdModel getModelWithID(String aID) {
    Future<ILcdModel> result = fModelMap.get(aID);
    if (result == null) {
      throw new IllegalArgumentException("Could not retrieve model with ID [" + aID + "], model with given ID does not exist!");
    }
    try {
      return result.get();
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    } catch (ExecutionException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Loads the modelset defined by the given XML hierarchy.
   *
   * @param aRoot      root element of the given XML hierarchy
   * @param aNamespace namespace in which the XML hierarchy is defined
   */
  public void loadModelSet(Element aRoot, Namespace aNamespace) throws DataSetException {
    disposeAll();
    try {
      loadModelFactories(aRoot, aNamespace);
      loadModels(aRoot, aNamespace);
    } catch (Exception e) {
      throw new DataSetException(e.getMessage(), e);
    }
  }

  private void loadModelFactories(Element aRoot, Namespace aNs) throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException, DataSetException {
    StringBuilder builder = new StringBuilder();
    List<Element> factoryElements = aRoot.getChildren("ModelFactory", aNs);
    for (Element fel : factoryElements) {
      String type = AliasResolver.resolve(fel, "type");
      String className = AliasResolver.resolve(fel, "class");
      try {
        // Instantiate factory
        Class<?> factoryClass = Class.forName(className);
        AbstractModelFactory factory;
        if (AbstractModelFactory.class.isAssignableFrom(factoryClass)) {
          Constructor<?> c = factoryClass.getConstructor(String.class);
          factory = (AbstractModelFactory) c.newInstance(type);
        } else {
          throw new DataSetException("Invalid model factory class [" + className + "], model factory must be a subclass of AbstractModelFactory");
        }

        // Extract properties and configure factory
        Properties props = new Properties();
        List<Element> propElements = fel.getChildren("Property", aNs);
        for (Element prop : propElements) {
          String key = AliasResolver.resolve(prop, "key");
          String value = AliasResolver.resolve(prop, "value");
          props.put(key, value);
        }
        factory.configure(props);

        // Register factory
        registerModelFactory(factory);
      } catch (Throwable t) {
        Throwable actualT = t instanceof InvocationTargetException ? t.getCause() : t;
        if (sLogger.isDebugEnabled()) {
          sLogger.debug("Could not load model factory for type " + type + " (" + className + ")", actualT);
        }
        builder.append("Error while creating model factory [").
            append(type).append(" (").append(className).append(")").
                   append("], reason: ");
        if (actualT instanceof NoClassDefFoundError) {
          builder.append("missing dependencies, did you install all required components?");
        } else {
          builder.append("\n").append(actualT.toString());
        }
        builder.append("\n");
      }
    }

    String logMessage = builder.toString();
    if (!logMessage.isEmpty()) {
      sLogger.warn("Not all model factories were loaded successfully:\n" + logMessage);
    }
  }

  private void loadModels(Element aRoot, Namespace aNs) throws DataSetException {
    List<Element> modelElements = aRoot.getChildren("Model", aNs);
    for (Element mel : modelElements) {
      String type = AliasResolver.resolve(mel, "type");
      String id = AliasResolver.resolve(mel, "id");
      String source = AliasResolver.resolve(mel, "source");
      boolean optional = Boolean.parseBoolean(mel.getAttributeValue("optional", "false"));
      final LogUtil.LogLevel logLevel = optional ? LogUtil.LogLevel.DEBUG : LogUtil.LogLevel.ERROR;

      if (sLogger.isTraceEnabled()) {
        sLogger.trace("Loading model [" + id + "]: type = [" + type + "], source = [" + source + "]");
      }

      final AbstractModelFactory factory = fModelFactoryMap.get(type);
      if (factory == null) {
        LogUtil.log(logLevel, sLogger, "No model factory found for model of type [" + type + "]");
      } else {
        final String srcPath = Framework.getInstance().getDataPath(source);
        final Future<ILcdModel> future = fExecutorService.submit(new Callable<ILcdModel>() {

          @Override
          public ILcdModel call() throws Exception {
            try {
              ILcdModel model = factory.createModel(srcPath);
              if (model == null) {
                LogUtil.log(logLevel, sLogger, "Model factory " + factory + " refused to create a model for " + srcPath);
              }
              return model;
            } catch (FileNotFoundException e) {
              LogUtil.log(logLevel, sLogger, "Could not find the file with path: '" + srcPath + "'.\n" + e.getLocalizedMessage());
              return null;
            } catch (Throwable e) {
              LogUtil.log(logLevel, sLogger, "Exception while loading model: " + srcPath, e);
              return null;
            }
          }
        });
        fModelMap.put(id, future);
      }
    }
  }
}
