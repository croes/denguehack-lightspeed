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
package samples.wms.server.updater;

import java.io.File;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.luciad.format.raster.ILcdMultilevelRaster;
import com.luciad.format.raster.ILcdRaster;
import com.luciad.format.raster.ILcdTile;
import com.luciad.format.raster.TLcdMultilevelRasterModelDescriptor;
import com.luciad.format.raster.TLcdRasterModelDescriptor;
import com.luciad.format.raster.TLcdRenderedImageTile;
import com.luciad.model.ILcd2DBoundsIndexedModel;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelDecoder;
import com.luciad.model.TLcd2DBoundsIndexedModel;
import com.luciad.model.TLcdSoft2DBoundsIndexedModel;
import com.luciad.ogc.common.ALcdRequestContext;
import com.luciad.util.logging.ILcdLogger;
import com.luciad.util.logging.TLcdLoggerFactory;
import com.luciad.wms.server.ILcdModelDecoderFactory;
import com.luciad.wms.server.ILcdModelProvider;

/**
 * Implementation of <code>ILcdModelProvider</code> that uses
 * a Least Recently Used (LRU) caching mechanism for decoded models.
 */
public class DynamicModelProvider implements ILcdModelProvider, ILcdModelDecoderFactory, DirectoryMonitorListener {

  private static ILcdLogger sLogger = TLcdLoggerFactory.getLogger(DynamicModelProvider.class.getName());

  // Size of the LRU cache.
  private static final int CACHE_LIMIT = 50;

  private List<ILcdModelDecoderFactory> fModelDecoders = new ArrayList<ILcdModelDecoderFactory>();
  private LRUCache fCache = new LRUCache(CACHE_LIMIT);

  /**
   * Creates a new instance with the specified model decoder factories.
   *
   * @param aModelDecoderFactories The model decoder factories to be used by this model provider.
   */
  public DynamicModelProvider(ILcdModelDecoderFactory[] aModelDecoderFactories) {
    fModelDecoders.addAll(Arrays.asList(aModelDecoderFactories));
  }

  public ILcdModel getModel(String aSource, ALcdRequestContext aRequestContext) throws IOException {
    ILcdModel model = retrieveCachedModel(aSource);
    if (model == null) {
      ILcdModelDecoder decoder = createModelDecoder(aSource);

      if (decoder == null) {
        throw new IOException("Unsupported file format: [" + aSource + "] (no decoder found).");
      }

      model = decodeData(aSource, decoder);
    }
    return model;
  }

  public ILcdModelDecoder createModelDecoder(String aSource) {
    int size = fModelDecoders.size();
    for (int i = 0; i < size; i++) {
      ILcdModelDecoderFactory modelDecoderFactory = fModelDecoders.get(i);
      try {
        ILcdModelDecoder modelDecoder = modelDecoderFactory.createModelDecoder(aSource);
        if (modelDecoder != null) {
          return modelDecoder;
        }
      } catch (IllegalArgumentException e) {
        sLogger.error(e.getMessage(), e);
      }
    }
    return null;
  }

  public void event(DirectoryMonitorEvent aEvent) {
    // Update the cache.
    for (int i = 0; i < aEvent.getTypes().length; i++) {
      if (aEvent.getTypes()[i] == DirectoryMonitorEvent.FILES_REMOVED ||
          aEvent.getTypes()[i] == DirectoryMonitorEvent.FILES_UPDATED) {
        for (int j = 0; j < aEvent.getFiles()[i].size(); j++) {
          File file = (File) aEvent.getFiles()[i].get(j);
          fCache.remove(file.getAbsolutePath());
        }
      }
    }
  }

  /**
   * Returns the cached model specified by the given key, if it exists.
   * Otherwise, null is returned.
   *
   * @param aKey The key whose associated model is to be returned.
   * @return the cached model specified by the given key if it exists, or null otherwise.
   */
  private ILcdModel retrieveCachedModel(String aKey) {
    SoftReference softReference = fCache.get(aKey);
    if (softReference == null) {
      return null;
    } else {
      return (ILcdModel) softReference.get();
    }
  }

  /**
   * Decodes a data source with a specified model decoder.
   *
   * @param aSourceName   The data source.
   * @param aModelDecoder The model decoder to decode the data.
   * @return the decoded model.
   * @throws IOException when an I/O error occurs
   */
  private synchronized ILcdModel decodeData(String aSourceName, ILcdModelDecoder aModelDecoder) throws IOException {
    ILcdModel model = aModelDecoder.decode(aSourceName);
    if (!(isSoftModel(model) || isRasterModel(model)) &&
        model instanceof TLcd2DBoundsIndexedModel) {
      model = new TLcdSoft2DBoundsIndexedModel(aSourceName, aModelDecoder, (ILcd2DBoundsIndexedModel) model);
    }
    fCache.put(aSourceName, new SoftReference<ILcdModel>(model));
    return model;
  }

  private static boolean isSoftModel(ILcdModel aModel) {
    return aModel instanceof TLcdSoft2DBoundsIndexedModel;
  }

  private static boolean isRasterModel(ILcdModel aModel) {
    return (aModel.getModelDescriptor() instanceof TLcdRasterModelDescriptor) ||
           (aModel.getModelDescriptor() instanceof TLcdMultilevelRasterModelDescriptor);
  }

  /**
   * Implementation of a Least Recently Used (LRU) cache.
   */
  private static class LRUCache {

    private static final float LOAD_FACTOR = 0.75f;
    private Map<String, SoftReference<ILcdModel>> fMap;

    /**
     * Creates a new LRU cache.
     *
     * @param aCacheSize the maximum number of entries that will be kept in this cache.
     */
    public LRUCache(final int aCacheSize) {
      int hashTableCapacity = (int) Math.ceil(aCacheSize / LOAD_FACTOR) + 1;
      fMap = new LinkedHashMap<String, SoftReference<ILcdModel>>(hashTableCapacity, LOAD_FACTOR, true) {
        protected boolean removeEldestEntry(Map.Entry<String, SoftReference<ILcdModel>> eldest) {
          // Remove eldest entry.
          if(size() > aCacheSize) {
            // Clean up eldest entry, if it is a raster model.
            // This makes sure that all files used by individual raster tiles are unlocked.
            SoftReference<ILcdModel> softReference = eldest.getValue();
            if (softReference != null) {
              ILcdModel model = softReference.get();

              if (model != null && isRasterModel(model)) {
                Enumeration elements = model.elements();
                while (elements.hasMoreElements()) {
                  Object element = elements.nextElement();
                  if (element instanceof ILcdMultilevelRaster) {
                    disposeMultilevelRaster((ILcdMultilevelRaster) element);
                  } else if (element instanceof ILcdRaster) {
                    disposeRaster((ILcdRaster) element);
                  }
                }
              }
            }

            return true;
          }
          else {
             return false;
          }
        }

        private void disposeMultilevelRaster(ILcdMultilevelRaster aMultilevelRaster) {
          for (int i = 0; i < aMultilevelRaster.getRasterCount(); i++) {
            disposeRaster(aMultilevelRaster.getRaster(i));
          }
        }

        private void disposeRaster(ILcdRaster aRaster) {
          for (int row = 0; row < aRaster.getTileRowCount(); row++) {
            for (int col = 0; col < aRaster.getTileColumnCount(); col++) {
              ILcdTile tile = aRaster.retrieveTile(row, col);
              disposeTile(tile);
            }
          }
        }

        private void disposeTile(ILcdTile aTile) {
          if (aTile instanceof TLcdRenderedImageTile) {
            TLcdRenderedImageTile renderedImageTile = (TLcdRenderedImageTile) aTile;
            renderedImageTile.dispose();
          }
        }
      };
    }

    /**
     * Retrieves an entry from the cache.<br>
     * The retrieved entry becomes the MRU (most recently used) entry.
     *
     * @param aKey the key whose associated value is to be returned.
     * @return the value associated to this key, or null if no value with this key exists in the cache.
     */
    public synchronized SoftReference get(String aKey) {
      return fMap.get(aKey);
    }

    /**
     * Adds an entry to this cache.
     * If the cache is full, the LRU (least recently used) entry is dropped.
     *
     * @param aKey   the key with which the specified value is to be associated.
     * @param aValue a value to be associated with the specified key.
     */
    public synchronized void put(String aKey, SoftReference<ILcdModel> aValue) {
      fMap.put(aKey, aValue);
    }

    /**
     * Removes an entry from the cache.
     *
     * @param aKey the key with which the specified value is to be associated.
     */
    public synchronized void remove(String aKey) {
      fMap.remove(aKey);
    }

    /**
     * Clears the cache.
     */
    public synchronized void clear() {
      fMap.clear();
    }
  }
}
