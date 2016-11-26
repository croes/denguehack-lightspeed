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

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;

import com.luciad.gui.ALcdAction;
import com.luciad.gui.ILcdAction;
import com.luciad.gui.TLcdIconFactory;
import com.luciad.gui.TLcdUserDialog;
import com.luciad.gui.swing.TLcdDialogManagerSW;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.TLspOpenGLProfile;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.ILspPaintableLayer;

import samples.common.SwingUtil;

/**
 * This ILcdAction pops up a JOptionPane with a JTextArea that shows the required OpenGL profile for
 * the view associated with this ShowGLProfileActionSW
 */
public class ShowGLProfileActionSW extends ALcdAction implements ILcdAction {

  private final static String NO_GL_PROFILE_STRING = "No required OpenGL profile available.";

  private JEditorPane fTextArea;
  private JScrollPane fScrollPane;
  private JDialog fDialog;
  private ILspView fView;

  public ShowGLProfileActionSW(ILspView aView) {
    setName("Show OpenGL profile");
    setLongDescription("Show required OpenGL profile for view");
    setShortDescription("Show OpenGL profile");
    setIcon(TLcdIconFactory.create(TLcdIconFactory.LAYER_PROPERTIES_ICON));

    fTextArea = new JEditorPane("text/plain", NO_GL_PROFILE_STRING) {
      @Override
      public Dimension getPreferredSize() {
        Dimension size = super.getPreferredSize();
        size.width = Math.min(500, size.width);
        size.height = Math.min(200, size.height);
        return size;
      }
    };
    fTextArea.setEditable(false);
    fScrollPane = new JScrollPane(fTextArea);

    fView = aView;

    setTextField();
    TLcdUserDialog.setDialogManager(new TLcdDialogManagerSW());
  }

  public void setTextField() {
    fTextArea.setText(getCombinedProfile().toString());
  }

  public TLspOpenGLProfile getCombinedProfile() {
    ArrayList<TLspOpenGLProfile> checkedProfiles = new ArrayList<TLspOpenGLProfile>();
    checkedProfiles.add(fView.getRequiredOpenGLProfile());
    for (int i = 0; i < fView.layerCount(); i++) {
      ILspLayer layer = fView.getLayer(i);
      if (layer instanceof ILspPaintableLayer) {
        checkedProfiles.add(((ILspPaintableLayer) layer).getRequiredOpenGLProfile());
      }
    }
    TLspOpenGLProfile[] array = new TLspOpenGLProfile[checkedProfiles.size()];
    checkedProfiles.toArray(array);
    return TLspOpenGLProfile.getSuperSet(array);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    setTextField();
    if (fDialog != null && fDialog.isVisible()) {
      fDialog.toFront();
    } else {
      JOptionPane option_pane = new JOptionPane(fScrollPane, JOptionPane.INFORMATION_MESSAGE);
      fDialog = option_pane.createDialog(null, "Information");
      if (fDialog.getOwner() instanceof Frame) {
        ((Frame) fDialog.getOwner()).setIconImages(SwingUtil.sLuciadFrameImage);
      }
      fDialog.setLocation(0, 0);
      fDialog.setModal(false);
      fDialog.setResizable(true);
      fDialog.pack();
      fDialog.setVisible(true);
    }
  }
}
