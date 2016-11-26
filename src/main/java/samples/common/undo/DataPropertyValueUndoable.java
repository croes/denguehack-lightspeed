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
package samples.common.undo;

import com.luciad.datamodel.ILcdDataObject;
import com.luciad.gui.ALcdUndoable;
import com.luciad.gui.TLcdCannotUndoRedoException;
import com.luciad.model.ILcdModel;
import com.luciad.util.concurrent.TLcdLockUtil;

/**
 * An undoable that is created when the value of a <code>TLcdDataProperty</code> is changed.
 */
public class DataPropertyValueUndoable extends ALcdUndoable {

  private final ILcdModel fModel;
  private final Object fModelObject;
  private final ILcdDataObject fDataObject;
  private final String fExpression;
  private final Object fOldValue;
  private final Object fNewValue;

  public DataPropertyValueUndoable(ILcdDataObject aDataObject,
                                   String aExpression,
                                   Object aOldValue,
                                   Object aNewValue) {
    this(null, null, aDataObject, aExpression, aOldValue, aNewValue);
  }

  public DataPropertyValueUndoable(ILcdModel aModel,
                                   Object aModelElement,
                                   ILcdDataObject aDataObject,
                                   String aExpression,
                                   Object aOldValue,
                                   Object aNewValue) {
    super("change" + " " + aExpression);
    fModel = aModel;
    fModelObject = aModelElement;
    fDataObject = aDataObject;
    fExpression = aExpression;
    fOldValue = aOldValue;
    fNewValue = aNewValue;
  }

  @Override
  protected void undoImpl() throws TLcdCannotUndoRedoException {
    setValue(fOldValue);
  }

  @Override
  protected void redoImpl() throws TLcdCannotUndoRedoException {
    setValue(fNewValue);
  }

  private void setValue(Object aValue) {
    if (fModel != null) {
      TLcdLockUtil.writeLock(fModel);
    }
    try {
      fDataObject.setValue(fExpression, aValue);
    } finally {
      if (fModel != null) {
        TLcdLockUtil.writeUnlock(fModel);
        fModel.elementChanged(fModelObject, ILcdModel.FIRE_NOW);
      }
    }
  }
}
