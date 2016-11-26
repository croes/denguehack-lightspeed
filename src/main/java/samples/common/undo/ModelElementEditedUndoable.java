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

import static com.luciad.util.concurrent.TLcdLockUtil.Lock;
import static com.luciad.util.concurrent.TLcdLockUtil.writeLock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.luciad.gui.ALcdUndoable;
import com.luciad.gui.TLcdCannotUndoRedoException;
import com.luciad.model.ILcdModel;
import com.luciad.util.ILcdFireEventMode;
import com.luciad.util.logging.ILcdLogger;
import com.luciad.util.logging.TLcdLoggerFactory;
import com.luciad.view.ILcdLayer;

/**
 * An undoable that can undo/redo the editing operations on a model element. It can only provide
 * undo/support for model elements implementing StateAware. This implementation is based on a
 * variation of the Memento design pattern.
 * <p/>
 * This class is merely a demonstration of how an ILcdUndoable could be implemented and it is in no
 * way mandatory to choose this implementation technique to implement your own ILcdUndoable.
 * <p/>
 * This implementation works as follows: it stores the state of an object right before it is changed
 * and right after it is changed. Undoing then consists of telling the object to restore its state
 * from the state saved before the change, while redoing consists of telling the object to restore
 * its state from the state saved after the change.
 * <p/>
 * To use this class, you must instantiate one object and invoke storeBeforeState before making a
 * change to the object. After you have changed the object, you should invoke storeAfterState and
 * notify the appropriate listeners of this undoable.
 *
 * @see "Design Patterns - Elements of Reusable Object-Oriented Software" by Gamma et al.
 */
public class ModelElementEditedUndoable extends ALcdUndoable {

  private static ILcdLogger sLogger = TLcdLoggerFactory.getLogger(ModelElementEditedUndoable.class.getName());

  private ILcdModel fModel;
  private StateAware fEditedObject;
  private Map<Object, Object> fBeforeMap = new HashMap<Object, Object>();
  private Map<Object, Object> fAfterMap = new HashMap<Object, Object>();
  private ILcdLayer fLayer;
  private final boolean fFireEvents;

  /**
   * Creates a new undoable. Do not forget to call storeBeforeState before changing the object!
   *
   * @param aDisplayName  The display name for this undoable.
   * @param aEditedObject The element that is about to be changed.
   * @param aLayer        The layer containing aEditedObject.
   * @param aFireEvents   Whether the model should be notified of model changes and whether this
   *                      undoable should acquire a write lock on the model before changing the
   *                      domain object.
   */
  public ModelElementEditedUndoable(String aDisplayName,
                                    StateAware aEditedObject,
                                    ILcdLayer aLayer,
                                    boolean aFireEvents) {
    super(aDisplayName);
    fLayer = aLayer;
    fFireEvents = aFireEvents;
    fModel = aLayer.getModel();
    fEditedObject = aEditedObject;
  }

  /**
   * Stores the state of the object. Call this method before actually changing the object!
   */
  public void storeBeforeState() {
    try {
      fBeforeMap = new HashMap<Object, Object>();
      fEditedObject.storeState(fBeforeMap, fModel);
    } catch (StateException e) {
      throw new RuntimeException(e); // this should never happen
    }
  }


  /**
   * Completes this undoable. This method must be called after the change to the element has been
   * made.
   */
  public void storeAfterState() {
    try {
      fAfterMap = new HashMap<Object, Object>();
      fEditedObject.storeState(fAfterMap, fModel);
    } catch (StateException e) {
      throw new RuntimeException(e); // this should never happen
    }
    removeRedundantState();
  }

  /**
   * This method weeds out the state that hasn't changed. For instance, the color of a polyline
   * could have changed while the locations of its points may have remained the same. In that case,
   * this method will make sure only the difference in color is kept in the changes map, and the
   * rest of the saved state (that has remained the same) is thrown out.
   */
  private void removeRedundantState() {
    List<Object> redundantKeys = new ArrayList<Object>(fAfterMap.size());
    for (Object key : fBeforeMap.keySet()) {
      if (fAfterMap.containsKey(key) &&
          afterEqualsBefore(key)) {
        redundantKeys.add(key);
      }
    }

    for (Object key : redundantKeys) {
      fBeforeMap.remove(key);
      fAfterMap.remove(key);
    }
  }

  /**
   * This method checks if the values for aKey in the fAfterMap and the fBeforeMap are
   * the same.
   *
   * @param aKey The key whose value will be checked for equality.
   *
   * @return true if the value in fBeforeMap and fAfterMap is the same for aKey.
   */
  private boolean afterEqualsBefore(Object aKey) {
    Object after = fAfterMap.get(aKey);
    Object before = fBeforeMap.get(aKey);

    boolean bothNull = after == null && before == null;
    boolean nonNullAndEqual = after != null && after.equals(before);
    boolean equalArrays = equalArrays(before, after);

    return bothNull || nonNullAndEqual || equalArrays;
  }

  /**
   * Checks whether the objects are arrays and if so if they contain equal elements.
   *
   * @param aBefore A possible array containing part of the state before the changes.
   * @param aAfter  A possible array containing part of the state after the changes.
   *
   * @return true if the objects are arrays containing equal elements, false otherwise
   */
  private boolean equalArrays(Object aBefore, Object aAfter) {
    boolean equalArrays = false;

    // all these instanceof statements are needed because the #equals() method
    // of arrays is implemented as identity equality.
    if (aBefore instanceof long[] && aAfter instanceof long[]) {
      equalArrays = Arrays.equals((long[]) aBefore, (long[]) aAfter);
    } else if (aBefore instanceof int[] && aAfter instanceof int[]) {
      equalArrays = Arrays.equals((int[]) aBefore, (int[]) aAfter);
    } else if (aBefore instanceof short[] && aAfter instanceof short[]) {
      equalArrays = Arrays.equals((short[]) aBefore, (short[]) aAfter);
    } else if (aBefore instanceof char[] && aAfter instanceof char[]) {
      equalArrays = Arrays.equals((char[]) aBefore, (char[]) aAfter);
    } else if (aBefore instanceof byte[] && aAfter instanceof byte[]) {
      equalArrays = Arrays.equals((byte[]) aBefore, (byte[]) aAfter);
    } else if (aBefore instanceof boolean[] && aAfter instanceof boolean[]) {
      equalArrays = Arrays.equals((boolean[]) aBefore, (boolean[]) aAfter);
    } else if (aBefore instanceof double[] && aAfter instanceof double[]) {
      equalArrays = Arrays.equals((double[]) aBefore, (double[]) aAfter);
    } else if (aBefore instanceof float[] && aAfter instanceof float[]) {
      equalArrays = Arrays.equals((float[]) aBefore, (float[]) aAfter);
    } else if (aBefore instanceof Object[] && aAfter instanceof Object[]) {
      equalArrays = Arrays.equals((Object[]) aBefore, (Object[]) aAfter);
    }
    return equalArrays;
  }

  protected void undoImpl() throws TLcdCannotUndoRedoException {
    restoreState(fBeforeMap);
  }

  protected void redoImpl() throws TLcdCannotUndoRedoException {
    restoreState(fAfterMap);
  }

  private void restoreState(Map aState) {
    try {
      if (!fFireEvents) {
        fEditedObject.restoreState(aState, fModel);
      } else {
        try (Lock autoUnlock = writeLock(fModel)) {
          fEditedObject.restoreState(aState, fModel);
          fModel.elementChanged(fEditedObject, ILcdFireEventMode.FIRE_NOW);
        }
        fLayer.selectObject(fEditedObject, true, ILcdFireEventMode.FIRE_NOW);
      }
    } catch (StateException e) {
      sLogger.error(e.getMessage(), e);
      TLcdCannotUndoRedoException exception = new TLcdCannotUndoRedoException(e.getMessage());
      exception.initCause(e);
      throw exception;
    }
  }

  protected void dieImpl() {
    fEditedObject = null;
    fBeforeMap.clear();
    fAfterMap.clear();
  }

  protected boolean canUndoImpl() {
    return fBeforeMap.size() != 0 && fAfterMap.size() != 0;
  }

  protected boolean canRedoImpl() {
    return fBeforeMap.size() != 0 && fAfterMap.size() != 0;
  }
}
