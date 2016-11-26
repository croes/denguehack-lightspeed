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

import java.util.List;

import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.asynchronous.ILcdGXYAsynchronousLayerWrapper;
import com.luciad.view.gxy.asynchronous.ILcdGXYAsynchronousPaintQueue;
import com.luciad.view.gxy.asynchronous.TLcdGXYAsynchronousPaintQueue;
import com.luciad.view.gxy.asynchronous.manager.ALcdGXYAsynchronousPaintQueueManager;

/**
 * <p>Implementation of <code>ALcdGXYAsynchronousPaintQueueManager</code> that tries to use a fixed
 * number of queues.  It could for example be used to make sure that all cpu's are used on a
 * multi-cpu machine.</p>
 */
public class FixedCountPaintQueueManager extends ALcdGXYAsynchronousPaintQueueManager {
  private static final int QUEUE_COUNT = Math.max(2, Runtime.getRuntime().availableProcessors());

  @Override
  public void evaluateModifiedPaintBlocks(List<PaintBlock> aChangedPaintBlocks, ILcdGXYLayer aLayer) {
    // Only merge if there are too many paint queues
    while (getAsynchronousQueueCount() > QUEUE_COUNT) {
      PaintBlock bestBlock = null;
      PaintBlock bestOtherBlock = null;
      int smallestSize = Integer.MAX_VALUE;

      // Simply loop over all blocks and find the best candidate for merging.
      // Best means it tries to minimize the number of layers in a block.
      List<PaintBlock> blocks = getPaintBlocks();
      for (int i = 0; i < blocks.size() - 1; i++) {
        PaintBlock block = blocks.get(i);
        PaintBlock other = blocks.get(i + 1);

        int size = block.getLayers().size() + other.getLayers().size();
        if (size < smallestSize && canMergePaintBlocks(block, other)) {
          bestBlock = block;
          bestOtherBlock = other;
          smallestSize = size;
        }
      }

      if (bestBlock != null) {
        mergeSmallestIntoLargest(bestBlock, bestOtherBlock);
      } else {
        // We were not able to reduce the queue count, stop trying.
        return;
      }
    }
  }

  // Merge the smallest block into the larger block, promoting reuse of existing paint queues.
  private void mergeSmallestIntoLargest(PaintBlock aPaintBlock, PaintBlock aAdjacentPaintBlock) {
    PaintBlock from = aPaintBlock;
    PaintBlock to = aAdjacentPaintBlock;
    if (from.getLayers().size() > to.getLayers().size()) {
      from = aAdjacentPaintBlock;
      to = aPaintBlock;
    }
    mergePaintBlocksSFCT(to, from);
  }

  private int getAsynchronousQueueCount() {
    int count = 0;
    for (PaintBlock block : getPaintBlocks()) {
      if (block.getAsynchronousPaintQueue() != null) {
        count++;
      }
    }
    return count;
  }

  @Override
  protected ILcdGXYAsynchronousPaintQueue createAsynchronousPaintQueue(List<ILcdGXYAsynchronousLayerWrapper> aLayers) {
    TLcdGXYAsynchronousPaintQueue queue = new TLcdGXYAsynchronousPaintQueue(getGXYView(),
                                                                            TLcdGXYAsynchronousPaintQueue.BODIES_AND_SKIP,
                                                                            0.2,
                                                                            500,
                                                                            25);
    queue.setPriority(5);
    return queue;
  }
}
