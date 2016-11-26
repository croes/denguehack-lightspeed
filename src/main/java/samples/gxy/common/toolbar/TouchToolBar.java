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

import java.awt.Component;
import java.awt.Container;

import javax.swing.Box;

import com.luciad.gui.ILcdAction;
import com.luciad.gui.ILcdIcon;
import com.luciad.gui.TLcdCompositeIcon;
import com.luciad.gui.TLcdIconFactory;
import com.luciad.gui.TLcdImageIcon;
import com.luciad.gui.TLcdResizeableIcon;
import com.luciad.gui.TLcdTranslatedIcon;
import com.luciad.input.ILcdAWTEventListener;
import com.luciad.view.TLcdDeleteSelectionAction;
import com.luciad.view.gxy.ILcdGXYController;
import com.luciad.view.gxy.ILcdGXYView;
import com.luciad.view.gxy.controller.ILcdGXYChainableController;
import com.luciad.view.gxy.controller.TLcdGXYNavigateControllerModel;
import com.luciad.view.gxy.controller.touch.TLcdGXYTouchNavigateController;
import com.luciad.view.gxy.controller.touch.TLcdGXYTouchSelectEditController;
import com.luciad.view.gxy.controller.touch.TLcdMapTouchRulerController;

import samples.common.action.ShowPopupAction;
import samples.common.action.ShowPropertiesAction;
import samples.common.action.ShowReadMeAction;
import samples.gxy.common.controller.SnappablesSubsetList;
import samples.gxy.common.touch.MouseToTouchGXYControllerWrapper;
import samples.gxy.decoder.MapSupport;
import samples.gxy.touch.editing.TouchNewController;
import samples.gxy.touch.editing.TouchSelectEditController;

/**
 * AToolBar with default touch controllers and a button to open a readme panel.
 */
public class TouchToolBar extends AToolBar {

  private boolean fTouchSupported;

  // default controllers
  private TLcdGXYTouchSelectEditController fEditController;
  private ILcdGXYChainableController fNavigateController;
  private TLcdMapTouchRulerController fRulerController;

  // the domain objects to which the controllers can snap
  private SnappablesSubsetList fSnappables;

  public TouchToolBar(ILcdGXYView aGXYView, boolean aIncludeReadme, boolean aTouchSupported, Component aParent,
                      Container aContainer) {
    this(aGXYView, aIncludeReadme, aTouchSupported, aParent, aContainer, createEditController(aContainer, true));
  }

  public TouchToolBar(ILcdGXYView aGXYView, boolean aIncludeReadme, boolean aTouchSupported, Component aParent,
                      Container aContainer, boolean aEditingEnabled) {
    this(aGXYView, aIncludeReadme, aTouchSupported, aParent, aContainer,
         createEditController(aContainer, aEditingEnabled));
  }

  public TouchToolBar(ILcdGXYView aGXYView, boolean aIncludeReadme, boolean aTouchSupported, Component aParent,
                      Container aContainer, TLcdGXYTouchSelectEditController aEditController) {
    super(aGXYView);
    fSnappables = new SnappablesSubsetList(aGXYView);
    fTouchSupported = aTouchSupported;

    // Set up the touch controllers.
    fEditController = aEditController;
    fNavigateController = createNavigateController();
    fRulerController = new TLcdMapTouchRulerController(new TouchNewController(null, aContainer),
                                                       new TouchSelectEditController(aContainer, 1, true));
    TLcdCompositeIcon ruler_icon = new TLcdCompositeIcon();
    ruler_icon.addIcon(new TLcdResizeableIcon(TLcdIconFactory.create(TLcdIconFactory.MEASURE_ICON)));
    ruler_icon.addIcon(
        new TLcdResizeableIcon(new TLcdTranslatedIcon(TLcdIconFactory.create(TLcdIconFactory.TOUCH_SELECT_ICON, TLcdIconFactory.getDefaultTheme(), TLcdIconFactory.Size.SIZE_16), 19, 0)));
    fRulerController.setIcon(ruler_icon);

    //append a navigation controller to the ruler controller
    fRulerController.setNextGXYController(new TLcdGXYTouchNavigateController());

    // configure a context popup menu
    ILcdAction[] popupActions = {
        new ShowPropertiesAction(aGXYView, ToolBar.getParentComponent(aGXYView)),
        new TLcdDeleteSelectionAction(aGXYView)
    };
    ShowPopupAction show_popup_action
        = new ShowPopupAction(popupActions, (Component) aGXYView);
    ILcdIcon touchAndHoldIcon = new TLcdImageIcon("images/gui/touchicons/pressandhold_80.png");
    fEditController.setTouchAndHoldIcon(touchAndHoldIcon);
    fEditController.setPostTouchAndHoldAction(show_popup_action);

    // let the edit and map rules controller share the snap targets
    fEditController.setSnappables(fSnappables);
    fRulerController.setSnappables(fSnappables);

    // Set nicer hot point icon
    fRulerController.setHotPointIcon(MapSupport.sHotPointIcon);

    addGXYController(composeEditController());
    addGXYController(fNavigateController);
    addGXYController(fRulerController);
    addSpace();
    addComponent(Box.createHorizontalGlue(), -1);

    // Display an info panel when running standalone.
    if (aIncludeReadme) {
      // Add action to display information on how to use this sample
      ShowReadMeAction showReadme = ShowReadMeAction.createForSample(aParent);
      if (showReadme != null) {
        addAction(showReadme, -1);
        ShowReadMeAction.showAtStartup(showReadme);
      }
    }

    // Set our edit controller as the initial ILcdGXYController
    aGXYView.setGXYController(getGXYController(getEditController()));
  }

  private static TLcdGXYTouchSelectEditController createEditController(Container aContainer, boolean aEditingEnabled) {
    return new TouchSelectEditController(aContainer, 1, aEditingEnabled);
  }

  private ILcdGXYChainableController composeEditController() {
    fEditController.appendGXYController(new TLcdGXYTouchNavigateController(new TLcdGXYNavigateControllerModel(), false, true, true));

    TLcdCompositeIcon icon = new TLcdCompositeIcon();
    icon.addIcon(new TLcdResizeableIcon(TLcdIconFactory.create(TLcdIconFactory.ARROW_ICON)));
    icon.addIcon(new TLcdResizeableIcon(new TLcdTranslatedIcon(TLcdIconFactory.create(TLcdIconFactory.TOUCH_SELECT_ICON, TLcdIconFactory.getDefaultTheme(), TLcdIconFactory.Size.SIZE_16), 19, 0)));

    fEditController.setIcon(icon);
    return fEditController;
  }

  private ILcdGXYChainableController createNavigateController() {
    TLcdGXYTouchNavigateController controller = new TLcdGXYTouchNavigateController();

    TLcdCompositeIcon icon = new TLcdCompositeIcon();
    icon.addIcon(new TLcdResizeableIcon(TLcdIconFactory.create(TLcdIconFactory.HAND_ICON)));
    icon.addIcon(new TLcdResizeableIcon(new TLcdTranslatedIcon(TLcdIconFactory.create(TLcdIconFactory.TOUCH_SELECT_ICON, TLcdIconFactory.getDefaultTheme(), TLcdIconFactory.Size.SIZE_16), 19, 0)));

    controller.setIcon(icon);
    return controller;
  }

  @Override
  protected ILcdGXYController wrapController(ILcdGXYController aController) {
    // Adds fall-back mouse support.
    return fTouchSupported || !(aController instanceof ILcdAWTEventListener) ?
           aController :
           new MouseToTouchGXYControllerWrapper(aController);
  }

  public ILcdGXYController getWrappedController(ILcdGXYController aController) {
    return getGXYController(aController);
  }

  public TLcdGXYTouchSelectEditController getEditController() {
    return fEditController;
  }

  public TLcdMapTouchRulerController getRulerController() {
    return fRulerController;
  }

  public ILcdGXYChainableController getNavigateController() {
    return fNavigateController;
  }

  public SnappablesSubsetList getSnappables() {
    return fSnappables;
  }
}
