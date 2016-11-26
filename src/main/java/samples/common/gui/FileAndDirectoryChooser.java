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
package samples.common.gui;

import java.awt.Component;
import java.awt.HeadlessException;
import java.io.File;
import java.util.Collection;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import samples.common.NoopStringTranslator;
import com.luciad.util.ILcdFilter;

/**
 * Enhances a JFileChooser to allow opening supported directories.
 */
public class FileAndDirectoryChooser {

  private final MyFileChooser fFileChooser;
  private static int sNextThread = 0;

  /**
   * The given file filters are used to determine if a file or directory can be opened.
   * Directories can always be freely navigated into.
   */
  public FileAndDirectoryChooser(Collection<FileFilter> aFileFilters) {
    fFileChooser = new MyFileChooser();
    fFileChooser.removeChoosableFileFilter(fFileChooser.getAcceptAllFileFilter());
    for (FileFilter filter : aFileFilters) {
      fFileChooser.addChoosableFileFilter(new FileFilterThatAllowsDirectories(filter));
    }
    fFileChooser.setFileFilter(fFileChooser.getChoosableFileFilters()[0]);
  }

  public int showOpenDialog(Component aParentComponent) {
    return fFileChooser.showOpenDialog(aParentComponent);
  }

  /**
   * @return the current file filter, or null if the accept-all filter was used.
   */
  public FileFilter getFileFilter() {
    FileFilter fileFilter = fFileChooser.getFileFilter();
    if (fileFilter == fFileChooser.getAcceptAllFileFilter()) {
      fileFilter = null;
    }
    if (fileFilter instanceof FileFilterThatAllowsDirectories) {
      fileFilter = ((FileFilterThatAllowsDirectories) fileFilter).getFilter();
    }
    return fileFilter;
  }

  public File[] getSelectedFiles() {
    File[] selectedFiles = fFileChooser.getSelectedFiles();
    if ((selectedFiles == null || selectedFiles.length == 0)) {
      File selectedDirectory = fFileChooser.getHelper().getSelectedDirectory();
      return new File[]{selectedDirectory};
    }
    return selectedFiles;
  }

  public void setCurrentDirectory(File aFile) {
    fFileChooser.setCurrentDirectory(aFile);
  }

  public void setMultiSelectionEnabled(boolean aEnabled) {
    fFileChooser.setMultiSelectionEnabled(aEnabled);
  }

  // used to determine if a directory can be opened
  protected Executor getExecutor() {
    ThreadFactory threadFactory = new ThreadFactory() {
      @Override
      public Thread newThread(Runnable aRunnable) {
        Thread background = new Thread(aRunnable, "Background Directory Chooser Executor " + sNextThread++);
        //Use daemon threads, so that they don't interfere with application shutdown
        background.setDaemon(true);
        background.setPriority(Thread.MIN_PRIORITY);
        return background;
      }
    };
    ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 1, 20, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), threadFactory);
    executor.allowCoreThreadTimeOut(true);
    return executor;
  }

  MyFileChooser getFileChooser() {
    return fFileChooser;
  }

  class MyFileChooser extends JFileChooser {

    private final FileAndDirectoryChooserHelper fAccessory;

    MyFileChooser() {
      fAccessory = new FileAndDirectoryChooserHelper(this, new NoopStringTranslator()) {
        @Override
        protected Executor getBackgroundExecutor()  {
          return FileAndDirectoryChooser.this.getExecutor();
        }
      };
      fAccessory.reset(true, new ILcdFilter<File>() {
        @Override
        public boolean accept(File aObject) {
          FileFilter filter = getFileFilter();
          return filter instanceof FileFilterThatAllowsDirectories &&
                 ((FileFilterThatAllowsDirectories) filter).getFilter().accept(aObject);
        }
      });
    }

    FileAndDirectoryChooserHelper getHelper() {
      return fAccessory;
    }

    @Override
    public int showOpenDialog(Component parent) throws HeadlessException {
      fAccessory.update(this);
      return handleDirectory(super.showOpenDialog(parent));
    }

    @Override
    public int showSaveDialog(Component parent) throws HeadlessException {
      fAccessory.update(this);
      return handleDirectory(super.showSaveDialog(parent));
    }

    private int handleDirectory(int aDialog) {
      if (aDialog != JFileChooser.APPROVE_OPTION) {
        return fAccessory.getSelectedDirectory() != null ? APPROVE_OPTION : CANCEL_OPTION;
      }
      return APPROVE_OPTION;
    }
  }

  private static class FileFilterThatAllowsDirectories extends FileFilter {

    private final FileFilter fFilter;

    FileFilterThatAllowsDirectories(FileFilter aFilter) {
      fFilter = aFilter;
    }

    @Override
    public boolean accept(File f) {
      return f.isDirectory() || fFilter.accept(f); // allow all directories to enable navigation
    }

    @Override
    public String getDescription() {
      return fFilter.getDescription();
    }

    public FileFilter getFilter() {
      return fFilter;
    }
  }
}
