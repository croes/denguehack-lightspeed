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
package samples.lightspeed.editing;

import com.luciad.model.ILcdModel;
import com.luciad.view.lightspeed.ILspAWTView;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.controller.ILspController;
import com.luciad.view.lightspeed.layer.ILspInteractivePaintableLayer;

import samples.lightspeed.common.LightspeedSample;
import samples.lightspeed.common.CreateAndEditToolBar;
import samples.lightspeed.common.LspDataUtil;
import samples.lightspeed.common.ToolBar;

/**
 * The Editing sample illustrates the editing and creation capabilities of the editors that are
 * available in the Lightspeed API. <p/> The sample contains a shape layer. All shapes that are
 * created with the provided creation controllers, will be added to this layer. All shapes in the
 * layer can be edited using default controller. <p/> Selected shapes can also be deleted by
 * pressing the delete key on the keyboard.
 */
public class MainPanel extends LightspeedSample {

  // Custom toolbar which adds creation controllers to the default Lightspeed sample toolbar
  private CreateAndEditToolBar fCreateAndEditToolBar;

  @Override
  protected ToolBar[] createToolBars(ILspAWTView aView) {
    final ToolBar regularToolBar = new ToolBar(aView, this, true, true);

    ILspController defaultController = regularToolBar.getDefaultController();

    if (fCreateAndEditToolBar == null) {
      ILspInteractivePaintableLayer shapesLayer = createAndAddEditableShapesLayer(aView);
      fCreateAndEditToolBar = new CreateAndEditToolBar(aView, this,
                                                       regularToolBar.getButtonGroup(),
                                                       true,
                                                       false,
                                                       false,
                                                       shapesLayer) {
        @Override
        protected ILspController createDefaultController() {
          return regularToolBar.getDefaultController();
        }
      };
    }

    getView().setController(defaultController);

    return new ToolBar[]{regularToolBar, fCreateAndEditToolBar};
  }

  @Override
  protected ILspAWTView createView() {
    return super.createView(ILspView.ViewType.VIEW_3D);
  }

  protected ILspInteractivePaintableLayer createAndAddEditableShapesLayer(ILspAWTView aView) {
    ILcdModel shapesModel = new ModelFactory().createShapesModel();
    return (ILspInteractivePaintableLayer) LspDataUtil.instance()
                                                      .model(shapesModel)
                                                      .layer(new EditableLayerFactory())
                                                      .label("Editable shapes")
                                                      .addToView(aView)
                                                      .getLayer();
  }

  public static void main(String[] aArgs) {
    startSample(MainPanel.class, "Editing - shapes");
  }

}
