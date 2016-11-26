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
package samples.common.action;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.IOException;

import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;

import com.luciad.gui.ALcdAction;
import com.luciad.gui.ILcdAction;
import com.luciad.gui.TLcdAWTUtil;
import com.luciad.gui.TLcdIconFactory;
import com.luciad.gui.TLcdUserDialog;
import com.luciad.gui.swing.TLcdDialogManagerSW;
import com.luciad.io.TLcdIOUtil;

import samples.common.Java2Util;
import samples.common.MacUtil;
import samples.common.SwingUtil;

/**
 * This ILcdAction pops up a JOptionPane with a JTextArea that contains the contents
 * of a readme file.
 */
public class ShowReadMeAction
    extends ALcdAction
    implements ILcdAction {

  private final static String NO_README_STRING = "Readme file name is not set.\nNo help available.";

  private final Component fParentComponent;
  private final JEditorPane fTextArea = new JEditorPane("text/html", NO_README_STRING);
  private final JScrollPane fScrollPane = new JScrollPane(fTextArea);
  private JDialog fDialog;

  public static ShowReadMeAction createForSample(Component aParentComponent) {
    String location = Java2Util.getPackageName(aParentComponent).replace('.', '/') + "/readme.html";
    return aParentComponent.getClass().getClassLoader().getResource(location) != null ? new ShowReadMeAction(location, aParentComponent) : null;
  }

  private ShowReadMeAction(String aReadMeFileName, Component aParentComponent) {
    fParentComponent = aParentComponent;
    setName("Show readme");
    setLongDescription("Show readme");
    setShortDescription("Show readme");
    setIcon(TLcdIconFactory.create(TLcdIconFactory.HINT_ICON));

    fTextArea.setEditable(false);
    fScrollPane.setPreferredSize(new Dimension(500, 200));
    fScrollPane.setMinimumSize(new Dimension(10, 10));

    setReadMeFileName(aReadMeFileName);

    // Let all LuciadLightspeed dialogs use Swing.
    TLcdUserDialog.setDialogManager(new TLcdDialogManagerSW());
  }

  public static boolean showAtStartup() {
    return "true".equalsIgnoreCase(System.getProperty("samples.showReadmeAtStartup", "true"));
  }

  public static void showAtStartup(final ShowReadMeAction aAction) {
    if (showAtStartup()) {
      //when the sample has started, show the readme action
      TLcdAWTUtil.invokeLater(new Runnable() {
        public void run() {
          aAction.actionPerformed(null);
        }
      });
    }
  }

  public void setReadMeFileName(String aReadMeFileName) {
    try {
      TLcdIOUtil io_util = new TLcdIOUtil();
      io_util.setSourceName(aReadMeFileName);
      fTextArea.setPage(io_util.getURL());
    } catch (IOException e) {
      fTextArea.setText(aReadMeFileName + " does not exist or cannot be read.");
    }
  }

  public void actionPerformed(ActionEvent e) {
    if (fDialog != null && fDialog.isVisible()) {
      fDialog.toFront();
    } else {
      JOptionPane option_pane = new JOptionPane(fScrollPane, JOptionPane.INFORMATION_MESSAGE);
      fDialog = option_pane.createDialog(fParentComponent, "Information");
      fDialog.getOwner().setIconImages(SwingUtil.sLuciadFrameImage);
      fDialog.setLocation(30, 30);
      fDialog.setModal(false);
      fDialog.setResizable(true);
      fDialog.pack();
      fDialog.setVisible(true);
      MacUtil.allowClosingWithCmdW(fDialog);
    }
  }

}
