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
package samples.lucy.datarepository;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.lucy.TLcyDataFormatManager;
import com.luciad.lucy.addons.ALcyPreferencesAddOn;
import com.luciad.lucy.gui.ILcyMenuBar;
import com.luciad.lucy.gui.TLcyGroupDescriptor;
import com.luciad.lucy.map.ILcyMapManager;
import com.luciad.lucy.model.ALcyFileTypeDescriptor;
import com.luciad.lucy.model.TLcyCompositeDataSourceHandler;
import com.luciad.lucy.model.TLcyCompositeFileTypeDescriptor;
import com.luciad.lucy.model.TLcyCompositeModelDecoder;
import com.luciad.lucy.util.ALcyTool;

/**
 * <p>This sample illustrates the usage of {@link ALcyFileTypeDescriptor
 * ALcyFileTypeDescriptor}s in combination with the {@link TLcyDataFormatManager#handleDataSources(String[],
 * com.luciad.lucy.map.ILcyGenericMapComponent, ALcyFileTypeDescriptor,
 * java.awt.Component) TLcyDataFormatManager#handleDataSource} method to open files.
 *
 * <p>This sample browses a directory on the file system and looks for all files that can be decoded
 * by Lucy. All these files are listed in the menu, and when a user clicks on such file it will be
 * passed to the data source handler.</p>
 *
 * <p>The directory to be searched for files, and the search-depth, are configured in the
 * <code>data_repository.cfg</code> config file.</p>
 *
 * <p>Notice that opening some files may result in an error. For example a CADGR file and a Dafif
 * file both have the extension .toc . Lucy indicates, based on the extension of the file, it can
 * handle the file when at least one of the addons responsible for these file types is loaded. So
 * when trying to open a Dafif file when this addon is not loaded, an error will be produced since
 * the CADGR decoder does not know how to decode a Dafif file.</p>
 */
public class DataRepositoryAddOn extends ALcyPreferencesAddOn {

  //constants to be used in the config file
  public static final String ROOT = "rootSourceName";
  public static final String DEPTH = "depth";

  /**
   * List with all actions that are added to the menu structure.
   */
  private List<AddDataAction> fAddedActions = new ArrayList<AddDataAction>();
  private ExecutorService fExecutor;

  public DataRepositoryAddOn() {
    super(ALcyTool.getLongPrefixWithClassName(DataRepositoryAddOn.class),
          ALcyTool.getShortPrefix(DataRepositoryAddOn.class));

    //use a separate thread to open the data
    //Use daemon threads, so that they don't interfere with application shutdown
    fExecutor = Executors.newFixedThreadPool(1, new ThreadFactory() {
      @Override
      public Thread newThread(Runnable r) {
        Thread background = new Thread(r, "Background Executor ");
        background.setDaemon(true);
        background.setPriority(Thread.MIN_PRIORITY);
        return background;
      }
    });
  }

  @Override
  public void plugInto(ILcyLucyEnv aLucyEnv) {
    super.plugInto(aLucyEnv);

    //retrieve the properties from the config file
    String root = getPreferences().getString(getShortPrefix() + ROOT, "");
    int depth = getPreferences().getInt(getShortPrefix() + DEPTH, -1);
    if (root == null || ("".equals(root)) || depth == -1) {
      System.err.println("The config file does not contain the necessary properties");
      return;
    }

    addDataRepository(aLucyEnv, root, depth, aLucyEnv.getMapManager());
  }

  /**
   * Create all the actions and add them to the menu structure
   *
   * @param aLucyEnv     the Lucy environment
   * @param aRoot        the root data directory
   * @param aSearchDepth the search depth
   * @param aMapManager  the map manager
   */
  private void addDataRepository(ILcyLucyEnv aLucyEnv, String aRoot, int aSearchDepth, ILcyMapManager aMapManager) {
    //get all the file type descriptors
    TLcyDataFormatManager formatManager = aLucyEnv.getDataFormatManager();
    TLcyCompositeFileTypeDescriptor supportedFileTypes = new TLcyCompositeFileTypeDescriptor("Composite");
    //first get all the model decoders file type descriptors
    getAllModelDecodersFileTypeDescriptors(formatManager.getCompositeModelDecoder(),
                                           supportedFileTypes);
    //then get all the data source handler file type descriptors
    getAllDataSourceHandlersFileTypeDescriptors(formatManager.getCompositeDataSourceHandler(),
                                                supportedFileTypes);
    //get all the actions
    createDataSourceActions(aLucyEnv, new String[]{"Data"}, aSearchDepth,
                            fAddedActions, aRoot, aMapManager, supportedFileTypes, fExecutor);
    //insert them in the menu bar
    ILcyMenuBar menuBar = aLucyEnv.getMainMenuBar();

    //first sort them
    Collections.sort(fAddedActions, new AddDataActionComparator());
    for (AddDataAction addDataAction : fAddedActions) {
      menuBar.insertAction(addDataAction, TLcyGroupDescriptor.DEFAULT, addDataAction.getMenuItemArray(), null);
    }
  }

  /**
   * Remove all the items from the menu
   *
   * @param aLucyEnv The lucy environment to unplug from.
   */
  @Override
  public void unplugFrom(ILcyLucyEnv aLucyEnv) {
    super.unplugFrom(aLucyEnv);
    ILcyMenuBar menuBar = aLucyEnv.getMainMenuBar();
    if (menuBar != null) {
      for (AddDataAction addDataAction : fAddedActions) {
        menuBar.removeAction(addDataAction);
      }
    }
    fAddedActions.clear();
  }

  /**
   * Create all the {@link AddDataAction AddDataAction}s for the
   * directory <code>aRootNode</code> and depth <code>aDepth</code>. These actions will be added to
   * the list <code>aDataActionListSFCT</code> and inserted in the main menu bar with item
   * <code>aMenuItem</code>.
   *
   * @param aLucyEnv                the Lucy backend
   * @param aMenuItem               the menu items, indicating where the actions must be placed
   * @param aDepth                  the depth for the search
   * @param aDataActionListSFCT     the list to which all the actions will be added
   * @param aRootNode               the directory to be searched
   * @param aMapManager             the map manager
   * @param aSupportedFileTypes     the descriptor for all supported file types, used for a quick
   *                                test to figure out if a file will probably be loadable.
   * @param aExecutorService the executor service
   */
  private void createDataSourceActions(ILcyLucyEnv aLucyEnv,
                                       String[] aMenuItem,
                                       int aDepth,
                                       List<AddDataAction> aDataActionListSFCT,
                                       String aRootNode,
                                       ILcyMapManager aMapManager,
                                       ALcyFileTypeDescriptor aSupportedFileTypes,
                                       ExecutorService aExecutorService) {
    //first search for all the files in the directory
    File file = new File(aRootNode);
    File[] files = file.listFiles();

    if (files != null) {
      for (File subFile : files) {
        boolean isDir = subFile.isDirectory();
        //go recursively through all the directories until a certain depth is reached
        if (aDepth > 0 && isDir) {
          String[] newPath = new String[aMenuItem.length + 1];
          System.arraycopy(aMenuItem, 0, newPath, 0, aMenuItem.length);
          newPath[newPath.length - 1] = subFile.getName();
          createDataSourceActions(aLucyEnv,
                                  newPath,
                                  aDepth - 1,
                                  aDataActionListSFCT,
                                  subFile.getPath(),
                                  aMapManager,
                                  aSupportedFileTypes,
                                  aExecutorService);
        } else if (!(isDir)) {
          if (aSupportedFileTypes.includes(subFile.getPath())) {
            //create the action
            aDataActionListSFCT.add(new AddDataAction(aLucyEnv.getDataFormatManager(),
                                                      subFile.getName(),
                                                      aMapManager,
                                                      aMenuItem,
                                                      subFile.getPath(),
                                                      aExecutorService));
          }
        }
      }
    }

  }

  /**
   * Adds all the <code>ALcyFileTypeDescriptor</code>s, registered in the
   * <code>TLcyCompositeModelDecoder</code>, to the list <code>aFileTypeDescriptorListSFCT</code>.
   *
   * @param aCompositeModelDecoder      the composite model decoder
   * @param aCompositeFileTypeDescriptorSFCT the list to which all the file type descriptors will be
   *                                    added
   */
  private void getAllModelDecodersFileTypeDescriptors(TLcyCompositeModelDecoder aCompositeModelDecoder,
                                                      TLcyCompositeFileTypeDescriptor aCompositeFileTypeDescriptorSFCT) {
    int count = aCompositeModelDecoder.getModelDecoderCount();
    for (int i = 0; i < count; i++) {
      ALcyFileTypeDescriptor decoderFileTypeDescriptor = aCompositeModelDecoder.getDecoderFileTypeDescriptor(i);
      if (decoderFileTypeDescriptor != null) {
        aCompositeFileTypeDescriptorSFCT.addFileTypeDescriptor(decoderFileTypeDescriptor);
      }
    }
  }

  /**
   * Adds all the <code>ALcyFileTypeDescriptor</code>s, registered in the
   * <code>TLcyCompositeDataSourceHandler</code>, to the list <code>aFileTypeDescriptorListSFCT</code>
   *
   * @param aCompositeDataSourceHandler the composite data source handler
   * @param aCompositeFileTypeDescriptorSFCT the list to which all the file type descriptors will be
   *                                    added
   */
  private void getAllDataSourceHandlersFileTypeDescriptors(TLcyCompositeDataSourceHandler aCompositeDataSourceHandler,
                                                           TLcyCompositeFileTypeDescriptor aCompositeFileTypeDescriptorSFCT) {
    int count = aCompositeDataSourceHandler.getDataSourceHandlerCount();
    for (int i = 0; i < count; i++) {
      ALcyFileTypeDescriptor fileTypeDescriptor = aCompositeDataSourceHandler.getFileTypeDescriptor(i);
      if (fileTypeDescriptor != null) {
        aCompositeFileTypeDescriptorSFCT.addFileTypeDescriptor(fileTypeDescriptor);
      }
    }
  }
}
