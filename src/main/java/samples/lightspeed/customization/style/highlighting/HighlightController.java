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
package samples.lightspeed.customization.style.highlighting;

import static com.luciad.view.lightspeed.layer.TLspPaintRepresentationState.getInstance;

import java.awt.AWTEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import com.luciad.input.touch.TLcdTouchEvent;
import com.luciad.input.touch.TLcdTouchPoint;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.shape2D.TLcdXYPoint;
import com.luciad.shape.shape3D.TLcdXYZPoint;
import com.luciad.view.lightspeed.ILspPaintingOrder;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.camera.ALspViewXYZWorldTransformation;
import com.luciad.view.lightspeed.controller.ALspController;
import com.luciad.view.lightspeed.layer.ALspTouchInfo;
import com.luciad.view.lightspeed.layer.ALspViewTouchInfo;
import com.luciad.view.lightspeed.layer.ALspWorldTouchInfo;
import com.luciad.view.lightspeed.layer.ILspInteractivePaintableLayer;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.TLspDomainObjectContext;
import com.luciad.view.lightspeed.layer.TLspPaintRepresentation;
import com.luciad.view.lightspeed.layer.TLspPaintRepresentationState;
import com.luciad.view.lightspeed.layer.TLspPaintState;
import com.luciad.view.lightspeed.painter.ILspPainter;
import com.luciad.view.lightspeed.painter.label.ILspLabelPainter;
import com.luciad.view.lightspeed.painter.label.TLspLabelID;
import com.luciad.view.lightspeed.query.TLspIsTouchedQuery;
import com.luciad.view.lightspeed.query.TLspPaintedObjectsTouchQuery;
import com.luciad.view.lightspeed.services.terrain.ILspTerrainSupport;
import com.luciad.view.lightspeed.style.ILspWorldElevationStyle.ElevationMode;

/**
 * This controller checks if an object needs to be highlighted, by checking if the cursor has
 * touched it. If so, an event is fired. This controller doesn't consume any events.
 */
public class HighlightController extends ALspController {

  private static final double SENSITIVITY = 1.0;

  private Map<ILspLayer, List<TLspPaintRepresentation>> fLayers = new WeakHashMap<>();
  private TLspDomainObjectContext fCurrentObject = null;

  private List<HighlightListener> fHighlightListeners = new CopyOnWriteArrayList<HighlightListener>();

  /**
   * Registers a layer for which to check if objects need to be highlighted.
   * @param aLayer               a layer.
   * @param aPaintRepresentation the paint representation to check.
   */
  public void registerLayer(ILspLayer aLayer, TLspPaintRepresentation aPaintRepresentation) {
    List<TLspPaintRepresentation> prs = fLayers.get(aLayer);
    if (prs == null) {
      prs = new ArrayList<TLspPaintRepresentation>();
      fLayers.put(aLayer, prs);
    }
    prs.add(aPaintRepresentation);
  }

  /**
   * Unregisters the given layer.
   * @param aLayer a layer.
   */
  public void unregisterLayer(ILspLayer aLayer) {
    fLayers.remove(aLayer);
    if (fCurrentObject != null && fCurrentObject.getLayer() == aLayer) {
      resetHighlightedObject();
    }
  }

  /**
   * Adds the given listener.
   * @param aListener a listener.
   */
  public void addHighlightListener(HighlightListener aListener) {
    fHighlightListeners.add(aListener);
  }

  /**
   * Removes the given listener.
   * @param aListener a listener.
   */
  public void removeHighlightListener(HighlightListener aListener) {
    fHighlightListeners.remove(aListener);
  }

  private void fireHighlightEvent(TLspDomainObjectContext aObject) {
    if (aObject == null) {
      for (HighlightListener highlightListener : fHighlightListeners) {
        highlightListener.objectHighlighted(null, null, null);
        highlightListener.labelHighlighted(null, null, null);
      }
      return;
    }

    Object object = aObject.getObject();
    TLspPaintRepresentationState prs = aObject.getPaintRepresentationState();
    TLspContext context = new TLspContext(aObject.getLayer(), getView());

    if (object instanceof TLspLabelID) {
      for (HighlightListener highlightListener : fHighlightListeners) {
        highlightListener.labelHighlighted((TLspLabelID) object, prs.getPaintState(), context);
      }
    } else {
      for (HighlightListener highlightListener : fHighlightListeners) {
        highlightListener.objectHighlighted(object, prs, context);
      }
    }
  }

  @Override
  public AWTEvent handleAWTEventImpl(AWTEvent aAWTEvent) {
    if (aAWTEvent instanceof MouseEvent) {
      MouseEvent mouseEvent = (MouseEvent) aAWTEvent;
      switch (mouseEvent.getID()) {
      case MouseEvent.MOUSE_MOVED:
        highlightPosition(mouseEvent.getX(), mouseEvent.getY());
        break;
      case MouseEvent.MOUSE_EXITED:
        resetHighlightedObject();
        break;
      default:
      }
    } else if (aAWTEvent instanceof TLcdTouchEvent) {
      TLcdTouchEvent touchEvent = (TLcdTouchEvent) aAWTEvent;
      TLcdTouchPoint touchPoint = touchEvent.getModifiedTouchPoint();
      if (touchPoint != null) {
        if (touchPoint.getState() == TLcdTouchPoint.State.DOWN) {
          highlightPosition(touchPoint.getLocation().x, touchPoint.getLocation().y);
        }
      }
    }
    return aAWTEvent;
  }

  private void highlightPosition(int aX, int aY) {
    // Retrieve the object that should be highlighted
    TLspDomainObjectContext object = getHighlightedObject(
        new TLcdXYPoint(aX, aY)
    );

    if (object != null) {
      if (!object.equals(fCurrentObject)) {
        // New object under the cursor, animate it
        fCurrentObject = object;
        fireHighlightEvent(fCurrentObject);
      }
    } else {
      // There is no object under the cursor, nothing needs to be animated.
      resetHighlightedObject();
    }
  }

  /**
   * Returns the object under the cursor that should be highlighted.
   *
   * @param aViewPoint the cursor position in view coordinates
   *
   * @return the object that should be highlighted or {@code null}
   */
  private TLspDomainObjectContext getHighlightedObject(final ILcdPoint aViewPoint) {
    // Determine the relevant paint steps
    List<ILspPaintingOrder.PaintStep> paintSteps = new ArrayList<>();
    ILspView view = getView();
    Enumeration layers = view.layers();
    int layerIndex = 0;
    while (layers.hasMoreElements()) {
      ILspLayer layer = (ILspLayer) layers.nextElement();
      if (layer.isVisible() &&
          fLayers.containsKey(layer) &&
          layer instanceof ILspInteractivePaintableLayer) {
        List<TLspPaintRepresentation> prs = fLayers.get(layer);
        if (prs == null) {
          continue;
        }
        for (TLspPaintRepresentation pr : prs) {
          if (layer.getPaintRepresentations().contains(pr)) {
            paintSteps.add(new ILspPaintingOrder.PaintStep(layer, getInstance(pr, TLspPaintState.REGULAR), view, layerIndex));
            if (layer.isSelectableSupported()) {
              paintSteps.add(new ILspPaintingOrder.PaintStep(layer, getInstance(pr, TLspPaintState.SELECTED), view, layerIndex));
            }
            if (layer.isEditableSupported()) {
              paintSteps.add(new ILspPaintingOrder.PaintStep(layer, getInstance(pr, TLspPaintState.EDITED), view, layerIndex));
            }
          }
        }
      }
      layerIndex++;
    }

    // Sort the paint steps according to how they are painted by the view
    Collections.sort(paintSteps, view.getPaintingOrder());

    // Perform a query for each of the paint steps and update the highlighting candidate
    final CandidateChooser candidateChooser = new CandidateChooser(aViewPoint);
    for (ILspPaintingOrder.PaintStep paintStep : paintSteps) {
      // Query for the current paint step
      final ILspInteractivePaintableLayer layer = (ILspInteractivePaintableLayer) paintStep.getLayer();
      layer.query(
          new TLspPaintedObjectsTouchQuery(
              paintStep.getPaintRepresentationState(),
              aViewPoint,
              SENSITIVITY
          ) {
            @Override
            public boolean touched(ALspWorldTouchInfo aTouchInfo) {
              // We have touched an object at a point in world coordinates, update the candidate
              candidateChooser.touched(layer, getPaintRepresentationState(), aTouchInfo);
              return true;
            }

            @Override
            public boolean touched(ALspViewTouchInfo aTouchInfo) {
              // We have touched an object at a point in view coordinates, update the candidate
              candidateChooser.touched(layer, getPaintRepresentationState(), aTouchInfo);
              return true;
            }
          },
          new TLspContext(layer, view)
      );
    }
    return candidateChooser.getCandidate();
  }

  public final void resetHighlightedObject() {
    if (fCurrentObject != null) {
      fCurrentObject = null;
      fireHighlightEvent(null);
    }
  }

  /**
   * This listener can be used to notify that an object should be highlighted. When no object
   * should be highlighted, null is passed for both objects.
   */
  public interface HighlightListener {

    /**
     * This method notifies that the given object should be highlighted. When no object
     * should be highlighted, null is passed for both objects.
     *
     * @param aObject                   the object that should be highlighted.
     * @param aPaintRepresentationState the paint representation state
     * @param aContext                  the context.
     */
    void objectHighlighted(Object aObject, TLspPaintRepresentationState aPaintRepresentationState, TLspContext aContext);

    /**
     * This method notifies that the given label should be highlighted. When no label should be
     * highlighted, null is passed as label ID and as context.
     * @param aLabelID    the label that should be highlighted.
     * @param aPaintState the paint state in which the label is painted.
     * @param aContext    the context.
     */
    void labelHighlighted(TLspLabelID aLabelID, TLspPaintState aPaintState, TLspContext aContext);
  }

  /**
   * Utility class for choosing a highlighting candidate among a number touched objects.
   */
  private class CandidateChooser {
    private final TLcdXYZPoint fTmpPt = new TLcdXYZPoint();
    private final ALspViewXYZWorldTransformation fViewXYZWorldTransformation;
    private final double fTerrainDepth;

    private ILspInteractivePaintableLayer fLayer;
    private TLspPaintRepresentationState fPrs;
    private ALspTouchInfo fTouchInfo;
    private double fDepth;

    private CandidateChooser(ILcdPoint aPoint) {
      fViewXYZWorldTransformation = getView().getViewXYZWorldTransformation();
      fTerrainDepth = getTerrainDepth(getView(), aPoint);
    }

    /**
     * Should be called when an object is touched to update the current candidate if the specified
     * touched object is closer to the viewer then the current candidate.
     *
     * @param aLayer     the layer
     * @param aPrs       the paint representation state
     * @param aTouchInfo the touch info
     */
    public void touched(ILspInteractivePaintableLayer aLayer, TLspPaintRepresentationState aPrs, ALspTouchInfo aTouchInfo) {
      double depth = getDepth(aTouchInfo);
      if ((fTouchInfo == null || depth < fDepth) && depth <= fTerrainDepth) {
        fLayer = aLayer;
        fPrs = aPrs;
        fTouchInfo = aTouchInfo.clone();
        fDepth = depth;
      }
    }

    private double getDepth(ALspTouchInfo aTouchInfo) {
      if (aTouchInfo instanceof ALspViewTouchInfo) {
        ALspViewTouchInfo info = (ALspViewTouchInfo) aTouchInfo;
        return info.hasDepth() ? info.getTouchedViewPoint().getZ() : 0.0;
      } else {
        ALspWorldTouchInfo info = (ALspWorldTouchInfo) aTouchInfo;
        return ElevationMode.ON_TERRAIN.equals(info.getElevationMode()) ? fTerrainDepth : getDepth(info.getTouchedWorldPoint());
      }
    }

    private double getDepth(ILcdPoint aWorldPoint) {
      fViewXYZWorldTransformation.worldPoint2ViewSFCT(aWorldPoint, fTmpPt);
      return fTmpPt.getZ();
    }

    public TLspDomainObjectContext getCandidate() {
      if (fTouchInfo == null || fDepth > fTerrainDepth) {
        return null;
      } else {
        ILspPainter painter = fLayer.getPainter(fPrs.getPaintRepresentation());
        TLspContext context = new TLspContext(fLayer, getView());
        if (painter instanceof ILspLabelPainter && fTouchInfo instanceof ALspViewTouchInfo) {
          ILspLabelPainter labelPainter = (ILspLabelPainter) painter;
          ALspViewTouchInfo viewTouchInfo = (ALspViewTouchInfo) fTouchInfo;

          Iterable<TLspLabelID> labelIDs = labelPainter.getLabelIDs(viewTouchInfo.getDomainObject(), fPrs, context);
          for (TLspLabelID labelID : labelIDs) {
            boolean touched = fLayer.query(new TLspIsTouchedQuery(labelID, viewTouchInfo.getTouchedViewPoint(), SENSITIVITY), context);
            if (touched) {
              return new TLspDomainObjectContext(labelID, getView(), fLayer, fPrs);
            }
          }
          return new TLspDomainObjectContext(fTouchInfo.getDomainObject(), getView(), fLayer, fPrs);
        } else {
          return new TLspDomainObjectContext(fTouchInfo.getDomainObject(), getView(), fLayer, fPrs);
        }
      }
    }

  }

  /**
   * Returns the depth value under a view point at which the terrain is painted.
   *
   * @param aView      the view
   * @param aViewPoint the view point
   *
   * @return the terrain depth
   */
  private static double getTerrainDepth(ILspView aView, ILcdPoint aViewPoint) {
    ILspTerrainSupport terrainSupport = aView.getServices().getTerrainSupport();
    if (terrainSupport == null || !terrainSupport.isDrapingOnTerrain()) {
      return Double.MAX_VALUE;
    }
    TLspContext context = new TLspContext(null, aView);
    ILcdPoint worldPoint = terrainSupport.getPointOnTerrain(aViewPoint, context);
    if (worldPoint == null) {
      return Double.MAX_VALUE;
    }
    TLcdXYZPoint point = new TLcdXYZPoint();
    aView.getViewXYZWorldTransformation().worldPoint2ViewSFCT(worldPoint, point);
    return point.getZ();
  }
}
