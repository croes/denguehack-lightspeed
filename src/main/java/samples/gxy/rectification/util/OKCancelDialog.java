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
package samples.gxy.rectification.util;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import com.luciad.gui.TLcdAWTUtil;

/**
 * A simple OK/Cancel dialog, which can wrap around any given JPanel. Depending on the pressed
 * button, it sets the fCanceled property. It is the responsibility of the caller to take the proper
 * actions.
 */
public class OKCancelDialog extends JDialog {

  private boolean fCanceled;

  public OKCancelDialog(Frame aFrame, JPanel aMainPanel) {
    super(aFrame, true);

    //----------------------------------------------
    // The OK/Cancel buttons, placed in their own panel
    final JButton OKButton = new JButton("OK");
    OKButton.setMnemonic('O');
    OKButton.setToolTipText("Confirm the current selection");
    OKButton.setEnabled(true);
    OKButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        buttons_actionPerformed(e);
      }
    });
    OKButton.setActionCommand("ok");

    JButton cancelButton = new JButton("Cancel");
    cancelButton.setMnemonic('u');
    cancelButton.setToolTipText("Discard your modifications");
    cancelButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        buttons_actionPerformed(e);
      }
    });
    cancelButton.setActionCommand("cancel");

    JPanel OKCancelPanel = new JPanel(new GridBagLayout());
    GridBagConstraints gc = new GridBagConstraints();
    // Layout
    gc.anchor = GridBagConstraints.WEST;
    gc.gridwidth = 1;
    gc.gridheight = 1;
    gc.weighty = 0;
    gc.fill = GridBagConstraints.HORIZONTAL;

    gc.gridx = 0;
    gc.weightx = 1;
    OKCancelPanel.add(Box.createHorizontalGlue(), gc);

    gc.insets = new Insets(0, 10, 5, 10);
    gc.gridx = 1;
    gc.weightx = 0;
    OKCancelPanel.add(OKButton, gc);

    gc.gridx = 2;
    OKCancelPanel.add(cancelButton, gc);

    //-------------------------------------------------------------
    getContentPane().setLayout(new BorderLayout());
    getContentPane().add(aMainPanel, BorderLayout.CENTER);
    getContentPane().add(OKCancelPanel, BorderLayout.SOUTH);

    pack();

    TLcdAWTUtil.centerWindowOnScreen(this);

    // Default button = ok
    getRootPane().setDefaultButton(OKButton);

    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        setCanceled(true);
      }

      public void windowOpened(WindowEvent e) {
        OKButton.requestFocus();
      }
    });
  }

  public boolean isCanceled() {
    return fCanceled;
  }

  private void setCanceled(boolean aCanceled) {
    fCanceled = aCanceled;
  }

  private void buttons_actionPerformed(ActionEvent aEvent) {
    String cmd = aEvent.getActionCommand();
    if (cmd.equals("ok")) {
      setCanceled(false);
      setVisible(false);
    } else if (cmd.equals("cancel")) {
      setCanceled(true);
      setVisible(false);
    }
  }
}
