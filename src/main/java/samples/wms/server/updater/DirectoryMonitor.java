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
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * This class monitors a directory for any changes of its contents: new files, removed files or updated files.
 * By using the methods {@link #addDirectoryMonitorListener addDirectoryMonitorListener(DirectoryMonitorListener)}
 * and {@link #removeDirectoryMonitorListener removeDirectoryMonitorListener(DirectoryMonitorListener)}, one
 * can register/remove listeners for these changes.
 */
class DirectoryMonitor {

  // The directory to be monitored.
  private File fDirectory;

  // The DirectoryMonitorListener objects that need to be updated for any changes.
  private List fListeners = new ArrayList();

  // A file cache.
  private Map fFileCache = new Hashtable();

  private long fDelay, fInterval;

  /**
   * Creates a new DirectoryMonitor for the given parameters.
   *
   * @param aDirectory The directory that needs to be monitored.
   * @param aDelay     The delay in milliseconds before the monitor task is to be executed.
   * @param aInterval  The time in milliseconds between successive monitor task executions.
   */
  public DirectoryMonitor(File aDirectory, long aDelay, long aInterval) {
    fDirectory = aDirectory;
    fDelay = aDelay;
    fInterval = aInterval;
  }

  /**
   * Starts the monitor.
   */
  public void start() {
    Timer timer = new Timer(true);
    TimerTask task = new TimerTask() {
      public void run() {
        update();
      }
    };
    timer.schedule(task, fDelay, fInterval);
  }

  /**
   * Recursively checks all files and directories in the monitored directory.
   * A cache is maintained to easily determine any new, removed or updated files.
   */
  public synchronized void update() {
    List removedFiles = new ArrayList();
    List changedFiles = new ArrayList();
    List newFiles = new ArrayList();

    // Update the file cache; all changes are saved in the supplied List objects.
    updateFilesSFCT(removedFiles, changedFiles, newFiles);

    // Only send an event when there are any changes.
    if (removedFiles.size() > 0 || changedFiles.size() > 0 || newFiles.size() > 0) {
      // Send event.
      fireEvent(new DirectoryMonitorEvent(
          new List[]{removedFiles, changedFiles, newFiles},
          new int[]{DirectoryMonitorEvent.FILES_REMOVED, DirectoryMonitorEvent.FILES_UPDATED, DirectoryMonitorEvent.FILES_ADDED}));
    }
  }

  /**
   * Fires the given DirectoryMonitorEvent to all registered DirectoryMonitorListener objects.
   *
   * @param aEvent The DirectoryMonitorEvent to be propagated.
   */
  private void fireEvent(DirectoryMonitorEvent aEvent) {
    for (int i = 0; i < fListeners.size(); i++) {
      ((DirectoryMonitorListener) fListeners.get(i)).event(aEvent);
    }
  }

  /**
   * Registers the given DirectoryMonitorListener.
   *
   * @param aListener The DirectoryMonitorListener to be registered.
   */
  public void addDirectoryMonitorListener(DirectoryMonitorListener aListener) {
    fListeners.add(aListener);
  }

  /**
   * Removes the given DirectoryMonitorListener.
   *
   * @param aListener The DirectoryMonitorListener to be removed.
   */
  public void removeDirectoryMonitorListener(DirectoryMonitorListener aListener) {
    fListeners.remove(aListener);
  }

  /**
   * Updates the current file cache by determining all new, changed or removed files.
   * As a side effect, these file updates are also added to the supplied
   * List objects.
   *
   * @param aRemovedFiles A List to which removed File objects are added.
   * @param aChangedFiles A List to which changed File (i.e., that are modified since
   *                      the last monitor process) objects are added.
   * @param aNewFiles     A List to which new File objects are added.
   */
  private void updateFilesSFCT(List aRemovedFiles, List aChangedFiles, List aNewFiles) {
    // Check whether there are files removed.
    updateRemovedFilesSFCT(aRemovedFiles);

    // Check whether there are new files or updated files.
    updateNewOrChangedFilesSFCT(fDirectory, aChangedFiles, aNewFiles);
  }

  /**
   * Updates the current file cache by determining all removed files.
   * As a side effect, these file updates are also added to the supplied
   * List object.
   *
   * @param aRemovedFiles A List to which removed File objects are added.
   */
  private void updateRemovedFilesSFCT(List aRemovedFiles) {
    Collection fileEntries = fFileCache.values();

    Iterator iterator = fileEntries.iterator();
    while (iterator.hasNext()) {
      FileEntry fileEntry = (FileEntry) iterator.next();
      if (fileEntry.isRemoved()) {
        aRemovedFiles.add(fileEntry.getFile());
        iterator.remove();
      }
    }
  }

  /**
   * Updates the current file cache by determining all new and changed files.
   * As a side effect, these file updates are also added to the supplied List objects.
   * <p/>
   * The given directory including all its subdirectories are checked recursively.
   *
   * @param aDirectory    The directory which must be checked (recursively).
   * @param aChangedFiles A List to which changed File (i.e., that are modified since
   *                      the last monitor process) objects are added.
   * @param aNewFiles     A List to which new File objects are added.
   */
  private void updateNewOrChangedFilesSFCT(File aDirectory, List aChangedFiles, List aNewFiles) {
    File[] files = aDirectory.listFiles();

    for (int i = 0; i < files.length; i++) {
      File file = files[i];

      if (file.isFile()) {
        // Can the file be opened?
        FileReader reader = null;
        try {
          // E.g., FileNotFoundException is thrown when file is currently being copied.
          reader = new FileReader(file);
          reader.ready();
        } catch (IOException e) {
          continue;
        } finally {
          if (reader != null) {
            try {
              reader.close();
            } catch (IOException e) {
              // Ignore.
            }
          }
        }

        // Check whether the file was already existing.
        FileEntry fileEntry = (FileEntry) fFileCache.get(file.getAbsolutePath());
        if (fileEntry != null) {
          // The file was already existing. Check if it has changed since the last monitor task.
          if (fileEntry.hasChanged()) {
            // The file has been updated.
            aChangedFiles.add(file);
            fileEntry.update();
          }
        } else {
          // This is a new file.
          aNewFiles.add(file);
          fFileCache.put(file.getAbsolutePath(), new FileEntry(file));
        }
      }
      // The file is a subdirectory: scan it.
      else {
        updateNewOrChangedFilesSFCT(file, aChangedFiles, aNewFiles);
      }
    }
  }

  /**
   * Container for a File object that stores its initial modification time.
   * The utility methods {@link #hasChanged()} and {@link #isRemoved()}
   * are provided to respectively check whether the file has been changed or removed.
   * By invoking {@link #update()}, the stored modification time gets updated
   * with the actual modification time of the file.
   */
  private static class FileEntry {

    private File fFile;
    private long fLastModified;

    /**
     * Creates a new FileEntry object for the given file.
     * The last modification time of the given file is automatically stored.
     *
     * @param aFile A file.
     */
    public FileEntry(File aFile) {
      fFile = aFile;
      update();
    }

    /**
     * Returns the file associated with this FileEntry object.
     *
     * @return the file associated with this FileEntry object.
     */
    public final File getFile() {
      return fFile;
    }

    /**
     * Checks whether the associated file has been changed
     * since the last invocation of {@link #update()}.
     *
     * @return whether the associated file has been changed
     *         since the last invocation of {@link #update()}.
     */
    public final boolean hasChanged() {
      return (fLastModified < fFile.lastModified());
    }

    /**
     * Checks whether the associate file has been removed.
     *
     * @return whether the associate file has been removed.
     */
    public final boolean isRemoved() {
      return !fFile.exists();
    }

    /**
     * Updates that stored modification time with the actual
     * modification time of the associated file.
     */
    public final void update() {
      fLastModified = fFile.lastModified();
    }
  }
}
