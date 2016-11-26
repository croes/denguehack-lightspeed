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
package samples.decoder.asterix.file;

import java.io.File;
import java.util.Collections;

import javax.swing.JPanel;

import com.luciad.format.asterix.ALcdASTERIXTransformationProvider;
import com.luciad.format.asterix.TLcdASTERIXModelDecoder;
import com.luciad.format.asterix.TLcdASTERIXTrackModelDescriptor;
import com.luciad.gui.TLcdAWTUtil;
import com.luciad.io.ILcdInputStreamFactory;
import com.luciad.io.TLcdInputStreamFactory;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelProducerListener;
import com.luciad.model.TLcdModelProducerEvent;
import com.luciad.realtime.ILcdSimulatorModel;
import com.luciad.realtime.TLcdSimulator;
import com.luciad.util.ILcdFilter;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.ILcdGXYView;
import com.luciad.view.gxy.labeling.TLcdGXYLabelPlacer;
import com.luciad.view.gxy.labeling.algorithm.TLcdGXYCompositeLabelingAlgorithm;
import com.luciad.view.map.TLcdMapJPanel;

import samples.common.formatsupport.OpenAction;
import samples.common.formatsupport.OpenSupport;
import samples.common.formatsupport.OpenTransferHandler;
import samples.common.serviceregistry.ServiceRegistry;
import samples.decoder.asterix.ASTERIXLayerFactory;
import samples.decoder.asterix.ASTERIXTrackSimulatorModelFactory;
import samples.decoder.asterix.AbstractSample;
import samples.decoder.asterix.HeadingSensitiveDeclutterer;
import samples.decoder.asterix.SimulationModelDescriptor;
import samples.decoder.asterix.SimulatorGXYLayerFactory;
import samples.decoder.asterix.TransformationProvider;
import samples.gxy.common.labels.GXYLabelingAlgorithmProvider;
import samples.gxy.common.labels.LayerBasedGXYLabelingAlgorithmProvider;
import samples.gxy.common.layers.GXYLayerUtil;
import samples.realtime.common.LabelObstacleProvider;
import samples.realtime.common.SimulatorControlPanel;

/**
 * This sample shows how to decode an ASTERIX file and create a realtime presentation of this data.
 * <p/>
 * In this sample you can decode an asterix file, which contain information in the form of
 * trajectories. For each of these trajectories, a track is created that represents that trajectory
 * at a certain point in time. This time can be controlled by the Simulator control panel at the
 * bottom of the window. Start the simulation or drag the time slider to move the tracks to the
 * position they were at that time.
 * <p/>
 * Since ASTERIX data usually is relative with respect to a radar, the positions of the radars must
 * be specified. These positions can be entered in the locations.cfg file. If you have your own
 * ASTERIX data you wish to load, you will need to add the locations of your radars to this file.
 * <p/>
 * Select a track to view more information about it. This information will be shown in a popup.
 */
public class MainPanel extends AbstractSample {

  private static final ILcdFilter<Object> TRACK_MODEL_FILTER = new TrackModelFilter();
  private ASTERIXLayerFactory fLayerFactory = new ASTERIXLayerFactory();
  private TLcdSimulator fSimulator;
  private ILcdGXYLayer fLastLayers;

  private TLcdASTERIXModelDecoder fModelDecoder;

  @Override
  protected void initializeDecoders() {
    TransformationProvider transformationProvider = getTransformationProvider();
    //create and initialize the ASTERIX model decoder.
    fModelDecoder = createAsterixModelDecoder(new TLcdInputStreamFactory(), transformationProvider);
  }

  @Override
  protected void createGUI() {
    super.createGUI();

    String initialSourceName = "" + "Data/ASTERIX/atx_fake62_wgs.astfin";
    OpenSupport openSupport = new OpenSupport(this, Collections.singletonList(fModelDecoder));
    openSupport.addStatusListener(getStatusBar());
    getView().setTransferHandler(new OpenTransferHandler(openSupport));
    openSupport.addModelProducerListener(new ASTERIXModelProducerListener());
    OpenAction openAction = new OpenAction(openSupport);
    openAction.setFirstInitialPath(new File(new File(System.getProperty("user.dir"), "Data"), "ASTERIX").getAbsolutePath());
    openAction.setPreferencesKey("ASTERIXDecoderSample");
    openAction.getOpenSupport().openSource(initialSourceName);

    getToolBars()[0].addAction(openAction);

    TLcdGXYCompositeLabelingAlgorithm composite = new TLcdGXYCompositeLabelingAlgorithm(new GXYLabelingAlgorithmProvider());
    TLcdGXYLabelPlacer labelPlacer = new TLcdGXYLabelPlacer(composite);
    labelPlacer.setLabelObstacleProvider(new LabelObstacleProvider(TRACK_MODEL_FILTER));
    getView().setGXYViewLabelPlacer(labelPlacer);
  }

  @Override
  protected JPanel createBottomPanel() {
    // This sample uses the SimulatorControlPanel of the realtime sample.
    SimulatorControlPanel simulatorControlPanel = new SimulatorControlPanel();

    // Create the simulator. For more information about this simulator,
    // please refer to the realtime sample (in the sample.realtime package).
    fSimulator = new TLcdSimulator();
    fSimulator.setGXYView(new ILcdGXYView[]{getView()});
    fSimulator.setGXYLayerFactory(new SimulatorGXYLayerFactory());
    fSimulator.setPauseOnMousePressed(false); //Keep the simulator running at all times
    simulatorControlPanel.setSimulator(fSimulator);
    return simulatorControlPanel;
  }


  public TLcdASTERIXModelDecoder createAsterixModelDecoder(
      ILcdInputStreamFactory aInputStreamFactory,
      ALcdASTERIXTransformationProvider aTransformationProvider
  ) {
    TLcdASTERIXModelDecoder decoder = new TLcdASTERIXModelDecoder();
    decoder.setInputStreamFactory(aInputStreamFactory);
    if (aTransformationProvider != null) {
      decoder.setTransformationProvider(aTransformationProvider);
    }
    return decoder;
  }

  /**
   * Handles newly produced ASTERIX model lists by
   * adding a layer to the view for all models in the model list and
   * deriving a realtime presentation of it.
   */
  private class ASTERIXModelProducerListener implements ILcdModelProducerListener {

    @Override
    public void modelProduced(TLcdModelProducerEvent aModelProducerEvent) {
      final ILcdModel model = aModelProducerEvent.getModel();
      TLcdAWTUtil.invokeAndWait(new Runnable() {
        @Override
        public void run() {
          createLayersAndSimulatorModel(model);
        }
      });
    }

    private void createLayersAndSimulatorModel(ILcdModel aModel) {
      // Remove the previously created layers from the map
      TLcdMapJPanel map = getView();
      if (map.containsLayer(fLastLayers)) {
        GXYLayerUtil.removeGXYLayer(map, fLastLayers, true);
      }

      // Create the ASTERIX layers
      fLayerFactory.setCreateAsynchronousLayers(true);
      fLastLayers = fLayerFactory.createGXYLayer(aModel);

      // Add the layers of the ASTERIX layer list to the view and fit on them.
      GXYLayerUtil.addGXYLayer(map, fLastLayers);
      GXYLayerUtil.fitGXYLayer(map, fLastLayers);

      // Create an ILcdSimulatorModel for the ASTERIX data
      ILcdSimulatorModel simulatorModel = new ASTERIXTrackSimulatorModelFactory().createSimulatorModel(aModel);

      // Set the simulator model on the simulator
      fSimulator.setSimulatorModel(simulatorModel);
      fSimulator.setDate(simulatorModel.getBeginDate());

      ILcdGXYLayer[][] layers = fSimulator.getSimulationGXYLayers();
       for (ILcdGXYLayer[] layers_array : layers) {
         for (ILcdGXYLayer layer : layers_array) {
           ServiceRegistry.getInstance().register(new LayerBasedGXYLabelingAlgorithmProvider(layer, new HeadingSensitiveDeclutterer()));
         }
       }
    }
  }

  private static class TrackModelFilter implements ILcdFilter<Object> {
    @Override
    public boolean accept(Object aObject) {
      if (aObject instanceof ILcdModel) {
        ILcdModel model = (ILcdModel) aObject;
        return model.getModelDescriptor() instanceof SimulationModelDescriptor ||
               model.getModelDescriptor() instanceof TLcdASTERIXTrackModelDescriptor;
      }
      return false;
    }
  }

  public static void main(final String[] aArgs) {
    startSample(MainPanel.class, "Decoding ASTERIX");
  }
}
