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
package samples.decoder.asterix.lightspeed.trackdisplay;

import java.awt.Color;
import java.awt.Component;
import java.io.IOException;

import javax.swing.JToolBar;

import com.luciad.gui.ILcdDialogManager;
import com.luciad.gui.TLcdImageIcon;
import com.luciad.gui.TLcdUserDialog;
import com.luciad.model.ILcdModel;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.shape.shape2D.TLcdLonLatBounds;
import com.luciad.util.ILcdFilter;
import com.luciad.view.TLcdAWTEventFilterBuilder;
import com.luciad.view.lightspeed.ILspAWTView;
import com.luciad.view.lightspeed.controller.ALspController;
import com.luciad.view.lightspeed.controller.ILspController;
import com.luciad.view.lightspeed.controller.manipulation.TLspEditController;
import com.luciad.view.lightspeed.controller.manipulation.TLspInteractiveLabelsController;
import com.luciad.view.lightspeed.label.TLspLabelPlacer;
import com.luciad.view.lightspeed.layer.ILspInteractivePaintableLayer;
import com.luciad.view.lightspeed.layer.ILspLayer;

import samples.decoder.asterix.LiveDecodedModel;
import samples.decoder.asterix.LiveDecoderResultCallback;
import samples.decoder.asterix.TransformationProvider;
import samples.lightspeed.common.FitUtil;
import samples.lightspeed.common.LightspeedSample;
import samples.lightspeed.common.ToolBar;
import samples.lightspeed.common.controller.ControllerFactory;
import samples.lightspeed.labels.util.ClampEditedLabelsUtil;

/**
 * This sample shows how to create a live stream from an ASTERIX final file, and how to display it as a track display.
 */
public class MainPanel extends samples.lightspeed.decoder.MainPanel {

  private static final String TARGET_FILE = "Data/ASTERIX/atx_cat21.astfin";
  private static final String TRANSFORMATION_CONFIG_NAME = "samples/decoder/asterix/locations.cfg";

  private final ASTERIXTrackAdditionalData fAdditionalData = new ASTERIXTrackAdditionalData();

  private final ASTERIXTrackLabel fInteractiveLabelComponentProvider = new ASTERIXTrackLabel(true, true, fAdditionalData);
  private final ASTERIXTrackInteractiveLabelProvider fInteractiveLabelProvider = new ASTERIXTrackInteractiveLabelProvider(fInteractiveLabelComponentProvider, fAdditionalData);
  static final ILcdFilter<ILspLayer> LAYER_FILTER = new ILcdFilter<ILspLayer>() {
    @Override
    public boolean accept(ILspLayer aLayer) {
      return aLayer instanceof ILspInteractivePaintableLayer &&
             ASTERIXTrackLayerFactory.isTrackModel(aLayer.getModel());
    }
  };

  @Override
  protected void addData() throws IOException {
    // This class makes sure that there is a maximum distance (of 100 px) between the center of the label and the track.
    new ClampEditedLabelsUtil(getView(), true, 100, false, LAYER_FILTER);

    // Start decoding
    initializeLiveDecodedModel();
    FitUtil.fitOnBounds(this, new TLcdLonLatBounds(7.5, 46.5, 2.5, 2), new TLcdGeodeticReference());
  }

  private void initializeLiveDecodedModel() {
    try {
      // Create and configure a live ASTERIX model decoder. This decoder reads data
      // from the specified input stream and updates the given modelList accordingly.
      TransformationProvider transformationProvider = new TransformationProvider(TRANSFORMATION_CONFIG_NAME);

      LiveDecodedModel liveDecodedModel = new LiveDecodedModel(TARGET_FILE, transformationProvider, new LiveDecoderResultCallback(this, TARGET_FILE) {
        @Override
        public void trackModelAdded(LiveDecodedModel aModel, ILcdModel aTrackModel) {
          ASTERIXTrackLayerFactory layerFactory = new ASTERIXTrackLayerFactory(fInteractiveLabelProvider, fInteractiveLabelComponentProvider, fAdditionalData);
          ILspLayer layer = layerFactory.createLayer(aTrackModel);
          getView().addLayer(layer);
        }
      });
      liveDecodedModel.startLiveDecoder();
    } catch (IOException e) {
      TLcdUserDialog.message(
          e.getMessage(),
          ILcdDialogManager.ERROR_MESSAGE,
          this, this
      );
    }
  }

  @Override
  protected Component[] createToolBars(ILspAWTView aView) {
    ToolBar toolBar = new ToolBar(aView, this, true, true) {
      @Override
      protected ILspController createDefaultController() {
        return createCompositeInteractiveController();
      }
    };

    return new JToolBar[]{toolBar};
  }

  /**
   * Creates a controller that
   * <ul>
   *   <li>Makes it possible to highlight tracks, by clicking on them.</li>
   *   <li>Adds interactive labels support. Interactive labels can be displayed on mouse hover, after a track has been highlighted.</li>
   *   <li>Allows a label to be moved without clicking on it, using the middle mouse button.</li>
   *   <li>Allows to navigate the view.</li>
   * </ul>
   * @return a controller
   */
  private ILspController createCompositeInteractiveController() {
    ALspController controller = new ASTERIXTrackHoverController(fAdditionalData);
    controller.appendController(createEditController());

    TLspInteractiveLabelsController interactiveLabelsController = new TLspInteractiveLabelsController(fInteractiveLabelProvider);
    interactiveLabelsController.setSelectObjectDuringInteraction(false);
    controller.appendController(interactiveLabelsController);

    controller.appendController(new ASTERIXTrackHighlightController(fAdditionalData));
    controller.appendController(ControllerFactory.createPanController());
    controller.appendController(ControllerFactory.createZoomController());
    controller.appendController(ControllerFactory.createRotateController());
    controller.setIcon(new TLcdImageIcon("images/icons/edit_16.png"));
    return controller;
  }

  private TLspEditController createEditController() {
    // Make sure that labels can be moved using the middle mouse button, without having to select the object.
    ASTERIXTrackNoSelectionEditController editController = new ASTERIXTrackNoSelectionEditController(fAdditionalData);
    editController.setAWTFilter(TLcdAWTEventFilterBuilder.newBuilder().middleMouseButton().build());
    return editController;
  }

  @Override
  protected ILspAWTView createView() {
    ILspAWTView view = super.createView();
    view.setBackground(Color.darkGray.brighter());

    // Add a label obstacle provider to the view. This provider makes sure that labels preferably
    // don't overlap with tracks.
    TLspLabelPlacer labelPlacer = new TLspLabelPlacer(view);
    labelPlacer.addLabelObstacleProvider(TLspLabelPlacer.DEFAULT_REALTIME_GROUP, new ASTERIXTrackLabelObstacleProvider(view));
    view.setLabelPlacer(labelPlacer);

    return view;
  }

  public static void main(final String[] aArgs) {
    LightspeedSample.startSample(MainPanel.class, "Lightspeed Track Display");
  }
}
