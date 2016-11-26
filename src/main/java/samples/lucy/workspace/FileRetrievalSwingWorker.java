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
package samples.lucy.workspace;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingWorker;

/**
 * SwingWorker which is going to collect all workspace files from a file folder.
 * The class allows to introduce some delay to fake a long running operation
 */
final class FileRetrievalSwingWorker extends SwingWorker<List<String>, Void> {

  private final long fDelayInMilliseconds;
  private final File fWorkspacesDir;

  FileRetrievalSwingWorker(long aDelayInMilliseconds, File aWorkspacesDir) {
    fDelayInMilliseconds = aDelayInMilliseconds;
    fWorkspacesDir = aWorkspacesDir;
  }

  @Override
  protected List<String> doInBackground() throws Exception {
    fakeLongRunningOperation();

    File[] availableWorkspaceFiles = fWorkspacesDir.listFiles(new FilenameFilter() {
      @Override
      public boolean accept(File dir, String name) {
        return name.endsWith(".lws");
      }
    });
    List<String> result = new ArrayList<>();
    for (File availableWorkspaceFile : availableWorkspaceFiles) {
      result.add(availableWorkspaceFile.getName());
    }
    return result;
  }

  /**
   * Retrieving files from the local file system is a rather fast operation.
   * For illustration purposes, we fake that this operation takes more time.
   * This would for example be the case when the workspace were stored remotely (e.g. in a database).
   */
  private void fakeLongRunningOperation() throws InterruptedException {
    Thread.sleep(fDelayInMilliseconds);
  }
}
