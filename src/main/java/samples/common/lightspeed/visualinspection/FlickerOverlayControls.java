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

import java.awt.Component;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.luciad.gui.swing.TLcdOverlayLayout;
import com.luciad.util.ILcdStringTranslator;
import com.luciad.view.lightspeed.ILspAWTView;
import com.luciad.view.lightspeed.controller.ILspController;
import com.luciad.view.lightspeed.controller.visualinspection.TLspFlickerController;
import com.luciad.view.lightspeed.layer.ILspLayer;


/**
 * Property change listener that puts the labels of the layers being flickered in the top left corner of the view.
 *
 * @since 2016.0
 */
class FlickerOverlayControls extends VisualInspectionOverlayControls<TLspFlickerController> {

  private final List<JPanel> fPanels = new ArrayList<JPanel>();

  public FlickerOverlayControls(ILspAWTView aView,
                                TLspFlickerController aFlickerController,
                                ILspController aFallbackController,
                                ILcdStringTranslator aStringTranslator) {
    super(aView, aFlickerController, aFallbackController, "Click anywhere in the view to toggle between the selected layers.", aStringTranslator);
    aFlickerController.addPropertyChangeListener(new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        if ("visibleIndex".equals(evt.getPropertyName())) {
          updatePanel();
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
  public void terminateInteraction() {
    super.terminateInteraction();
    fPanels.clear();
  }

  private void invalidate() {
    JPanel panel = getPanel();
    panel.getLayout().layoutContainer(panel);
    panel.setSize(panel.getLayout().preferredLayoutSize(panel));
    revalidate(getView().getOverlayComponent());
    getView().getOverlayComponent().repaint();
  }

  @Override
  protected void updateContents() {
    fPanels.clear();
    List<Collection<ILspLayer>> layers = getController().getLayers();
    for (Collection<ILspLayer> layers1 : layers) {
      JPanel panel = createBasePanel();

      for (ILspLayer l : layers1) {
        String text = l.getLabel();
        panel.add(createLabel(text, true));
        panel.add(Box.createRigidArea(new Dimension(5, 0)));
      }
      if (layers1.isEmpty()) {
        String text = getStringTranslator().translate("(No layers)");
        panel.add(createLabel(text, true));
        panel.add(Box.createRigidArea(new Dimension(5, 0)));
      }
      fPanels.add(panel);
    }
    updatePanel();
  }

  @Override
  JLabel createLabel(String aText, boolean aTruncateText) {
    JLabel label = super.createLabel(aText, aTruncateText);
    label.setAlignmentX(Component.CENTER_ALIGNMENT);
    return label;
  }

  private void updatePanel() {
    if (getPanel() != null) {
      getView().getOverlayComponent().remove(getPanel());
      setPanel(null);
    }
    if (fPanels.size() > getController().getVisibleIndex()) {
      JPanel panel = fPanels.get(getController().getVisibleIndex());
      setPanel(panel);
      if (isShowLayerSelectionDialog()) {
        panel.add(getEditClosePanel());
      }
      getView().getOverlayComponent().add(panel, TLcdOverlayLayout.Location.NORTH);
      hideTooltip();
      invalidate();
    }
  }

}
