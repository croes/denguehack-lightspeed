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

import static samples.common.lightspeed.visualinspection.LayerComparisonChooser.chooseLayerSets;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.luciad.gui.ILcdIcon;
import com.luciad.gui.TLcdIconFactory;
import com.luciad.gui.swing.TLcdOverlayLayout;
import com.luciad.gui.swing.TLcdSWIcon;
import samples.common.HaloLabel;
import samples.common.SwingUtil;
import com.luciad.util.ILcdStringTranslator;
import com.luciad.view.ILcdLayeredListener;
import com.luciad.view.TLcdLayeredEvent;
import com.luciad.view.lightspeed.ILspAWTView;
import com.luciad.view.lightspeed.controller.ILspController;
import com.luciad.view.lightspeed.controller.visualinspection.TLspFlickerController;
import com.luciad.view.lightspeed.controller.visualinspection.TLspSwipeController;
import com.luciad.view.lightspeed.layer.ILspLayer;

import samples.common.gui.RoundedBorder;

/**
 * Base class for the property change listeners that are used for
 * the visual inspection tools to add a visual indication of which
 * layers are inspected.
 *
 * @see SwipeOverlayControls
 * @see TLspSwipeController
 * @see TLspFlickerController
 * @since 2016.0
 */
abstract class VisualInspectionOverlayControls<T extends ILspController> {

  /**
   * Maximum number of characters allowed for a label shown.
   */
  private static final int MAX_CHARACTERS = 17;

  private static final RoundedBorder ROUNDED_BORDER = new RoundedBorder(new Color(0.3f, 0.3f, 0.3f, 0.5f), 10);

  private final T fController;
  private final MyListener fListener = new MyListener();
  private JPanel fPanel;
  private final List<ILspLayer> fLayers = new ArrayList<>();

  private final ILspController fFallbackController;
  private final ILspAWTView fView;
  private final JPanel fTooltipPanel;

  private ILcdStringTranslator fStringTranslator;

  private boolean fShowLayerSelectionDialog = true;

  public JPanel getEditClosePanel() {
    return fEditClosePanel;
  }

  private JPanel fEditClosePanel;

  protected VisualInspectionOverlayControls(final ILspAWTView aView,
                                            T aController,
                                            ILspController aFallbackController,
                                            String aTooltipText,
                                            ILcdStringTranslator aStringTranslator) {
    fView = aView;
    fController = aController;
    fFallbackController = aFallbackController;
    fTooltipPanel = new JPanel(new BorderLayout());
    fTooltipPanel.setName("tooltip");
    fTooltipPanel.setBorder(ROUNDED_BORDER);
    fTooltipPanel.add(createLabel(aStringTranslator.translate(aTooltipText), false));
    makeTransparent(fTooltipPanel);

    fEditClosePanel = createEditClosePanel();
    fStringTranslator = aStringTranslator;

    fView.addLayeredListener(fListener);
  }

  /**
   * Creates a panel that contains a button to allow changing the layers set on the controller and one to remove the
   * controller from the view.
   */
  private JPanel createEditClosePanel() {
    JButton editLayersButton = createBorderLessButton(TLcdIconFactory.create(TLcdIconFactory.EDIT_ICON, TLcdIconFactory.Theme.WHITE_THEME, TLcdIconFactory.getDefaultSize()));
    editLayersButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        askForLayers();
      }
    });

    JButton closeButton = createBorderLessButton(TLcdIconFactory.create(TLcdIconFactory.CLOSE_ICON, TLcdIconFactory.Theme.WHITE_THEME, TLcdIconFactory.getDefaultSize()));
    closeButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        fView.setController(fFallbackController);
      }
    });

    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
    panel.add(editLayersButton);
    panel.add(Box.createRigidArea(new Dimension(5, 0)));
    panel.add(closeButton);
    makeTransparent(panel);

    return panel;
  }

  private JButton createBorderLessButton(ILcdIcon aIcon) {
    JButton closeButton = new JButton();
    closeButton.setBorderPainted(false);
    closeButton.setContentAreaFilled(false);
    closeButton.setFocusPainted(false);
    closeButton.setOpaque(false);
    closeButton.setIcon(new TLcdSWIcon(aIcon));
    SwingUtil.makeSquare(closeButton);
    return closeButton;
  }

  /**
   * Called by the controller to provide overlay controls for.
   * @return true if valid layers have been selected and the controller can start its interaction
   */
  public boolean startInteraction() {
    if (fShowLayerSelectionDialog) {
      askForLayers();
    }
    if (getLayers().isEmpty()) {
      fView.setController(fFallbackController);
      return false;
    }
    updateContents();
    addLayers(getLayers());
    fController.addPropertyChangeListener(fListener);
    showTooltip();
    return true;
  }

  /**
   * A user-friendly name for the first set of layers involved.
   * When asking the user for input, this set is presented at the left.
   * @return an untranslated display name
   */
  protected String getLeftLayerSetTitle() {
    return "Set 1";
  }

  /**
   * A user-friendly name for the second set of layers involved.
   * When asking the user for input, this set is presented at the right.
   * @return an untranslated display name
   */
  protected String getRightLayerSetTitle() {
    return "Set 2";
  }

  private void askForLayers() {
    String left = getLeftLayerSetTitle();
    String right = getRightLayerSetTitle();
    Map<String, Set<ILspLayer>> stringSetMap = chooseLayerSets(fController.getName(), left, right, fView, getLayers(), fStringTranslator);
    Set<ILspLayer> layers1 = stringSetMap.get(left);
    Set<ILspLayer> layers2 = stringSetMap.get(right);
    if (layers1.size() + layers2.size() >= 1) {
      setLayers(layers1, layers2);
    }
  }

  protected abstract List<Collection<ILspLayer>> getLayers();

  protected abstract void setLayers(Set<ILspLayer> aLayers1, Set<ILspLayer> aLayers2);

  public void terminateInteraction() {
    fController.removePropertyChangeListener(fListener);
    if (fPanel != null) {
      getView().getOverlayComponent().remove(fPanel);
      fPanel = null;
      revalidate(getView().getOverlayComponent());
      getView().getOverlayComponent().repaint();
    }
    for (ILspLayer fLayer : fLayers) {
      fLayer.removePropertyChangeListener(fListener);
    }
    fLayers.clear();
    hideTooltip();
  }

  private void addLayers(List<Collection<ILspLayer>> aLayers) {
    for (int i = 0; i < aLayers.size(); i++) {
      Collection<ILspLayer> layers = aLayers.get(i);
      for (ILspLayer layer : layers) {
        layer.addPropertyChangeListener(fListener);
        fLayers.add(layer);
      }
    }
  }

  /**
   * The view the labels are shown in.
   * @return the view the labels are shown in
   */
  protected ILspAWTView getView() {
    return fView;
  }

  /**
   * The controller for which the layer labels are shown.
   * @return the controller for which the layer labels are shown.
   */
  protected T getController() {
    return fController;
  }

  /**
   * The panel that is currently shown in the view.
   * @return the currently shown panel
   */
  protected JPanel getPanel() {
    return fPanel;
  }

  /**
   * Sets the panel that is currently shown in the view.
   * @param aPanel the currently shown panel
   */
  protected void setPanel(JPanel aPanel) {
    fPanel = aPanel;
  }

  /**
   * Should update and register the panel to the view
   * so that the labels of the currently inspected layers
   * are shown in the view.
   */
  protected abstract void updateContents();

  public void showTooltip() {
    getView().getOverlayComponent().add(fTooltipPanel, TLcdOverlayLayout.Location.CENTER);
    getView().getOverlayComponent().revalidate();
    getView().getOverlayComponent().repaint();
  }

  public void hideTooltip() {
    getView().getOverlayComponent().remove(fTooltipPanel);
    getView().getOverlayComponent().revalidate();
    getView().getOverlayComponent().repaint();
  }

  /**
   * Revalidates the given component and its parents.
   *
   * @param aComponent the component to revalidate
   */
  void revalidate(Component aComponent) {
    synchronized (aComponent.getTreeLock()) {
      aComponent.invalidate();

      Container root = aComponent.getParent();
      if (root == null) {
        // There's no parents. Just validate itself.
        aComponent.validate();
      } else {
        while (!(root instanceof JComponent) || !root.isValidateRoot()) {
          if (root.getParent() == null) {
            // If there's no validate roots, we'll validate the
            // topmost container
            break;
          }

          root = root.getParent();
        }

        root.validate();
      }
    }
  }

  /**
   * Makes the component's background transparent.
   *
   * @param aComponent the component
   */
  void makeTransparent(JComponent aComponent) {
    aComponent.setBackground(new Color(0f, 0f, 0f, 0f));
    aComponent.setOpaque(false);
  }

  protected JPanel createBasePanel() {
    JPanel panelWithEditButton = new JPanel();
    panelWithEditButton.setLayout(new BoxLayout(panelWithEditButton, BoxLayout.X_AXIS));
    panelWithEditButton.setBorder(ROUNDED_BORDER);
    makeTransparent(panelWithEditButton);
    return panelWithEditButton;
  }

  /**
   * Creates a styled label for the given text with at most
   * MAX_CHARACTERS+3 (for the dots) characters.
   *
   * @param aText the text of which the first part (up to MAX_CHARACTERS) should be shown
   * @param aTruncateText determines if the text should be truncated
   * @return the label containing the text
   */
  JLabel createLabel(String aText, boolean aTruncateText) {
    if (aText.length() > MAX_CHARACTERS && aTruncateText) {
      aText = aText.substring(0, MAX_CHARACTERS) + "...";
    }
    HaloLabel label = new HaloLabel(aText);
    label.setTextColor(new Color(1f, 1f, 1f, 0.9f));
    label.setHaloColor(new Color(0.3f, 0.3f, 0.3f, 0.7f));
    makeTransparent(label);
    return label;
  }

  public boolean isShowLayerSelectionDialog() {
    return fShowLayerSelectionDialog;
  }

  public ILcdStringTranslator getStringTranslator() {
    return fStringTranslator;
  }

  public void setShowLayerSelectionDialog(boolean aShowLayerSelectionDialog) {
    fShowLayerSelectionDialog = aShowLayerSelectionDialog;
  }

  private class MyListener implements PropertyChangeListener, ILcdLayeredListener  {
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
      if (evt != null && ("layers".equals(evt.getPropertyName()) || "label".equals(evt.getPropertyName()))
          && getView().getController() == fController) {
        updateContents();
      }
    }

    @Override
    public void layeredStateChanged(TLcdLayeredEvent e) {
      if (e.getID() == TLcdLayeredEvent.LAYER_REMOVED && fLayers.contains(e.getLayer())
          && getView().getController() == fController) {
        EventQueue.invokeLater(new Runnable() {
          @Override
          public void run() {
            getView().setController(fFallbackController);
          }
        });
      }
    }
  }
}
