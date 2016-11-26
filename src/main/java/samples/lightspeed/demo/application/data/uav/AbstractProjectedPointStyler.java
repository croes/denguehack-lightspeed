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
package samples.lightspeed.demo.application.data.uav;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.nio.IntBuffer;
import java.util.Collection;

import javax.swing.Timer;

import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelListener;
import com.luciad.model.TLcdModelChangedEvent;
import com.luciad.reference.ILcdGeoReference;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.shape3D.ILcd3DEditablePoint;
import com.luciad.transformation.TLcdGeoReference2GeoReference;
import com.luciad.util.TLcdOutOfBoundsException;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.layer.TLspPaintRepresentationState;
import com.luciad.view.lightspeed.layer.imageprojection.ILspImageProjectionLayer;
import com.luciad.view.lightspeed.layer.imageprojection.ILspImageProjector;
import com.luciad.view.lightspeed.style.styler.ALspStyleCollector;
import com.luciad.view.lightspeed.style.styler.ALspStyler;

import samples.lightspeed.demo.framework.util.video.stream.GStreamerVideoStream;
import samples.lightspeed.demo.framework.util.video.stream.VirtualCamera;

/**
 * Styler that uses geometry based on the projection of a point on the terrain as geometry.
 */
public abstract class AbstractProjectedPointStyler extends ALspStyler implements ILcdModelListener,
                                                                                 GStreamerVideoStream.VideoStreamListener,
                                                                                 Runnable {
  private static final int TOT_POLL_COUNT = 10;

  private final ILspImageProjectionLayer fImageProjectionLayer;
  private final GStreamerVideoStream fVideoStream;
  private Timer fTimer;
  private int fCurrPollCount = 0;

  public AbstractProjectedPointStyler(ILcdModel aModel, ILspImageProjectionLayer aImageProjectionLayer, GStreamerVideoStream aVideoStream) {
    fImageProjectionLayer = aImageProjectionLayer;
    fVideoStream = aVideoStream;

    aModel.addModelListener(this);
    aVideoStream.addVideoStreamListener(this);

    // Poll the image projection layer a few times because
    // we have no way of knowing when it changes.
    fTimer = new Timer(100, new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        styleChangeOnEdt();
        fCurrPollCount++;
        if (fCurrPollCount >= TOT_POLL_COUNT) {
          fTimer.stop();
        }
      }
    });
  }

  public GStreamerVideoStream getVideoStream() {
    return fVideoStream;
  }

  @Override
  public void style(Collection<?> aObjects, ALspStyleCollector aStyleCollector, TLspContext aContext) {
    UAVVideoLayerFactory.UAVVideoStyler styler = (UAVVideoLayerFactory.UAVVideoStyler) fImageProjectionLayer.getStyler(TLspPaintRepresentationState.REGULAR_BODY);
    ILspImageProjector projector = styler.getProjector();
    TLspContext projectionContext = new TLspContext(fImageProjectionLayer, aContext.getView());
    TLcdGeoReference2GeoReference uavToModel = new TLcdGeoReference2GeoReference(
        (ILcdGeoReference) fImageProjectionLayer.getModel().getModelReference(),
        (ILcdGeoReference) aContext.getModelReference()
    );

    for (Object object : aObjects) {
      // Project the point on the terrain
      UAVVideoPoint UAVVideoPoint = (UAVVideoPoint) object;
      ILcdPoint pointOnTerrain = fImageProjectionLayer.projectPoint(
          UAVVideoPoint.getVideoPoint(),
          projector,
          projectionContext.getView()
      );
      if (pointOnTerrain == null) {
        startTimer(true);
        return;
      }
      // Transform the point on the terrain to model coordinates
      ILcd3DEditablePoint modelPoint = aContext.getModelReference().makeModelPoint().cloneAs3DEditablePoint();
      try {
        uavToModel.sourcePoint2destinationSFCT(pointOnTerrain, modelPoint);
      } catch (TLcdOutOfBoundsException e) {
        startTimer(true);
        continue;
      }
      styleImpl(object, UAVVideoPoint, aStyleCollector, modelPoint);
    }
    startTimer(false);
  }

  /**
   * @param aObject              the object
   * @param aUAVVideoPoint       the UAV video point
   * @param aStyleCollector      the style collector
   * @param aProjectedModelPoint the projected point in model coordinates
   */
  protected abstract void styleImpl(Object aObject, UAVVideoPoint aUAVVideoPoint, ALspStyleCollector aStyleCollector, ILcd3DEditablePoint aProjectedModelPoint);

  @Override
  public void frame(VirtualCamera aCamera, int aWidth, int aHeight, IntBuffer aImage) {
    styleChangeOnEdt();
  }

  @Override
  public void modelChanged(TLcdModelChangedEvent aEvent) {
    styleChangeOnEdt();
  }

  private void styleChangeOnEdt() {
    runOnEdt(this);
  }

  private static void runOnEdt(Runnable aRunnable) {
    if (EventQueue.isDispatchThread()) {
      aRunnable.run();
    } else {
      EventQueue.invokeLater(aRunnable);
    }
  }

  @Override
  public void run() {
    fireStyleChangeEvent();
  }

  private void startTimer(final boolean aProjectionFailed) {
    runOnEdt(new Runnable() {
      @Override
      public void run() {
        if (aProjectionFailed) {
          fCurrPollCount = 0;
        }
        if (!fTimer.isRunning() && fCurrPollCount < TOT_POLL_COUNT) {
          fTimer.start();
        }
      }
    });
  }
}
