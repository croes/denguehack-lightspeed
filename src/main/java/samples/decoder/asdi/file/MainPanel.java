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
package samples.decoder.asdi.file;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;

import javax.swing.JPanel;

import com.luciad.datamodel.ILcdDataObject;
import com.luciad.format.asdi.ILcdASDIMessageFilter;
import com.luciad.format.asdi.TLcdASDIFacility;
import com.luciad.format.asdi.TLcdASDIFlightPlanHistoryModelDescriptor;
import com.luciad.format.asdi.TLcdASDIModelDecoder;
import com.luciad.format.asdi.TLcdASDITrajectoryModelDescriptor;
import com.luciad.gui.ILcdAction;
import com.luciad.gui.TLcdAWTUtil;
import com.luciad.io.ILcdInputStreamFactory;
import com.luciad.io.TLcdInputStreamFactory;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelDecoder;
import com.luciad.model.ILcdModelProducerListener;
import com.luciad.model.TLcdModelList;
import com.luciad.model.TLcdModelProducerEvent;
import com.luciad.realtime.ILcdSimulatorModel;
import com.luciad.realtime.TLcdSimulator;
import com.luciad.realtime.TLcdSimulatorModelList;
import com.luciad.util.ILcdFilter;
import com.luciad.util.logging.ILcdLogger;
import com.luciad.util.logging.TLcdLoggerFactory;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.ILcdGXYView;
import com.luciad.view.gxy.labeling.TLcdGXYLabelPlacer;
import com.luciad.view.gxy.labeling.algorithm.TLcdGXYCompositeLabelingAlgorithm;
import com.luciad.view.map.TLcdMapJPanel;

import samples.common.formatsupport.OpenAction;
import samples.common.formatsupport.OpenSupport;
import samples.common.formatsupport.OpenTransferHandler;
import samples.common.serviceregistry.ServiceRegistry;
import samples.decoder.asdi.ASDILayerFactory;
import samples.decoder.asdi.AbstractSample;
import samples.decoder.asdi.FlightPlanSimulationModelDescriptor;
import samples.decoder.asdi.FlightPlanSimulatorModel;
import samples.decoder.asdi.HeadingSensitiveDeclutterer;
import samples.decoder.asdi.SimulatorGXYLayerFactory;
import samples.decoder.asdi.TrackSimulationModelDescriptor;
import samples.decoder.asdi.TrackSimulatorModel;
import samples.gxy.common.labels.DefaultGXYLabelingAlgorithmProvider;
import samples.gxy.common.labels.LayerBasedGXYLabelingAlgorithmProvider;
import samples.gxy.common.layers.GXYLayerUtil;
import samples.gxy.common.toolbar.ToolBar;
import samples.realtime.common.LabelObstacleProvider;
import samples.realtime.common.SimulatorControlPanel;

/**
 * This sample shows how to decode an ASDI file and create a realtime presentation of this data.</p>
 *
 * In this sample you can decode an asdi file, which contains information in the form of
 * trajectories and flight plan history. For each trajectory, a track is created that represents that trajectory
 * at a certain point in time. For each flight plan history, a flight plan
 * is created that represents the situation at a certain point in time.
 * This time can be controlled by the Simulator control panel at the
 * bottom of the window. Start the simulation or drag the time slider to move the tracks or
 * flight plans to the position they were at that time.</p>
 *
 * When a track is selected the associated trajectory and flight plan (if available) are selected too.</p>
 *
 * Additional information about the selected track and associated flight plan is shown in a popup.</p>
 *
 * The decoder in this sample has a filter that filters out messages from certain facilities. You can
 * adapt it to filter out other messages, for instance all track messages, but bear in mind that
 * simulated flight plans are only visible if you can select an associated track. </p>
 */
public class MainPanel extends AbstractSample {

  protected static final String DEFAULT_SAMPLE_FILE = "Data/ASDI/asdi_fake_data.asdi";
  private static ILcdLogger sLogger = TLcdLoggerFactory.getLogger(MainPanel.class.getName());

  private ASDILayerFactory fLayerFactory = new ASDILayerFactory();
  private TLcdSimulator fSimulator;
  private ILcdGXYLayer fLastLayers;
  private ILcdModelDecoder fModelDecoder;

  private TLcdGXYLabelPlacer fLabelPlacer;

  public MainPanel() {
    super(new FinalTrackModelFilter(), new FinalFlightPlanModelFilter());
  }

  protected void initializeDecoders() {
    //create and initialize the ASDI model decoder.
    fModelDecoder = createASDIModelDecoder(new TLcdInputStreamFactory());
  }

  protected void createGUI() {
    super.createGUI();
    // remove double click action from edit controller
    getToolBars()[0].getGXYControllerEdit().setDoubleClickAction(null);

    ILcdAction action;
      //create an action to open an ASDI file.
      OpenSupport openSupport = new OpenSupport(this, Collections.singletonList(fModelDecoder));
      getView().setTransferHandler(new OpenTransferHandler(openSupport));
      openSupport.addStatusListener(getStatusBar());
      openSupport.addModelProducerListener(new ASDIModelProducerListener());
      OpenAction standaloneOpenAction = new OpenAction(openSupport);

      File sampleDataDirectory = findSampleDataDirectory();
      if (sampleDataDirectory != null) {
        standaloneOpenAction.setFirstInitialPath(sampleDataDirectory.getAbsolutePath());
        standaloneOpenAction.setPreferencesKey("ASDIDecoderSample");
      }

      action = standaloneOpenAction;
      openSupport.openSource("Data/ASDI/asdi_fake_data.asdi");

    ToolBar toolbar = getToolBars()[0];
    toolbar.addAction(action);

    TLcdGXYCompositeLabelingAlgorithm composite = new TLcdGXYCompositeLabelingAlgorithm(new DefaultGXYLabelingAlgorithmProvider());
    fLabelPlacer = new TLcdGXYLabelPlacer(composite);
    getView().setGXYViewLabelPlacer(fLabelPlacer);
  }

  private File findSampleDataDirectory() {
    if (new File(DEFAULT_SAMPLE_FILE).exists()) {
      return new File(DEFAULT_SAMPLE_FILE).getParentFile();
    }

    URL defaultData = getClass().getClassLoader().getResource(DEFAULT_SAMPLE_FILE);
    if (defaultData != null && "file".equals(defaultData.getProtocol())) {
      try {
        return new File(defaultData.toURI()).getParentFile();
      } catch (URISyntaxException e) {
        return null;
      }
    }
    return null;
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

  private ILcdModelDecoder createASDIModelDecoder(
      ILcdInputStreamFactory aInputStreamFactory
                                                 ) {
    TLcdASDIModelDecoder decoder = new TLcdASDIModelDecoder();
    decoder.setMessageFilter(new FinalMessageFilter());
    decoder.setInputStreamFactory(aInputStreamFactory);
    return decoder;
  }

  /**
   * Handles newly created ASDI model lists by
   * adding a layer to the view for all models in the model list and
   * deriving a realtime presentation of it.
   */
  private class ASDIModelProducerListener implements ILcdModelProducerListener {

    public void modelProduced(TLcdModelProducerEvent aModelProducerEvent) {
      ILcdModel model = aModelProducerEvent.getModel();
      // Remove the previously created layers from the map
      final TLcdMapJPanel mapPanel = getView();
      TLcdAWTUtil.invokeAndWait(new Runnable() {
        @Override
        public void run() {
          if (mapPanel.containsLayer(fLastLayers)) {
            GXYLayerUtil.removeGXYLayer(mapPanel, fLastLayers, false);
          }
        }
      });

      // Create the layer tree
      fLayerFactory.setCreateAsynchronousLayers(true);
      fLastLayers = fLayerFactory.createGXYLayer(model);

      // Add and fit the sub layers of the layer list.
      GXYLayerUtil.addGXYLayer(mapPanel, fLastLayers);
      GXYLayerUtil.fitGXYLayer(mapPanel, fLastLayers);

      // Create a ILcdSimulatorModel for the ASDI data
      final ILcdSimulatorModel simulator_model = createSimulatorModelList(model);

      // Set the simulator model on the simulator
      fSimulator.setSimulatorModel(simulator_model);
      fSimulator.setDate(simulator_model.getBeginDate());

      ILcdGXYLayer[][] layers = fSimulator.getSimulationGXYLayers();
      for (ILcdGXYLayer[] layers_array : layers) {
        for (ILcdGXYLayer layer : layers_array) {
          // GXYLabelingAlgorithmProvider picks up the labeling algorithm providers that are registered in the ServiceRegistry
          HeadingSensitiveDeclutterer algorithm = new HeadingSensitiveDeclutterer();
          ServiceRegistry.getInstance().register(new LayerBasedGXYLabelingAlgorithmProvider(layer, algorithm));
        }
      }
      fLabelPlacer.setLabelObstacleProvider(new LabelObstacleProvider(new ILcdFilter() {
        private ILcdModel[] fTrackModels = simulator_model.getTrackModels().clone();

        public boolean accept(Object aObject) {
          for (ILcdModel model : fTrackModels) {
            if (aObject == model) {
              return true;
            }
          }
          return false;
        }
      }));
    }
  }

  private ILcdSimulatorModel createSimulatorModelList(ILcdModel aStaticDataModel) {
    TLcdSimulatorModelList list = new TLcdSimulatorModelList();

    if (aStaticDataModel instanceof TLcdModelList) {
      TLcdModelList modelList = (TLcdModelList) aStaticDataModel;
      for (int i = 0; i < modelList.getModelCount(); i++) {
        ILcdSimulatorModel simulation_model = createSimulationModel(modelList.getModel(i));
        if (simulation_model != null) {
          list.addSimulatorModel(simulation_model);
        }
      }
    } else {
      ILcdSimulatorModel simulation_model = createSimulationModel(aStaticDataModel);
      if (simulation_model != null) {
        list.addSimulatorModel(simulation_model);
      }
    }
    return list;
  }

  private ILcdSimulatorModel createSimulationModel(ILcdModel aModel) {
    if (aModel.getModelDescriptor() instanceof TLcdASDITrajectoryModelDescriptor) {
      try {
        TrackSimulatorModel simulator_model = new TrackSimulatorModel();
        simulator_model.setTrajectoryModel(aModel);
        return simulator_model;
      } catch (IllegalArgumentException aException) {
        sLogger.warn(aException.getMessage(), aException);
        return null;
      }
    } else if (aModel.getModelDescriptor() instanceof TLcdASDIFlightPlanHistoryModelDescriptor) {
      try {
        FlightPlanSimulatorModel fp_simulator_model = new FlightPlanSimulatorModel();
        fp_simulator_model.setFlightPlanHistoryModel(aModel);
        return fp_simulator_model;
      } catch (IllegalArgumentException aException) {
        sLogger.warn(aException.getMessage(), aException);
        return null;
      }
    }
    return null;
  }

  private static class FinalMessageFilter implements ILcdASDIMessageFilter {
    public boolean accept(
        ILcdDataObject aMessageDataObject
                         ) {
      TLcdASDIFacility facility = (TLcdASDIFacility) aMessageDataObject.getValue("Facility");
      return (!(facility != null && facility.getCode().equals("KGT*")));
    }
  }

  private static class FinalTrackModelFilter implements ILcdFilter {
    public boolean accept(Object aObject) {
      if (aObject instanceof ILcdModel) {
        ILcdModel model = (ILcdModel) aObject;
        return model.getModelDescriptor() instanceof TrackSimulationModelDescriptor;
      }
      return false;
    }
  }

  private static class FinalFlightPlanModelFilter implements ILcdFilter {
    public boolean accept(Object aObject) {
      if (aObject instanceof ILcdModel) {
        ILcdModel model = (ILcdModel) aObject;
        return model.getModelDescriptor() instanceof FlightPlanSimulationModelDescriptor;
      }
      return false;
    }
  }

  public static void main(final String[] aArgs) {
    startSample(MainPanel.class, "Decoding ASDI");
  }
}
