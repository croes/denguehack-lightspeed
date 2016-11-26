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
package samples.decoder.asterix.live;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Enumeration;

import javax.swing.Timer;

import com.luciad.format.asterix.TLcdASTERIXTrackModelDescriptor;
import com.luciad.gui.ILcdDialogManager;
import com.luciad.gui.TLcdUserDialog;
import com.luciad.model.ILcdModel;
import com.luciad.shape.ILcdBounds;
import com.luciad.shape.shape2D.TLcdLonLatBounds;
import com.luciad.util.ILcdFilter;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.ILcdGXYView;
import com.luciad.view.gxy.labeling.TLcdGXYLabelPlacer;
import com.luciad.view.gxy.labeling.algorithm.TLcdGXYCompositeLabelingAlgorithm;
import com.luciad.view.map.TLcdMapJPanel;

import samples.decoder.asterix.AbstractSample;
import samples.decoder.asterix.HeadingSensitiveDeclutterer;
import samples.decoder.asterix.LiveDecodedModel;
import samples.decoder.asterix.LiveDecoderResultCallback;
import samples.decoder.asterix.SimulatorGXYLayerFactory;
import samples.decoder.asterix.TransformationProvider;
import samples.gxy.common.labels.GXYLabelingAlgorithmProvider;
import samples.gxy.common.layers.GXYDataUtil;
import samples.realtime.common.LabelObstacleProvider;

/**
 * This sample shows how to create a live stream from an ASTERIX final file.
 */
public class MainPanel extends AbstractSample {

  private static final String TARGET_FILE = "Data/ASTERIX/atx_fake62_wgs.astfin";
  private static final LiveTrackModelFilter TRACK_MODEL_FILTER = new LiveTrackModelFilter();

  private final SimulatorGXYLayerFactory fLiveTrackGXYLayerFactory = new SimulatorGXYLayerFactory();

  private LiveDecodedModel fLiveDecodedModel;

  private final Timer fTimer = new Timer(50, new InvalidateLabelsActionListener());

  @Override
  protected ILcdBounds getInitialBounds() {
    return new TLcdLonLatBounds(5.00, 43.00, 5.00, 5.00);
  }

  @Override
  protected TLcdMapJPanel createMap() {
    TLcdMapJPanel panel = super.createMap();
    TLcdGXYCompositeLabelingAlgorithm composite = new TLcdGXYCompositeLabelingAlgorithm(new GXYLabelingAlgorithmProvider());
    TLcdGXYLabelPlacer label_placer = new TLcdGXYLabelPlacer(composite);
    label_placer.setLabelObstacleProvider(new LabelObstacleProvider(TRACK_MODEL_FILTER));
    panel.setGXYViewLabelPlacer(label_placer);
    return panel;
  }

  @Override
  protected void initializeDecoders() {
    //Create and configure a live ASTERIX model decoder. This decoder reads data
    //from the specified input stream and updates the given modelList accordingly.
    TransformationProvider transformationProvider = getTransformationProvider();

    fLiveDecodedModel = new LiveDecodedModel(TARGET_FILE, transformationProvider, new LiveDecoderResultCallback(this, TARGET_FILE) {
      @Override
      public void trackModelAdded(LiveDecodedModel aModel, ILcdModel aTrackModel) {
        GXYDataUtil.instance()
                   .model(aTrackModel)
                   .layer(fLiveTrackGXYLayerFactory)
                   .labelingAlgorithm(new HeadingSensitiveDeclutterer())
                   .addToView(getView());

        fTimer.setRepeats(true);
        fTimer.start();
      }
    });
    try {
      fLiveDecodedModel.startLiveDecoder();
    } catch (IOException e) {
      TLcdUserDialog.message(
          e.getMessage(),
          ILcdDialogManager.ERROR_MESSAGE,
          this, this
      );
    }
  }

  @Override
  public void tearDown() {
    fTimer.stop();
    fLiveDecodedModel.dispose();
    super.tearDown();
  }

  private static class LiveTrackModelFilter implements ILcdFilter {
    @Override
    public boolean accept(Object aObject) {
      if (aObject instanceof ILcdModel) {
        ILcdModel model = (ILcdModel) aObject;
        return model.getModelDescriptor() instanceof TLcdASTERIXTrackModelDescriptor;
      }
      return false;
    }
  }

  // Main method
  public static void main(final String[] aArgs) {
    startSample(MainPanel.class, "Decoding ASTERIX live streams");
  }

  private class InvalidateLabelsActionListener implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
      // Firing model changes because of track updates automatically refreshes the map, but the label decluttering
      // algorithm (see TLcdGXYContinuousLabelingAlgorithm) needs a refresh from time to time, to make the
      // labels move away gently if overlap is about to occur.
      ILcdGXYLayer trackLayer = findTrackLayer(getView());
      if (trackLayer != null && trackLayer.isLabeled()) {
        getView().invalidateGXYLayer(trackLayer, true, this, "Invalidating labels of track layer");
      }
    }

    private ILcdGXYLayer findTrackLayer(ILcdGXYView aGXYView) {
      Enumeration layers = aGXYView.layers();
      while (layers.hasMoreElements()) {
        ILcdGXYLayer layer = (ILcdGXYLayer) layers.nextElement();
        if (fLiveTrackGXYLayerFactory.accept(layer.getModel())) {
          return layer;
        }
      }
      return null;
    }
  }
}
