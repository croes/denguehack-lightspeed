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
package samples.lucy.frontend.mapcentric.status;

import java.awt.Color;

import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.OverlayLayout;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.luciad.gui.ILcdAction;
import com.luciad.gui.TLcdSymbol;
import samples.common.gui.PopupPanelButton;
import samples.lucy.gui.status.ShowProgressAction;
import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.lucy.gui.TLcyToolBar;

/**
 * Extension of {@link ShowProgressAction} which uses a circular progress bar.
 */
final class ProgressIndicationAction extends ShowProgressAction {
  ProgressIndicationAction(ILcyLucyEnv aLucyEnv) {
    super(aLucyEnv);
  }

  @Override
  protected JProgressBar createMainProgressBar() {
    JProgressBar progressBar = new JProgressBar() {
      @Override
      public void updateUI() {
        super.updateUI();
        setUI(new ProgressCircleUI());
      }
    };
    progressBar.setBorderPainted(false);
    progressBar.setStringPainted(false);
    return progressBar;
  }

  @Override
  protected JComponent createMainProgressBarComponent(final JProgressBar aMainProgressBar, TLcyToolBar aInterruptActionToolBar, PopupPanelButton aManyTasksButton) {
    JLayeredPane layered = new JLayeredPane();
    JPanel centeringPanel = new JPanel(new FormLayout("center:pref:grow", "center:pref:grow"));
    centeringPanel.setOpaque(false);
    centeringPanel.add(aInterruptActionToolBar.getComponent(), new CellConstraints(1, 1));
    layered.add(centeringPanel, new Integer(0));

    JPanel popupButtonPanel = new JPanel(new FormLayout("center:pref:grow", "center:pref:grow"));
    popupButtonPanel.setOpaque(false);
    popupButtonPanel.add(wrapWithToolBar(aManyTasksButton), new CellConstraints(1, 1));
    layered.add(popupButtonPanel, new Integer(1));

    // Overlay the progress bar on the buttons (transparently). The borders of the interrupt and multi-task buttons
    // (when hovering over them) may otherwise cause artifacts.
    layered.setLayout(new OverlayLayout(layered));
    layered.add(aMainProgressBar, new Integer(2));
    aMainProgressBar.setOpaque(false);

    // Overlap center points of all components
    aMainProgressBar.setAlignmentX(0.5f);
    aMainProgressBar.setAlignmentY(0.5f);
    aInterruptActionToolBar.getComponent().setAlignmentX(0.5f);
    aInterruptActionToolBar.getComponent().setAlignmentY(0.5f);
    aManyTasksButton.setAlignmentX(0.5f);
    aManyTasksButton.setAlignmentY(0.5f);

    return layered;
  }

  @Override
  protected void customizeInterruptAction(ILcdAction aAction) {
    aAction.putValue(ILcdAction.SMALL_ICON, new TLcdSymbol(TLcdSymbol.CROSS, 8, Color.white));
  }
}
