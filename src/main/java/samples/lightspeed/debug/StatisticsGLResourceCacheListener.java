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
package samples.lightspeed.debug;

import java.awt.EventQueue;
import java.util.IdentityHashMap;
import java.util.Map;

import com.luciad.view.lightspeed.services.glcache.ALspGLResource;
import com.luciad.view.lightspeed.services.glcache.ILspGLResourceCacheListener;
import com.luciad.view.lightspeed.services.glcache.TLspGLResourceCacheEvent;

/**
 * Listener to obtain statistics about per-frame resources used by the
 * GL resource cache that is set on the view.
 */
public class StatisticsGLResourceCacheListener implements ILspGLResourceCacheListener {

  private long fMaxBytesUsed = 0;
  private int fMaxCountUsed = 0;
  private Map<ALspGLResource, Long> fResourcesUsed
      = new IdentityHashMap<ALspGLResource, Long>();

  private static void checkThread() {
    if (!EventQueue.isDispatchThread()) {
      throw new RuntimeException("wrong thread: " + Thread.currentThread());
    }
  }

  /**
   * Resets the count of bytes used since the last call to reset.
   * Also updates the maximal bytes used values.
   * This method is called between each frame.
   */
  public void reset() {
    checkThread();
    fMaxBytesUsed = Math.max(fMaxBytesUsed, getBytesUsedSinceLastReset());
    fMaxCountUsed = Math.max(fMaxCountUsed, getResourcesCountUsedSinceLastReset());
    fResourcesUsed.clear();
  }

  @Override
  public void resourceCacheEvent(TLspGLResourceCacheEvent aEvent) {
    checkThread();
    // Store the resource and its size if it was added or used.
    if (aEvent.getType() == TLspGLResourceCacheEvent.Type.RESOURCE_ADDED ||
        aEvent.getType() == TLspGLResourceCacheEvent.Type.RESOURCE_USED) {
      ALspGLResource resource = aEvent.getResource();
      fResourcesUsed.put(resource, resource.getBytes());
    }
  }

  /**
   * Gets the total bytes of resources that are used
   * since the last call to {@link #reset()}. If a resource
   * was used multiple times, it is only counted once.
   *
   * @return the total number of bytes of used resources.
   */
  public long getBytesUsedSinceLastReset() {
    checkThread();
    long result = 0;
    for (Map.Entry<ALspGLResource, Long> entry : fResourcesUsed.entrySet()) {
      result += entry.getValue();
    }
    return result;
  }

  /**
   * Gets the number of resources used since the last
   * call to {@link #reset()}. If a resource
   * was used multiple times, it is only counted once.
   *
   * @return the number of resources used
   */
  public int getResourcesCountUsedSinceLastReset() {
    return fResourcesUsed.size();
  }

  /**
   * Returns the maximal total bytes used between two
   * consecutive calls to {@link #reset()}.
   *
   * @return the maximal bytes used.
   */
  public long getMaxBytesUsed() {
    return fMaxBytesUsed;
  }

  /**
   * Returns the maximal number of resources used
   * between two consecutive calls to {@link #reset()}.
   *
   * @return the maximal number of resources used
   */
  public int getMaxCountUsed() {
    return fMaxCountUsed;
  }
}
