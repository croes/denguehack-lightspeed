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
package samples.lightspeed.internal.editing;

import java.io.IOException;

import javax.swing.JLabel;

import com.luciad.view.lightspeed.ILspAWTView;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.controller.ILspController;
import com.luciad.view.lightspeed.layer.ILspInteractivePaintableLayer;
import com.luciad.view.lightspeed.layer.ILspLayer;

import samples.lightspeed.common.FitUtil;
import samples.lightspeed.common.LightspeedSample;
import samples.lightspeed.common.CreateAndEditToolBar;
import samples.lightspeed.common.ToolBar;

/**
 */
public class MainPanel extends LightspeedSample {

  // Custom toolbar which adds creation controllers to the default Lightspeed sample toolbar
  private CreateAndEditToolBar fCreateAndEditToolBarLonLat;
  private CustomCreateAndEditToolbar fCreateAndEditToolBarXY;
  private CustomCreateAndEditToolbar fCreateAndEditToolBarHasAShapeXY;
  private CustomCreateAndEditToolbar fCreateAndEditToolBarView;

  private ILspInteractivePaintableLayer fIsAShapeLonLatLayer;
  private ILspInteractivePaintableLayer fIsaShapeXYLayer;
  private ILspInteractivePaintableLayer fHasAShapeXYLayer;
  private ILspInteractivePaintableLayer fIsAShapeViewLayer;

  @Override
  protected ToolBar[] createToolBars(ILspAWTView aView) {
    final ToolBar regularToolBar = new ToolBar(aView, this, true, true);

    ILspController defaultController = regularToolBar.getDefaultController();

    ModelFactory modelFactory = new ModelFactory();

    fIsAShapeLonLatLayer = (ILspInteractivePaintableLayer) new LayerFactory().createLayers(modelFactory.createLonLatModel(false)).iterator().next();
    fIsaShapeXYLayer = (ILspInteractivePaintableLayer) new LayerFactory().createLayers(new ModelFactory().createXYModel(false)).iterator().next();
    fHasAShapeXYLayer = (ILspInteractivePaintableLayer) new LayerFactory().createLayers(new ModelFactory().createXYModel(true)).iterator().next();
    fIsAShapeViewLayer = (ILspInteractivePaintableLayer) new LayerFactory().createLayers(new ModelFactory().createViewModel(false)).iterator().next();

    if (fCreateAndEditToolBarLonLat == null) {
      fCreateAndEditToolBarLonLat = new CreateAndEditToolBar(aView, this,
                                                             regularToolBar.getButtonGroup(),
                                                             true,
                                                             false,
                                                             false,
                                                             fIsAShapeLonLatLayer) {
        @Override
        protected ILspController createDefaultController() {
          return regularToolBar.getDefaultController();
        }
      };
      fCreateAndEditToolBarLonLat.addSpace();
      fCreateAndEditToolBarLonLat.addComponent(new JLabel("LonLat-Layer toolbar"));
      fCreateAndEditToolBarLonLat.addSpace();
    }

    if (fCreateAndEditToolBarXY == null) {
      fCreateAndEditToolBarXY = new CustomCreateAndEditToolbar(aView, this,
                                                               regularToolBar.getButtonGroup(),
                                                               defaultController,
                                                               true,
                                                               false,
                                                               false,
                                                               fIsaShapeXYLayer,
                                                               false, false);
      fCreateAndEditToolBarXY.addSpace();
      fCreateAndEditToolBarXY.addComponent(new JLabel("XY-Layer toolbar"));
      fCreateAndEditToolBarXY.addSpace();
    }

    if (fCreateAndEditToolBarHasAShapeXY == null) {
      fCreateAndEditToolBarHasAShapeXY = new CustomCreateAndEditToolbar(aView, this,
                                                                        regularToolBar.getButtonGroup(),
                                                                        defaultController,
                                                                        true,
                                                                        false,
                                                                        false,
                                                                        fHasAShapeXYLayer,
                                                                        false, true);
      fCreateAndEditToolBarHasAShapeXY.addSpace();
      fCreateAndEditToolBarHasAShapeXY.addComponent(new JLabel("Has-a-shape-XY-Layer toolbar"));
      fCreateAndEditToolBarHasAShapeXY.addSpace();
    }

    if (fCreateAndEditToolBarView == null) {
      fCreateAndEditToolBarView = new CustomCreateAndEditToolbar(aView, this,
                                                                 regularToolBar.getButtonGroup(),
                                                                 defaultController,
                                                                 true,
                                                                 false,
                                                                 false,
                                                                 fIsAShapeViewLayer,
                                                                 false, false);
      fCreateAndEditToolBarView.addSpace();
      fCreateAndEditToolBarView.addComponent(new JLabel("View-Layer toolbar"));
      fCreateAndEditToolBarView.addSpace();
    }

    getView().setController(defaultController);

    return new ToolBar[]{
        regularToolBar,
        fCreateAndEditToolBarLonLat,
        fCreateAndEditToolBarXY,
        fCreateAndEditToolBarHasAShapeXY,
        fCreateAndEditToolBarView
    };
  }

  @Override
  protected ILspAWTView createView() {
    return super.createView(ILspView.ViewType.VIEW_3D);
  }

  protected void addData() throws IOException {
    super.addData();

    ILspLayer hasAshapeLonLat = new LayerFactory().createLayers(new ModelFactory().createLonLatModel(true)).iterator().next();
    ILspLayer hasAShapeView = new LayerFactory().createLayers(new ModelFactory().createViewModel(true)).iterator().next();

    getView().addLayer(fIsAShapeLonLatLayer);
    getView().addLayer(fIsaShapeXYLayer);
    getView().addLayer(fIsAShapeViewLayer);
    getView().addLayer(hasAshapeLonLat);
    getView().addLayer(fHasAShapeXYLayer);
    getView().addLayer(hasAShapeView);

    fIsAShapeLonLatLayer.setVisible(false);
    fIsaShapeXYLayer.setVisible(false);
    fIsAShapeViewLayer.setVisible(false);
    hasAshapeLonLat.setVisible(false);
    fHasAShapeXYLayer.setVisible(true);
    hasAShapeView.setVisible(false);

    FitUtil.fitOnLayers(this, fIsAShapeLonLatLayer);
  }

  public static void main(final String[] aArgs) {
    startSample(MainPanel.class, "Editing - shapes");
  }

}
