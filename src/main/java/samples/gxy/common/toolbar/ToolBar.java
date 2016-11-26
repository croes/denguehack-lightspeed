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
package samples.gxy.common.toolbar;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Rectangle;

import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JLabel;

import com.luciad.gui.ILcdAction;
import com.luciad.gui.TLcdUndoManager;
import com.luciad.gui.swing.TLcdOverlayLayout;
import com.luciad.view.TLcdDeleteSelectionAction;
import com.luciad.view.gxy.ILcdGXYLayerSubsetList;
import com.luciad.view.gxy.ILcdGXYView;
import com.luciad.view.gxy.TLcdGXYPainterColorStyle;
import com.luciad.view.gxy.controller.ALcdGXYController;
import com.luciad.view.gxy.controller.TLcdGXYCompositeController;
import com.luciad.view.gxy.controller.TLcdGXYEditController2;
import com.luciad.view.gxy.controller.TLcdGXYSelectControllerModel2;
import com.luciad.view.map.TLcdAdvancedMapRulerController;

import samples.common.action.DialogSelectionHandler;
import samples.common.action.ShowPopupAction;
import samples.common.action.ShowPropertiesAction;
import samples.common.action.ShowReadMeAction;
import samples.gxy.common.MouseLocationComponent;
import samples.gxy.common.controller.RulerControllerPanel;
import samples.gxy.common.controller.SnappablesSubsetList;
import samples.gxy.decoder.MapSupport;

/**
 * AToolBar with default controllers, a mouse coordinate panel and a button to open a readme panel.
 */
public class ToolBar extends AToolBar {

  // controllers
  private TLcdGXYEditController2 fGXYControllerEdit;
  private TLcdAdvancedMapRulerController fAdvancedRulerController;

  // the domain objects to which the controllers can snap
  private final SnappablesSubsetList fSnappables;
  // the undo manager that can be used for undo-redo actions
  private TLcdUndoManager fUndoManager = new TLcdUndoManager();

  public ToolBar(ILcdGXYView aGXYView, boolean aIncludeReadme, Component aParent) {
    this(aGXYView, aIncludeReadme, aParent, new TLcdGXYEditController2(), null);
  }

  public ToolBar(ILcdGXYView aGXYView, boolean aIncludeReadme, Component aParent, JComponent aOverlayPanel) {
    this(aGXYView, aIncludeReadme, aParent, new TLcdGXYEditController2(), aOverlayPanel);
  }

  public ToolBar(ILcdGXYView aGXYView, boolean aIncludeReadme, Component aParent, TLcdGXYEditController2 aEditController) {
    this(aGXYView, aIncludeReadme, aParent, aEditController, null);
  }

  public ToolBar(ILcdGXYView aGXYView, boolean aIncludeReadme, Component aParent, TLcdGXYEditController2 aEditController, JComponent aOverlayPanel) {
    super(aGXYView);

    fSnappables = new SnappablesSubsetList(aGXYView);

    // Left side buttons.
    configureEditController(aEditController);
    configureRulerController(aOverlayPanel);
    addGXYController(fGXYControllerEdit);
    addGXYController(fAdvancedRulerController);
    addSpace();

    // Right side content.
    addComponent(Box.createHorizontalGlue(), -1);

    Component locationComponent = createMouseLocationComponent();
    if (locationComponent != null) {
      addComponent(locationComponent, -1);
      addSpace(-1);
    }
    addReadmeAction(aIncludeReadme, aParent);

    // Activate the default controller.
    aGXYView.setGXYController(getGXYController(fGXYControllerEdit));
  }

  protected ILcdAction[] createEditActions() {
    ShowPropertiesAction showPropertiesAction = new ShowPropertiesAction(getGXYView(), ToolBar.getParentComponent(getGXYView()));
    ILcdAction deleteSelectionAction = new TLcdDeleteSelectionAction(getGXYView());
    return new ILcdAction[]{showPropertiesAction, deleteSelectionAction};
  }

  private void configureEditController(TLcdGXYEditController2 aEditController) {
    fGXYControllerEdit = aEditController;
    fGXYControllerEdit.addUndoableListener(fUndoManager);
    fGXYControllerEdit.setSelectControllerModel(new SelectControllerModelWithDialog());
    // Context menu actions.
    ILcdAction[] popupActions = createEditActions();
    ShowPopupAction showPopupAction = new ShowPopupAction(popupActions, (Component) getGXYView());
    fGXYControllerEdit.setRightClickAction(showPopupAction);
    fGXYControllerEdit.setDoubleClickAction(new ZoomToShowPropertiesAction(getGXYView(), (ShowPropertiesAction) popupActions[0]));

    // Let the edit and map rules controller share the snap targets
    fGXYControllerEdit.setSnappables(fSnappables);
  }

  private void configureRulerController(final JComponent aOverlayPanel) {
    // Add a ruler control panel.
    fAdvancedRulerController = new TLcdAdvancedMapRulerController() {

      RulerControllerPanel panel;

      @Override
      public void startInteraction(ILcdGXYView aGXYView) {
        super.startInteraction(aGXYView);
        if (aOverlayPanel != null) {
          panel = new RulerControllerPanel(this);
          aOverlayPanel.add(panel, TLcdOverlayLayout.Location.NORTH);
          aOverlayPanel.validate();
        }
      }

      @Override
      public void terminateInteraction(ILcdGXYView aGXYView) {
        super.terminateInteraction(aGXYView);
        if (aOverlayPanel != null) {
          aOverlayPanel.remove(panel);
          aOverlayPanel.validate();
        }
        panel = null;
      }
    };
    // Allow snapping.
    fAdvancedRulerController.setSnappables(fSnappables);

    // Add a nice hot point icon
    fAdvancedRulerController.setHotPointIcon(MapSupport.sHotPointIcon);

    fAdvancedRulerController.setLineStyle(new TLcdGXYPainterColorStyle(Color.WHITE));
    fAdvancedRulerController.setHaloEnabled(true);
    fAdvancedRulerController.setHaloThickness(1);
    fAdvancedRulerController.setHaloColor(Color.blue);
    fAdvancedRulerController.setDisplayAzimuth(true);
    fAdvancedRulerController.setLabelVGap(4);

    // change the cursor on the advanced map ruler controller, using a crosshair cursor
    // to designate that you have to choose a location.
    ((ALcdGXYController) fAdvancedRulerController.getGXYEditController()).setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
    ((ALcdGXYController) fAdvancedRulerController.getGXYNewController()).setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
  }

  private void addReadmeAction(boolean aIncludeReadme, Component aParent) {
    if (aIncludeReadme) {
      // Add action to display information on how to use this sample
      ShowReadMeAction showReadme = ShowReadMeAction.createForSample(aParent);
      if (showReadme != null) {
        addAction(showReadme, -1);
        ShowReadMeAction.showAtStartup(showReadme);
      }
    }
  }

  protected Component createMouseLocationComponent() {
    MouseLocationComponent locationComponent = new MouseLocationComponent(getGXYView(), null, null);
    locationComponent.setColor(new JLabel().getForeground());
    locationComponent.setHaloColor(null);
    locationComponent.setFont(new JLabel().getFont());
    locationComponent.setShowValues(false);
    return locationComponent;
  }

  public SnappablesSubsetList getSnappables() {
    return fSnappables;
  }

  public TLcdUndoManager getUndoManager() {
    return fUndoManager;
  }

  public void setUndoManager(TLcdUndoManager aUndoManager) {
    fUndoManager = aUndoManager;
  }

  public TLcdGXYEditController2 getGXYControllerEdit() {
    return fGXYControllerEdit;
  }

  public void setGXYControllerEdit(TLcdGXYEditController2 aGXYControllerEdit) {
    removeGXYController(fGXYControllerEdit);
    configureEditController(aGXYControllerEdit);
    fGXYControllerEdit = aGXYControllerEdit;
    addGXYController(aGXYControllerEdit, 0);
  }

  /**
   * Convenience method to retrieve the composite edit controller.
   * @return the composite edit controller
   */
  public TLcdGXYCompositeController getGXYCompositeEditController() {
    return (TLcdGXYCompositeController) getGXYController(getGXYControllerEdit());
  }

  public TLcdAdvancedMapRulerController getAdvancedRulerController() {
    return fAdvancedRulerController;
  }

  public static Component getParentComponent(ILcdGXYView aGXYView) {
    return aGXYView instanceof Component ? (Component) aGXYView : null;
  }

  public static class SelectControllerModelWithDialog extends TLcdGXYSelectControllerModel2 {
    // Show a nice selection dialog if the user presses ALT.
    @Override
    public void applySelection(ILcdGXYView aGXYView, Rectangle aSelectionBounds, int aInputMode, int aX, int aY, int aSelectByWhatMode, int aSelectHowMode, ILcdGXYLayerSubsetList aSelectionCandidates) {
      if ((aSelectHowMode & SELECT_HOW_CHOOSE) != 0) {
        DialogSelectionHandler.handleSelectionCandidates((Component) aGXYView, aSelectionCandidates);
      } else {
        super.applySelection(aGXYView, aSelectionBounds, aInputMode, aX, aY, aSelectByWhatMode, aSelectHowMode, aSelectionCandidates);
      }
    }
  }
}
