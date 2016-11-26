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
package samples.decoder.asdi.live;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Enumeration;
import java.util.TimeZone;

import javax.swing.Timer;

import com.luciad.format.asdi.TLcdASDIFileReplayInputStream;
import com.luciad.format.asdi.TLcdASDIFlightPlanModelDescriptor;
import com.luciad.format.asdi.TLcdASDILiveDecoder;
import com.luciad.format.asdi.TLcdASDITrackModelDescriptor;
import com.luciad.gui.ILcdDialogManager;
import com.luciad.gui.TLcdUserDialog;
import com.luciad.io.TLcdInputStreamFactory;
import com.luciad.model.ILcdModel;
import com.luciad.model.TLcdModelList;
import com.luciad.shape.ILcdBounds;
import com.luciad.shape.shape2D.TLcdLonLatBounds;
import com.luciad.util.ILcdFilter;
import com.luciad.util.ILcdFireEventMode;
import com.luciad.util.logging.ILcdLogger;
import com.luciad.util.logging.TLcdLoggerFactory;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.labeling.TLcdGXYLabelPlacer;
import com.luciad.view.gxy.labeling.algorithm.TLcdGXYCompositeLabelingAlgorithm;
import com.luciad.view.map.TLcdMapJPanel;

import samples.decoder.asdi.AbstractSample;
import samples.decoder.asdi.HeadingSensitiveDeclutterer;
import samples.decoder.asdi.SimulatorGXYLayerFactory;
import samples.gxy.common.labels.GXYLabelingAlgorithmProvider;
import samples.gxy.common.layers.GXYDataUtil;
import samples.realtime.common.LabelObstacleProvider;

/**
 * This sample shows how to create a live stream from an ASDI file.
 */
public class MainPanel extends AbstractSample {

  private static ILcdLogger sLogger = TLcdLoggerFactory.getLogger(MainPanel.class.getName());

  private static final String TARGET_FILE = "Data/ASDI/asdi_fake_data.asdi";
  private static final String REF_DATE_STRING = "11/06/2007 12:19:47";
  private static int HISTORY = 5;
  private static final int INITIAL_DELAY = 1000;
  private static final int DELAY = 50; //ms

  private SimulatorGXYLayerFactory fSimulatorLayerFactory;
  private Timer fTimer;
  private Thread fUpdateThread;

  public MainPanel() {
    super(new LiveTrackModelFilter(), new LiveFlightPlanModelFilter());
  }

  @Override
  protected ILcdBounds getInitialBounds() {
    return new TLcdLonLatBounds(-86.0, 38.0, 2.5, 2.5);
  }

  @Override
  protected TLcdMapJPanel createMap() {
    TLcdMapJPanel panel = super.createMap();
    TLcdGXYCompositeLabelingAlgorithm composite = new TLcdGXYCompositeLabelingAlgorithm(new GXYLabelingAlgorithmProvider());
    TLcdGXYLabelPlacer labelPlacer = new TLcdGXYLabelPlacer(composite);
    labelPlacer.setLabelObstacleProvider(new LabelObstacleProvider(new LiveTrackModelFilter()));
    panel.setGXYViewLabelPlacer(labelPlacer);
    return panel;
  }

  @Override
  protected void createGUI() {
    super.createGUI();
    //remove default double click action from edit controller
    getToolBars()[0].getGXYControllerEdit().setDoubleClickAction(null);
  }

  protected void initializeDecoders() {
    fSimulatorLayerFactory = new SimulatorGXYLayerFactory();

    // Create and initialize the ASDI model decoder. For the purpose of this sample,
    // a file containing ASDI data is replayed using the
    // TLcdASDIFileReplayInputStream. This input stream will take the ASDI
    // data and let clients pretend that it is a live ASDI stream. In real-world
    // scenarios, this step would be replaced by the preparation of an InputStream that
    // receives data from a radar, for instance, over the network.
    final InputStream input_stream = createFileReplayInputStream("" + TARGET_FILE);

    //Create and configure a live ASDI model decoder. This decoder reads data
    //from the specified input stream and updates the given modellist accordingly.
    final TLcdModelList model_list = new MyModelList();
    long ref_date = getRefDate(REF_DATE_STRING);
    startLiveDecoder(
        ref_date,
        HISTORY,
        input_stream,
        model_list,
        "Successfully finished replaying [" + TARGET_FILE + "]",
        "An error occurred during the decoding of [" + TARGET_FILE + "]"
                    );

    //Create a timer that will periodically fire all collected model changes so
    //that the visual representation in the layers is updated. This timer is
    //needed because we told the decoder that it should not fire the model
    //events itself (the ILcdFireEventMode.FIRE_LATER parameter in decodeSFCT).
    fTimer = new Timer(DELAY, new TimerActionListener(model_list));
    fTimer.setInitialDelay(INITIAL_DELAY);
    fTimer.setRepeats(true);
    fTimer.setDelay(DELAY);
    fTimer.start();
  }

  private InputStream createFileReplayInputStream(String aASDIFile) {
    try {
      TLcdInputStreamFactory input_stream_factory = new TLcdInputStreamFactory();
      InputStream input_stream = input_stream_factory.createInputStream(aASDIFile);
      return new TLcdASDIFileReplayInputStream(input_stream);
    } catch (IOException e) {
      sLogger.error(TLcdASDIFileReplayInputStream.class.getName() + "<init>", "The file [" + aASDIFile + "] could not be loaded.", e);
      TLcdUserDialog.message(
          "The file [" + aASDIFile + "] could not be loaded.",
          ILcdDialogManager.ERROR_MESSAGE,
          this, this
                            );
      return null;
    }
  }

  private void startLiveDecoder(
      long aStartTime,
      int aHistoryLength,
      final InputStream aInputStream,
      final TLcdModelList aModelList,
      final String aSuccessMessage,
      final String aErrorMessage
                               ) {
    final TLcdASDILiveDecoder live_decoder = new TLcdASDILiveDecoder();
    live_decoder.setStartTime(aStartTime);
    live_decoder.setHistoryLength(aHistoryLength);

    Runnable runnable = new Runnable() {
      public void run() {
        try {
          live_decoder.decodeSFCT(aInputStream, ILcdFireEventMode.FIRE_LATER, aModelList);
          aInputStream.close();
          showMessage(aSuccessMessage, ILcdDialogManager.PLAIN_MESSAGE);
        } catch (IOException e) {
          sLogger.error("decodeSFCT:" + aErrorMessage, e);
          showMessage(aErrorMessage, ILcdDialogManager.ERROR_MESSAGE);
        }
      }
    };
    fUpdateThread = new Thread(runnable, "ASDI Update Thread");
    fUpdateThread.setPriority(Thread.MIN_PRIORITY);
    fUpdateThread.start();
  }

  public void tearDown() {
    if (fTimer != null) {
      fTimer.stop();
    }
    if (fUpdateThread != null) {
      if (fUpdateThread.isAlive()) {
        fUpdateThread.interrupt();
      }
    }
    super.tearDown();
  }

  public void showMessage(final String aMessage, final int aMessageType) {
      EventQueue.invokeLater(new Runnable() {
        public void run() {
          TLcdUserDialog.message(
              aMessage,
              aMessageType,
              MainPanel.this,
              MainPanel.this
                                );
        }
      }
                            );
  }

  public static long getRefDate(String aStringDate) {
    try {
      SimpleDateFormat date_format = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
      date_format.setTimeZone(TimeZone.getTimeZone("UTC"));
      return date_format.parse(aStringDate).getTime();
    } catch (ParseException e) {
      return -1;
    }
  }


  private class TimerActionListener implements ActionListener {
    private TLcdModelList fModelList;

    public TimerActionListener(TLcdModelList aModelList) {
      fModelList = aModelList;
    }

    public void actionPerformed(ActionEvent e) {
      fModelList.fireCollectedModelChanges();

      // Firing the collected model changes automatically refreshes the map,
      // but the label decluttering algorithm (TLcdContinuousDeclutteringLabelPainter,
      // see SimulatorGXYLayerFactory) needs a refresh from time to time,
      // to make the labels move away gently if overlap is about to occur.
      Enumeration layers = getView().layers();
      while (layers.hasMoreElements()) {
        ILcdGXYLayer layer = (ILcdGXYLayer) layers.nextElement();
        if (fSimulatorLayerFactory.accept(layer.getModel())) {
          if (layer.isLabeled()) {
            getView().invalidateGXYLayer(layer, true, this, "Invalidating labels of track layers");
          }
        }
      }
    }
  }

  /**
   * Extension of TLcdModelList that, when new sub models are added,
   * creates a layer for them and inserts them into the map.
   */
  private class MyModelList extends TLcdModelList {
    public boolean addModel(ILcdModel aModel) throws IllegalArgumentException {
      boolean b = super.addModel(aModel);

      //Add the layer on the event dispatch thread.
      EventQueue.invokeLater(new AddLayerForModelRunnable(aModel));
      return b;
    }
  }

  /**
   * Runnable that creates an ILcdGXYLayer for a given ILcdModel and adds it to getMap().
   */
  private class AddLayerForModelRunnable implements Runnable {
    private ILcdModel fModel;

    public AddLayerForModelRunnable(ILcdModel aModel) {
      fModel = aModel;
    }

    public void run() {
      GXYDataUtil.instance()
                 .model(fModel)
                 .layer(fSimulatorLayerFactory)
                 .labelingAlgorithm(new HeadingSensitiveDeclutterer())
                 .addToView(getView());
    }
  }

  private static class LiveTrackModelFilter implements ILcdFilter {
    public boolean accept(Object aObject) {
      if (aObject instanceof ILcdModel) {
        ILcdModel model = (ILcdModel) aObject;
        return model.getModelDescriptor() instanceof TLcdASDITrackModelDescriptor;
      }
      return false;
    }
  }

  private static class LiveFlightPlanModelFilter implements ILcdFilter {
    public boolean accept(Object aObject) {
      if (aObject instanceof ILcdModel) {
        ILcdModel model = (ILcdModel) aObject;
        return model.getModelDescriptor() instanceof TLcdASDIFlightPlanModelDescriptor;
      }
      return false;
    }
  }

  // Main method
  public static void main(final String[] aArgs) {
    startSample(MainPanel.class, "Decoding ASDI live streams");
  }
}
