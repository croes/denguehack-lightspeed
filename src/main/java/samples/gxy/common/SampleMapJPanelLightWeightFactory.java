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
package samples.gxy.common;

import java.awt.Color;

import com.luciad.shape.ILcdBounds;
import com.luciad.view.gxy.ILcdGXYView;
import com.luciad.view.gxy.labeling.TLcdGXYAsynchronousLabelPlacer;
import com.luciad.view.gxy.labeling.algorithm.TLcdGXYCompositeLabelingAlgorithm;
import com.luciad.view.gxy.swing.TLcdGXYScaleIndicator;
import com.luciad.view.map.TLcdMapJPanelLightWeight;

import samples.gxy.common.labels.GXYLabelingAlgorithmProvider;
import samples.gxy.grid.GridLayerFactory;

/**
 * Factory implementation that creates <code>TLcdMapJPanelLightWeight</code>s with
 * some default sample settings. It adds the following behavior:
 * <ul>
 *   <li>background color</li>
 *   <li>scale icon in the lower right corner</li>
 *   <li>label placer for label decluttering</li>
 *   <li>grid layer with custom colors</li>
 *   <li>fitting on the specified bounds</li>
 * </ul>
 */
public class SampleMapJPanelLightWeightFactory {

  public static TLcdMapJPanelLightWeight createMapJPanelLightWeight() {
    return createMapJPanelLightWeight(null);
  }

  public static TLcdMapJPanelLightWeight createMapJPanelLightWeight(ILcdBounds aInitialBoundsToFit) {
    TLcdMapJPanelLightWeight map = new TLcdMapJPanelLightWeight();

    // Set background color to represent sea.
    map.setBackground(new Color(140, 150, 210));

    // Set scale icon
    map.putCornerIcon(new TLcdGXYScaleIndicator(map), ILcdGXYView.LOWERRIGHT);

    // Create a view label placer, that will take care of placing the labels.
    // It leaves the drawing of the labels to the layer.
    GXYLabelingAlgorithmProvider provider = new GXYLabelingAlgorithmProvider();
    TLcdGXYCompositeLabelingAlgorithm algorithm = new TLcdGXYCompositeLabelingAlgorithm(provider);
    TLcdGXYAsynchronousLabelPlacer view_label_placer = new TLcdGXYAsynchronousLabelPlacer(algorithm);
    map.setGXYViewLabelPlacer(view_label_placer);

    // Set up a grid layer.
    map.setGridLayer(GridLayerFactory.createLonLatGridLayer());

    // Initially fit on the grid layer using given bounds.
    SampleMapJPanelFactory.scheduleInitialFit(map, aInitialBoundsToFit);

    return map;
  }
}
