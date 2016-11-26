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
package samples.realtime.lightspeed.tracksimulator;

import static com.luciad.util.ILcdFireEventMode.FIRE_LATER;

import java.awt.Color;
import java.awt.Font;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import com.luciad.gui.swing.TLcdOverlayLayout;
import com.luciad.model.ILcdModel;
import com.luciad.model.TLcdVectorModel;
import com.luciad.realtime.TLcdSimulator;
import com.luciad.shape.shape2D.TLcdXYBounds;
import com.luciad.util.ILcdChangeListener;
import com.luciad.util.ILcdSelectionListener;
import com.luciad.util.TLcdChangeEvent;
import com.luciad.util.TLcdSelectionChangedEvent;
import com.luciad.view.lightspeed.layer.ILspInteractivePaintableLayer;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.TLspPaintState;
import com.luciad.view.lightspeed.layer.shape.TLspShapeLayerBuilder;
import com.luciad.view.lightspeed.style.TLspFillStyle;
import com.luciad.view.lightspeed.style.TLspLineStyle;

import samples.common.SampleData;
import samples.lightspeed.common.FitUtil;
import samples.lightspeed.common.LightspeedSample;
import samples.lightspeed.common.LspDataUtil;
import samples.lightspeed.timeview.TimeSlider;
import samples.lightspeed.timeview.model.TimeReference;
import samples.realtime.common.ITrajectory;
import samples.realtime.common.TimeStampedTrack;
import samples.realtime.common.TrajectorySimulatorModel;

/**
 * This sample shows a map with moving tracks.
 *
 * Below is a {@link TimeSlider} which shows:
 *   - A histogram of airborne flights over time.
 *   - The selected track's trajectory height over time.
 *
 * The time slider can be used to replay the tracks.
 * The tracks are updated using a {@link TLcdSimulator}.
 */
public class MainPanel extends LightspeedSample {

  private TimeSlider fTimeSlider;
  private JLabel fHelpLabel;

  @Override
  protected void tearDown() {
    super.tearDown();
    fTimeSlider.getView().destroy();
  }

  @Override
  protected void addData() throws IOException {
    super.addData();

    // Add countries and cities to the view
    LspDataUtil.instance().model(SampleData.COUNTRIES).layer().label("Countries").labeled(false).addToView(getView());
    ILcdModel citiesModel = LspDataUtil.instance().model(SampleData.CITIES).layer().label("Cities").labeled(false).addToView(getView()).fit().getModel();

    Calendar calendar = Calendar.getInstance();
    calendar.set(2016, Calendar.JANUARY, 1, 12, 0, 0);
    final Date beginDate = calendar.getTime();

    calendar.set(2016, Calendar.JANUARY, 4, 12, 0, 0);
    final Date endDate = calendar.getTime();

    //Create a trajectory simulator model based on the cities model
    final TrajectorySimulatorModel simulatorModel = new TrajectorySimulatorModel(citiesModel, 2000, beginDate, endDate);
    ILcdModel trajectoryModel = simulatorModel.getTrajectoryLinesModel();
    ILspInteractivePaintableLayer trajectoryLayer = TLspShapeLayerBuilder.newBuilder()
                                                                         .model(trajectoryModel)
                                                                         .bodyStyler(TLspPaintState.REGULAR, TLspLineStyle.newBuilder().color(new Color(255, 255, 255, 40)).build())
                                                                         .bodyStyler(TLspPaintState.SELECTED, TLspLineStyle.newBuilder().color(Color.white).width(2).build())
                                                                         .build();
    getView().addLayer(trajectoryLayer);

    final ILspLayer trackLayer = new TrackLayerFactory().createLayer(simulatorModel.getTrackModels()[0]);
    getView().addLayer(trackLayer);

    //Add time slider
    fTimeSlider = new TimeSlider();
    fTimeSlider.addChangeListener(new ILcdChangeListener() {
      @Override
      public void stateChanged(TLcdChangeEvent aChangeEvent) {
        simulatorModel.setDate(new Date(fTimeSlider.getTime()));
      }
    });
    addComponentBelow(fTimeSlider);
    addHelpLabel();

    //Show selected trajectories in the time view
    ILspLayer trajectoryTimeLayer = new TrajectoryTimeHeightLayerFactory().createLayer(trajectoryModel);
    fTimeSlider.getView().addLayer(trajectoryTimeLayer);

    //When a track is selected, select its trajectory in the time line
    syncSelection(trackLayer, trajectoryLayer, trajectoryTimeLayer);

    //Fit when the component has been added to the UI and has a size
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        hideLayerPanel();
        fTimeSlider.setValidRange(beginDate.getTime(), endDate.getTime(), 0, 400000);
        FitUtil.fitOnLayers(MainPanel.this, trackLayer);
      }
    });

    //Add histogram layer
    fTimeSlider.getView().addLayer(TLspShapeLayerBuilder.newBuilder()
                                                        .model(getHistogramModel(trajectoryModel))
                                                        .bodyStyles(TLspPaintState.REGULAR,
                                                                    TLspFillStyle.newBuilder().color(new Color(96, 104, 144, 200)).build(),
                                                                    TLspLineStyle.newBuilder().color(Color.black).zOrder(1).build())
                                                        .build());
  }

  private TLcdVectorModel getHistogramModel(ILcdModel aTrajectoryModel) {

    Map<Long, Integer> histogram = new HashMap<>();
    int maxValue = 0;
    int histogramBucketSize = 30 * 60 * 1000; // each half hour

    Enumeration trajectories = aTrajectoryModel.elements();
    while (trajectories.hasMoreElements()) {
      ITrajectory trajectory = (ITrajectory) trajectories.nextElement();
      long start = trajectory.getBeginTime() / histogramBucketSize * histogramBucketSize;
      long end = trajectory.getEndTime() / histogramBucketSize * histogramBucketSize;
      for (long bucket = start; bucket <= end; bucket += histogramBucketSize) {
        int value = (histogram.containsKey(bucket) ? histogram.get(bucket) : 0) + 1;
        histogram.put(bucket, value);
        maxValue = Math.max(maxValue, value);
      }
    }

    TLcdVectorModel histogramModel = new TLcdVectorModel(TimeReference.INSTANCE);
    for (Map.Entry<Long, Integer> entry : histogram.entrySet()) {
      long time = entry.getKey();
      int value = entry.getValue();
      histogramModel.addElement(new TLcdXYBounds(time - (histogramBucketSize / 2), 10000, histogramBucketSize, (value * 250000) / maxValue), ILcdModel.NO_EVENT);
    }

    return histogramModel;
  }

  private void syncSelection(final ILspLayer aTrackLayer, final ILspLayer aTrajectoryLayer, final ILspLayer aTrajectoryTimeLayer) {
    aTrackLayer.addSelectionListener(new ILcdSelectionListener() {
      @Override
      public void selectionChanged(TLcdSelectionChangedEvent aSelectionEvent) {
        removeHelpLabel();
        aTrajectoryLayer.clearSelection(FIRE_LATER);
        aTrajectoryTimeLayer.clearSelection(FIRE_LATER);
        Enumeration selectedObjects = aSelectionEvent.getSelection().selectedObjects();
        while (selectedObjects.hasMoreElements()) {
          TimeStampedTrack track = (TimeStampedTrack) selectedObjects.nextElement();
          aTrajectoryLayer.selectObject(track.getTrajectory(), true, FIRE_LATER);
          aTrajectoryTimeLayer.selectObject(track.getTrajectory(), true, FIRE_LATER);
        }
        aTrajectoryLayer.fireCollectedSelectionChanges();
        aTrajectoryTimeLayer.fireCollectedSelectionChanges();
      }
    });
  }

  private void addHelpLabel() {
    fHelpLabel = new JLabel("<html><center>Select a track to display its trajectory height over time.<br>The histogram shows the number of airborne aircraft over time.</center></html>");
    fHelpLabel.setFont(Font.decode("Default 20"));
    fTimeSlider.getView().getOverlayComponent().add(fHelpLabel, TLcdOverlayLayout.Location.CENTER);
  }

  private void removeHelpLabel() {
    fTimeSlider.getView().getOverlayComponent().remove(fHelpLabel);
    fTimeSlider.getView().getOverlayComponent().revalidate();
    fTimeSlider.getView().getOverlayComponent().invalidate();
    fTimeSlider.getView().getOverlayComponent().repaint();
  }

  public static void main(final String[] aArgs) {
    useBlackLime();
    startSample(MainPanel.class, "Track simulator");
  }
}
