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
import java.util.ArrayList;
import java.util.Enumeration;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;

import com.luciad.gui.ILcdUndoableListener;
import com.luciad.gui.swing.TLcdOverlayLayout;
import com.luciad.gui.swing.TLcdSWAction;
import com.luciad.util.ILcdSelectionListener;
import com.luciad.util.TLcdSelectionChangedEvent;
import com.luciad.view.lightspeed.ILspAWTView;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.action.TLspDeleteSelectionAction;
import com.luciad.view.lightspeed.controller.touch.TLspTouchSelectEditController;
import com.luciad.view.lightspeed.layer.ILspInteractivePaintableLayer;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.TLspDomainObjectContext;
import com.luciad.view.lightspeed.layer.TLspPaintRepresentationState;

import samples.gxy.common.touch.TouchUtil;

/**
 * Touch enabled edit controller with a button to delete selected shapes.
 */
public class TouchEditController extends TLspTouchSelectEditController implements ILcdSelectionListener {

  private Container fContainer;
  private JPanel fButtonPanel;
  private boolean fAdded = false;
  private JButton fDeleteButton;
  private TLspDeleteSelectionAction fDeleteSelectionAction;
  private ILcdUndoableListener fUndoManager;

  public TouchEditController(ILcdUndoableListener aUndoManager) {
    fUndoManager = aUndoManager;
    addUndoableListener(fUndoManager);

    fButtonPanel = new JPanel();
    fButtonPanel.setLayout(new GridLayout(1, 1));

    fDeleteButton = new JButton("Delete");

    fButtonPanel.add(fDeleteButton);

    TouchUtil.setTouchLookAndFeel(fButtonPanel);
  }

  @Override
  protected void startInteractionImpl(ILspView aView) {
    super.startInteractionImpl(aView);
    fDeleteSelectionAction = new TLspDeleteSelectionAction(aView);
    fDeleteButton.setAction(new TLcdSWAction(fDeleteSelectionAction));
    fDeleteSelectionAction.addUndoableListener(fUndoManager);

    if (aView instanceof ILspAWTView) {
      fContainer = ((ILspAWTView) aView).getOverlayComponent();
      // Be sure to add the "Delete" button if objects were already selected
      selectionChanged(null);
    }
    if (fContainer != null && fContainer.getLayout() instanceof TLcdOverlayLayout) {
      aView.addLayerSelectionListener(this);
    }
  }

  @Override
  public void terminateInteraction(ILspView aView) {
    fDeleteButton.setAction(null);
    if (fContainer != null) {
      aView.removeLayerSelectionListener(this);
      if (fAdded) {
        fContainer.remove(fButtonPanel);
        fAdded = false;
      }
      revalidateContainer();
      fContainer = null;
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

  @Override
  public void selectionChanged(TLcdSelectionChangedEvent aSelectionEvent) {
    boolean hasSelectedObjects = !findSelectedObjects(getView()).isEmpty();
    if (hasSelectedObjects && !fAdded) {
      fAdded = true;
      if (fContainer != null && fContainer.getLayout() instanceof TLcdOverlayLayout) {
        fContainer.add(fButtonPanel, TLcdOverlayLayout.Location.NORTH_WEST);
        revalidateContainer();
      }
    } else if (!hasSelectedObjects && fAdded) {
      fAdded = false;
      if (fContainer != null) {
        fContainer.remove(fButtonPanel);
        revalidateContainer();
      }
    }
  }

  private java.util.List<TLspDomainObjectContext> findSelectedObjects(ILspView aView) {
    java.util.List<TLspDomainObjectContext> selection = new ArrayList<TLspDomainObjectContext>();
    for (Enumeration enum1 = aView.layers(); enum1.hasMoreElements(); ) {
      ILspLayer layer = (ILspLayer) enum1.nextElement();
      if (layer instanceof ILspInteractivePaintableLayer) {
        for (Enumeration enum2 = layer.selectedObjects(); enum2.hasMoreElements(); ) {
          selection.add(new TLspDomainObjectContext(
                            enum2.nextElement(),
                            aView,
                            (ILspInteractivePaintableLayer) layer,
                            TLspPaintRepresentationState.REGULAR_BODY)
          );
        }
      }
    }
    return selection;
  }
}
