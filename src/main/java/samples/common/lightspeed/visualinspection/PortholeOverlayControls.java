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
package samples.common.lightspeed.visualinspection;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.swing.Box;
import javax.swing.JPanel;

import com.luciad.gui.swing.TLcdOverlayLayout;
import com.luciad.util.ILcdStringTranslator;
import com.luciad.view.lightspeed.ILspAWTView;
import com.luciad.view.lightspeed.controller.ILspController;
import com.luciad.view.lightspeed.controller.visualinspection.TLspPortholeController;
import com.luciad.view.lightspeed.layer.ILspLayer;

/**
 * Can be used to display the labels of the layers being inside and outside the porthole.
 *
 * @since 2016.0
 */
class PortholeOverlayControls extends VisualInspectionOverlayControls<TLspPortholeController> {

  public PortholeOverlayControls(ILspAWTView aView, TLspPortholeController aController, ILspController aFallbackController, ILcdStringTranslator aStringTranslator) {
    super(aView, aController, aFallbackController, "Move the mouse to move the porthole.", aStringTranslator);
    aController.addPropertyChangeListener(new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        if ("portholeSize".equals(evt.getPropertyName())) {
          updateContents();
        } else if ("portholeLocation".equals(evt.getPropertyName())) {
          updateContents();
        }
      }
    });
  }

  @Override
  protected List<Collection<ILspLayer>> getLayers() {
    return getController().getLayers();
  }

  @Override
  protected void setLayers(Set<ILspLayer> aLayers1, Set<ILspLayer> aLayers2) {
    getController().setLayers(aLayers1, aLayers2);
  }

  @Override
  protected String getLeftLayerSetTitle() {
    return "Outside";
  }

  @Override
  protected String getRightLayerSetTitle() {
    return "Inside";
  }

  @Override
  protected void updateContents() {
    if (getPanel() != null) {
      getView().getOverlayComponent().remove(getPanel());
    }
    JPanel mainPanel = new JPanel();
    makeTransparent(mainPanel);
    mainPanel.setLayout(new GridLayout(1, 2, 5, 5));
    List<Collection<ILspLayer>> layers = getController().getLayers();
    for (Collection<ILspLayer> layers1 : layers) {
      JPanel panel = new JPanel(new GridLayout(layers1.size(), 1));
      makeTransparent(panel);
      for (ILspLayer l : layers1) {
        String text = l.getLabel();
        panel.add(createLabel(text, true));
      }
      if (layers1.isEmpty()) {
        String text = getStringTranslator().translate("(No layers)");
        panel.add(createLabel(text, true));
      }
      mainPanel.add(panel);
    }

    JPanel panelWithEditButton = createBasePanel();
    panelWithEditButton.add(mainPanel);
    if (isShowLayerSelectionDialog()) {
      panelWithEditButton.add(Box.createRigidArea(new Dimension(5, 0)));
      panelWithEditButton.add(getEditClosePanel());
    }

    setPanel(panelWithEditButton);
    getView().getOverlayComponent().add(panelWithEditButton, TLcdOverlayLayout.Location.NORTH);
    invalidate();
  }

  private void invalidate() {
    revalidate(getView().getOverlayComponent());
    getView().getOverlayComponent().repaint();
  }


}
