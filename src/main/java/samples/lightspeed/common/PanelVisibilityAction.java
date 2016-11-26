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
package samples.lightspeed.common;

import java.awt.event.ActionEvent;

import javax.swing.JPanel;

import com.luciad.gui.ALcdAction;

/**
 * Action that makes a panel visible or invisible, depending on the
 * message given to the action.
 */
public class PanelVisibilityAction extends ALcdAction {

  private JPanel fPanel;
  private final String fShowCommand;
  private final String fHideCommand;

  /**
   * Creates an action that shows or hides the given panel
   * @param aPanel the panel to show or hide
   * @param aShowCommand the command to use to show the panel
   * @param aHideCommand the command to use to hide the panel
   */
  public PanelVisibilityAction(JPanel aPanel, String aShowCommand, String aHideCommand) {
    if (aPanel == null || aShowCommand == null || aHideCommand == null) {
      throw new IllegalArgumentException("Null not accepted as a parameter");
    }
    fPanel = aPanel;
    fShowCommand = aShowCommand;
    fHideCommand = aHideCommand;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    //These two strings are documented in TLspShapeEditor
    if (fShowCommand.equals(e.getActionCommand())) {
      fPanel.setVisible(true);
    } else if (fHideCommand.equals(e.getActionCommand())) {
      fPanel.setVisible(false);
    }
  }
}
