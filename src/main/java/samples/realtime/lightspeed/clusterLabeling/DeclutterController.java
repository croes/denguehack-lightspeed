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
package samples.realtime.lightspeed.clusterLabeling;

import java.awt.AWTEvent;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;

import javax.swing.Timer;

import com.luciad.model.ILcdModel;
import com.luciad.shape.ILcdBounds;
import com.luciad.shape.shape2D.ILcd2DEditableBounds;
import com.luciad.shape.shape2D.TLcdXYBounds;
import com.luciad.util.ILcdFilter;
import com.luciad.util.TLcdNoBoundsException;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.controller.ALspController;
import com.luciad.view.lightspeed.layer.ALspBoundsInfo;
import com.luciad.view.lightspeed.layer.ALspViewBoundsInfo;
import com.luciad.view.lightspeed.layer.ILspInteractivePaintableLayer;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.TLspDomainObjectContext;
import com.luciad.view.lightspeed.layer.TLspPaintRepresentation;
import com.luciad.view.lightspeed.layer.TLspPaintRepresentationState;
import com.luciad.view.lightspeed.layer.TLspPaintState;
import com.luciad.view.lightspeed.painter.ILspPainter;
import com.luciad.view.lightspeed.painter.label.ILspLabelPainter;
import com.luciad.view.lightspeed.painter.label.TLspLabelID;
import com.luciad.view.lightspeed.query.TLspBoundsQuery;
import com.luciad.view.lightspeed.query.TLspPaintedObjectsQuery;

/**
 * This controller will declutter the icons near the mouse when it stops.
 */
class DeclutterController extends ALspController {

  private static final int MOUSE_DELAY = 150;
  private static final double MOUSE_MOVE_THRESHOLD = 5.0d;
  private static final Dimension SENSITIVITY = new Dimension(30, 30);

  private final ILcdFilter<ILcdModel> fModelFilter;
  private final AnimatedDeclutterLabelingAlgorithmProvider fAlgorithmProvider;

  // This timer is here to initiate the decluttering if the mouse has stopped moving for a moment.
  private final Timer fInitiateDeclutteringTimer;

  // Keep track of the last mouse moved event, in order to know where to declutter the labels
  private MouseEvent fLastMouseEvent;

  DeclutterController(ILcdFilter<ILcdModel> aModelFilter, AnimatedDeclutterLabelingAlgorithmProvider aAlgorithmProvider) {
    setShortDescription("Hover over a cluster of labels to declutter them");
    fModelFilter = aModelFilter;
    fAlgorithmProvider = aAlgorithmProvider;

    // When the fInitiateDeclutteringTimer fires, it starts to declutter the icons
    // and labels in the neighborhood of the mouse.
    fInitiateDeclutteringTimer = new Timer(0, new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        declutterNear(fLastMouseEvent.getX(), fLastMouseEvent.getY());
      }
    });
    fInitiateDeclutteringTimer.setInitialDelay(MOUSE_DELAY);
    fInitiateDeclutteringTimer.setRepeats(false);
  }

  @Override
  public void terminateInteraction(ILspView aView) {
    super.terminateInteraction(aView);
    fInitiateDeclutteringTimer.stop();
  }

  private void declutterNear(int aX, int aY) {
    // Find the labels of all objects that are in range of the mouse, and remember their objects.
    List<TLspDomainObjectContext> objectsToDeclutter = findLabeledObjectsNearLocation(aX, aY);
    if (objectsToDeclutter.size() <= 1) {
      return;
    }
    fAlgorithmProvider.declutterObjects(objectsToDeclutter, getView());
  }

  /**
   * Finds the objects whose labels are near the specified location.
   *
   * @param aX        The x-coordinate in view coordinates of the location.
   * @param aY        The y-coordinate in view coordinates of the location.
   *
   * @return A List of domain objects, whose labels are near the mouse location.
   */
  private List<TLspDomainObjectContext> findLabeledObjectsNearLocation(int aX, int aY) {
    ILcdBounds rectangle = new TLcdXYBounds(
        aX - SENSITIVITY.width / 2,
        aY - SENSITIVITY.height / 2,
        SENSITIVITY.width,
        SENSITIVITY.height
    );

    TLspContext context = new TLspContext();
    List<TLspDomainObjectContext> toDeclutter = new ArrayList<TLspDomainObjectContext>();
    for (Enumeration layers = getView().layers(); layers.hasMoreElements(); ) {
      ILspLayer layer = (ILspLayer) layers.nextElement();

      if (!fModelFilter.accept(layer.getModel())) {
        continue;
      }

      if (!(layer instanceof ILspInteractivePaintableLayer)) {
        continue;
      }
      ILspInteractivePaintableLayer paintableLayer = (ILspInteractivePaintableLayer) layer;
      context.resetFor(layer, getView());

      for (TLspPaintState paintState : TLspPaintState.values()) {
        for (TLspPaintRepresentation pr : layer.getPaintRepresentations()) {
          ILspPainter painter = paintableLayer.getPainter(pr);
          if (painter instanceof ILspLabelPainter) {
            TLspPaintRepresentationState labelPrs = TLspPaintRepresentationState.getInstance(pr, paintState);
            TLspPaintRepresentationState bodyPrs = TLspPaintRepresentationState.getInstance(TLspPaintRepresentation.BODY, paintState);
            Collection<Object> result = paintableLayer.query(new TLspPaintedObjectsQuery(labelPrs, rectangle), context);
            for (Object domainObject : result) {
              toDeclutter.add(new TLspDomainObjectContext(domainObject, getView(), paintableLayer, bodyPrs));
            }
          }
        }
      }
    }

    return toDeclutter;
  }

  @Override
  public AWTEvent handleAWTEventImpl(AWTEvent aAWTEvent) {
    if (aAWTEvent instanceof MouseEvent) {
      MouseEvent me = (MouseEvent) aAWTEvent;
      if (me.getID() == MouseEvent.MOUSE_MOVED ||
          me.getID() == MouseEvent.MOUSE_DRAGGED) {
        scheduleDecluttering(me);

      } else if (me.getID() == MouseEvent.MOUSE_EXITED) {
        fInitiateDeclutteringTimer.stop();
        fAlgorithmProvider.stopDecluttering();
      }
    }
    // Do not consume the events. Other controller may need them.
    return aAWTEvent;
  }

  private void scheduleDecluttering(MouseEvent aMouseEvent) {
    // Do not let small mouse movements delay the decluttering.
    if (fLastMouseEvent != null) {
      double distance = aMouseEvent.getPoint().distance(fLastMouseEvent.getPoint());
      if (distance < MOUSE_MOVE_THRESHOLD) {
        return;
      }
    }

    List<TLspLabelID> labelsToDeclutter = fAlgorithmProvider.getLabelsToDeclutter();

    ILcdBounds regionOfDecluttered = getRegion(labelsToDeclutter, getView());
    if (regionOfDecluttered == null || !regionOfDecluttered.contains2D(aMouseEvent.getX(), aMouseEvent.getY())) {
      fAlgorithmProvider.stopDecluttering();
      fInitiateDeclutteringTimer.stop();
      fInitiateDeclutteringTimer.restart();
    }
    fLastMouseEvent = aMouseEvent;
  }

  private static ILcdBounds getRegion(List<TLspLabelID> aLabelsToDeclutter, ILspView aView) {
    if (aLabelsToDeclutter != null && aLabelsToDeclutter.size() > 0) {
      try {
        ILcd2DEditableBounds allBounds = null;

        TLspContext context = new TLspContext();
        for (TLspLabelID labelToDeclutter : aLabelsToDeclutter) {
          context.resetFor(labelToDeclutter.getLayer(), aView);

          ILcdBounds bounds = labelBoundsSFCT(labelToDeclutter, context);
          if (bounds != null) {
            if (allBounds == null) {
              allBounds = new TLcdXYBounds(bounds);
            } else {
              allBounds.setTo2DUnion(bounds);
            }
          }
        }
        return allBounds;
      } catch (TLcdNoBoundsException e) {
        return null;
      }
    } else {
      return null;
    }
  }

  private static ILcdBounds labelBoundsSFCT(TLspLabelID aLabel, TLspContext aContext) throws TLcdNoBoundsException {
    if (!(aLabel.getLayer() instanceof ILspInteractivePaintableLayer)) {
      throw new TLcdNoBoundsException("Layer should implement ILspInteractivePaintableLayer");
    }
    ILspInteractivePaintableLayer layer = (ILspInteractivePaintableLayer) aLabel.getLayer();
    aLabel.getLayer();
    ALspBoundsInfo result = layer.query(new TLspBoundsQuery(aLabel), aContext);
    if (result instanceof ALspViewBoundsInfo) {
      ALspViewBoundsInfo boundsInfo = (ALspViewBoundsInfo) result;
      return boundsInfo.getViewBounds();
    }
    return null;
  }
}
