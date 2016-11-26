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
package samples.lightspeed.customization.hippodrome;

import java.awt.AWTEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.luciad.model.ILcdModelReference;
import com.luciad.shape.ILcdPoint;
import com.luciad.view.lightspeed.editor.ALspEditor;
import com.luciad.view.lightspeed.editor.TLspEditContext;
import com.luciad.view.lightspeed.editor.handle.ALspEditHandle;
import com.luciad.view.lightspeed.editor.handle.TLspObjectTranslationHandle;
import com.luciad.view.lightspeed.editor.handle.TLspPointSetHandle;
import com.luciad.view.lightspeed.editor.handle.TLspPointTranslationHandle;
import com.luciad.view.lightspeed.editor.handle.TLspStaticCreateHandle;
import com.luciad.view.lightspeed.editor.operation.ELspInteractionStatus;
import com.luciad.view.lightspeed.editor.operation.TLspEditHandleResult;
import com.luciad.view.lightspeed.editor.operation.TLspEditOperation;
import com.luciad.view.lightspeed.editor.operation.TLspEditOperationResult;
import com.luciad.view.lightspeed.editor.operation.TLspEditOperationType;
import com.luciad.view.lightspeed.editor.operation.TLspMoveDescriptor;
import com.luciad.view.lightspeed.editor.operation.TLspPropertyChangeDescriptor;

import samples.gxy.hippodromePainter.IHippodrome;

/**
 * Editor implementation for the hippodrome sample.
 */
public class HippodromeEditor extends ALspEditor {


  /**
   * Keys used for properties on edit handles.
   */
  public static enum PropertyKeys {
    /**
     * Maps to a {@link HandleIdentifier}, which indicates the purpose of the edit handle.
     */
    HANDLE_IDENTIFIER,
  }

  /**
   * Describes the type of an edit handle created by the enclosing editor implementation.
   *
   * @since 2012.0
   */
  public static enum HandleIdentifier {
    /**
     * Identifies the handle at the hippodrome's start point.
     */
    START_POINT,
    /**
     * Identifies the handle at the hippodrome's end point.
     */
    END_POINT,
    /**
     * Identifies the whole-object translation handle.
     */
    TRANSLATE,
    /**
     * Identifies the hippodrome radius (or width) handle.
     */
    RADIUS
  }

  private static final String RADIUS_PROPERTY_NAME = "width";

  @Override
  public List<ALspEditHandle> getEditHandles(TLspEditContext aContext) {
    // Don't edit if the object is not a hippodrome
    Object object = aContext.getGeometry();
    if (!(object instanceof IHippodrome)) {
      return Collections.emptyList();
    }
    final IHippodrome hippodrome = (IHippodrome) object;

    // Create handles and add them to a list
    ArrayList<ALspEditHandle> handles = new ArrayList<ALspEditHandle>(4);

    ALspEditHandle start = createStartPointHandle(hippodrome, aContext, true);
    handles.add(start);

    ALspEditHandle end = createEndPointHandle(hippodrome, aContext, true);
    handles.add(end);

    ALspEditHandle outline = createWidthHandle(hippodrome, true);
    handles.add(outline);

    ALspEditHandle translate = createTranslationHandle(hippodrome);
    handles.add(translate);

    return handles;
  }

  @Override
  public ALspEditHandle getCreateHandle(TLspEditContext aContext) {
    // Don't edit if the object is not a hippodrome
    Object object = aContext.getGeometry();
    if (!(object instanceof IHippodrome)) {
      return null;
    }
    final IHippodrome hippodrome = (IHippodrome) object;

    // We use a static create handle, since we know beforehand
    // how many handles are needed to initialize the hippodrome
    Collection<ALspEditHandle> handles = new ArrayList<ALspEditHandle>();

    ALspEditHandle start = createStartPointHandle(hippodrome, aContext, false);
    handles.add(start);

    ALspEditHandle end = createEndPointHandle(hippodrome, aContext, false);
    handles.add(end);

    ALspEditHandle width = createWidthHandle(hippodrome, false);
    handles.add(width);

    return new TLspStaticCreateHandle(hippodrome, handles);
  }

  private ALspEditHandle createTranslationHandle(IHippodrome aHippodrome) {
    TLspObjectTranslationHandle translate = new TLspObjectTranslationHandle(aHippodrome);
    translate.getProperties().put(PropertyKeys.HANDLE_IDENTIFIER, HandleIdentifier.TRANSLATE);
    return translate;
  }

  private ALspEditHandle createWidthHandle(IHippodrome aHippodrome, boolean aEditing) {
    HippodromeOutlineResizeHandle outline = new HippodromeOutlineResizeHandle(
        aHippodrome
    );
    outline.getProperties().put(PropertyKeys.HANDLE_IDENTIFIER, HandleIdentifier.RADIUS);
    outline.setTranslateOnDrag(aEditing);
    return outline;
  }

  private ALspEditHandle createEndPointHandle(final IHippodrome aHippodrome, TLspEditContext aEditContext, boolean aEditing) {
    final ILcdModelReference modelReference = aEditContext.getObjectContext().getModelReference();
    TLspPointTranslationHandle end = new TLspPointTranslationHandle(
        aHippodrome, aHippodrome.getEndPoint(), modelReference
    );
    end.getProperties().put(PropertyKeys.HANDLE_IDENTIFIER, HandleIdentifier.END_POINT);
    end.setTranslateOnDrag(aEditing);
    return end;
  }

  private ALspEditHandle createStartPointHandle(
      final IHippodrome aHippodrome,
      TLspEditContext aContext,
      boolean aEditing
  ) {
    ALspEditHandle start;
    // When editing, use a TLspPointTranslationHandle. This handle allows a point to
    // be dragged using the mouse.
    final ILcdModelReference modelReference = aContext.getObjectContext().getModelReference();
    if (aEditing) {
      start = new TLspPointTranslationHandle(
          aHippodrome, aHippodrome.getStartPoint(), modelReference
      );
    }
    // When creating, use an TLspPointSetHandle instead. This handle allows the
    // point to be positioned using a mouse click.
    else {
      start = new TLspPointSetHandle(aHippodrome, aHippodrome.getStartPoint(), modelReference) {
        @Override
        protected TLspEditHandleResult createEditHandleResult(ILcdPoint aViewPoint,
                                                              AWTEvent aOriginalEvent,
                                                              AWTEvent aProcessedEvent,
                                                              ELspInteractionStatus aInteractionStatus,
                                                              TLspEditContext aEditContext) {
          // Add operations to move the end point, and initialize the width
          // This results in a better visualization during creation
          TLspEditHandleResult editHandleResult = super.createEditHandleResult(aViewPoint,
                                                                               aOriginalEvent,
                                                                               aProcessedEvent,
                                                                               aInteractionStatus,
                                                                               aEditContext);
          List<TLspEditOperation> operations = new ArrayList<TLspEditOperation>();
          for (TLspEditOperation operation : editHandleResult.getEditOperations()) {
            // Add the original operation
            operations.add(operation);

            if (operation.getType() == TLspEditOperationType.MOVE) {
              String movePropertyKey = TLspEditOperationType.MOVE.getPropertyKey();
              TLspMoveDescriptor moveDescriptor =
                  (TLspMoveDescriptor) operation.getProperties().get(movePropertyKey);

              // Add an operation to move the end point to the same location
              Map<Object, Object> properties1 = new HashMap<Object, Object>();
              properties1.put(PropertyKeys.HANDLE_IDENTIFIER, HandleIdentifier.END_POINT);
              properties1.put(movePropertyKey, moveDescriptor);
              operations.add(new TLspEditOperation(TLspEditOperationType.MOVE, properties1));

              // Add an operation to set the width to an initial value
              Map<Object, Object> properties2 = new HashMap<Object, Object>();
              properties2.put(PropertyKeys.HANDLE_IDENTIFIER, HandleIdentifier.RADIUS);
              properties2.put(TLspEditOperationType.PROPERTY_CHANGE.getPropertyKey(),
                              new TLspPropertyChangeDescriptor<Double>(RADIUS_PROPERTY_NAME,
                                                                       aHippodrome.getWidth(),
                                                                       0.001)
              );
              operations.add(new TLspEditOperation(TLspEditOperationType.PROPERTY_CHANGE, properties2));
            }
          }

          // Return a new handle result with the extra operations
          return new TLspEditHandleResult(operations,
                                          editHandleResult.getProcessedEvent(),
                                          editHandleResult.getInteractionStatus());
        }
      };
    }
    start.getProperties().put(PropertyKeys.HANDLE_IDENTIFIER, HandleIdentifier.START_POINT);
    return start;
  }

  @Override
  protected TLspEditOperationResult editImpl(TLspEditOperation aOperation, ELspInteractionStatus aInteractionStatus, TLspEditContext aContext) {
    Object object = aContext.getGeometry();
    if (!(object instanceof IHippodrome)) {
      return TLspEditOperationResult.FAILED;
    }
    IHippodrome hippodrome = (IHippodrome) object;

    TLspEditOperationType type = aOperation.getType();
    if (type == TLspEditOperationType.MOVE) {
      TLspMoveDescriptor descriptor =
          (TLspMoveDescriptor) aOperation.getProperties().get(type.getPropertyKey());
      applyMove(hippodrome, aOperation, descriptor);
      return TLspEditOperationResult.SUCCESS;
    } else if (type == TLspEditOperationType.PROPERTY_CHANGE) {
      TLspPropertyChangeDescriptor descriptor =
          (TLspPropertyChangeDescriptor) aOperation.getProperties().get(type.getPropertyKey());
      if (RADIUS_PROPERTY_NAME.equals(descriptor.getPropertyName()) &&
          descriptor.getNewValue() != null) {
        hippodrome.setWidth(Math.abs((Double) descriptor.getNewValue()));
        return TLspEditOperationResult.SUCCESS;
      }
    }
    return TLspEditOperationResult.FAILED;
  }

  private void applyMove(
      IHippodrome aHippodrome,
      TLspEditOperation aOperation,
      TLspMoveDescriptor aDescriptor
  ) {
    ILcdPoint startPoint = aDescriptor.getStartPoint();
    ILcdPoint targetPoint = aDescriptor.getTargetPoint();
    HandleIdentifier handleIdentifier = (HandleIdentifier) aOperation.getProperties().get(
        PropertyKeys.HANDLE_IDENTIFIER
    );

    if (handleIdentifier == null) {
      // This can happen for multi-object translation handles, which are created
      // by the controller rather than the editor.
      if (startPoint != null) {
        aHippodrome.translate2D(
            targetPoint.getX() - startPoint.getX(),
            targetPoint.getY() - startPoint.getY()
        );
      }
    } else {
      switch (handleIdentifier) {
      case START_POINT:
        aHippodrome.moveReferencePoint(targetPoint, IHippodrome.START_POINT);
        break;
      case END_POINT:
        aHippodrome.moveReferencePoint(targetPoint, IHippodrome.END_POINT);
        break;
      case TRANSLATE:
        if (startPoint != null) {
          aHippodrome.translate2D(
              targetPoint.getX() - startPoint.getX(),
              targetPoint.getY() - startPoint.getY()
          );
        }
        break;
      }
    }
  }

  @Override
  public boolean canEdit(TLspEditContext aContext) {
    // Do not forget to call super.canEdit
    if (!super.canEdit(aContext)) {
      return false;
    }
    // This editor can only edit IHippodrome objects.
    // If editing of extruded IHippodrome objects is required,
    // this editor should be wrapped in a TLspExtrudedShapeEditor.
    return aContext.getGeometry() instanceof IHippodrome;
  }

  @Override
  public void copyGeometrySFCT(Object aSourceGeometry, Object aDestinationGeometrySFCT) {
    if (aDestinationGeometrySFCT instanceof IHippodrome && aSourceGeometry instanceof IHippodrome) {
      IHippodrome destinationGeometrySFCT = (IHippodrome) aDestinationGeometrySFCT;
      destinationGeometrySFCT.setWidth(((IHippodrome) aSourceGeometry).getWidth());
      ILcdPoint startPoint = ((IHippodrome) aSourceGeometry).getStartPoint();
      ILcdPoint endPoint = ((IHippodrome) aSourceGeometry).getEndPoint();
      destinationGeometrySFCT.moveReferencePoint(startPoint, IHippodrome.START_POINT);
      destinationGeometrySFCT.moveReferencePoint(endPoint, IHippodrome.END_POINT);
    }
  }

  @Override
  public boolean canCopyGeometry(Object aSourceGeometry, Object aDestinationGeometry) {
    return aSourceGeometry instanceof IHippodrome && aDestinationGeometry instanceof IHippodrome;
  }
}
