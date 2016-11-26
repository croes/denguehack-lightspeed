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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.luciad.model.ILcdModel;
import com.luciad.util.logging.ILcdLogger;
import com.luciad.util.logging.TLcdLoggerFactory;
import com.luciad.wms.server.ILcdModelProvider;
import com.luciad.wms.server.ILcdWMSCapabilitiesUpdater;
import com.luciad.wms.server.TLcdOGCWMSCommandDispatcher;
import com.luciad.wms.server.model.ALcdWMSLayer;
import com.luciad.wms.server.model.TLcdWMSCapabilities;
import com.luciad.wms.server.model.TLcdWMSLayer;

import samples.wms.server.config.xml.WMSBoundsUtility;

/**
 * An implementation of ILcdWMSCapabilitiesUpdater that defines a time-based update strategy for
 * the available layers in the capabilities of the WMS server. Following a fixed time interval,
 * the updater monitors a directory '/Data/dynamic' (including subdirectories) in the configured
 * map data folder on any new, updated or removed files. If the server can decode a new or updated file,
 * it is added to the capabilities.
 */
public class CapabilitiesUpdater implements ILcdWMSCapabilitiesUpdater {

  private static ILcdLogger sLogger = TLcdLoggerFactory.getLogger(CapabilitiesUpdater.class.getName());

  // The monitored directory.
  private static final String DYNAMIC_DIRECTORY = "/Data/dynamic";

  // Time interval between different updates (in ms).
  private static final long UPDATE_INTERVAL = 10000;

  // The delay before the update task is executed.
  private static final long UPDATE_DELAY = 15000;

  // The active command dispatcher.
  private TLcdOGCWMSCommandDispatcher fCommandDispatcher;

  // The model provider.
  private ILcdModelProvider fModelProvider;

  /**
   * Creates a new CapabilitiesUpdater.
   *
   * @param aCommandDispatcher The command dispatcher.
   */
  public CapabilitiesUpdater(TLcdOGCWMSCommandDispatcher aCommandDispatcher) {
    fCommandDispatcher = aCommandDispatcher;
    fModelProvider = aCommandDispatcher.getModelProvider();

    // Create the directory that will be monitored.
    String dynamicPath = aCommandDispatcher.getWMSCapabilities().getMapDataFolder() + DYNAMIC_DIRECTORY;
    File updateDirectory = new File(dynamicPath);
    boolean updateDirectoryExists = updateDirectory.exists();
    if (!updateDirectoryExists) {
      updateDirectoryExists = updateDirectory.mkdirs();
    }

    // Create and activate a directory monitor, but only if the dynamic data directory exists.
    if (updateDirectoryExists) {

      DirectoryMonitor monitor = new DirectoryMonitor(updateDirectory, UPDATE_DELAY, UPDATE_INTERVAL);

      // A listener is registered on the directory monitor;
      // this listener will update the server's capabilities.
      monitor.addDirectoryMonitorListener(new Monitor());

      // The model provider is also registered as a listener on the directory monitor,
      // since it needs to update its model cache after directory changes.
      monitor.addDirectoryMonitorListener((DirectoryMonitorListener) fModelProvider);
      monitor.start();
    } else {
      sLogger.warn("Dynamic data folder " + dynamicPath + " could not be created.\n " +
                   "Please check whether there are any write restrictions in the folder " + aCommandDispatcher.getWMSCapabilities().getMapDataFolder() + ".\n " +
                   "Note that the server now works further, but without a dynamic data directory.");
    }
  }

  public void update(HttpServletRequest aRequest) {
    // In this update strategy, we don't need request notifications.
  }

  /**
   * Implementation of DirectoryMonitorListener that sets new capabilities on the command dispatcher
   * each time a DirectorMonitorEvent is received.
   */
  private class Monitor implements DirectoryMonitorListener {

    /**
     * Reports a change in the monitored directory: some files are added, removed or updated.
     */
    public void event(DirectoryMonitorEvent aEvent) {
      // To reflect the data changes in the monitored directory, we create a shallow copy of the capabilities object
      // that is currently active in the WMS server. We do not adapt the active capabilities directly, as
      // this can pose problems regarding consistency.
      TLcdWMSCapabilities newCapabilities = new TLcdWMSCapabilities(fCommandDispatcher.getWMSCapabilities());

      // We set a new update sequence, corresponding to the current date / time.
      DateFormat updateSequenceDateFormat = new SimpleDateFormat("yyyyMMdd:HHmmss");
      newCapabilities.setUpdateSequence(updateSequenceDateFormat.format(new Date()));

      // Process all file events.
      for (int fileListCount = 0; fileListCount < aEvent.getFiles().length; fileListCount++) {
        if (aEvent.getTypes()[fileListCount] == DirectoryMonitorEvent.FILES_ADDED ||
            aEvent.getTypes()[fileListCount] == DirectoryMonitorEvent.FILES_UPDATED) {
          List files = aEvent.getFiles()[fileListCount];

          for (Object object : files) {
            // Check whether we can load this data.
            File file = (File) object;

            // Updated file.
            if (aEvent.getTypes()[fileListCount] == DirectoryMonitorEvent.FILES_UPDATED) {
              try {
                TLcdWMSLayer layer = (TLcdWMSLayer) newCapabilities.findWMSLayer(file.getName());
                if (layer != null) {
                  updateWMSLayer(layer);
                  sLogger.info("Updated data file recognized: " + file.getAbsolutePath());
                }
              } catch (IOException e) {
                // Unrecognized data file.
              }
            }
            // New file.
            else {
              try {
                // Create a new WMS layer.
                TLcdWMSLayer layer = createWMSLayer(file);

                // Update the capabilities.
                TLcdWMSLayer rootLayer = (TLcdWMSLayer) newCapabilities.getRootWMSLayer(0);
                rootLayer.addChildWMSLayer(layer);
                sLogger.info("New data file recognized: " + file.getAbsolutePath());
              } catch (Exception e) {
                // Unrecognized data file.
                sLogger.info("Ignoring new data file: " + file.getAbsolutePath() + ". Reason: " + e.getMessage());
              }
            }
          }
        } else if (aEvent.getTypes()[fileListCount] == DirectoryMonitorEvent.FILES_REMOVED) {
          List files = aEvent.getFiles()[fileListCount];

          for (Object object : files) {
            File file = (File) object;
            ALcdWMSLayer layer = newCapabilities.findWMSLayer(file.getName());
            if (layer != null) {
              ((TLcdWMSLayer) newCapabilities.getRootWMSLayer(0)).removeChildWMSLayer(layer);
              sLogger.info("Removed data file recognized: " + file.getAbsolutePath());
            }
          }
        }
      }

      // Update the capabilities.
      fCommandDispatcher.setWMSCapabilities(newCapabilities);
      sLogger.info("New capabilities loaded with UPDATESEQUENCE value: " + newCapabilities.getUpdateSequence());
    }

    /**
     * Creates a new WMS layer for the given data source.
     *
     * @param aFile The data source.
     * @return a TLcdWMSLayer representing the WMS layer for a data source.
     * @throws IOException if an I/O error occurs.
     */
    private TLcdWMSLayer createWMSLayer(File aFile) throws IOException {
      // Decode the model to check whether we can load this type of data.
      ILcdModel model = fModelProvider.getModel(aFile.getAbsolutePath(), null);
      if (model != null && model.getModelReference() != null) {
        // Create a WMS layer.
        TLcdWMSLayer layer = new TLcdWMSLayer();
        layer.setNameVisible(true);
        layer.setName(aFile.getName());
        layer.setTitle(aFile.getName());
        layer.setSourceName(aFile.getAbsolutePath());
        layer.setLatLonBoundingBox(WMSBoundsUtility.calculateWGS84Bounds(aFile.getAbsolutePath(), fModelProvider));

        return layer;
      } else {
        if (model != null && model.getModelReference() == null) {
          throw new IOException("Model reference is null.");
        } else {
          throw new IOException("Model is null.");
        }
      }
    }

    /**
     * Updates the bounding box parameter of a WMS layer to reflect any changes in the data source.
     * Note: this bounding box parameter is used in the capabilities that are sent to the client.
     *
     * @param aLayer The WMS layer to update.
     * @throws IOException if an I/O error occurs.
     */
    private void updateWMSLayer(TLcdWMSLayer aLayer) throws IOException {
      ILcdModel model = fModelProvider.getModel(aLayer.getSourceName(), null);
      if (model != null) {
        aLayer.setLatLonBoundingBox(WMSBoundsUtility.calculateWGS84Bounds(aLayer.getSourceName(), fModelProvider));
      } else {
        throw new IOException("Model is null.");
      }
    }
  }
}
