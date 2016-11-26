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
package samples.gxy.labels.createalgorithm;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.Iterator;
import java.util.List;

import com.luciad.util.TLcdNoBoundsException;
import com.luciad.view.TLcdLabelIdentifier;
import com.luciad.view.TLcdLabelLocation;
import com.luciad.view.gxy.ILcdGXYContext;
import com.luciad.view.gxy.ILcdGXYLabelPainter;
import com.luciad.view.gxy.ILcdGXYLabelPainter2;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.ILcdGXYView;
import com.luciad.view.gxy.TLcdGXYContext;
import com.luciad.view.gxy.labeling.algorithm.discrete.ALcdGXYDiscretePlacementsLabelingAlgorithm;
import com.luciad.view.gxy.labeling.algorithm.discrete.ALcdGXYDiscretePlacementsLabelingAlgorithmWrapper;
import com.luciad.view.gxy.labeling.util.TLcdGXYCollectedLabelInfoUtil;
import com.luciad.view.labeling.algorithm.ILcdLabelConflictChecker;
import com.luciad.view.labeling.algorithm.TLcdCollectedLabelInfo;
import com.luciad.view.labeling.algorithm.TLcdCollectedLabelInfoList;
import com.luciad.view.labeling.algorithm.TLcdLabelPlacement;
import com.luciad.view.labeling.util.TLcdLabelingUtil;

/**
 * This wrapper adds more label placement possibilities when labeling by trying extra
 * placements using a different number of displayed properties or a different font size.
 *
 * To do this, the wrapper uses LabelDetailLabelLocation, an extension of TLcdLabelLocation that
 * adds a property count setting and a font size. The algorithm relies on the label painter to
 * interpret these label locations, and adjust the number of displayed properties and font size
 * accordingly. The algorithm also relies on the layer's ALcdLabelLocations to create
 * LabelDetailLabelLocation objects.
 *
 * This wrapper doesn't work correctly when combining it with other wrappers that adjust the
 * dimensions of the label.
 */
public class LabelDetailAlgorithmWrapper extends ALcdGXYDiscretePlacementsLabelingAlgorithmWrapper {

  // The keys we will use to store the extra label dimensions. The second part of the keys
  // denotes the font size, the third part denotes the number of displayed properties in the label.
  private static final String DIMENSION_LARGE_3_KEY = "dimensionLarge3Key";
  private static final String DIMENSION_LARGE_2_KEY = "dimensionLarge2Key";
  private static final String DIMENSION_LARGE_1_KEY = "dimensionLarge1Key";
  private static final String DIMENSION_SMALL_1_KEY = "dimensionSmall1Key";

  public LabelDetailAlgorithmWrapper(ALcdGXYDiscretePlacementsLabelingAlgorithm aDelegate) {
    super(aDelegate);
  }

  /**
   * Here we collect all painter and label painter information that we cannot directly access
   * in the compute step. This allows accessing the iterators in a separate label placement thread
   * (e.g. by TLcdGXYAsynchronousLabelPlacer).
   */
  @Override
  public TLcdCollectedLabelInfoList collectLabelInfo(List<TLcdLabelIdentifier> aLabelToCollect, Graphics aGraphics, ILcdGXYView aGXYView) {
    TLcdCollectedLabelInfoList label_infos = super.collectLabelInfo(aLabelToCollect, aGraphics, aGXYView);

    // Retrieves the object's anchor point and label anchor offset that will be used to create label
    // placements in the iterator.
    TLcdGXYCollectedLabelInfoUtil.addLabelLocationPrototypeDataSFCT(label_infos, aGXYView);
    TLcdGXYCollectedLabelInfoUtil.addDimensionAndLabelAnchorOffsetDataSFCT(label_infos, aGXYView, aGraphics);
    TLcdGXYCollectedLabelInfoUtil.addLabelAnchorOffsetDataSFCT(label_infos, aGXYView, aGraphics);

    // Calculates and stores new label dimensions based on the number of displayed properties in the
    // label and different font sizes.
    addDimensions(label_infos.getLabels(), aGraphics, aGXYView);

    // We remove the previous label location. Since this previous label location already contains
    // a different number of displayed properties or a different font size added by this wrapper,
    // it would make this information leak into the wrapped algorithm. This might cause unexpected
    // behavior. By removing the previous placement, we make sure that the wrapped algorithm doesn't
    // use it. Another solution would be to undo the font size change from the previous label
    // placement.
    for (TLcdCollectedLabelInfo label : label_infos.getLabels()) {
      label.setPreviousLabelPlacement(null);
      label.setPreviousPainted(false);
    }

    return label_infos;
  }

  private void addDimensions(List<TLcdCollectedLabelInfo> aInfoList, Graphics aGraphics, ILcdGXYView aGXYView) {
    TLcdGXYContext context = new TLcdGXYContext();
    for (TLcdCollectedLabelInfo label_info : aInfoList) {
      // Resets the context if needed.
      if (context.getGXYLayer() != label_info.getLabeledObject().getLayer()) {
        context.resetFor((ILcdGXYLayer) label_info.getLabeledObject().getLayer(), aGXYView);
      }

      Dimension[] dimensions = getDimensions(label_info, aGraphics, context);
      if (dimensions == null) {
        continue;
      }

      label_info.getProperties().put(DIMENSION_LARGE_3_KEY, dimensions[0]);
      label_info.getProperties().put(DIMENSION_LARGE_2_KEY, dimensions[1]);
      label_info.getProperties().put(DIMENSION_LARGE_1_KEY, dimensions[2]);
      label_info.getProperties().put(DIMENSION_SMALL_1_KEY, dimensions[3]);
    }
  }

  private LabelDetailLabelLocation createTestLabelLocation(TLcdCollectedLabelInfo aLabel) {
    try {
      TLcdLabelLocation prototype = aLabel.getLabelLocationPrototype();
      if (!(prototype instanceof LabelDetailLabelLocation)) {
        return null;
      }

      LabelDetailLabelLocation location = (LabelDetailLabelLocation) prototype.clone();
      location.setLocationIndex(-1);
      location.setLocationX(0);
      location.setLocationY(0);
      location.setRotation(0);

      return location;
    } catch (CloneNotSupportedException e) {
      throw new RuntimeException("Could not create label location");
    }
  }

  private Dimension[] getDimensions(TLcdCollectedLabelInfo aLabelInfo, Graphics aGraphics, ILcdGXYContext aGXYContext) {
    // Creates a label location that will be used to determine the dimensions of the labels.
    LabelDetailLabelLocation location = createTestLabelLocation(aLabelInfo);
    if (location == null) {
      return null;
    }

    ILcdGXYLabelPainter label_painter = aGXYContext.getGXYLayer().getGXYLabelPainter(aLabelInfo.getLabelIdentifier().getDomainObject());
    if (!(label_painter instanceof ILcdGXYLabelPainter2)) {
      throw new RuntimeException("The label painter should be an ILcdGXYLabelPainter2");
    }
    ILcdGXYLabelPainter2 label_painter2 = (ILcdGXYLabelPainter2) label_painter;

    label_painter2.setLabelIndex(aLabelInfo.getLabelIdentifier().getLabelIndex());
    label_painter2.setSubLabelIndex(aLabelInfo.getLabelIdentifier().getSubLabelIndex());

    try {
      Rectangle bounds = new Rectangle();

      location.setFontSize(LabelDetailLabelLocation.FontSize.LARGE);
      location.setPropertyCount(3);
      label_painter2.setLabelLocation(location);
      label_painter2.labelBoundsSFCT(aGraphics, ILcdGXYLabelPainter2.BODY | ILcdGXYLabelPainter2.DEFAULT, aGXYContext, bounds);
      Dimension large_3 = new Dimension(bounds.width, bounds.height);

      location.setFontSize(LabelDetailLabelLocation.FontSize.LARGE);
      location.setPropertyCount(2);
      label_painter2.setLabelLocation(location);
      label_painter2.labelBoundsSFCT(aGraphics, ILcdGXYLabelPainter2.BODY | ILcdGXYLabelPainter2.DEFAULT, aGXYContext, bounds);
      Dimension large_2 = new Dimension(bounds.width, bounds.height);

      location.setFontSize(LabelDetailLabelLocation.FontSize.LARGE);
      location.setPropertyCount(1);
      label_painter2.setLabelLocation(location);
      label_painter2.labelBoundsSFCT(aGraphics, ILcdGXYLabelPainter2.BODY | ILcdGXYLabelPainter2.DEFAULT, aGXYContext, bounds);
      Dimension large_1 = new Dimension(bounds.width, bounds.height);

      location.setFontSize(LabelDetailLabelLocation.FontSize.SMALL);
      location.setPropertyCount(1);
      label_painter2.setLabelLocation(location);
      label_painter2.labelBoundsSFCT(aGraphics, ILcdGXYLabelPainter2.BODY | ILcdGXYLabelPainter2.DEFAULT, aGXYContext, bounds);
      Dimension small_1 = new Dimension(bounds.width, bounds.height);

      return new Dimension[]{large_3, large_2, large_1, small_1};
    } catch (TLcdNoBoundsException ignored) {
    }
    return null;
  }

  @Override
  protected Iterator<TLcdLabelPlacement> createLabelPlacementIterator(TLcdCollectedLabelInfo aLabel, TLcdCollectedLabelInfoList aLabelInfoList, ILcdLabelConflictChecker aBoundsConflictChecker, ILcdGXYView aView) {
    Iterator<TLcdLabelPlacement> delegate = super.createLabelPlacementIterator(aLabel, aLabelInfoList, aBoundsConflictChecker, aView);
    return new FontSizeLabelPlacementIterator(delegate);
  }

  private static class FontSizeLabelPlacementIterator implements Iterator<TLcdLabelPlacement> {

    private Iterator<TLcdLabelPlacement> fDelegate;
    private TLcdLabelPlacement fNextPlacement;

    private int fNextPlacementIndex = 0;
    private TLcdLabelPlacement fOriginalPlacement;

    public FontSizeLabelPlacementIterator(Iterator<TLcdLabelPlacement> aDelegate) {
      fDelegate = aDelegate;
    }

    public boolean hasNext() {
      fNextPlacement = getNextPlacement();
      return fNextPlacement != null;
    }

    public TLcdLabelPlacement next() {
      TLcdLabelPlacement next_placement = fNextPlacement;
      fNextPlacement = null;
      return next_placement;
    }

    public void remove() {
      throw new UnsupportedOperationException("Removing label placements is not supported for this Iterator : " + this);
    }

    private TLcdLabelPlacement getNextPlacement() {
      if (fNextPlacement != null) {
        return fNextPlacement;
      }

      int placement_index = fNextPlacementIndex;
      fNextPlacementIndex++;

      if (placement_index == 0) {
        // The other placements will be based on this one.
        if (!fDelegate.hasNext()) {
          return null;
        }
        TLcdLabelPlacement placement = fDelegate.next();
        if (!(placement.getLabelLocation() instanceof LabelDetailLabelLocation)) {
          // Only try to modify the label properties or font size when a LabelDetailLabelLocation
          // is used. Otherwise just return the original placement.
          fNextPlacementIndex = 0;
          return placement;
        }
        fOriginalPlacement = placement.clone();
      }

      if (placement_index == 0) {
        // Try the large font size
        Dimension dimension_large_3 = (Dimension) fOriginalPlacement.getLabel().getProperties().get(DIMENSION_LARGE_3_KEY);
        TLcdLabelPlacement placement = createPlacement(fOriginalPlacement, dimension_large_3, 3, LabelDetailLabelLocation.FontSize.LARGE);
        if (placement == null) {
          return getNextPlacement();
        }
        return placement;
      }
      if (placement_index == 1) {
        // Try the large font size
        Dimension dimension_large_2 = (Dimension) fOriginalPlacement.getLabel().getProperties().get(DIMENSION_LARGE_2_KEY);
        TLcdLabelPlacement placement = createPlacement(fOriginalPlacement, dimension_large_2, 2, LabelDetailLabelLocation.FontSize.LARGE);
        if (placement == null) {
          return getNextPlacement();
        }
        return placement;
      }
      if (placement_index == 2) {
        // Try the large font size
        Dimension dimension_large_1 = (Dimension) fOriginalPlacement.getLabel().getProperties().get(DIMENSION_LARGE_1_KEY);
        TLcdLabelPlacement placement = createPlacement(fOriginalPlacement, dimension_large_1, 1, LabelDetailLabelLocation.FontSize.LARGE);
        if (placement == null) {
          return getNextPlacement();
        }
        return placement;
      }
      if (placement_index == 3) {
        // Try the large font size
        Dimension dimension_small_1 = (Dimension) fOriginalPlacement.getLabel().getProperties().get(DIMENSION_SMALL_1_KEY);
        TLcdLabelPlacement placement = createPlacement(fOriginalPlacement, dimension_small_1, 1, LabelDetailLabelLocation.FontSize.SMALL);
        if (placement == null) {
          return getNextPlacement();
        }
        return placement;
      } else {
        fNextPlacementIndex = 0;
        return getNextPlacement();
      }
    }

    private TLcdLabelPlacement createPlacement(TLcdLabelPlacement aOriginalPlacement, Dimension aDimension, int aPropertyCount, LabelDetailLabelLocation.FontSize aFontSize) {
      TLcdLabelPlacement new_placement = aOriginalPlacement.clone();

      // Adjust the width of the placement bounds
      new_placement.setWidth((int) (aDimension.getWidth()));
      new_placement.setHeight((int) (aDimension.getHeight()));

      // Adjust the upper left point of the placement bounds to take into account the change in width
      // and height. The label is scaled around its middle.
      double offset_x = (aOriginalPlacement.getWidth() - new_placement.getWidth()) / 2.0;
      double offset_y = (aOriginalPlacement.getHeight() - new_placement.getHeight()) / 2.0;
      double dx = new_placement.getCosRotation() * offset_x - new_placement.getSinRotation() * offset_y;
      double dy = new_placement.getSinRotation() * offset_x + new_placement.getCosRotation() * offset_y;
      new_placement.setX(new_placement.getX() + (int) dx);
      new_placement.setY(new_placement.getY() + (int) dy);

      // Adjust the font size in the FontSizeLabelLocation
      LabelDetailLabelLocation new_location = (LabelDetailLabelLocation) new_placement.getLabelLocation();
      new_location.setFontSize(aFontSize);
      new_location.setPropertyCount(aPropertyCount);

      // Adjust the label location based on the placement bounds.
      Point label_anchor_offset = retrieveLabelAnchorOffset(new_placement);
      Point object_anchor_point = fOriginalPlacement.getLabel().getLabeledObject().getObjectAnchorPoint();
      TLcdLabelingUtil.adjustLabelLocationFromBoundsSFCT(object_anchor_point, label_anchor_offset, new_placement);

      return new_placement;
    }

    private Point retrieveLabelAnchorOffset(TLcdLabelPlacement aPlacement) {
      // This label anchor offset is calculated for the original dimension => adjust for new dimension.
      Point label_anchor_offset = new Point(aPlacement.getLabel().getLabelAnchorOffset());
      Dimension original_dimension = aPlacement.getLabel().getLabelDimension();
      double ratio_w = (double) aPlacement.getWidth() / original_dimension.getWidth();
      double ratio_h = (double) aPlacement.getHeight() / original_dimension.getHeight();
      label_anchor_offset.setLocation(label_anchor_offset.getX() * ratio_w, label_anchor_offset.getY() * ratio_h);
      return label_anchor_offset;
    }
  }
}
