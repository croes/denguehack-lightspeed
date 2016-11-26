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
package samples.decoder.asterix.lightspeed.trackdisplay;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.luciad.format.asterix.TLcdASTERIXTrack;
import com.luciad.model.ILcdModel;
import com.luciad.shape.shape2D.TLcdXYBounds;
import com.luciad.shape.shape3D.TLcdXYZPoint;
import com.luciad.transformation.ILcdModelXYZWorldTransformation;
import com.luciad.util.TLcdOutOfBoundsException;
import com.luciad.util.concurrent.TLcdLockUtil;
import com.luciad.view.ALcdWeakLayeredListener;
import com.luciad.view.TLcdLayeredEvent;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.camera.ALspViewXYZWorldTransformation;
import com.luciad.view.lightspeed.label.ILspLabelObstacleProvider;
import com.luciad.view.lightspeed.label.TLspLabelObstacle;
import com.luciad.view.lightspeed.layer.ILspInteractivePaintableLayer;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.TLspPaintRepresentationState;
import com.luciad.view.lightspeed.query.TLspPaintedObjectsQuery;

/**
 * This label obstacle provider returns obstacles for all tracks in the view. It only returns label obstacles
 * with bounds that correspond with the main track icons, and not its history points.
 */
class ASTERIXTrackLabelObstacleProvider implements ILspLabelObstacleProvider {

  // Consistent with the non-highlighted track icon used in ASTERIXTrackStyler
  private static final double TRACK_SIZE = 7.0;

  // Use a CopyOnWriteArrayList to avoid thread-safety problems. This list is read from a background thread
  // by the #getLabelObstacles method, and updated on the paint thread (EDT) by LayeredListener.
  private final List<ILspInteractivePaintableLayer> fLayers = new CopyOnWriteArrayList<>();

  public ASTERIXTrackLabelObstacleProvider(ILspView aView) {
    aView.addLayeredListener(new LayeredListener(this));
  }

  @Override
  public void getLabelObstacles(ILspView aView, List<TLspLabelObstacle> aObstaclesSFCT) {
    TLcdXYZPoint worldPoint = new TLcdXYZPoint();
    TLcdXYZPoint viewPoint = new TLcdXYZPoint();
    TLcdXYBounds viewBounds = new TLcdXYBounds(0, 0, aView.getWidth(), aView.getHeight());
    for (ILspInteractivePaintableLayer layer : fLayers) {
      TLspContext context = new TLspContext(layer, aView);
      Collection<Object> result = layer.query(new TLspPaintedObjectsQuery(TLspPaintRepresentationState.REGULAR_BODY, viewBounds), context);
      ILcdModel model = layer.getModel();
      TLcdLockUtil.readLock(model);
      try {
        for (Object object : result) {
          if (!(object instanceof TLcdASTERIXTrack)) {
            throw new IllegalArgumentException("Only asterix tracks are supported");
          }
          // Convert the track location to view coordinates, and create an obstacle based it.
          TLcdASTERIXTrack track = (TLcdASTERIXTrack) object;
          ILcdModelXYZWorldTransformation m2w = context.getModelXYZWorldTransformation();
          ALspViewXYZWorldTransformation v2w = context.getViewXYZWorldTransformation();
          try {
            m2w.modelPoint2worldSFCT(track, worldPoint);
            v2w.worldPoint2ViewSFCT(worldPoint, viewPoint);
            TLspLabelObstacle obstacle = new TLspLabelObstacle(
                viewPoint.getX() - TRACK_SIZE * 0.5,
                viewPoint.getY() - TRACK_SIZE * 0.5,
                TRACK_SIZE, TRACK_SIZE, 0.0
            );
            aObstaclesSFCT.add(obstacle);
          } catch (TLcdOutOfBoundsException ignored) {
            // Do nothing
          }
        }
      } finally {
        TLcdLockUtil.readUnlock(model);
      }
    }
  }

  private static class LayeredListener extends ALcdWeakLayeredListener<ASTERIXTrackLabelObstacleProvider> {

    public LayeredListener(ASTERIXTrackLabelObstacleProvider aObstacleProvider) {
      super(aObstacleProvider);
    }

    @Override
    protected void layeredStateChangeImpl(ASTERIXTrackLabelObstacleProvider aObstacleProvider, TLcdLayeredEvent aLayeredEvent) {
      // Make sure we only perform actions for track layers.
      if (!MainPanel.LAYER_FILTER.accept((ILspLayer) aLayeredEvent.getLayer())) {
        return;
      }
      // This is a safe cast, since ASTERIXTrackLayerFactory always creates ILspInteractivePaintableLayer instances.
      ILspInteractivePaintableLayer layer = (ILspInteractivePaintableLayer) aLayeredEvent.getLayer();
      if (aLayeredEvent.getID() == TLcdLayeredEvent.LAYER_ADDED) {
        aObstacleProvider.fLayers.add(layer);
      } else if (aLayeredEvent.getID() == TLcdLayeredEvent.LAYER_REMOVED) {
        aObstacleProvider.fLayers.remove(layer);
      }
    }
  }
}
