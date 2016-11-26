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
package samples.gxy.undo;

import java.awt.Graphics;
import java.text.MessageFormat;

import com.luciad.gui.ILcdUndoableListener;
import com.luciad.gui.ILcdUndoableSource;
import com.luciad.gui.TLcdUndoSupport;
import samples.common.undo.ModelElementEditedUndoable;
import samples.common.undo.StateAware;
import com.luciad.view.gxy.ILcdGXYContext;
import com.luciad.view.gxy.painter.TLcdGXYPointListPainter;

/**
 * Extension of TLcdGXYPointListPainter that implements ILcdUndoableSource. It saves the state of
 * the polyline before it applies the changes to it and right after it, and creates an ILcdUndoable
 * from that.
 */
class UndoablePointListPainter extends TLcdGXYPointListPainter implements ILcdUndoableSource {

  private final static MessageFormat FORMAT = new MessageFormat("Edit {0}");

  private TLcdUndoSupport fUndoSupport = new TLcdUndoSupport(this);

  public UndoablePointListPainter(int aPointListMode) {
    super(aPointListMode);
  }

  public void addUndoableListener(ILcdUndoableListener aUndoableListener) {
    fUndoSupport.addUndoableListener(aUndoableListener);
  }

  public void removeUndoableListener(ILcdUndoableListener aUndoableListener) {
    fUndoSupport.removeUndoableListener(aUndoableListener);
  }

  /**
   * Overridden method that saves the state right before and right after applying the change, and
   * creates an ILcdUndoable from that information.
   */
  public boolean edit(Graphics aGraphics, int aMode, ILcdGXYContext aContext) {
    StateAware edited_object = (StateAware) getObject();

    // Create a ModelElementEditedUndoable and store the original state of the object
    // that is about to be edited.
    // Note that TLcdGXYNewController2 uses the creation undoables internally (i.e. backspace will
    // undo a single creation step) and generates a single undoable for the addition of the object
    // when creation is finished.
    ModelElementEditedUndoable undoable = new ModelElementEditedUndoable(
        generateDisplayName(edited_object),
        edited_object,
        aContext.getGXYLayer(),
        false
    );
    undoable.storeBeforeState();

    boolean object_changed = super.edit(aGraphics, aMode, aContext);

    finishAndFireUndoable(undoable, object_changed);

    return object_changed;
  }

  /**
   * Finishes the undoable and notifies the registered listeners of this undoable. As part of this
   * process, the state of the object after the modification is recorded.
   *
   * @param aUndoable      The undoable to finish. This undoable already contains the initial state of
   *                       the edited object.
   * @param aObjectChanged Whether or not the object changed. This indicates if the registered
   *                       listeners should be notified of this undoable.
   */
  private void finishAndFireUndoable(ModelElementEditedUndoable aUndoable, boolean aObjectChanged) {
    if (aUndoable != null) {
      aUndoable.storeAfterState();
      if (aObjectChanged && aUndoable.canUndo()) {
        fUndoSupport.fireUndoableHappened(aUndoable);
      } else {
        aUndoable.die();
      }
    }
  }

  private String generateDisplayName(Object aEditedObject) {
    return FORMAT.format(new Object[]{aEditedObject.toString()});
  }

  public Object clone() {
    UndoablePointListPainter clone = (UndoablePointListPainter) super.clone();

    //don't clone the listeners
    clone.fUndoSupport = new TLcdUndoSupport(clone);

    return clone;
  }
}
