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
package samples.lightspeed.decoder;

import java.io.IOException;

import javax.swing.JPopupMenu;

import com.luciad.gui.ILcdAction;
import com.luciad.gui.TLcdUndoManager;
import com.luciad.gui.swing.TLcdSWAction;
import com.luciad.imaging.ALcdImage;
import com.luciad.imaging.operator.ALcdImageOperatorChain;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.action.TLspSetControllerAction;
import com.luciad.view.lightspeed.controller.ILspController;
import com.luciad.view.lightspeed.layer.ILspEditableStyledLayer;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.TLspPaintRepresentationState;

import samples.common.action.LspSaveAction;
import samples.common.dimensionalfilter.LayerDimensionalFilterCustomizer;
import samples.common.dimensionalfilter.model.DimensionalFilterProvider;
import samples.common.formatsupport.OpenAction;
import samples.common.formatsupport.OpenURLAction;
import samples.common.gui.PopupMenuButton;
import samples.common.serviceregistry.ServiceRegistry;
import samples.gxy.decoder.raster.multispectral.BandSelectLayeredListener;
import samples.lightspeed.common.LightspeedSample;
import samples.lightspeed.common.LspOpenSupport;
import samples.lightspeed.common.ToolBar;
import samples.lightspeed.common.controller.ControllerFactory;
import samples.lightspeed.fundamentals.step1.Main;
import samples.lightspeed.imaging.multispectral.MultispectralOperatorStyler;

/**
 * This sample demonstrates the ability to load data from sources in
 * different formats using {@link LspOpenSupport}.
 * The support class allows you to load data in almost all LuciadLightspeed supported formats.
 * <br/>
 * To accomplish this, a composite layer factory and composite model decoder are populated
 * by making use of a service registry.
 * <p/>
 * For a step-by-step explanation of how to load and visualize models in a view, refer to the {@link Main fundamentals samples}
 * and the developer's guide.
 *
 * @see ServiceRegistry
 */
public class MainPanel extends LightspeedSample {

  private LspOpenSupport fOpenSupport;
  private BandSelectLayeredListener<ILspView, ILspLayer> fBandSelectLayeredListener;
  private LayerDimensionalFilterCustomizer fLayerDimensionalFilterCustomizer;
  private final String[] fArgs;

  public MainPanel() {
    super();
    fArgs = new String[0];
  }

  public MainPanel(boolean aUseTouchToolBar, boolean aAnimateSideBar) {
    super(aUseTouchToolBar, aAnimateSideBar);
    fArgs = new String[0];
  }

  public MainPanel(final String[] aArgs) {
    super();
    fArgs = aArgs;
  }

  /**
   * Adds open and save buttons to the toolbar.
   */
  @Override
  protected void createGUI() {
    super.createGUI();

    fOpenSupport = new LspOpenSupport(getView());
    fOpenSupport.addStatusListener(getStatusBar());
    ToolBar toolBar = getToolBars()[0];

    // Decodes a model from a local file
    ILcdAction openAction = createOpenAction();
    if (openAction != null) {
      toolBar.addAction(openAction, ToolBar.FILE_GROUP);
    }
    // Decodes a model from a URL
    ILcdAction openURLAction = createOpenURLAction();
    if (openURLAction != null) {
      toolBar.addAction(openURLAction, ToolBar.FILE_GROUP);
    }
    // Saves a model to a file
    ILcdAction saveAction = createSaveAction();
    if (saveAction != null) {
      toolBar.addAction(saveAction, ToolBar.FILE_GROUP);
    }
    createBandSelectListener();

    TLcdUndoManager undoManager = getToolBars()[0].getUndoManager();
    JPopupMenu menu = new JPopupMenu("Visual inspection");
    ILspController fallbackController = getToolBars()[0].getDefaultController();
    menu.add(new TLcdSWAction(new TLspSetControllerAction(getView(), ControllerFactory.createSwipeController(getView(), undoManager, fallbackController))));
    menu.add(new TLcdSWAction(new TLspSetControllerAction(getView(), ControllerFactory.createFlickerController(getView(), fallbackController))));
    menu.add(new TLcdSWAction(new TLspSetControllerAction(getView(), ControllerFactory.createPortholeController(getView(), undoManager, fallbackController))));
    toolBar.add(PopupMenuButton.createButtonWithoutText(menu), 2);

    Iterable<DimensionalFilterProvider> query = ServiceRegistry.getInstance().query(DimensionalFilterProvider.class);
    fLayerDimensionalFilterCustomizer = new LayerDimensionalFilterCustomizer(getView(), getSelectedLayers(), getOverlayPanel(), query);
  }

  protected void createBandSelectListener() {
    // Displays an information message for multi-spectral images
    fBandSelectLayeredListener = new BandSelectLayeredListener<ILspView, ILspLayer>(getView().getHostComponent(), getView(), getSelectedLayers()) {
      @Override
      protected void setBandSelectFilter(ILspLayer aLayer, ALcdImage aImage, ALcdImageOperatorChain aBandSelect) {
        if (aLayer instanceof ILspEditableStyledLayer) {
          ILspEditableStyledLayer layer = (ILspEditableStyledLayer) aLayer;
          MultispectralOperatorStyler filterStyler = new MultispectralOperatorStyler(true);
          filterStyler.setImageOperatorChain(aBandSelect);
          layer.setStyler(TLspPaintRepresentationState.REGULAR_BODY, filterStyler);
        }
      }
    };
  }

  /**
   * Creates an action that is capable of open a file. If this
   * method returns null, no open action is added to the toolbar.
   * @return An action capable op opening files using a file
   *         chooser; or null
   */
  protected ILcdAction createOpenAction() {
    return new OpenAction(fOpenSupport);
  }

  /**
   * Creates an action that is capable of open a URL. If this
   * method returns null, no open URL action is added to the toolbar.
   * @return An action capable op opening URLs; or null
   */
  protected ILcdAction createOpenURLAction() {
    return new OpenURLAction(fOpenSupport);
  }

  /**
   * Creates an action that is capable of saving the currently selected layer. If this
   * method returns null, no save action is added to the toolbar.
   * @return An action capable of saving selected layers as files; or null
   */
  protected ILcdAction createSaveAction() {
    return new LspSaveAction(getView());
  }

  @Override
  protected void addData() throws IOException {
    super.addData();
    for (String arg : fArgs) {
      openSourceOnMap(arg);
    }
  }

  /**
   * Open data on the map, using the registered model decoders and layer factories. This is
   * equivalent to using the Open action through the UI.
   * <p>
   * This method is typically used in the {@link #addData()} method to open source files.
   * <p>
   * For a step-by-step explanation of how to load and visualize models in a view, refer to the {@link samples.lightspeed.fundamentals.step1.Main fundamentals samples}
   * and the developer's guide.
   *
   * @param aSourceName The source name of the file to open
   */
  protected void openSourceOnMap(String aSourceName) {
    fOpenSupport.openSource(aSourceName, null);
  }

  @Override
  protected void tearDown() {
    if (fBandSelectLayeredListener != null) {
      fBandSelectLayeredListener.dispose();
    }
    fLayerDimensionalFilterCustomizer.dispose();
    super.tearDown();
  }

  public static void main(final String[] aArgs) {
    startSample(MainPanel.class, aArgs, "Lightspeed Data Viewer");
  }
}
