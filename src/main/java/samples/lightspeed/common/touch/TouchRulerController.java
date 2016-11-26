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

import java.awt.AWTEvent;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComponent;

import com.luciad.gui.swing.TLcdOverlayLayout;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.controller.ruler.TLspRulerController;

import samples.gxy.touch.editing.TouchNewController;

/**
 * Ruler controller with buttons to finish or cancel creation process.
 */
public class TouchRulerController extends TLspRulerController {

  private Container fContainer;
  private JButton fCommitButton;
  private JButton fCancelButton;
  private boolean fButtonsAdded = false;

  public TouchRulerController(Container aContainer, boolean aAddLayerToView) {
    super(aAddLayerToView);
    fContainer = aContainer;
    fCommitButton = new TouchNewController.MyActionButton("images/gui/touchicons/commit_64.png");
    fCommitButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        finish();
        reevaluateButtons();
      }
    });

    fCancelButton = new TouchNewController.MyActionButton("images/gui/touchicons/cancel_64.png");
    fCancelButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        cancel();
        reevaluateButtons();
      }
    });
    setShortDescription("Ruler: tap to measure distances, press commit or cancel to stop");
  }

  @Override
  public void startInteractionImpl(ILspView aView) {
    super.startInteractionImpl(aView);
    if (isCreating()) {
      addButtons();
    }
  }

  @Override
  public void terminateInteraction(ILspView aView) {
    removeButtons();
    super.terminateInteraction(aView);
  }

  @Override
  public AWTEvent handleAWTEventImpl(AWTEvent aAWTEvent) {
    AWTEvent event = super.handleAWTEventImpl(aAWTEvent);
    reevaluateButtons();
    return event;
  }

  private void reevaluateButtons() {
    if (isCreating()) {
      addButtons();
    } else {
      removeButtons();
    }
  }

  private void addButtons() {
    if (fContainer != null && fContainer.getLayout() instanceof TLcdOverlayLayout && !fButtonsAdded) {
      TLcdOverlayLayout layout = (TLcdOverlayLayout) fContainer.getLayout();
      fContainer.add(fCommitButton, TLcdOverlayLayout.Location.NORTH_WEST);
      fContainer.add(fCancelButton);
      layout.putConstraint(fCancelButton,
                           TLcdOverlayLayout.Location.NORTH_WEST,
                           TLcdOverlayLayout.ResolveClash.VERTICAL);
      revalidateContainer();
      fButtonsAdded = true;
    }
  }

  private void removeButtons() {
    if (fContainer != null && fButtonsAdded) {
      fContainer.remove(fCommitButton);
      fContainer.remove(fCancelButton);
      revalidateContainer();
      fButtonsAdded = false;
    }
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
