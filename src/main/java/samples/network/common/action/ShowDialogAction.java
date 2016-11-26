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
package samples.network.common.action;

import java.awt.Frame;
import java.awt.event.ActionEvent;

import javax.swing.JComponent;
import javax.swing.JDialog;

import com.luciad.gui.ALcdAction;
import com.luciad.gui.ILcdIcon;

import samples.common.SwingUtil;

/**
 * Action for displaying a dialog.
 */
public class ShowDialogAction extends ALcdAction {

  private JComponent fContent;
  private String fTitle;
  private JDialog fDialog;

  public ShowDialogAction(JComponent aContent, String aTitle, String aTooltip, ILcdIcon aIcon) {
    fContent = aContent;
    fTitle = aTitle;
    setIcon(aIcon);
    setShortDescription(aTooltip);
    setEnabled(fContent != null);
  }

  public void setContent(JComponent aComponent) {
    fContent = aComponent;
    setEnabled(fContent != null);
  }

  // Implementations for ActionListener.

  public void actionPerformed(ActionEvent e) {
    if (fDialog == null) {
      fDialog = new JDialog((Frame) null, fTitle);
      fDialog.getContentPane().add(fContent);
      if (fDialog.getOwner() instanceof Frame) {
        ((Frame) fDialog.getOwner()).setIconImages(SwingUtil.sLuciadFrameImage);
      }
      fDialog.setLocation(0, 0);
      fDialog.setModal(false);
      fDialog.setResizable(true);
      fDialog.pack();
      fDialog.setVisible(true);
    } else {
      fDialog.setVisible(true);
    }
  }

}
