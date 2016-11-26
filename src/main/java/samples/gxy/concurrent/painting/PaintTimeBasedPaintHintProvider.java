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
package samples.gxy.concurrent.painting;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.luciad.view.ILcdLayer;
import com.luciad.view.ILcdLayeredListener;
import com.luciad.view.TLcdLayeredEvent;
import com.luciad.view.gxy.ILcdGXYView;
import com.luciad.view.gxy.asynchronous.ILcdGXYAsynchronousLayerWrapper;
import com.luciad.view.gxy.asynchronous.manager.ILcdGXYAsynchronousPaintHintProvider;
import com.luciad.view.gxy.asynchronous.manager.TLcdGXYAsynchronousPaintHint;

/**
 * <p>Implementation of <code>ILcdGXYAsynchronousPaintHintProvider</code> which creates
 * <code>TLcdGXYAsynchronousPaintHint</code> instances based on the paint times of the layers.</p>
 *
 * <p>Layers will only be grouped in the same paint queue when their paint times do not differ too
 * much.</p>
 */
public class PaintTimeBasedPaintHintProvider implements ILcdGXYAsynchronousPaintHintProvider {
  /**
   * The factor the paint time must differ from the last used one before re-evaluating the paint
   * queue
   */
  private static final double UPDATE_PAINT_TIME_FACTOR = 0.1;
  /**
   * The factor the paint time of any layer may differ from the paint time of the fastest layer in
   * the paint queue
   */
  private static final double MAXIMUM_DIFFERENCE_FACTOR = 3;

  private static final String PAINT_TIME_KEY = "paintTime";

  /**
   * The number of paint times used to calculate the mean time
   */
  private static final int NUMBER_OF_PAINT_TIMES = 20;

  /**
   * A mapping between the layers and the mean paint time of the last paints
   */
  private Map<ILcdLayer, Long> fLayerToMeanPaintTime = new HashMap<ILcdLayer, Long>();
  /**
   * A mapping between the layers and their last paint times
   */
  private Map<ILcdLayer, List<Long>> fLayerToLastPaintTimes = new HashMap<ILcdLayer, List<Long>>();

  /**
   * Create a paint-time based <code>ILcdGXYAsynchronousPaintHintProvider</code>
   *
   * @param aGXYView the view to create the paint hint provider for
   */
  public PaintTimeBasedPaintHintProvider(final ILcdGXYView aGXYView) {
    aGXYView.addLayeredListener(new ILcdLayeredListener() {
      public void layeredStateChanged(TLcdLayeredEvent e) {
        //make sure we do not keep a reference to removed layers
        if (e.getID() == TLcdLayeredEvent.LAYER_REMOVED) {
          fLayerToMeanPaintTime.remove(e.getLayer());
          fLayerToLastPaintTimes.remove(e.getLayer());
        }
      }
    });
  }

  /**
   * <p>Update the paint time of <code>aLayer</code>.</p>
   *
   * <p>This method should be called on the AWT-thread, and when it returns <code>true</code>, the
   * {@link com.luciad.view.gxy.asynchronous.manager.ALcdGXYAsynchronousPaintQueueManager#evaluateModifiedPaintBlocks(List,
   * com.luciad.view.gxy.ILcdGXYLayer)} method should be called within the same runnable.</p>
   *
   * @param aLayer     The layer
   * @param aPaintTime The measured paint time of the layer
   *
   * @return <code>true</code> when the update of the paint time has influenced the paint hint for
   *         <code>aLayer</code>, and the {@link com.luciad.view.gxy.asynchronous.manager.ALcdGXYAsynchronousPaintQueueManager#evaluateModifiedPaintBlocks(List,
   *         com.luciad.view.gxy.ILcdGXYLayer) evaluateModifiedPaintBlocks} method should be called,
   *         <code>false</code> otherwise
   */
  public boolean updatePaintTime(ILcdLayer aLayer, Long aPaintTime) {
    //add the measured paint time to the List of paint times
    if (!(fLayerToLastPaintTimes.containsKey(aLayer))) {
      fLayerToLastPaintTimes.put(aLayer, new LinkedList<Long>());
    }

    List<Long> paintTimes = fLayerToLastPaintTimes.get(aLayer);
    paintTimes.add(aPaintTime);
    if (paintTimes.size() > NUMBER_OF_PAINT_TIMES) {
      paintTimes.remove(0);
    }

    //update the mean paint time
    Long currentMeanPaintTime = fLayerToMeanPaintTime.get(aLayer);
    long newMeanTime = calculateMean(paintTimes);
    fLayerToMeanPaintTime.put(aLayer, newMeanTime);

    //Check if the paint blocks need to be evaluated
    boolean evaluate = currentMeanPaintTime == null;//first time paint time is measured, evaluate block
    if (!evaluate) {
      //calculate the new mean paint time and compare it with the current time
      double difference = Math.abs((double) (newMeanTime - currentMeanPaintTime) / (double) currentMeanPaintTime);
      //paint time differs too much from the previous value, evaluate block
      evaluate = difference > UPDATE_PAINT_TIME_FACTOR;
    }
    return evaluate;
  }

  private long calculateMean(List<Long> aListOfLongs) {
    long total = 0;
    for (Long aLong : aListOfLongs) {
      total = total + aLong;
    }
    return (long) ((double) total / (double) aListOfLongs.size());
  }

  public TLcdGXYAsynchronousPaintHint getPaintHint(ILcdGXYAsynchronousLayerWrapper aLayer) {
    TLcdGXYAsynchronousPaintHint hint = new TLcdGXYAsynchronousPaintHint();
    hint.getProperties().put(PAINT_TIME_KEY, calculatePaintTimeInterval(aLayer));
    return hint;
  }

  private TLcdGXYAsynchronousPaintHint.Range calculatePaintTimeInterval(
      ILcdGXYAsynchronousLayerWrapper aAsynchronousLayerWrapper) {

    Long time = fLayerToMeanPaintTime.get(aAsynchronousLayerWrapper);
    if (time == null) {
      //as long as the paint time is not measured, we allow to merge with every queue
      //this avoids unnecessary splitting of paint queues
      //drawback: adding a slow layer to a fast queue would temporarily slow down the fast queue,
      //until the slow layer has been moved to a different queue
      return new TLcdGXYAsynchronousPaintHint.Range(0, Double.MAX_VALUE);
    } else {
      //use the mean paint time to define the interval of allowed paint times
      return new TLcdGXYAsynchronousPaintHint.Range(time, time * MAXIMUM_DIFFERENCE_FACTOR);
    }
  }
}
