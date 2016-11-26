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
package samples.lightspeed.common.touch;

import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;

import com.luciad.gui.swing.TLcdOverlayLayout;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.controller.manipulation.ALspCreateControllerModel;
import com.luciad.view.lightspeed.controller.touch.TLspTouchCreateController;

import samples.gxy.common.touch.TouchUtil;

/**
 * Touch enabled create controller with buttons to finish or cancel creation process.
 */
public class TouchCreateController extends TLspTouchCreateController {

  private Container fContainer;
  private JPanel fButtonPanel;

  public TouchCreateController(Container aContainer, ALspCreateControllerModel aCreateControllerModel, boolean aWithCommitButton) {
    super(aCreateControllerModel);
    fContainer = aContainer;
    fButtonPanel = new JPanel();
    fButtonPanel.setLayout(new GridLayout(1, aWithCommitButton ? 2 : 1));

    JButton cancelButton = new JButton("Cancel");
    cancelButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        cancel();
      }
    });
    fButtonPanel.add(cancelButton);

    if (aWithCommitButton) {
      JButton commitButton = new JButton("Commit");
      commitButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          finish();
        }
      });
      fButtonPanel.add(commitButton);
    }

    TouchUtil.setTouchLookAndFeel(fButtonPanel);
  }

  @Override
  protected void startInteractionImpl(ILspView aView) {
    super.startInteractionImpl(aView);
    if (fContainer != null && fContainer.getLayout() instanceof TLcdOverlayLayout) {
      fContainer.add(fButtonPanel, TLcdOverlayLayout.Location.NORTH_WEST);
      revalidateContainer();
    }
  }

  @Override
  public void terminateInteraction(ILspView aView) {
    if (fContainer != null) {
      fContainer.remove(fButtonPanel);
      revalidateContainer();
    }
    super.terminateInteraction(aView);
  }

  private void revalidateContainer() {
    if (fContainer instanceof JComponent) {
      ((JComponent) fContainer).revalidate();
    } else {
      fContainer.invalidate();
      fContainer.validate();
    }
    fContainer.repaint();
  }
}
