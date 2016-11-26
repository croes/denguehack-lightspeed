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
package samples.lightspeed.demo.application.controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.controller.manipulation.ALspCreateControllerModel;
import com.luciad.view.lightspeed.controller.touch.TLspTouchCreateController;

/**
 * Touch enabled create controller with buttons to finish or cancel creation process.
 */
public class TouchCreateController extends TLspTouchCreateController {
  private JButton fCommitButton;
  private JButton fCancelButton;
  private ActionListener fCancelActionListener;
  private ActionListener fFinishActionListener;

  public TouchCreateController(JButton aCancelButton, JButton aFinishButton, ALspCreateControllerModel aCreateControllerModel) {
    super(aCreateControllerModel);
    fCancelButton = aCancelButton;
    fCommitButton = aFinishButton;
  }

  @Override
  protected void startInteractionImpl(ILspView aView) {
    super.startInteractionImpl(aView);
    fCancelButton.setEnabled(true);
    fCommitButton.setEnabled(true);

    fCancelActionListener = new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        cancel();
      }
    };
    fCancelButton.addActionListener(fCancelActionListener);

    fFinishActionListener = new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        finish();
      }
    };
    fCommitButton.addActionListener(fFinishActionListener);

  }

  @Override
  public void terminateInteraction(ILspView aView) {
    fCancelButton.removeActionListener(fCancelActionListener);
    fCommitButton.removeActionListener(fFinishActionListener);
    fCancelButton.setEnabled(false);
    fCommitButton.setEnabled(false);
    super.terminateInteraction(aView);
  }
}
