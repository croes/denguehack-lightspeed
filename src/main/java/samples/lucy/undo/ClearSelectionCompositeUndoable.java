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

import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import com.luciad.gui.TLcdCannotUndoRedoException;
import com.luciad.util.ILcdFireEventMode;
import com.luciad.view.ILcdLayer;
import com.luciad.view.ILcdTreeLayered;
import com.luciad.view.ILcdView;
import com.luciad.view.TLcdLayerTreeNodeUtil;

/**
 * Abstract Composite undoable that clears the selection of the view before letting the others undo.
 */
public class ClearSelectionCompositeUndoable<T extends ILcdView & ILcdTreeLayered> extends CompositeUndoable {
  private T fView;
  private boolean fClearOnUndo = true;
  private boolean fClearOnRedo = true;

  public ClearSelectionCompositeUndoable(String aDisplayName, T aView) {
    super(aDisplayName);
    fView = aView;
  }

  public void setClearOnUndo(boolean aClearOnUndo) {
    fClearOnUndo = aClearOnUndo;
  }

  public void setClearOnRedo(boolean aClearOnRedo) {
    fClearOnRedo = aClearOnRedo;
  }

  @Override
  public void undoImpl() throws TLcdCannotUndoRedoException {
    if (fClearOnUndo) {
      clearSelection(fView);
    }
    super.undoImpl();
  }

  @Override
  public void redoImpl() throws TLcdCannotUndoRedoException {
    if (fClearOnRedo) {
      clearSelection(fView);
    }
    super.redoImpl();
  }

  protected void clearSelection(T aView) {
    boolean old_auto_update = aView.isAutoUpdate();
    aView.setAutoUpdate(false);
    try {

      for (Enumeration<ILcdLayer> layers = getLayers(aView); layers.hasMoreElements(); ) {
        ILcdLayer layer = layers.nextElement();
        layer.clearSelection(ILcdFireEventMode.FIRE_NOW);
      }
    } finally {
      aView.setAutoUpdate(old_auto_update);
    }
  }

  protected Enumeration<ILcdLayer> getLayers(T aView) {
    List<ILcdLayer> allLayers = TLcdLayerTreeNodeUtil.getLayers(aView.getRootNode());
    return Collections.enumeration(allLayers);
  }
}
