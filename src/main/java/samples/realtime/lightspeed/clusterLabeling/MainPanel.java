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
package samples.realtime.lightspeed.clusterLabeling;

import static samples.realtime.gxy.clusterLabeling.MainPanel.*;

import java.awt.Component;
import java.io.IOException;

import javax.swing.JToolBar;

import com.luciad.model.ILcdModel;
import com.luciad.model.TLcdVectorModel;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.shape.shape2D.TLcdLonLatBounds;
import com.luciad.util.ILcdFilter;
import com.luciad.view.lightspeed.ALspAWTView;
import com.luciad.view.lightspeed.ILspAWTView;
import com.luciad.view.lightspeed.controller.ILspController;
import com.luciad.view.lightspeed.label.TLspLabelPlacer;

import samples.common.serviceregistry.ServiceRegistry;
import samples.lightspeed.common.FitUtil;
import samples.lightspeed.common.LightspeedSample;
import samples.lightspeed.common.ToolBar;

/**
 * <p>This sample demonstrates how to combine labels of labels and continuous label decluttering.
 * Upon hovering the mouse on a group of objects, the objects are interactively decluttered
 * based on concentric circle positions.</p>
 *
 * <p>This is done by a controller (DeclutterController) that adjusts an advanced label
 * placement algorithm provider (AnimatedDeclutterLabelingAlgorithmProvider).</p>
 */
public class MainPanel extends LightspeedSample {

  private AnimatedDeclutterLabelingAlgorithmProvider fLabelingAlgorithmProvider;

  protected final AnimatedDeclutterLabelingAlgorithmProvider getLabelingAlgorithmProvider() {
    return fLabelingAlgorithmProvider;
  }

  protected ILcdFilter<ILcdModel> createModelFilter() {
    return new TrackModelFilter();
  }

  @Override
  protected ILspAWTView createView() {
    ALspAWTView view = (ALspAWTView) super.createView();
    TLspLabelPlacer labelPlacer = new TLspLabelPlacer(view);
    fLabelingAlgorithmProvider = new AnimatedDeclutterLabelingAlgorithmProvider();
    labelPlacer.addLabelObstacleProvider(TLspLabelPlacer.DEFAULT_DECLUTTER_GROUP, fLabelingAlgorithmProvider);
    view.setLabelPlacer(labelPlacer);
    return view;
  }

  @Override
  protected Component[] createToolBars(ILspAWTView aView) {
    ToolBar toolBar = new ToolBar(aView, this, true, true) {
      @Override
      protected ILspController createDefaultController() {
        ILspController defaultController = super.createDefaultController();
        // Wrap the default controller with a controller that triggers cluster decluttering
        DeclutterController controller = new DeclutterController(
            createModelFilter(),
            fLabelingAlgorithmProvider
        );
        controller.appendController(defaultController);
        controller.setIcon(defaultController.getIcon());
        controller.setName(defaultController.getName());
        return controller;
      }
    };
    return new JToolBar[]{toolBar};
  }

  @Override
  protected void addData() throws IOException {
    super.addData();

    ServiceRegistry.getInstance().register(new TrackLayerFactory(fLabelingAlgorithmProvider));

    for (int i = 0; i < CLUSTERS.length; i++) {
      TLcdVectorModel trackModel = createTrackModel(i);
      getView().addModel(trackModel);
    }

    FitUtil.fitOnBounds(this, new TLcdLonLatBounds(48.00, 48.00, 14.00, 14.00), new TLcdGeodeticReference());
  }

  public static void main(final String[] aArgs) {
    startSample(MainPanel.class, "Cluster labeling");
  }

}
