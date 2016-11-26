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
package samples.decoder.asterix.lightspeed.radarvideo.radar;

import java.awt.Component;
import java.io.EOFException;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.swing.JOptionPane;

import com.luciad.format.asterix.TLcdASTERIXLiveDecoder;
import com.luciad.format.asterix.TLcdASTERIXReferenceProvider;
import com.luciad.gui.TLcdAWTUtil;
import com.luciad.model.ILcd2DBoundsIndexedModel;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelContainerListener;
import com.luciad.model.TLcdModelContainerEvent;
import com.luciad.model.TLcdModelList;
import com.luciad.realtime.ILcdSimulatorModel;
import com.luciad.util.ILcdDisposable;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.layer.ALspSingleLayerFactory;
import com.luciad.view.lightspeed.layer.ILspLayer;

/**
 * Utility class that can start up a simulated radar video stream, feed it into the
 * ASTERIX live decoder, and add it as a layer to a view.
 */
public class RadarFactory {

  public static void addRadarToView(
      final ILspView aView,
      final SimulatedRadarProperties aSimulatedRadarProperties,
      ILcdSimulatorModel aSimulatorModel,
      RadarStyleProperties aStyleProperties,
      List<ILcdDisposable> aDisposablesSFCT,
      final Component aHostComponent
  ) throws IOException {
    final CountDownLatch latch = new CountDownLatch(1);

    //Model list needed to decode ASTERIX data. When the radar model is added to the list, the radar layer is created.
    //This needs to be done in a ILcdModelContainerListener because the model is only available after the first data block is decoded.
    final ALspSingleLayerFactory radarLayerFactory = new RadarVideoLayerFactory(aStyleProperties);
    final TLcdModelList modelList = new TLcdModelList();
    modelList.addModelContainerListener(new ILcdModelContainerListener() {
      @Override
      public void modelContainerStateChanged(final TLcdModelContainerEvent aModelContainerEvent) {
        Runnable runnable = new Runnable() {
          public void run() {
            final ILcdModel model = aModelContainerEvent.getModel();
            final ILspLayer radarLayer = radarLayerFactory.createLayer(model);
            radarLayer.setLabel(aSimulatedRadarProperties.getName());
            int layerIndex = aView.getRootNode().layerCount() - 1;
            aView.getRootNode().addLayer(radarLayer, layerIndex);
          }
        };
        latch.countDown();
        TLcdAWTUtil.invokeAndWait(runnable);
      }
    });

    //Create the simulated ASTERIX Category 240 data stream.
    SimulatedRadarStream radarStream = new SimulatedRadarStream(
        aSimulatedRadarProperties.getPosition(),
        aSimulatedRadarProperties.getAngularResolution(),
        aSimulatedRadarProperties.getRange(),
        aSimulatedRadarProperties.getCellRange(),
        aSimulatedRadarProperties.getSecondsPerRotation()
    );
    //Add the models to track on the radar.
    ILcdModel[] trackModels = aSimulatorModel.getTrackModels();
    for (int i = 0; i < trackModels.length; i++) {
      if (trackModels[i] instanceof ILcd2DBoundsIndexedModel) {
        radarStream.addModel((ILcd2DBoundsIndexedModel) trackModels[i]);
      }
    }

    final TLcdASTERIXLiveDecoder liveDecoder = new TLcdASTERIXLiveDecoder();
    //The position of the radar is configured via the reference provider.
    liveDecoder.setReferenceProvider(new TLcdASTERIXReferenceProvider(aSimulatedRadarProperties.getPosition()));
    liveDecoder.initDecode(radarStream);

    //Read data blocks from the data stream at fixed time intervals.
    final DisposableExecutor executor = new DisposableExecutor(radarStream);
    aDisposablesSFCT.add(executor);
    executor.scheduleAtFixedRate(
        new Runnable() {
          @Override
          public void run() {
            try {
              int decodedMessages = 0;
              while (liveDecoder.blockAvailable()) {
                liveDecoder.decodeBlockSFCT(ILcdModel.FIRE_LATER, modelList);
                decodedMessages++;
              }
              if (decodedMessages > 0) {
                modelList.fireCollectedModelChanges();
              }
            } catch (EOFException e) {
              if (!executor.isDisposed()) {
                JOptionPane.showMessageDialog(aHostComponent, e.getMessage(), "Reached end of file.", JOptionPane.INFORMATION_MESSAGE);
              }
            } catch (IOException e) {
              if (!executor.isDisposed()) {
                JOptionPane.showMessageDialog(aHostComponent, e.getMessage(), "Error while decoding radar data", JOptionPane.ERROR_MESSAGE);
              }
            }
          }
        },
        0, 50, TimeUnit.MILLISECONDS
    );

    try {
      latch.await();
    } catch (InterruptedException ignored) {
    }
  }

  private static class DisposableExecutor extends ScheduledThreadPoolExecutor implements ILcdDisposable {
    private boolean fDisposed = false;
    private SimulatedRadarStream fRadarStream;

    private DisposableExecutor(SimulatedRadarStream aRadarStream) {
      super(1);
      fRadarStream = aRadarStream;
    }

    @Override
    public void dispose() {
      fDisposed = true;
      try {
        fRadarStream.close();
      } catch (IOException e) {
      }

      shutdown();
      try {
        awaitTermination(10, TimeUnit.SECONDS);
      } catch (InterruptedException e) {
      }
    }

    public boolean isDisposed() {
      return fDisposed;
    }
  }

}
