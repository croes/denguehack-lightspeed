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

import java.awt.Component;
import java.io.File;

import javax.swing.JOptionPane;

import com.luciad.lucy.addons.workspace.TLcyWorkspaceAddon;
import com.luciad.lucy.workspace.TLcyWorkspaceManager;

/**
 * An extension of the {@link TLcyWorkspaceAddon} which saves the workspaces
 * to a fixed location.
 * For workspace loading, a list of workspaces from that fixed location is presented to the user.
 */
public class ChooseFromListWorkspaceAddOn extends TLcyWorkspaceAddon {

  private static final long FAKE_DELAY_IN_MILLISECONDS = 3000;

  private final File fWorkspaceDirectory = new File(getClass().getClassLoader().getResource("samples/workspace/cities.lws").getFile()).getParentFile();

  /**
   * Use a background thread to retrieve workspaces from a fixed location.
   * Show a progress dialog to the user, and once those workspaces are retrieved update the dialog to show them to the user.
   * As the {@link TLcyWorkspaceManager#getWorkspaceCodec()} has not been replaced, this method should still return a file path
   * and not just the file name.
   */
  @Override
  protected String selectWorkspaceSource(Component aParentComponent) {
    OpenDialogUI openDialogUI = new OpenDialogUI(FAKE_DELAY_IN_MILLISECONDS, fWorkspaceDirectory);
    openDialogUI.start();
    int option = JOptionPane.showConfirmDialog(aParentComponent,
                                               openDialogUI,
                                               "Select workspace to open",
                                               JOptionPane.OK_CANCEL_OPTION);
    if (option == JOptionPane.OK_OPTION) {
      String selectedWorkspace = openDialogUI.getSelectedWorkspace();
      return selectedWorkspace != null ? new File(fWorkspaceDirectory, selectedWorkspace).getAbsolutePath() : null;
    }
    return null;
  }

  @Override
  protected String selectWorkspaceDestination(String aProposedDestination, Component aParentComponent) {
    if (aProposedDestination != null && new File(aProposedDestination).exists()) {
      //when using File | Save workspace, just save the workspace and not show any UI
      //see javadoc of this method for more information
      return aProposedDestination;
    }
    String input = JOptionPane.showInputDialog(aParentComponent, "Name for the workspace", "Save workspace as", JOptionPane.QUESTION_MESSAGE);
    if (input == null || input.isEmpty()) {
      return null;
    }
    if (!input.endsWith(".lws")) {
      input += ".lws";
    }
    return new File(fWorkspaceDirectory, input).getAbsolutePath();
  }
}
