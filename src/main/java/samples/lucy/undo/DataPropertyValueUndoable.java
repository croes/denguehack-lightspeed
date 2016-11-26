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
package samples.lucy.undo;

import static com.luciad.util.concurrent.TLcdLockUtil.Lock;
import static com.luciad.util.concurrent.TLcdLockUtil.writeLock;

import com.luciad.datamodel.ILcdDataObject;
import com.luciad.gui.TLcdCannotUndoRedoException;
import samples.lucy.treetableview.ExpressionUtility;
import com.luciad.lucy.util.language.TLcyLang;
import com.luciad.model.ILcdModel;

/**
 * An undoable that is created when the value of a <code>TLcdDataProperty</code> is changed.
 */
public class DataPropertyValueUndoable extends Undoable {

  private final ILcdModel fModel;
  private final ILcdDataObject fDataObject;
  private final String fExpression;
  private final Object fOldValue;
  private final Object fNewValue;

  public DataPropertyValueUndoable(ILcdModel aModel,
                                   ILcdDataObject aDataObject,
                                   String aExpression,
                                   Object aOldValue,
                                   Object aNewValue) {
    super(TLcyLang.getString("change") + " " + aExpression);
    fModel = aModel;
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
    try (Lock autoUnlock = writeLock(fModel)) {
      ExpressionUtility.updateValue(fDataObject, fExpression, aValue);
    }
    fModel.elementChanged(fDataObject, ILcdModel.FIRE_NOW);
  }
}