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
package samples.realtime.gxy.tracksimulator;

import static com.luciad.realtime.gxy.labeling.TLcdGXYContinuousLabelingAlgorithm.LabelMovementBehavior.REDUCED_MOVEMENT;

import java.awt.Point;

import javax.swing.JPanel;

import com.luciad.format.shp.TLcdSHPModelDecoder;
import com.luciad.model.ILcdModel;
import com.luciad.realtime.TLcdSimulator;
import com.luciad.realtime.gxy.labeling.TLcdGXYContinuousLabelingAlgorithm;
import com.luciad.shape.ILcdBounds;
import com.luciad.shape.shape2D.TLcdLonLatBounds;
import com.luciad.util.ILcdFilter;
import com.luciad.util.TLcdInterval;
import com.luciad.view.ALcdLabelLocations;
import com.luciad.view.gxy.ILcdGXYEditableLabelsLayer;
import com.luciad.view.gxy.ILcdGXYView;
import com.luciad.view.gxy.TLcdGXYLayer;
import com.luciad.view.gxy.controller.TLcdGXYEditController2;
import com.luciad.view.gxy.labeling.TLcdGXYAsynchronousLabelPlacer;
import com.luciad.view.gxy.painter.TLcdGXYPointListPainter;

import samples.common.SampleData;
import samples.gxy.common.GXYSample;
import samples.gxy.common.layers.GXYDataUtil;
import samples.gxy.labels.offset.LabelLocationsInvalidationListener;
import samples.realtime.common.LabelObstacleProvider;
import samples.realtime.common.SimulatorControlPanel;
import samples.realtime.common.TimeStampedTrackModelDescriptor;
import samples.realtime.common.TrajectorySimulatorModel;

/**
 * This sample demonstrates the possibility to show time-simulated data within
 * LuciadMap.
 * The controls at the bottom manage the flow of the simulation:
 * <ul>
 * <li> The simulation can be started/paused with the Run/Pause button.
 * <li> The simulation can be stopped with the stop button. Pressing the stop
 * button resets the simulation to the beginning.
 * <li> The current status of the simulation is indicated by a label next to
 * the stop button.
 * <li> A time slider allows to see your progression in the simulation. When the
 * simulation is not running, the slider also allows you to change the
 * "simulation time" with your mouse.
 * <li> The current "simulation time" is indicated in a label next to the time
 * slider.
 * <li> The maximum percentage of CPU used for the simulation application can be
 * controlled with the "% CPU" slider. Using a lower percentage of CPU
 * will result in a less smooth simulation, but the flow of "simulation
 * time" w.r.t. "real time", as expressed by the time-factor (see later)
 * will still be correct.
 * <li> The "time-factor" or "speed-up" can be controller with the "Speed-ip"
 * slider. This slider indicates how much faster "simulation time" flows
 * with respect to "real time", e.g. when this is set to 60, one minute
 * of "simulation time" will tick by every second.
 * </ul>
 * <p/>
 * The actual data that is shown during the simulation is determined by the
 * SimulatorModel. In this case, the SimulatorModel contains two
 * different models that are governed by the same simulation.
 * The models contains points that move in circles.
 * Each model is contained in a different layer, and its elements are represented
 * by a different symbol to make a clear visual distinction.
 * Labeling can be turned on by clicking on the layer name
 * in the layerControl to select it, and then press the button with the label
 * icon.
 */
public class MainPanel extends GXYSample {

  private static final int DELAY_BETWEEN_UPDATES = 25;

  @Override
  protected ILcdBounds getInitialBounds() {
    return new TLcdLonLatBounds(-100, 30, 14, 14);
  }

  /**
   * Initializes the label decluttering algorithm of the view. All labels
   * visible in the view are positioned by this label placer.
   */
  private void setupLabelDecluttering() {
    TLcdGXYContinuousLabelingAlgorithm algorithm = new TLcdGXYContinuousLabelingAlgorithm();
    algorithm.setReuseLocationsScaleRatioInterval(new TLcdInterval(0, Double.MAX_VALUE));
    algorithm.setClampOnScreenEdges(false);
    algorithm.setMinDistance(10);
    algorithm.setMaxDistance(50);
    algorithm.setLabelMovementBehavior(REDUCED_MOVEMENT);
    algorithm.setPadding(2);
    algorithm.setDesiredRelativeLocation(new Point(0, -15));
    algorithm.setMaxLabelCoverage(0.25);

    //TLcdGXYLabelPlacer placer = new TLcdGXYLabelPlacer( algorithm );
    TLcdGXYAsynchronousLabelPlacer placer = new TLcdGXYAsynchronousLabelPlacer(algorithm);
    placer.setLabelObstacleProvider(new LabelObstacleProvider(new TrackModelFilter()));
    getView().setGXYViewLabelPlacer(placer);
  }

  /**
   * Filter that only accepts ILcdModel's containing tracks.
   */
  private static class TrackModelFilter implements ILcdFilter {
    public boolean accept(Object aObject) {
      return aObject instanceof ILcdModel &&
             ((ILcdModel) aObject).getModelDescriptor() instanceof TimeStampedTrackModelDescriptor;
    }
  }

  protected void createGUI() {
    super.createGUI();
    getToolBars()[0].setGXYControllerEdit(createEditController());
    setupLabelDecluttering();
  }

  @Override
  protected JPanel createBottomPanel() {
    // Create trajectories model
    ILcdModel citiesModel;
    try {
      citiesModel = new TLcdSHPModelDecoder().decode("Data/Shp/Usa/city_125.shp");
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
    TrajectorySimulatorModel simulatorModel = new TrajectorySimulatorModel(citiesModel, 100);

    //Add layer that shows trajectories
    TLcdGXYLayer trajectoriesLayer = new TLcdGXYLayer(simulatorModel.getTrajectoryLinesModel(), "Trajectories");
    trajectoriesLayer.setGXYPainterProvider(new TLcdGXYPointListPainter(TLcdGXYPointListPainter.POLYLINE));
    getView().addGXYLayer(trajectoriesLayer);

    // initialization of everything regarding the simulation
    TLcdSimulator simulator;
    simulator = new TLcdSimulator();

    //Keep the simulator running at all times
    simulator.setPauseOnMousePressed(false);

    simulator.setGXYView(new ILcdGXYView[]{getView()});
    simulator.setGXYLayerFactory(new SimulatorGXYLayerFactory());
    simulator.setSimulatorModel(simulatorModel);
    simulator.setDate(simulator.getBeginDate());
    simulator.setDelayBetweenUpdateMs(DELAY_BETWEEN_UPDATES);
    SimulatorControlPanel simulator_control = new SimulatorControlPanel();
    simulator_control.setSimulator(simulator);

    // Create invalidation loop
    ILcdGXYEditableLabelsLayer layer = (ILcdGXYEditableLabelsLayer) simulator.getSimulationGXYLayers()[0][0];
    ALcdLabelLocations locations = layer.getLabelLocations();
    LabelLocationsInvalidationListener invalidateListener = new LabelLocationsInvalidationListener(layer, locations, getView());
    invalidateListener.setInvalidationDelay(DELAY_BETWEEN_UPDATES);
    locations.addLabelLocationListener(invalidateListener);
    locations.addLabelPaintedListener(invalidateListener);
    return simulator_control;
  }

  protected void addData() {
    // Add some background data
    GXYDataUtil.instance().model(SampleData.COUNTRIES).layer().label("Countries").selectable(false).addToView(getView());
    GXYDataUtil.instance().model(SampleData.US_CITIES).layer().label("Cities").addToView(getView());
  }

  private TLcdGXYEditController2 createEditController() {
    FirstTouchedLabelEditController edit_controller = new FirstTouchedLabelEditController();
    edit_controller.setStickyLabelsLayerFilter(new MySelectableRealtimeLayersFilter());
    edit_controller.setInstantEditing(true);
    edit_controller.setEditFirstTouchedLabelOnly(true);
    return edit_controller;
  }

  /**
   * Filter for the selectable realtime layers. Only accepts layers if:
   *  - they are ILcdGXYEditableLabelsLayer
   *  - they are selectable
   *  - they are realtime layers (defined by the number of cached background layers)
   */
  private class MySelectableRealtimeLayersFilter implements ILcdFilter {
    public boolean accept(Object aObject) {
      if (aObject instanceof ILcdGXYEditableLabelsLayer) {
        ILcdGXYEditableLabelsLayer layer = (ILcdGXYEditableLabelsLayer) aObject;
        return layer.isSelectableSupported() && layer.isSelectable() &&
               getView().indexOf(layer) >= getView().getNumberOfCachedBackgroundLayers();
      } else {
        return false;
      }
    }
  }

  public static void main(final String[] aArgs) {
    startSample(MainPanel.class, "Displaying dynamic data");
  }
}
