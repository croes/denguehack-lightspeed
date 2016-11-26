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
package samples.wms.server;

import java.awt.Color;
import java.util.List;

import com.luciad.ogc.sld.view.gxy.TLcdSLDLabelingAlgorithm;
import com.luciad.shape.ILcdBounds;
import com.luciad.view.ILcdXYWorldReference;
import com.luciad.view.TLcdLabelIdentifier;
import com.luciad.view.gxy.ILcdGXYView;
import com.luciad.view.gxy.TLcdGXYViewBufferedImage;
import com.luciad.view.gxy.labeling.TLcdGXYLabelPlacer;
import com.luciad.view.gxy.labeling.algorithm.ILcdGXYLabelLabelingAlgorithmProvider;
import com.luciad.view.gxy.labeling.algorithm.ILcdGXYLabelingAlgorithm;
import com.luciad.view.gxy.labeling.algorithm.TLcdGXYCompositeLabelingAlgorithm;
import com.luciad.view.gxy.labeling.algorithm.discrete.ALcdGXYDiscretePlacementsLabelingAlgorithm;
import com.luciad.view.gxy.labeling.algorithm.discrete.ALcdGXYDiscretePlacementsLabelingAlgorithmWrapper;
import com.luciad.view.gxy.labeling.algorithm.discrete.TLcdGXYLabelPainterLocationLabelingAlgorithm;
import com.luciad.view.gxy.labeling.algorithm.discrete.TLcdGXYLocationListLabelingAlgorithm;
import com.luciad.view.labeling.algorithm.ILcdLabelConflictChecker;
import com.luciad.view.labeling.algorithm.TLcdCollectedLabelInfo;
import com.luciad.view.labeling.algorithm.TLcdCollectedLabelInfoList;
import com.luciad.view.labeling.algorithm.TLcdLabelPlacement;
import com.luciad.view.labeling.algorithm.discrete.ILcdLabelPlacementEvaluator;
import com.luciad.wms.server.TLcdWMSGXYViewFactory;
import com.luciad.wms.server.TLcdWMSRequestContext;

/**
 * Extension of <code>TLcdWMSGXYViewFactory</code> that configures a view label painter on each view
 * (map request) to achieve label decluttering and to avoid labels being painted
 * over the view border; the latter avoids  the truncated label behavior
 * when query the WMS with a tiled approach.
 */
class WMSGXYViewFactory extends TLcdWMSGXYViewFactory {

  @Override
  public TLcdGXYViewBufferedImage createGXYView(boolean aTransparent, int aWidth, int aHeight, ILcdBounds aWorldBounds,
                                                ILcdXYWorldReference aXYWorldReference, Color aBackgroundColor,
                                                TLcdWMSRequestContext aRequestContext) {
    TLcdGXYViewBufferedImage view = super.createGXYView(aTransparent, aWidth, aHeight, aWorldBounds,
                                                        aXYWorldReference, aBackgroundColor, aRequestContext);
    // Define a labeling algorithm.
    ILcdGXYLabelingAlgorithm algorithm = new TLcdGXYCompositeLabelingAlgorithm(new WMSLabelingAlgorithmProvider());

    // Configure the labeling algorithm on a label placer and set it on the view.
    TLcdGXYLabelPlacer labelPlacer = new TLcdGXYLabelPlacer(algorithm);
    view.setGXYViewLabelPlacer(labelPlacer);

    return view;
  }

  private static class WMSLabelingAlgorithmProvider implements
                                                    ILcdGXYLabelLabelingAlgorithmProvider<ILcdGXYLabelingAlgorithm> {

    private TLcdSLDLabelingAlgorithm fSLDAlgorithm;
    private ILcdGXYLabelingAlgorithm fDefault = new TLcdGXYLabelPainterLocationLabelingAlgorithm();

    public WMSLabelingAlgorithmProvider() {
      // We define two labeling algorithms for use inside the WMS:

      // 1. A labeling algorithm specific for SLD map requests. This algorithm is capable
      // to work together with the SLD rendering engine.
      fSLDAlgorithm = new TLcdSLDLabelingAlgorithm();

      // 2. A default labeling algorithm for all non-SLD map requests: a location list
      // labeling algorithm to determine the label positions on the view.
      // In this sample, we use a location list labeling algorithm, adapted as follows:
      // 2.1 restrict the label positions to one direction (e.g., NORTH), to end up with
      // one deterministic position for the label. This avoids duplicate labels across
      // neighbouring tiles.
      TLcdGXYLocationListLabelingAlgorithm algorithm = new TLcdGXYLocationListLabelingAlgorithm();
      algorithm.setLocationList(new TLcdGXYLocationListLabelingAlgorithm.Location[]{
          TLcdGXYLocationListLabelingAlgorithm.Location.NORTH});
      // 2.2 only paint the labels when they do not interact with the view bounds
      fDefault = new LabelingAlgorithmWrapper(algorithm);
    }

    public ILcdGXYLabelingAlgorithm getLabelingAlgorithm(TLcdLabelIdentifier aLabel) {
      if (fSLDAlgorithm.canHandle(aLabel)) {
        return fSLDAlgorithm;
      }
      return fDefault;
    }
  }

  private static class LabelingAlgorithmWrapper extends ALcdGXYDiscretePlacementsLabelingAlgorithmWrapper {

    protected LabelingAlgorithmWrapper(ALcdGXYDiscretePlacementsLabelingAlgorithm aDiscretePlacementsLabelingAlgorithm) {
      super(aDiscretePlacementsLabelingAlgorithm);
    }

    @Override
    protected ILcdLabelPlacementEvaluator createLabelPlacementEvaluator(TLcdCollectedLabelInfoList aCollectedLabelInfoList,
                                                                        ILcdLabelConflictChecker aLabelConflictChecker,
                                                                        ILcdGXYView aGXYView) {
      ILcdLabelPlacementEvaluator placementEvaluator = super.createLabelPlacementEvaluator(aCollectedLabelInfoList, aLabelConflictChecker, aGXYView);
      return new PlacementEvaluatorWrapper(placementEvaluator);
    }
  }

  private static class PlacementEvaluatorWrapper implements ILcdLabelPlacementEvaluator {

    private ILcdLabelPlacementEvaluator fDelegate;

    public PlacementEvaluatorWrapper(ILcdLabelPlacementEvaluator aDelegate) {
      fDelegate = aDelegate;
    }

    public PlacementResult evaluatePlacement(TLcdLabelPlacement aLabelPlacement,
                                             ILcdLabelConflictChecker.Conflict aConflict,
                                             List<TLcdLabelPlacement> aLabelPlacements) {
      if (aConflict != null && aConflict.isPartiallyOutsideView()) {
        // View bounds conflict => try a new placement!
        return PlacementResult.TRY_NEW_PLACEMENT;
      } else {
        return fDelegate.evaluatePlacement(aLabelPlacement, aConflict, aLabelPlacements);
      }
    }

    public void placementApplied(TLcdLabelPlacement aLabelPlacement, List<TLcdLabelPlacement> aLabelPlacements) {
      fDelegate.placementApplied(aLabelPlacement, aLabelPlacements);
    }

    public void noPlacementApplied(TLcdCollectedLabelInfo aCollectedLabelInfo, List<TLcdLabelPlacement> aLabelPlacements) {
      fDelegate.noPlacementApplied(aCollectedLabelInfo, aLabelPlacements);
    }
  }
}
