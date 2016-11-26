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
package samples.lucy.tableview;

import java.awt.EventQueue;
import java.util.Collection;
import java.util.Collections;

import javax.swing.table.AbstractTableModel;

import com.luciad.model.ILcdIntegerIndexedModel;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelListener;
import com.luciad.model.TLcdModelChangedEvent;
import com.luciad.model.TLcdModelTreeNodeUtil;
import com.luciad.model.transformation.ALcdTransformingModel;
import com.luciad.util.concurrent.TLcdLockUtil;
import com.luciad.util.logging.ILcdLogger;
import com.luciad.util.logging.TLcdLoggerFactory;

/**
 * A base table model implementation based on an underlying ILcdIntegerIndexedModel.
 */
public abstract class LuciadModelWrapper extends AbstractTableModel implements
                                                                    ILcdModelListener,
                                                                    IExtendedTableModel {

  private static final ILcdLogger LOG = TLcdLoggerFactory.getLogger(LuciadModelWrapper.class.getName());


  public static boolean acceptsModel(ILcdModel aModel) {
    return aModel instanceof ILcdIntegerIndexedModel && (!TLcdModelTreeNodeUtil.isEmptyModel(aModel));
  }

  /**
   * Retrieves the model which should be used to create the {@link javax.swing.table.TableModel}
   *
   * @param aModel The model to use, or a transformed model
   *
   * @return The model which should be used to create the {@link javax.swing.table.TableModel}
   */
  public static ILcdModel retrieveModelForTableView(ILcdModel aModel) {
    if (aModel instanceof ALcdTransformingModel) {
      return retrieveModelForTableView(((ALcdTransformingModel) aModel).getOriginalModel());
    }
    return aModel;
  }

  private final ILcdIntegerIndexedModel fOriginalModel;
  private final ILcdModel fTransformedModel;

  /**
   * <p>Creates a new <code>LcdModelWrapper</code> for the given <code>ILcdModel</code>.</p>
   *
   * @param aModel The model to create the table model for. It should pass the {@link
   *               #acceptsModel(ILcdModel)} (com.luciad.model.ILcdModel)} check
   */
  public LuciadModelWrapper(ILcdModel aModel) {
    ILcdModel originalModel = retrieveModelForTableView(aModel);
    if (!acceptsModel(originalModel)) {
      throw new IllegalArgumentException("No LcdModelWrapper can be constructed for [" + aModel + "].");
    }
    fOriginalModel = (ILcdIntegerIndexedModel) originalModel;
    fTransformedModel = aModel;

    fOriginalModel.addModelListener(new WeakModelChangeListener(this));
  }

  @Override
  public Object getObjectAtRow(int aRowIndex) {
    return fOriginalModel.elementAt(aRowIndex);
  }

  @Override
  public int getRowOfObject(Object aDomainObject) {
    return fOriginalModel.indexOf(aDomainObject);
  }

  @Override
  public int getRowCount() {
    return fOriginalModel.size();
  }

  @Override
  public final ILcdIntegerIndexedModel getOriginalModel() {
    return fOriginalModel;
  }

  @Override
  public final ILcdModel getTransformedModel() {
    return fTransformedModel;
  }

  @Override
  public final Collection<Object> originalToTransformed(Object aOriginalModelElement) {
    try (TLcdLockUtil.Lock autoUnlock = TLcdLockUtil.readLock(fTransformedModel)) {
      ILcdModel tableViewModel = fOriginalModel;
      if (tableViewModel == fTransformedModel) {
        return Collections.singletonList(aOriginalModelElement);
      }
      Collection<Object> result = Collections.singleton(aOriginalModelElement);
      ILcdModel current = tableViewModel;
      ALcdTransformingModel parent = findParentModel(current);
      result = parent.originalToTransformed(result);
      while (parent != fTransformedModel) {
        current = parent;
        parent = findParentModel(current);
        result = parent.originalToTransformed(result);
      }
      return result;
    }
  }

  private ALcdTransformingModel findParentModel(ILcdModel aModel) {
    ILcdModel model = fTransformedModel;
    while (model instanceof ALcdTransformingModel && ((ALcdTransformingModel) model).getOriginalModel() != aModel) {
      model = ((ALcdTransformingModel) model).getOriginalModel();
    }
    return (ALcdTransformingModel) model;
  }

  @Override
  public final Collection<Object> transformedToOriginal(Object aTransformedModelElement) {
    try (TLcdLockUtil.Lock autoUnlock = TLcdLockUtil.readLock(fTransformedModel)) {
      ILcdModel tableViewModel = fOriginalModel;
      if (tableViewModel == fTransformedModel) {
        return Collections.singletonList(aTransformedModelElement);
      }
      Collection<Object> result = Collections.singleton(aTransformedModelElement);
      ALcdTransformingModel transformingModel = (ALcdTransformingModel) fTransformedModel;
      ILcdModel baseModel = transformingModel.getOriginalModel();
      result = transformingModel.transformedToOriginal(result);
      while (baseModel != tableViewModel) {
        transformingModel = (ALcdTransformingModel) baseModel;
        baseModel = transformingModel.getOriginalModel();
        result = transformingModel.transformedToOriginal(result);
      }
      return result;
    }
  }

  /**
   * Converts the given TLcdModelChangedEvent into swing table events.
   *
   * @param aEvent The event describing the change in the ILcdModel.
   */
  @Override
  public void modelChanged(TLcdModelChangedEvent aEvent) {
    assertOnEDT();
    int code = aEvent.getCode();
    //Small optimization if only one element is involved, otherwise
    //a table changed event is fired.
    if (aEvent.elementCount() == 1 &&
        !(allObjectsAreRemoved(code) || allObjectsAreChanged(code))) {
      int index = fOriginalModel.indexOf(aEvent.elements().nextElement());
      if (objectsAreAdded(code)) {
        fireTableRowsInserted(index, index);
      } else if (objectsAreChanged(code)) {
        fireTableRowsUpdated(index, index);
      } else {
        //An element is removed, but at what index?
        fireTableDataChanged();
      }
    } else if (objectsAreChangedAndNoneAreAddedOrRemoved(code)) {
      //it's possible that a ALL_OBJECTS_CHANGED event is fired from an empty model
      //that isn't wrong per se, so the table model should handle that gracefully
      if (fOriginalModel.size() > 0) {
        //for these events we know that no elements are added and/or removed, and only
        //data has been changed
        fireTableRowsUpdated(0, fOriginalModel.size() - 1);
      }
    } else {
      fireTableDataChanged();
    }
  }

  private void assertOnEDT() {
    if (!EventQueue.isDispatchThread()) {
      String errorMessage =
          String.format("The ILcdModel [%s] has been changed on thread [%s]. This is not allowed in Lucy. "
                        + "All model changes and all model changed events should happen on the EDT. "
                        + "Consult the LuciadLightspeed developer guide for more information on the LuciadLightspeed threading rules.",
                        fOriginalModel.getModelDescriptor().getDisplayName(),
                        Thread.currentThread().getName());
      LOG.error(errorMessage);
    }
  }

  private boolean objectsAreRemoved(int aCode) {
    return (aCode & TLcdModelChangedEvent.SOME_OBJECTS_REMOVED) != 0;
  }

  private boolean objectsAreChanged(int aCode) {
    return (aCode & TLcdModelChangedEvent.OBJECTS_CHANGED) != 0;
  }

  private boolean objectsAreAdded(int aCode) {
    return (aCode & TLcdModelChangedEvent.OBJECTS_ADDED) != 0;
  }

  private boolean allObjectsAreChanged(int aCode) {
    return (aCode & TLcdModelChangedEvent.ALL_OBJECTS_CHANGED) != 0;
  }

  private boolean allObjectsAreRemoved(int aCode) {
    return (aCode & TLcdModelChangedEvent.ALL_OBJECTS_REMOVED) != 0;
  }

  private boolean objectsAreChangedAndNoneAreAddedOrRemoved(int aCode) {
    return (allObjectsAreChanged(aCode) || objectsAreChanged(aCode)) &&
           noObjectsAreAddedOrRemoved(aCode);
  }

  private boolean noObjectsAreAddedOrRemoved(int aCode) {
    return !objectsAreAddedOrRemoved(aCode);
  }

  private boolean objectsAreAddedOrRemoved(int aCode) {
    return objectsAreAdded(aCode) || objectsAreRemoved(aCode) || allObjectsAreRemoved(aCode);
  }

}
