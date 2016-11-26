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
package samples.tea.lightspeed.visibility;

import java.awt.EventQueue;

import javax.swing.JPanel;

import com.luciad.model.ILcdModel;
import com.luciad.reference.ILcdGeoReference;
import com.luciad.shape.ILcdPoint;
import com.luciad.tea.TLcdHeightProviderAdapter;
import com.luciad.util.height.ILcdHeightProvider;
import com.luciad.view.lightspeed.editor.operation.ILspEditingStateListener;
import com.luciad.view.lightspeed.editor.operation.TLspEditingStateEvent;
import com.luciad.view.lightspeed.layer.ILspInteractivePaintableLayer;
import com.luciad.view.lightspeed.layer.ILspLayer;

import samples.common.SampleData;
import samples.gxy.common.TitledPanel;
import samples.lightspeed.common.FitUtil;
import samples.lightspeed.common.LightspeedSample;
import samples.lightspeed.common.LspDataUtil;
import samples.tea.IntervisibilityPanel;
import samples.tea.lightspeed.HeightProviderUtil;
import samples.tea.lightspeed.HeightProviderUtil.DTEDLevel;

/**
 * In this sample, we demonstrate the to-polygon (or to-area) and
 * to-polyline (or to-path) intervisibility computations.
 */
public class MainPanel extends LightspeedSample {

  private static final DTEDLevel DTED_LEVEL = DTEDLevel.LEVEL_0;

  private ILspInteractivePaintableLayer fPointLayer = InputLayerFactory.createPointLayer();
  private ILspInteractivePaintableLayer fPolylineLayer = InputLayerFactory.createPolylineLayer();
  private ILspInteractivePaintableLayer fPolygonLayer = InputLayerFactory.createPolygonLayer();

  private ILspLayer fToPolylineLayer = VisibilityLayerFactory.createToPolylineLayer();
  private ILspLayer fToPolygonLayer = VisibilityLayerFactory.createToPolygonLayer();

  private TLcdHeightProviderAdapter fTerrainElevationProvider;
  private ILcdModel fHeightModel;

  protected void createGUI() {
    super.createGUI();
    ILcdModel pointModel = fPointLayer.getModel();
    ILcdGeoReference geoReference = (ILcdGeoReference) pointModel.getModelReference();

    fHeightModel = LspDataUtil.instance().model(SampleData.ALPS_ELEVATION).getModel();
    ILcdHeightProvider heightProvider = HeightProviderUtil.getHeightProvider(fHeightModel, geoReference, (ILcdPoint) pointModel.elements().nextElement(), DTED_LEVEL);
    fTerrainElevationProvider = new TLcdHeightProviderAdapter(heightProvider, geoReference);
    addComponentToRightPanel(createIntervisibilityPanel());
  }

  /**
   * Loads the sample data.
   */
  protected void addData() {
    LspDataUtil.instance().grid().addToView(getView());
    LspDataUtil.instance().model(fHeightModel).layer().label("Alps").addToView(getView());

    getView().addLayer(fPointLayer);
    getView().addLayer(fPolylineLayer);
    getView().addLayer(fPolygonLayer);

    getView().addLayer(fToPolylineLayer);
    getView().addLayer(fToPolygonLayer);

    FitUtil.fitOnLayers(this, fPointLayer, fPolylineLayer, fPolygonLayer);
  }

  private JPanel createIntervisibilityPanel() {
    final IntervisibilityPanel intervisibilityPanel = new IntervisibilityPanel(createAction());

    ILspEditingStateListener listener = new ILspEditingStateListener() {
      @Override
      public void editingStateChanged(TLspEditingStateEvent aEvent) {
        if (TLspEditingStateEvent.ChangeType.EDITING_CHANGE.equals(aEvent.getChangeType())) {
          intervisibilityPanel.performAction(aEvent.getObject());
        }
      }
    };
    fPointLayer.addEditingStateListener(listener);
    fPolylineLayer.addEditingStateListener(listener);
    fPolygonLayer.addEditingStateListener(listener);

    return TitledPanel.createTitledPanel("Visibility computations", intervisibilityPanel);
  }

  private LspVisibilityAction createAction() {
    return new LspVisibilityAction(this, fTerrainElevationProvider, fPointLayer, fPolylineLayer, fPolygonLayer, fToPolylineLayer, fToPolygonLayer, DTED_LEVEL);
  }

  // Main method
  public static void main(final String[] aArgs) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        startSample(MainPanel.class, "Visibility");
      }
    } );
  }

}
