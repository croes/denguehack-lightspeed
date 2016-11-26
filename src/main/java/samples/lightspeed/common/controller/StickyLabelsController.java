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
package samples.lightspeed.common.controller;

import java.awt.AWTEvent;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.geom.Dimension2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.luciad.shape.ILcdPoint;
import com.luciad.shape.shape2D.ILcd2DEditablePoint;
import com.luciad.shape.shape2D.TLcdXYPoint;
import com.luciad.shape.shape3D.ILcd3DEditableBounds;
import com.luciad.shape.shape3D.ILcd3DEditablePoint;
import com.luciad.shape.shape3D.TLcdXYZBounds;
import com.luciad.shape.shape3D.TLcdXYZPoint;
import com.luciad.util.ALcdWeakPropertyChangeListener;
import com.luciad.util.TLcdNoBoundsException;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.TLspPaintPhase;
import com.luciad.view.lightspeed.TLspPaintProgress;
import com.luciad.view.lightspeed.camera.ALspViewXYZWorldTransformation;
import com.luciad.view.lightspeed.controller.ALspController;
import com.luciad.view.lightspeed.label.location.ALspLabelLocation;
import com.luciad.view.lightspeed.label.location.ALspLabelLocations;
import com.luciad.view.lightspeed.layer.ILspInteractivePaintableLayer;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.TLspPaintRepresentation;
import com.luciad.view.lightspeed.layer.TLspPaintRepresentationState;
import com.luciad.view.lightspeed.layer.TLspPaintState;
import com.luciad.view.lightspeed.painter.ILspPainter;
import com.luciad.view.lightspeed.painter.label.ILspLabelPainter;
import com.luciad.view.lightspeed.painter.label.TLspLabelID;
import com.luciad.view.lightspeed.painter.label.location.ALspStampLabelLocation;
import com.luciad.view.lightspeed.painter.label.location.ILspStampLocationLabelPainter;
import com.luciad.view.lightspeed.painter.label.location.TLspStampLabelLocation;
import com.luciad.view.opengl.binding.ILcdGLDrawable;

import samples.lightspeed.customization.style.highlighting.HighlightController;
import samples.lightspeed.labels.util.LspLabelPainterUtil;

/**
 * This controller makes sure that labels under the mouse cursor are made absolute (and sticky)
 * locations. This makes sure that the labels remain in the same screen position, making it easier
 * to select and edit them.
 */
public class StickyLabelsController extends ALspController {

  private HighlightController fDelegateController;
  private Map<TLspLabelID, RestoreLocationInfo> fAbsoluteLabels = new HashMap<>();

  private PropertyChangeListener fStickyLabelsInvalidationLister;

  public StickyLabelsController() {
    fDelegateController = new HighlightController();
    fDelegateController.addHighlightListener(new HighlightController.HighlightListener() {
      @Override
      public void objectHighlighted(Object aObject, TLspPaintRepresentationState aPrs, TLspContext aContext) {
        // Do nothing
      }

      @Override
      public void labelHighlighted(TLspLabelID aLabelID, TLspPaintState aPaintState, TLspContext aContext) {
        if (aLabelID == null) {
          makeLabelsNonSticky(getView());
        } else {
          makeLabelSticky(aLabelID, aPaintState, aContext);
        }
      }
    });
  }

  /**
   * Registers a layer for which to check if objects need to be highlighted.
   *
   * @param aLayer a layer.
   */
  public void registerLayer(ILspLayer aLayer) {
    if (aLayer instanceof ILspInteractivePaintableLayer) {
      ILspInteractivePaintableLayer layer = (ILspInteractivePaintableLayer) aLayer;
      Collection<TLspPaintRepresentation> prs = layer.getPaintRepresentations();
      for (TLspPaintRepresentation pr : prs) {
        ILspPainter painter = layer.getPainter(pr);
        if (painter instanceof ILspLabelPainter) {
          fDelegateController.registerLayer(aLayer, pr);
        }
      }
    }
  }

  /**
   * Unregisters the given layer.
   *
   * @param aLayer a layer.
   */
  public void unregisterLayer(ILspLayer aLayer) {
    fDelegateController.unregisterLayer(aLayer);
  }

  @Override
  public AWTEvent handleAWTEventImpl(AWTEvent aAWTEvent) {
    if (aAWTEvent.getID() == MouseEvent.MOUSE_EXITED) {
      return aAWTEvent;
    }
    return fDelegateController.handleAWTEventImpl(aAWTEvent);
  }

  @Override
  protected void startInteractionImpl(final ILspView aView) {
    fDelegateController.startInteraction(aView);
    fStickyLabelsInvalidationLister = new StickyLabelInvalidatePropertyChangeListener(this, aView);
    aView.addPropertyChangeListener(fStickyLabelsInvalidationLister);
  }

  @Override
  protected void terminateInteractionImpl(ILspView aView) {
    fDelegateController.terminateInteraction(aView);
    aView.removePropertyChangeListener(fStickyLabelsInvalidationLister);
    makeLabelsNonSticky(aView);
  }

  @Override
  protected TLspPaintProgress paintImpl(ILcdGLDrawable aGLDrawable, ILspView aView, TLspPaintPhase aPaintPhase) {
    return fDelegateController.paint(aGLDrawable, aView, aPaintPhase);
  }

  private void makeLabelSticky(TLspLabelID aLabelID, TLspPaintState aPaintState, TLspContext aContext) {
    if (fAbsoluteLabels.containsKey(aLabelID)) {
      return;
    }
    makeLabelsNonSticky(aContext.getView());
    RestoreLocationInfo restoreLocationInfo = makeSticky(aLabelID, aPaintState, aContext);
    if (restoreLocationInfo != null) {
      fAbsoluteLabels.put(aLabelID, restoreLocationInfo);
    }
  }

  private void makeLabelsNonSticky(ILspView aView) {
    makeLabelsNonSticky(fAbsoluteLabels, aView);
    fAbsoluteLabels.clear();
  }

  private static void makeLabelsNonSticky(Map<TLspLabelID, RestoreLocationInfo> aLabels, ILspView aView) {
    for (Map.Entry<TLspLabelID, RestoreLocationInfo> entry : aLabels.entrySet()) {
      TLspLabelID label = entry.getKey();
      RestoreLocationInfo restoreLocationInfo = entry.getValue();
      makeNonSticky(label, restoreLocationInfo, aView);
    }
  }

  private static RestoreLocationInfo makeSticky(TLspLabelID aLabelID, TLspPaintState aPaintState, TLspContext aContext) {
    ILspView view = aContext.getView();
    ALspLabelLocations labelLocations = view.getLabelPlacer().getLabelLocations();
    ALspLabelLocations.LocationInfo locationInfo = labelLocations.getLabelLocation(view, aLabelID);
    ALspLabelLocation labelLocation = locationInfo.getLocation();

    ILspStampLocationLabelPainter labelPainter = LspLabelPainterUtil.getStampLabelPainter(aLabelID);

    if (locationInfo.isVisible() &&
        labelLocation instanceof ALspStampLabelLocation &&
        aLabelID.getLayer().isVisible(aLabelID.getPaintRepresentation()) &&
        aLabelID.getLayer().isEditable(aLabelID.getPaintRepresentation())) {
      try {
        TLspStampLabelLocation newLocation = new TLspStampLabelLocation();
        if (adjustLocationAbsolute(aLabelID, aPaintState, aContext, labelLocation, labelLocations, true, newLocation)) {
          ALspStampLabelLocation stampLocation = (ALspStampLabelLocation) labelLocation;
          ALspStampLabelLocation.LocationData locationData = new ALspStampLabelLocation.LocationData();
          stampLocation.getLocationData(aLabelID, labelLocations, labelPainter, aPaintState, aContext, locationData);

          boolean wasAbsoluteViewLocation = locationData.isAbsoluteViewLocation();
          boolean wasSticky = labelLocation.isSticky();

          labelLocations.updateLabelLocations(Collections.singletonList(aLabelID),
                                              Collections.<ALspLabelLocation>singletonList(newLocation),
                                              Collections.singletonList(locationInfo.isVisible()),
                                              view);

          return new RestoreLocationInfo(wasAbsoluteViewLocation, wasSticky, aPaintState);
        }
      } catch (TLcdNoBoundsException e) {
        // Do nothing
      }
    }
    return null;
  }

  private static void makeNonSticky(TLspLabelID aLabelID, RestoreLocationInfo aRestoreLocationInfo, ILspView aView) {
    ALspLabelLocations labelLocations = aView.getLabelPlacer().getLabelLocations();
    ALspLabelLocations.LocationInfo locationInfo = labelLocations.getLabelLocation(aView, aLabelID);
    TLspContext context = new TLspContext(aLabelID.getLayer(), aView);

    TLspStampLabelLocation newLocation = new TLspStampLabelLocation();
    if (aRestoreLocationInfo.wasAbsoluteViewLocation()) {
      if (!adjustLocationAbsolute(aLabelID, aRestoreLocationInfo.getPaintState(), context, locationInfo.getLocation(), labelLocations, false, newLocation)) {
        throw new RuntimeException("Label location cannot be reverted");
      }
    } else {
      if (!adjustLocationRelative(aLabelID, aRestoreLocationInfo.getPaintState(), context, locationInfo.getLocation(), labelLocations, newLocation)) {
        labelLocations.updateLabelLocations(Collections.singletonList(aLabelID),
                                            Collections.<ALspLabelLocation>singletonList(null),
                                            Collections.singletonList(false),
                                            aView);
        return;
      }
    }
    newLocation.setSticky(aRestoreLocationInfo.wasSticky());
    labelLocations.updateLabelLocations(Collections.singletonList(aLabelID),
                                        Collections.<ALspLabelLocation>singletonList(newLocation),
                                        Collections.singletonList(locationInfo.isVisible()),
                                        aView);
  }

  private static boolean adjustLocationAbsolute(final TLspLabelID aLabelID, final TLspPaintState aPaintState, final TLspContext aContext, ALspLabelLocation aLabelLocation, ALspLabelLocations aLabelLocations, boolean aSetSticky, TLspStampLabelLocation aLocationSFCT) {
    ILspStampLocationLabelPainter labelPainter = LspLabelPainterUtil.getStampLabelPainter(aLabelID);

    try {
      ILcd3DEditablePoint labelAnchorPoint = new TLcdXYZPoint();
      labelPainter.labelAnchorPointSFCT(aLabelID, aLabelLocation, aLabelLocations, aPaintState, aContext, labelAnchorPoint);

      ILcd3DEditableBounds labelBounds = new TLcdXYZBounds();
      double rotation = labelPainter.labelBoundsSFCT(aLabelID, aLabelLocation, aLabelLocations, aPaintState, aContext, labelBounds);

      aLocationSFCT.setRotation(rotation);
      aLocationSFCT.setViewOffset(labelAnchorPoint.getX(), labelAnchorPoint.getY());
      aLocationSFCT.setAbsoluteViewLocation(true);
      aLocationSFCT.setSticky(aSetSticky);
      aLocationSFCT.setEditedByEditor(aLabelLocation.isEditedByEditor());
      return true;
    } catch (TLcdNoBoundsException e) {
      // Do nothing
    }
    return false;
  }

  private static boolean adjustLocationRelative(final TLspLabelID aLabelID, final TLspPaintState aPaintState, final TLspContext aContext, ALspLabelLocation aLabelLocation, ALspLabelLocations aLabelLocations, TLspStampLabelLocation aLocationSFCT) {
    ILspStampLocationLabelPainter labelPainter = LspLabelPainterUtil.getStampLabelPainter(aLabelID);

    try {

      ILcd3DEditablePoint viewObjectAnchorPoint = new TLcdXYZPoint();
      labelPainter.viewObjectAnchorPointSFCT(aLabelID, aLabelLocations, aPaintState, aContext, viewObjectAnchorPoint);

      Dimension2D dimension = new Dimension();
      labelPainter.labelDimensionSFCT(aLabelID, aPaintState, aContext, dimension);

      ILcd2DEditablePoint labelAnchorOffset = new TLcdXYPoint();
      labelPainter.labelAnchorPointOffsetSFCT(aLabelID, dimension, aPaintState, aContext, labelAnchorOffset);

      ILcd3DEditableBounds labelBounds = new TLcdXYZBounds();
      double rotation = labelPainter.labelBoundsSFCT(aLabelID, aLabelLocation, aLabelLocations, aPaintState, aContext, labelBounds);

      double sin_rotation = Math.sin(rotation);
      double cos_rotation = Math.cos(rotation);
      adjustLocationFromBoundsSFCT(viewObjectAnchorPoint, labelAnchorOffset, labelBounds.getLocation().getX(), labelBounds.getLocation().getY(), rotation, sin_rotation, cos_rotation, aLocationSFCT);
      aLocationSFCT.setEditedByEditor(aLabelLocation.isEditedByEditor());
      return true;
    } catch (TLcdNoBoundsException e) {
      // Do nothing
    }
    return false;
  }

  private static void adjustLocationFromBoundsSFCT(ILcdPoint aViewObjectAnchorPoint, ILcdPoint aLabelAnchorOffset, double aUpperLeftX, double aUpperLeftY, double aRotation, double aSinRotation, double aCosRotation, TLspStampLabelLocation aLocationSFCT) {
    double offset_x = aLabelAnchorOffset.getX();
    double offset_y = aLabelAnchorOffset.getY();
    if (aRotation != 0.0) {
      offset_x = aCosRotation * aLabelAnchorOffset.getX() - aSinRotation * aLabelAnchorOffset.getY();
      offset_y = aSinRotation * aLabelAnchorOffset.getX() + aCosRotation * aLabelAnchorOffset.getY();
    }

    aLocationSFCT.setViewOffset(aUpperLeftX - aViewObjectAnchorPoint.getX() + offset_x,
                                aUpperLeftY - aViewObjectAnchorPoint.getY() + offset_y);
    aLocationSFCT.setWorldOffset(0.0, 0.0, 0.0);
    aLocationSFCT.setRotation(aRotation);
  }

  private static class RestoreLocationInfo {

    private final boolean fWasAbsoluteViewLocation;
    private final boolean fWasSticky;
    private final TLspPaintState fPaintState;

    public RestoreLocationInfo(boolean aWasAbsoluteViewLocation, boolean aWasSticky, TLspPaintState aPaintState) {
      fWasAbsoluteViewLocation = aWasAbsoluteViewLocation;
      fWasSticky = aWasSticky;
      fPaintState = aPaintState;
    }

    public boolean wasAbsoluteViewLocation() {
      return fWasAbsoluteViewLocation;
    }

    public boolean wasSticky() {
      return fWasSticky;
    }

    public TLspPaintState getPaintState() {
      return fPaintState;
    }
  }

  /**
   * This listener makes sure that sticky labels are made non-sticky as soon as the view is navigated. If not,
   * labels may behave unexpectedly while panning or zooming.
   */
  private static class StickyLabelInvalidatePropertyChangeListener extends ALcdWeakPropertyChangeListener<StickyLabelsController> {

    private final StickyLabelInvalidateOnNavigateListener fNavigateListener;

    public StickyLabelInvalidatePropertyChangeListener(StickyLabelsController aStickyLabelsController, ILspView aView) {
      super(aStickyLabelsController);
      fNavigateListener = new StickyLabelInvalidateOnNavigateListener(aStickyLabelsController);
      aView.getViewXYZWorldTransformation().addPropertyChangeListener(fNavigateListener);
    }

    @Override
    protected void propertyChangeImpl(StickyLabelsController aController, PropertyChangeEvent aPropertyChangeEvent) {
      if ("viewXYZWorldTransformation".equals(aPropertyChangeEvent.getPropertyName())) {
        ALspViewXYZWorldTransformation oldV2w = (ALspViewXYZWorldTransformation) aPropertyChangeEvent.getOldValue();
        oldV2w.removePropertyChangeListener(fNavigateListener);

        ALspViewXYZWorldTransformation newV2w = (ALspViewXYZWorldTransformation) aPropertyChangeEvent.getNewValue();
        newV2w.addPropertyChangeListener(fNavigateListener);
      }
    }

    private static class StickyLabelInvalidateOnNavigateListener extends ALcdWeakPropertyChangeListener<StickyLabelsController> {

      protected StickyLabelInvalidateOnNavigateListener(StickyLabelsController aStickyLabelsController) {
        super(aStickyLabelsController);
      }

      @Override
      protected void propertyChangeImpl(StickyLabelsController aController, PropertyChangeEvent aPropertyChangeEvent) {
        aController.fDelegateController.resetHighlightedObject();
      }
    }
  }
}
