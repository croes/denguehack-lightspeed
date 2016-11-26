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
package samples.gxy.common.layers;

import java.awt.EventQueue;
import java.lang.reflect.InvocationTargetException;

import com.luciad.shape.ILcdBounds;
import com.luciad.util.logging.ILcdLogger;
import com.luciad.util.logging.TLcdLoggerFactory;
import com.luciad.view.ILcdLayerTreeNode;
import com.luciad.view.TLcdLayerTreeNodeUtil;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.ILcdGXYView;
import com.luciad.view.gxy.TLcdFitGXYLayerInViewClipAction;
import com.luciad.view.gxy.TLcdGXYViewFitAction;

/**
 * Utility methods to add, remove, move, and fit layers in a view.
 */
public class GXYLayerUtil {

  private static ILcdLogger sLogger = TLcdLoggerFactory.getLogger(GXYLayerUtil.class.getName());

  /**
   * Adds the layer to the <code>ILcdGXYView</code> just beneath the grid layer,
   * and updates the view immediately.
   * It uses invokeAndWait to be sure the layers are added on the AWT thread and
   * the function returns after the layers are added.
   * Keep in mind that a 'null' layer is not added.
   * @param aGXYView the view to add the layer to.
   * @param aGXYLayer the layer to add.
   */
  public static void addGXYLayer(final ILcdGXYView aGXYView,
                                 final ILcdGXYLayer aGXYLayer) {
    addGXYLayer(aGXYView, aGXYLayer, true, true);
  }

  /**
   * Adds the layer to the <code>ILcdGXYView</code> just beneath the grid layer.
   * It uses invokeAndWait to ensure that the layers are added on the AWT thread.
   * <p/>
   * <em>The function also adjusts the view's setNumberOfCachedBackgroundLayers property,
   * so make sure to also call {@link #removeGXYLayer} when you want to remove a layer.</em>
   *
   * @param aGXYView the view to add the layer to.
   * @param aGXYLayer the layer to add.
   * @param aRepaintView indicates whether the view should be repainted after the layer
   *                     was added and moved beneath the grid layer. For better performance,
   *                     add all layers using <code>false</code>, except for the last one.
   * @param aBackgroundLayer if false the layer will be moved just beneath the grid layer. If true,
   *                     the layer will be moved as high up as possible, not interfering with dynamic layers.
   */
  public static void addGXYLayer(final ILcdGXYView aGXYView,
                                 final ILcdGXYLayer aGXYLayer,
                                 final boolean aRepaintView,
                                 final boolean aBackgroundLayer) {
    // do not add null layers
    if (aGXYLayer == null || aGXYView == null) {
      return;
    }
    try {
      invokeAndWait(new Runnable() {
        public void run() {
          // backup the auto update flag
          boolean previousAutoUpdate = aGXYView.isAutoUpdate();
          try {
            // do not update while adding and moving the layer
            aGXYView.setAutoUpdate(false);
            int previousLayerCount = aGXYView.layerCount();
            // add the layer to the map panel
            aGXYView.addGXYLayer(aGXYLayer);
            // set the number of cached background layers. This will have an effect on the
            // views performance.
            if (aBackgroundLayer) {
              int numberOfCachedBackgroundLayers = aGXYView.getNumberOfCachedBackgroundLayers();
              aGXYView.moveLayerAt(numberOfCachedBackgroundLayers, aGXYLayer);
              // increase the number of cached background layers to include the layer(s) added
              // for example in case of an ILcdLayerTreeNode multiple layers can be added at once
              aGXYView.setNumberOfCachedBackgroundLayers(numberOfCachedBackgroundLayers +
                                                         aGXYView.layerCount() - previousLayerCount);
            } else {
              // move the layer just beneath the grid layer
              aGXYView.moveLayerAt(aGXYView.layerCount() - 2, aGXYLayer);
            }
          } finally {
            // reset the auto update flag
            aGXYView.setAutoUpdate(previousAutoUpdate);
          }
          if (aRepaintView) {
            aGXYView.invalidateGXYLayer(aGXYLayer, aRepaintView, this,
                                        "Adding a layer beneath the grid layer.");
          }
        }
      });
    } catch (InterruptedException e) {
      sLogger.error("Layer cannot be added. " + e.getMessage());
      sLogger.error(e.getMessage(), e);
    } catch (InvocationTargetException e) {
      sLogger.error("Layer cannot be added. " + e.getMessage());
      sLogger.error(e.getMessage(), e);
    }
  }

  /**
   * Adds the layer <code>aLayer</code> as a new child layer to the node <code>aParentNode</code>.
   * It uses invokeAndWait to ensure that the layers are added on the AWT thread.
   * <p/>
   * <em>The function also adjusts the view's setNumberOfCachedBackgroundLayers property,
   * so make sure to also call {@link #removeGXYLayer} when you want to remove a layer.</em>
   *
   * @param aParentNode the node to which the layer is added. Notice that this node must be contained in the view
   * @param aGXYLayer the layer to be added
   * @param aGXYView the view
   * @param aRepaintView indicates whether the view should be repainted after the layer
   *                     was added. For better performance,
   *                     add all layers using <code>false</code>, except for the last one.
   */
  public static void addGXYLayer(final ILcdLayerTreeNode aParentNode,
                                 final ILcdGXYLayer aGXYLayer,
                                 final ILcdGXYView aGXYView,
                                 final boolean aRepaintView) {
    // do not add null layers
    if (aGXYLayer == null || aGXYView == null || aParentNode == null) {
      return;
    }
    try {
      invokeAndWait(new Runnable() {
        public void run() {
          // backup the auto update flag
          boolean previousAutoUpdate = aGXYView.isAutoUpdate();
          try {
            // do not update while adding and moving the layer
            aGXYView.setAutoUpdate(false);
            // add the layer to the node
            aParentNode.addLayer(aGXYLayer);
            // set the number of cached background layers. This will have an effect on the
            // view's performance.
            int numberOfCachedBackgroundLayers = aGXYView.getNumberOfCachedBackgroundLayers();
            // increase the number of cached background layers to include the layer(s) added
            // for example in case of an ILcdLayerTreeNode multiple layers can be added at once
            aGXYView.setNumberOfCachedBackgroundLayers(numberOfCachedBackgroundLayers +
                                                       TLcdLayerTreeNodeUtil.getLayerCount(aGXYLayer));
          } finally {
            // reset the auto update flag
            aGXYView.setAutoUpdate(previousAutoUpdate);
          }
          if (aRepaintView) {
            aGXYView.invalidateGXYLayer(aGXYLayer, aRepaintView, this,
                                        "Adding a layer as a child of " + aParentNode.getLabel());
          }
        }
      });
    } catch (InterruptedException e) {
      sLogger.error("Layer cannot be added. " + e.getMessage());
      sLogger.error(e.getMessage(), e);
    } catch (InvocationTargetException e) {
      sLogger.error("Layer cannot be added. " + e.getMessage());
      sLogger.error(e.getMessage(), e);
    }
  }

  /**
   * Removes the layer from the <code>ILcdGXYView</code>.
   * It uses invokeAndWait to be sure the layers are removed on the AWT thread and decreases the number
   * of cached background layers if necessary.
   *
   * @param aGXYView the view to remove the layer from.
   * @param aGXYLayer the layer to remove.
   * @param aRepaintView indicates whether the view should be repainted after the layer
   *                     was removed. For better performance, remove all layers using <code>false</code>,
   *                     except for the last one.
   */
  public static void removeGXYLayer(final ILcdGXYView aGXYView,
                                    final ILcdGXYLayer aGXYLayer,
                                    final boolean aRepaintView) {
    // do not remove null layers
    if (aGXYLayer == null || aGXYView == null) {
      return;
    }
    try {
      invokeAndWait(new Runnable() {
        public void run() {
          if (aGXYView.containsLayer(aGXYLayer)) {
            int index = aGXYView.indexOf(aGXYLayer);
            // backup the auto update flag
            boolean previousAutoUpdate = aGXYView.isAutoUpdate();
            try {
              // do not update while removing the layer
              aGXYView.setAutoUpdate(false);
              int previousLayerCount = aGXYView.layerCount();
              aGXYView.removeLayer(aGXYLayer);
              // check whether the layer was a background layer to update the number of cached background
              // layers if necessary.
              int numberOfCachedBackgroundLayers = aGXYView.getNumberOfCachedBackgroundLayers();
              if (index < numberOfCachedBackgroundLayers) {
                // decrease the number of cached background layers with the number of layers which were removed
                aGXYView.setNumberOfCachedBackgroundLayers(numberOfCachedBackgroundLayers - (previousLayerCount - aGXYView.layerCount()));
              }
            } finally {
              // reset the auto update flag
              aGXYView.setAutoUpdate(previousAutoUpdate);
            }
            if (aRepaintView) {
              aGXYView.invalidate(aRepaintView, this, "Removing layer from view.");
            }
          }
        }
      });
    } catch (InterruptedException e) {
      sLogger.error("Layer cannot be removed. " + e.getMessage());
    } catch (InvocationTargetException e) {
      sLogger.error("Layer cannot be removed. " + e.getMessage());
    }
  }

  /**
   * Move the specified layer to the given position in the <code>ILcdGXYView</code>.
   * This is done on the AWT event queue.
   * Keep in mind that a 'null' layers is not moved.
   * @param aGXYView the view in which the layer is contained
   * @param aPosition the position in the view where the layer should be moved to. This should be smaller than
   * the number of layers in the view.
   * @param aGXYLayer the layer to move.
   */
  public static void moveGXYLayer(final ILcdGXYView aGXYView,
                                  final int aPosition,
                                  final ILcdGXYLayer aGXYLayer) {
    // do not move null layers
    if (aGXYLayer == null) {
      return;
    }
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        // move the layer just beneath the grid layer
        aGXYView.moveLayerAt(aPosition, aGXYLayer);
      }
    });
  }

  /**
   * Rescales and pans the view so that the objects in the layer are visible in the view..
   * This is done on the AWT event queue.
   * Keep in mind that a 'null' layer is not fitted.
   * @param aGXYView the view that should be fitted. It should contain the layer passed.
   * @param aGXYLayer the layer around which the view should be fitted. It should be part of the view passed.
   */
  public static void fitGXYLayer(final ILcdGXYView aGXYView,
                                 final ILcdGXYLayer aGXYLayer) {
    // do not fit null layers
    if (aGXYLayer == null) {
      return;
    }
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        TLcdGXYViewFitAction fitAction = new TLcdGXYViewFitAction();
        fitAction.setFitToChildren(true);
        fitAction.fitGXYLayer(aGXYLayer, aGXYView);
      }
    });
  }

  /**
   * Rescales and pans the view so that the objects in the layers are visible in the view..
   * This is done on the AWT event queue.
   * @param aGXYView   the view that should be fitted. It should contain the layer passed.
   * @param aGXYLayers the layers around which the view should be fitted. These should be part of the view passed.
   */
  public static void fitGXYLayers(final ILcdGXYView aGXYView,
                                  final ILcdGXYLayer[] aGXYLayers) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        TLcdGXYViewFitAction fitAction = new TLcdGXYViewFitAction();
        fitAction.fitGXYLayers(aGXYLayers, aGXYView, null);
      }
    });
  }

  /**
   * Rescales and pans the view so that the given bounds are visible in the view..
   * This is done on the AWT event queue.
   * Keep in mind that a 'null' layer or a 'null' bounds are not fitted.
   * @param aGXYView the view that should be fitted. It should contain the layer passed.
   * @param aGXYLayer the layer in whose model reference the bounds are expressed. It should be part of the view passed.
   * @param aBounds the bounds to fit to.
   */
  public static void fitGXYLayer(final ILcdGXYView aGXYView,
                                 final ILcdGXYLayer aGXYLayer,
                                 final ILcdBounds aBounds) {
    // do not fit null layers
    if (aGXYLayer == null) {
      return;
    }
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        TLcdFitGXYLayerInViewClipAction.doFit(aGXYLayer, aGXYView, aBounds);
      }
    });
  }

  private static void invokeAndWait(Runnable doRun)
      throws InterruptedException, InvocationTargetException {
    if (EventQueue.isDispatchThread()) {
      doRun.run();
    } else {
      EventQueue.invokeAndWait(doRun);
    }
  }


}
