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
package samples.lightspeed.demo.application.data.uav;

import static com.luciad.util.concurrent.TLcdLockUtil.writeLock;

import java.util.ArrayList;
import java.util.List;

import com.luciad.model.ILcdModel;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.shape2D.TLcdXYBounds;
import com.luciad.util.concurrent.TLcdLockUtil;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.editor.ILspEditor;
import com.luciad.view.lightspeed.editor.TLspEditContext;
import com.luciad.view.lightspeed.editor.handle.ALspEditHandle;
import com.luciad.view.lightspeed.editor.operation.ELspInteractionStatus;
import com.luciad.view.lightspeed.editor.operation.TLspEditOperation;
import com.luciad.view.lightspeed.editor.operation.TLspEditOperationResult;
import com.luciad.view.lightspeed.editor.operation.TLspEditOperationType;
import com.luciad.view.lightspeed.editor.operation.TLspMoveDescriptor;

/**
 * Editor for {@link ScreenSpaceBounds} objects.
 */
class ScreenSpaceBoundsEditor implements ILspEditor {

  static final String LOCATION_IDENTIFIER = ScreenSpaceBoundsEditor.class.getSimpleName() + "LOCATION_IDENTIFIER";

  /**
   * Enumeration type indicating the four different
   * locations on the bounds at which the handle can
   * be positioned.
   */
  public static enum Location {
    LEFT_EDGE(0, 0.5),
    RIGHT_EDGE(1, 0.5),
    TOP_EDGE(0.5, 1),
    BOTTOM_EDGE(0.5, 0);

    // X, Y coordinates of the bounds' location
    // relative to the bounds' center point.
    // These coordinates are also defined as fractions
    // of the width and/or height of the bounds.
    private final double fX;
    private final double fY;

    Location(double aX, double aY) {
      fX = aX;
      fY = aY;
    }

    public double getX() {
      return fX;
    }

    public double getY() {
      return fY;
    }
  }

  public ScreenSpaceBoundsEditor() {
  }

  @Override
  public ALspEditHandle getCreateHandle(TLspEditContext aContext) {
    //We don't support creation
    return null;
  }

  @Override
  public boolean canEdit(TLspEditContext aContext) {
    return aContext.getGeometry() instanceof ScreenSpaceBounds;
  }

  private void resizeBounds(Location aLocationIdentifier, ILcdPoint aNewLocation, ILspView aView, ScreenSpaceBounds aBounds) {
    TLcdXYBounds absoluteBounds = new TLcdXYBounds();
    aBounds.retrieveAbsoluteBoundsSFCT(aView.getWidth(), aView.getHeight(), absoluteBounds);
    double bx = absoluteBounds.getLocation().getX();
    double by = absoluteBounds.getLocation().getY();
    double bw = absoluteBounds.getWidth();
    double bh = absoluteBounds.getHeight();
    double aspectRatio = bw / bh;

    if (aLocationIdentifier == Location.TOP_EDGE) {
      double h = Math.max(50.0, aNewLocation.getY() - by);
      double w = h * aspectRatio;
      absoluteBounds.setWidth(w);
      absoluteBounds.setHeight(h);
      absoluteBounds.move2D((bx + bw / 2) - w / 2, (by + bh / 2) - h / 2);
    } else if (aLocationIdentifier == Location.BOTTOM_EDGE) {
      double h = Math.max(50.0, by + bh - aNewLocation.getY());
      double w = h * aspectRatio;
      absoluteBounds.setWidth(w);
      absoluteBounds.setHeight(h);
      absoluteBounds.move2D((bx + bw / 2) - w / 2, (by + bh / 2) - h / 2);
    } else if (aLocationIdentifier == Location.LEFT_EDGE) {
      double w = Math.max(50.0, bx + bw - aNewLocation.getX());
      double h = w / aspectRatio;
      absoluteBounds.setWidth(w);
      absoluteBounds.setHeight(h);
      absoluteBounds.move2D((bx + bw / 2) - w / 2, (by + bh / 2) - h / 2);
    } else if (aLocationIdentifier == Location.RIGHT_EDGE) {
      double w = Math.max(50.0, aNewLocation.getX() - bx);
      double h = w / aspectRatio;
      absoluteBounds.setWidth(w);
      absoluteBounds.setHeight(h);
      absoluteBounds.move2D((bx + bw / 2) - w / 2, (by + bh / 2) - h / 2);
    }

    //Change the absolute bounds of the screen space bounds to the calculated bounds
    aBounds.setAbsoluteBounds(absoluteBounds, aView.getWidth(), aView.getHeight());
  }

  @Override
  public List<ALspEditHandle> getEditHandles(TLspEditContext aContext) {
    //Check if the handles were already created, and if not create and add the new handles
    Object object = aContext.getGeometry();
    if (!(object instanceof ScreenSpaceBounds)) {
      return null;
    } else {
      List<ALspEditHandle> handles = new ArrayList<ALspEditHandle>();
      handles.add(new ScreenSpaceBoundsHandle((ScreenSpaceBounds) object, Location.TOP_EDGE, aContext.getObjectContext().getView()));
      handles.add(new ScreenSpaceBoundsHandle((ScreenSpaceBounds) object, Location.BOTTOM_EDGE, aContext.getObjectContext().getView()));
      handles.add(new ScreenSpaceBoundsHandle((ScreenSpaceBounds) object, Location.LEFT_EDGE, aContext.getObjectContext().getView()));
      handles.add(new ScreenSpaceBoundsHandle((ScreenSpaceBounds) object, Location.RIGHT_EDGE, aContext.getObjectContext().getView()));
      handles.add(new ScreenSpaceObjectTranslationHandle(object));
      return handles;
    }
  }

  @Override
  public TLspEditOperationResult edit(TLspEditOperation aOperation, ELspInteractionStatus aInteractionStatus, TLspEditContext aContext) {
    //We lock the model to make sure this edit operation is thread safe.
    ILcdModel model = aContext.getObjectContext().getModel();
    try (TLcdLockUtil.Lock autoUnlock = writeLock(model)) {
      ScreenSpaceBounds bounds = (ScreenSpaceBounds) aContext.getGeometry();
      if (aOperation.getType().equals(TLspEditOperationType.MOVE)) {
        TLspMoveDescriptor moveDescriptor = (TLspMoveDescriptor) aOperation.getProperties().get(aOperation.getType().getPropertyKey());
        Location pointIdentifier = (Location) aOperation.getProperties().get(LOCATION_IDENTIFIER);
        if (pointIdentifier != null) {
          resizeBounds(pointIdentifier,
                       moveDescriptor.getTargetPoint(),
                       aContext.getObjectContext().getView(),
                       bounds);
          return TLspEditOperationResult.SUCCESS;
        } else {
          if (moveDescriptor.getStartPoint() != null) {
            double translationX = moveDescriptor.getTargetPoint().getX() - moveDescriptor.getStartPoint().getX();
            double translationY = moveDescriptor.getTargetPoint().getY() - moveDescriptor.getStartPoint().getY();
            ILspView view = aContext.getObjectContext().getView();
            bounds.translate2D(translationX, translationY, view.getWidth(), view.getHeight());
            return TLspEditOperationResult.SUCCESS;
          } else {
            return TLspEditOperationResult.FAILED;
          }
        }
      }
      return TLspEditOperationResult.FAILED;
    }
  }

  @Override
  public boolean canPerformOperation(TLspEditOperation aOperation, TLspEditContext aContext) {
    if (!aOperation.getType().equals(TLspEditOperationType.MOVE)) {
      return false;
    }
    TLspMoveDescriptor descriptor = (TLspMoveDescriptor) aOperation.getProperties().get(aOperation.getType().getPropertyKey());
    return descriptor.getModelReference() == null;
  }

}
