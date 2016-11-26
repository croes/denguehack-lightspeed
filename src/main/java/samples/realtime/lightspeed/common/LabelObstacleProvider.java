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
package samples.realtime.lightspeed.common;

import java.util.List;

import com.luciad.shape.ILcdBounds;
import com.luciad.shape.shape2D.TLcdXYBounds;
import com.luciad.shape.shape3D.TLcdXYZBounds;
import com.luciad.util.logging.ILcdLogger;
import com.luciad.util.logging.TLcdLoggerFactory;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.camera.ALspViewXYZWorldTransformation;
import com.luciad.view.lightspeed.label.ILspLabelObstacleProvider;
import com.luciad.view.lightspeed.label.TLspLabelObstacle;
import com.luciad.view.lightspeed.layer.ALspBoundsInfo;
import com.luciad.view.lightspeed.layer.ALspViewBoundsInfo;
import com.luciad.view.lightspeed.layer.ALspWorldBoundsInfo;
import com.luciad.view.lightspeed.layer.ILspInteractivePaintableLayer;
import com.luciad.view.lightspeed.layer.TLspPaintRepresentation;
import com.luciad.view.lightspeed.layer.TLspPaintRepresentationState;
import com.luciad.view.lightspeed.layer.TLspPaintState;
import com.luciad.view.lightspeed.query.TLspPaintedObjectsBoundsQuery;

/**
 * A label obstacle provider that uses the visual representations of the visible domain
 * objects in a layer as obstacles.
 * <p>
 * A typical usage is that labels should not overlap with icon visualizations of domain objects.
 * </p>
 * <p>
 * Usually, domain objects are painted as {@link TLspPaintRepresentation#BODY} representations.
 * </p>
 * <p>
 * No
 * </p>
 *
 * @since 2012.0
 */
public class LabelObstacleProvider implements ILspLabelObstacleProvider {

  private static final ILcdLogger sLogger = TLcdLoggerFactory.getLogger(LabelObstacleProvider.class);

  private final ILspInteractivePaintableLayer fLayer;
  private final TLspPaintRepresentation[] fPaintRepresentations;

  /**
   * Creates an obstacle provider using the given layer and paint representation.
   *
   * @param aLayer The layer to use the domain objects from.
   * @param aPaintRepresentations The paint representations to include, for example {@link TLspPaintRepresentation#BODY}
   */
  public LabelObstacleProvider(ILspInteractivePaintableLayer aLayer, TLspPaintRepresentation... aPaintRepresentations) {
    fLayer = aLayer;
    fPaintRepresentations = aPaintRepresentations;
  }

  @Override
  public void getLabelObstacles(ILspView aView, List<TLspLabelObstacle> aObstaclesSFCT) {
    TLspContext context = new TLspContext(fLayer, aView);
    ILcdBounds viewBounds = new TLcdXYBounds(0, 0, aView.getWidth(), aView.getHeight());
    TLcdXYZBounds tmpBounds = new TLcdXYZBounds();
    ALspViewXYZWorldTransformation v2w = aView.getViewXYZWorldTransformation();

    int tested = 0;
    int added = 0;

    for (TLspPaintRepresentation paintRepresentation : fPaintRepresentations) {
      for (TLspPaintState paintState : TLspPaintState.values()) {
        TLspPaintRepresentationState paintRepresentationState =
            TLspPaintRepresentationState.getInstance(paintRepresentation, paintState);
        for (ALspBoundsInfo boundsInfo : fLayer.query(new TLspPaintedObjectsBoundsQuery(paintRepresentationState, viewBounds, 0.0), context)) {
          if (boundsInfo instanceof ALspViewBoundsInfo) {
            ILcdBounds result = ((ALspViewBoundsInfo) boundsInfo).getViewBounds();
            tmpBounds.move2D(result.getLocation());
            tmpBounds.setWidth(result.getWidth());
            tmpBounds.setHeight(result.getHeight());
            tmpBounds.setDepth(result.getDepth());
          } else if (boundsInfo instanceof ALspWorldBoundsInfo) {
            v2w.worldBounds2viewSFCT(((ALspWorldBoundsInfo) boundsInfo).getWorldBounds(), tmpBounds);
          }
          if (tmpBounds.interacts2D(viewBounds)) {
            aObstaclesSFCT.add(new TLspLabelObstacle(tmpBounds.getLocation().getX(), tmpBounds.getLocation().getY(),
                                                     tmpBounds.getWidth(), tmpBounds.getHeight(), 0));
            added++;
          }
          tested++;
        }
      }
    }

    sLogger.trace("Added " + added + " obstacles out of " + tested + " candidates in view");
  }
}
