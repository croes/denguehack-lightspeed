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
import java.awt.Point;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.luciad.gui.TLcdImageIcon;
import com.luciad.gui.swing.TLcdOverlayLayout;
import com.luciad.gui.swing.TLcdSWIcon;
import com.luciad.util.ILcdStringTranslator;
import com.luciad.view.lightspeed.ILspAWTView;
import com.luciad.view.lightspeed.controller.ILspController;
import com.luciad.view.lightspeed.controller.visualinspection.TLspSwipeController;
import com.luciad.view.lightspeed.layer.ILspLayer;

/**
 * Property change listener that puts the labels of the layers being swiped next to the swipe line.
 *
 * @since 2016.0
 */
class SwipeOverlayControls extends VisualInspectionOverlayControls<TLspSwipeController> {

  private final JLabel fIconLabel;
  private final Dimension fIconSize;

  private Dimension fSize;
  private Dimension fTotalSize;

  public SwipeOverlayControls(ILspAWTView aView, TLspSwipeController aSwipeController, ILspController aFallbackController, ILcdStringTranslator aStringTranslator) {
    super(aView, aSwipeController, aFallbackController, "Left drag the swipe line horizontally or vertically.", aStringTranslator);
    final Icon icon = new TLcdSWIcon(new TLcdImageIcon("samples/images/4waydrag.png"));
    final Icon focusedIcon = new TLcdSWIcon(new TLcdImageIcon("samples/images/4waydrag_focused.png"));
    fIconSize = new Dimension(icon.getIconWidth(), icon.getIconHeight());
    fIconLabel = new JLabel(icon);
    fIconLabel.setSize(fIconSize);
    aSwipeController.addPropertyChangeListener(new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        if ("swipeLineOrientation".equals(evt.getPropertyName())) {
          updateContents();
        } else if ("swipeLineLocation".equals(evt.getPropertyName())) {
          updateLocation();
        } else if ("focused".equals(evt.getPropertyName())) {
          fIconLabel.setIcon((Boolean)evt.getNewValue() ? focusedIcon : icon);
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
    return "Left (top)";
  }

  @Override
  protected String getRightLayerSetTitle() {
    return "Right (bottom)";
  }

  @Override
  public boolean startInteraction() {
    boolean interactionStarted = super.startInteraction();

    if (interactionStarted) {
      getView().getOverlayComponent().add(fIconLabel, TLcdOverlayLayout.Location.NO_LAYOUT);
    }

    return interactionStarted;
  }

  @Override
  public void terminateInteraction() {
    getView().getOverlayComponent().remove(fIconLabel);
    super.terminateInteraction();
  }

  @Override
  protected void updateContents() {
    if (getPanel() != null) {
      getView().getOverlayComponent().remove(getPanel());
    }
    JPanel mainPanel = new JPanel();
    makeTransparent(mainPanel);
    boolean verticalLine = getController().getSwipeLineOrientation() == TLspSwipeController.SwipeLineOrientation.VERTICAL;
    mainPanel.setLayout(new GridLayout(verticalLine ? 1 : 2, verticalLine ? 2 : 1, 5, 5));
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

    fSize = mainPanel.getLayout().preferredLayoutSize(mainPanel);

    JPanel panelWithEditButton = createBasePanel();
    panelWithEditButton.add(mainPanel);
    if (isShowLayerSelectionDialog()) {
      panelWithEditButton.add(Box.createRigidArea(new Dimension(5, 0)));
      panelWithEditButton.add(getEditClosePanel());
    }
    fTotalSize = panelWithEditButton.getLayout().preferredLayoutSize(panelWithEditButton);

    setPanel(panelWithEditButton);

    getView().getOverlayComponent().add(panelWithEditButton, TLcdOverlayLayout.Location.NO_LAYOUT);

    updateLocation();
    invalidate();
  }

  private void updateLocation() {
    hideTooltip();
    JPanel panel = getPanel();
    boolean verticalLine = getController().getSwipeLineOrientation() == TLspSwipeController.SwipeLineOrientation.VERTICAL;
    Point swipeLineLocation = getController().getSwipeLineLocation();
    panel.getLayout().layoutContainer(panel);
    panel.setSize(panel.getLayout().preferredLayoutSize(panel));

    // Convert between view size and overlay component size
    // For low-resolution views these sizes do not necessarily correspond
    double width = getView().getWidth();
    double height = getView().getHeight();

    double panelWidth = getView().getOverlayComponent().getWidth();
    double panelHeight = getView().getOverlayComponent().getHeight();

    panel.setLocation((int) (verticalLine ? (swipeLineLocation.getX()*panelWidth/width - (fSize.getWidth() / 2)) : 5),
                      (int) ((verticalLine ? 5 : swipeLineLocation.getY()*panelHeight/height - fSize.getHeight() / 2)));
    fIconLabel.setLocation((int) (verticalLine ? (swipeLineLocation.getX()*panelWidth/width - (fIconSize.getWidth() / 2)) : 2*5 + fTotalSize.getWidth() ),
                           (int) ((verticalLine ? 2*5 + fTotalSize.getHeight() : swipeLineLocation.getY()*panelHeight/height - fIconSize.getHeight() / 2)));
  }

  private void invalidate() {
    revalidate(getView().getOverlayComponent());
    getView().getOverlayComponent().repaint();
  }

}
