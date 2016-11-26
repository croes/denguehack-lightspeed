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

import java.util.Arrays;
import java.util.Map;
import java.util.WeakHashMap;

import com.luciad.gui.TLcdAWTUtil;
import com.luciad.view.gxy.asynchronous.ILcdGXYAsynchronousLayerWrapper;
import com.luciad.view.gxy.asynchronous.ILcdGXYAsynchronousPaintListener;
import com.luciad.view.gxy.asynchronous.ILcdGXYAsynchronousPaintQueue;
import com.luciad.view.gxy.asynchronous.TLcdGXYAsynchronousPaintEvent;
import com.luciad.view.gxy.asynchronous.manager.TLcdGXYAsynchronousPaintHint;
import com.luciad.view.gxy.asynchronous.manager.TLcdGXYAsynchronousPaintQueueManager;

/**
 * <p>Implementation of <code>ALcdGXYAsynchronousPaintQueueManager</code> which is based on the
 * paint times of the layers.</p>
 *
 * <p>Layers will only be grouped in the same paint queue when their paint times do not differ too
 * much.</p>
 */
public class PaintTimeBasedAsynchronousPaintQueueManager extends TLcdGXYAsynchronousPaintQueueManager {
  /**
   * <p>Constructor.</p>
   *
   * <p><strong>Warning:</strong> this manager will do nothing until {@link #setGXYView(com.luciad.view.gxy.ILcdGXYView) setGXYView}
   * is called.</p>
   * @param aPaintHintProvider The paint hint provider
   */
  public PaintTimeBasedAsynchronousPaintQueueManager(PaintTimeBasedPaintHintProvider aPaintHintProvider) {
    super(aPaintHintProvider);
  }

  @Override
  protected ILcdGXYAsynchronousPaintQueue createAsynchronousPaintQueue(TLcdGXYAsynchronousPaintHint aPaintHint, ILcdGXYAsynchronousPaintQueue aPaintQueue) {

    ILcdGXYAsynchronousPaintQueue queue = super.createAsynchronousPaintQueue(aPaintHint, aPaintQueue);
    if (aPaintQueue == null ||
        queue != aPaintQueue) {
      //add listener which updates the paint times of the layers
      queue.addGXYAsynchronousPaintListener(new AsynchronousPaintListener());
    }
    return queue;
  }

  /**
   * Listener which measures the paint time of the layers
   */
  private class AsynchronousPaintListener implements ILcdGXYAsynchronousPaintListener {

    private Map<ILcdGXYAsynchronousLayerWrapper, Long> fLayerToStartPaintTime = new WeakHashMap<ILcdGXYAsynchronousLayerWrapper, Long>();

    public void asynchronousPaintStateChanged(final TLcdGXYAsynchronousPaintEvent aEvent) {
      final ILcdGXYAsynchronousLayerWrapper layer = aEvent.getLayer();
      if (aEvent.getState() == TLcdGXYAsynchronousPaintEvent.STARTED) {
        fLayerToStartPaintTime.put(layer, System.currentTimeMillis());
      } else if (aEvent.getState() == TLcdGXYAsynchronousPaintEvent.FINISHED) {
        Long startTime = fLayerToStartPaintTime.get(layer);
        if (startTime != null) {
          final Long paintTime = System.currentTimeMillis() - startTime;
          //update the paint time, and if necessary evaluate the paint block
          TLcdAWTUtil.invokeLater(new Runnable() {
            public void run() {
              //due to the invokeLater, it is possible another manager is already active
              //only perform the runnable when we are still managing the paint queues
              if (getGXYView() != null) {
                boolean update = ((PaintTimeBasedPaintHintProvider) getPaintHintProvider()).updatePaintTime(layer, paintTime);
                if (update) {
                  //mark the paint block containing layer as changed, and call the evaluate method
                  //this method will verify whether the block should be split/merged/...
                  evaluateModifiedPaintBlocks(Arrays.asList(getPaintBlock(layer)), layer);
                }
              }
            }
          });
        }
        //remove the layer from the mapping
        fLayerToStartPaintTime.remove(layer);
      }
    }

  }

}
