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
package samples.lightspeed.labels.util;

import java.awt.Dimension;
import java.awt.geom.Dimension2D;
import java.util.Collections;
import java.util.Enumeration;

import com.luciad.shape.ILcdBounds;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.shape2D.TLcdXYPoint;
import com.luciad.shape.shape3D.ILcd3DEditableBounds;
import com.luciad.shape.shape3D.TLcdXYZBounds;
import com.luciad.shape.shape3D.TLcdXYZPoint;
import com.luciad.util.ILcdFilter;
import com.luciad.util.TLcdNoBoundsException;
import com.luciad.view.lightspeed.ALspViewAdapter;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.label.location.ALspLabelLocation;
import com.luciad.view.lightspeed.label.location.ALspLabelLocations;
import com.luciad.view.lightspeed.layer.ILspInteractivePaintableLayer;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.ILspPaintableLayer;
import com.luciad.view.lightspeed.layer.TLspPaintRepresentation;
import com.luciad.view.lightspeed.layer.TLspPaintState;
import com.luciad.view.lightspeed.painter.ILspPainter;
import com.luciad.view.lightspeed.painter.label.TLspLabelID;
import com.luciad.view.lightspeed.painter.label.location.ILspStampLocationLabelPainter;
import com.luciad.view.lightspeed.painter.label.location.TLspStampLabelLocation;
import com.luciad.view.opengl.binding.ILcdGLDrawable;

/**
 * Clamps edited labels to the view bounds, or to a certain distance from their anchor point.
 *
 * Current limitations:
 * - Rotation of labels is currently not taken into account
 * - When the CPU is heavily occupied, label locations next to the border may jitter a bit when panning the map
 */
public class ClampEditedLabelsUtil {

  private final boolean fClampDistanceFromAnchor;
  private final boolean fClampEditedLabelsToViewBounds;

  private final ILcdFilter<ILspLayer> fLayerFilter;
  private final double fMaxDistanceFromObject;

  public ClampEditedLabelsUtil(ILspView aView, boolean aClampDistanceFromAnchor, double aMaxDistanceFromAnchor, boolean aClampEditedLabelsToViewBounds, ILcdFilter<ILspLayer> aLayerFilter) {
    fClampDistanceFromAnchor = aClampDistanceFromAnchor;
    fClampEditedLabelsToViewBounds = aClampEditedLabelsToViewBounds;
    fLayerFilter = aLayerFilter;
    fMaxDistanceFromObject = aMaxDistanceFromAnchor;
    aView.addViewListener(new ClampLabelViewAdapter());
  }

  private class ClampLabelViewAdapter extends ALspViewAdapter {

    @Override
    public void preRender(ILspView aView, ILcdGLDrawable aGLDrawable) {
      clampEditedLabels(aView);
    }

    private void clampEditedLabels(ILspView aView) {
      Enumeration layers = aView.layers();
      while (layers.hasMoreElements()) {
        ILspLayer layer = (ILspLayer) layers.nextElement();
        if (fLayerFilter == null || fLayerFilter.accept(layer)) {
          if (!(layer instanceof ILspPaintableLayer)) {
            continue;
          }
          ILspInteractivePaintableLayer paintableLayer = (ILspInteractivePaintableLayer) layer;
          clampEditedLabels(aView, paintableLayer);
        }
      }
    }

    private void clampEditedLabels(ILspView aView, ILspInteractivePaintableLayer aPaintableLayer) {
      TLspContext context = new TLspContext(aPaintableLayer, aView);
      for (TLspPaintRepresentation paintRepresentation : aPaintableLayer.getPaintRepresentations()) {
        ILspPainter painter = aPaintableLayer.getPainter(paintRepresentation);
        if (!(painter instanceof ILspStampLocationLabelPainter)) {
          continue;
        }
        ILspStampLocationLabelPainter labelPainter = (ILspStampLocationLabelPainter) painter;
        clampEditedLabels(labelPainter, aPaintableLayer, paintRepresentation, context);
      }
    }

    private void clampEditedLabels(ILspStampLocationLabelPainter aLabelPainter, ILspInteractivePaintableLayer aPaintableLayer, TLspPaintRepresentation paintRepresentation, TLspContext aContext) {
      ALspLabelLocations labelLocations = aContext.getView().getLabelPlacer().getLabelLocations();
      Iterable<ALspLabelLocations.LocationInfo> locations = labelLocations.getLabelLocations(aContext.getView(), aPaintableLayer, paintRepresentation, true);
      for (ALspLabelLocations.LocationInfo locationInfo : locations) {
        ALspLabelLocation location = locationInfo.getLocation();
        if (location.isEditedByEditor()) {
          TLspLabelID label = locationInfo.getLabel();
          TLspPaintState paintState = getPaintStateForLabel(aPaintableLayer, label);

          TLcdXYZBounds bounds = new TLcdXYZBounds();
          try {
            // Note: rotation returned by labelBoundsSFCT is not taken into account.
            aLabelPainter.labelBoundsSFCT(label, location, labelLocations, paintState, aContext, bounds);

            TLcdXYZPoint objectAnchorPoint = new TLcdXYZPoint();
            aLabelPainter.viewObjectAnchorPointSFCT(label, labelLocations, paintState, aContext, objectAnchorPoint);

            TLcdXYPoint offset = new TLcdXYPoint();

            if (fClampDistanceFromAnchor) {
              // Clamp the distance between the label and the anchor
              ILcdPoint offsetFromAnchor = calculateOffsetFromAnchor(bounds, objectAnchorPoint);
              offset.translate2D(offsetFromAnchor.getX(), offsetFromAnchor.getY());
            }

            if (fClampEditedLabelsToViewBounds) {
              // Clamp the label to the view bounds, as long as the object is visible
              if (objectAnchorPoint.getX() > 0 && objectAnchorPoint.getX() < aContext.getView().getWidth() &&
                  objectAnchorPoint.getY() > 0 && objectAnchorPoint.getY() < aContext.getView().getHeight()) {
                ILcd3DEditableBounds clampedBounds = bounds.cloneAs3DEditableBounds();
                clampedBounds.translate2D(offset.getX(), offset.getY());
                ILcdPoint offsetFromViewBorder = calculateOffsetFromViewBorder(clampedBounds, aContext.getView());
                offset.translate2D(offsetFromViewBorder.getX(), offsetFromViewBorder.getY());
              }
            }

            // Update the label location
            if (offset.getX() != 0.0 || offset.getY() != 0.0) {
              Dimension2D dimension = new Dimension();
              aLabelPainter.labelDimensionSFCT(label, paintState, aContext, dimension);

              TLcdXYZPoint labelAnchorOffset = new TLcdXYZPoint();
              aLabelPainter.labelAnchorPointOffsetSFCT(label, dimension, paintState, aContext, labelAnchorOffset);

              double anchorX = bounds.getLocation().getX() + labelAnchorOffset.getX();
              double anchorY = bounds.getLocation().getY() + labelAnchorOffset.getY();
              double viewOffsetX = anchorX - objectAnchorPoint.getX() + offset.getX();
              double viewOffsetY = anchorY - objectAnchorPoint.getY() + offset.getY();

              TLspStampLabelLocation newLabelLocation = new TLspStampLabelLocation();
              newLabelLocation.setEditedByEditor(location.isEditedByEditor());
              newLabelLocation.setViewOffset(viewOffsetX, viewOffsetY);
              labelLocations.updateLabelLocations(Collections.singletonList(label),
                                                  Collections.<ALspLabelLocation>singletonList(newLabelLocation),
                                                  Collections.singletonList(locationInfo.isVisible()),
                                                  aContext.getView());
            }
          } catch (TLcdNoBoundsException ignored) {
            // Should not happen
          }
        }
      }
    }

    private TLspPaintState getPaintStateForLabel(ILspInteractivePaintableLayer aPaintableLayer, TLspLabelID aLabel) {
      TLspPaintState paintState = TLspPaintState.REGULAR;
      if (aPaintableLayer.getObjectsWithPaintState(TLspPaintState.EDITED).contains(aLabel.getDomainObject())) {
        paintState = TLspPaintState.EDITED;
      } else if (aPaintableLayer.isSelected(aLabel.getDomainObject())) {
        paintState = TLspPaintState.SELECTED;
      }
      return paintState;
    }

    private ILcdPoint calculateOffsetFromViewBorder(ILcdBounds aBounds, ILspView aView) {
      double offsetX = 0.0;
      double offsetY = 0.0;
      if (aBounds.getLocation().getX() < 0) {
        offsetX = -aBounds.getLocation().getX();
      }
      if (aBounds.getLocation().getX() + aBounds.getWidth() > aView.getWidth()) {
        offsetX = aView.getWidth() - (aBounds.getLocation().getX() + aBounds.getWidth());
      }
      if (aBounds.getLocation().getY() < 0) {
        offsetY = -aBounds.getLocation().getY();
      }
      if (aBounds.getLocation().getY() + aBounds.getHeight() > aView.getHeight()) {
        offsetY = aView.getHeight() - (aBounds.getLocation().getY() + aBounds.getHeight());
      }
      return new TLcdXYPoint(offsetX, offsetY);
    }

    private ILcdPoint calculateOffsetFromAnchor(ILcdBounds aBounds, ILcdPoint aObjectAnchorPoint) {
      double centerX = aBounds.getLocation().getX() + aBounds.getWidth() * 0.5;
      double centerY = aBounds.getLocation().getY() + aBounds.getHeight() * 0.5;
      double dx = centerX - aObjectAnchorPoint.getX();
      double dy = centerY - aObjectAnchorPoint.getY();
      double dist = Math.sqrt(dx * dx + dy * dy);
      TLcdXYPoint offset = new TLcdXYPoint();
      if (dist > fMaxDistanceFromObject) {
        double newCenterX = aObjectAnchorPoint.getX() + fMaxDistanceFromObject * dx / dist;
        double newCenterY = aObjectAnchorPoint.getY() + fMaxDistanceFromObject * dy / dist;
        offset.translate2D(newCenterX - centerX, newCenterY - centerY);
      }
      return offset;
    }
  }
}
