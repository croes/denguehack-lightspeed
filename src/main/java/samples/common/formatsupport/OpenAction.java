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
package samples.common.formatsupport;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.prefs.Preferences;

import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileFilter;

import com.luciad.gui.ALcdAction;
import com.luciad.gui.ILcdAction;
import com.luciad.gui.TLcdIconFactory;

import samples.common.MetaKeyUtil;
import samples.common.gui.FileAndDirectoryChooser;

/**
 * Action that shows a file chooser and decodes the chosen files using an OpenSupport instance.
 * Only the files that can be decoded are shown in the dialog.
 */
public class OpenAction extends ALcdAction {

  private static final String PREFERENCES_PATH_KEY = "path";

  private final OpenSupport fOpenSupport;

  private String fInitialPath;
  private boolean fRememberPreviousPath = true;
  private Preferences fPreferences;

  public OpenAction(OpenSupport aOpenSupport) {
    fOpenSupport = aOpenSupport;
    setIcon(TLcdIconFactory.create(TLcdIconFactory.OPEN_ICON));
    setShortDescription("Open a file or directory");
    setName("Open file...");
    setPreferencesKey("LuciadDecoderSample");
    fInitialPath = System.getProperty("user.dir") + "/resources" + "/Data";
    putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_O, MetaKeyUtil.getCMDMask()));
    // disable this if you don't want the text to appear in a button.
    putValue(ILcdAction.SHOW_ACTION_NAME, true);
  }

  public void setFirstInitialPath(String aPath) {
    fInitialPath = aPath;
  }

  public void setRememberPreviousPath(boolean aRememberPreviousPath) {
    fRememberPreviousPath = aRememberPreviousPath;
  }

  public void setPreferencesKey(String aKey) {
    fPreferences = Preferences.userRoot().node(aKey);
  }

  private FileAndDirectoryChooser createFileChooser() {
    String path = fRememberPreviousPath ? fPreferences.get(PREFERENCES_PATH_KEY, System.getProperty("user.dir") + "/resources" + "/Data") : fInitialPath;
    FileAndDirectoryChooser chooser = new FileAndDirectoryChooser(fOpenSupport.getFileFilters());
    chooser.setCurrentDirectory(new File(path));
    chooser.setMultiSelectionEnabled(true);
    return chooser;
  }

  /**
   * Open the specified file
   * @param aSource     The source
   * @param aFileFilter Filter which is used to determine which model decoder should be used for the
   *                    decoding. May be {@code null}
   */
  public void openSource(String aSource, FileFilter aFileFilter) {
    fOpenSupport.openSource(aSource, aFileFilter);
  }

  public OpenSupport getOpenSupport() {
    return fOpenSupport;
  }

  // Implementations for ALcdAction

  public void actionPerformed(ActionEvent aEvent) {
    FileAndDirectoryChooser chooser = createFileChooser();
    if (chooser.showOpenDialog(fOpenSupport.getParentComponent()) == JFileChooser.APPROVE_OPTION) {
      FileFilter fileFilter = chooser.getFileFilter();
      if (fRememberPreviousPath) {
        fPreferences.put(PREFERENCES_PATH_KEY, chooser.getSelectedFiles()[0].getParent());
      }
      File[] selectedFiles = chooser.getSelectedFiles();
      for (File selectedFile : selectedFiles) {
        String pathToSelectedFile = selectedFile.getAbsolutePath();
        openSource(pathToSelectedFile, fileFilter);
      }
    }
  }

}
