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
/**
 *
 */
package samples.lucy.undo;

import static com.luciad.util.concurrent.TLcdLockUtil.Lock;
import static com.luciad.util.concurrent.TLcdLockUtil.writeLock;

import java.lang.ref.SoftReference;

import com.luciad.gui.ILcdUndoable;
import com.luciad.gui.TLcdCannotUndoRedoException;
import com.luciad.model.ILcdModel;
import com.luciad.util.ILcdFireEventMode;
import com.luciad.view.ILcdLayer;

/**
 * This undoable acquires the necessary locks and fires the necessary model events when
 * the wrapped undoable is undone or redone.
 */
public class ModelElementChangedUndoable extends Undoable {

  private final ILcdUndoable fDelegate;
  private final SoftReference<ILcdModel> fModel;
  private final SoftReference<ILcdLayer> fLayer;
  private final SoftReference<Object> fChangedDomainObject;

  private final Runnable fUndoRunnable = new Runnable() {
    @Override
    public void run() {
      fDelegate.undo();
    }
  };

  private final Runnable fRedoRunnable = new Runnable() {
    @Override
    public void run() {
      fDelegate.redo();
    }
  };

  public ModelElementChangedUndoable(ILcdUndoable aDelegate, Object aChangedDomainObject, ILcdLayer aLayer) {
    super(""); //getDisplayName is overridden.
    fDelegate = aDelegate;
    fModel = new SoftReference<ILcdModel>(aLayer.getModel());
    fLayer = new SoftReference<ILcdLayer>(aLayer);
    fChangedDomainObject = new SoftReference<Object>(aChangedDomainObject);
  }

  @Override
  public void undoImpl() throws TLcdCannotUndoRedoException {
    doRunnable(fUndoRunnable);
  }

  @Override
  public void redoImpl() throws TLcdCannotUndoRedoException {
    doRunnable(fRedoRunnable);
  }

  private void doRunnable(Runnable aRunnable) {
    ILcdModel model = getModel();
    ILcdLayer layer = getLayer();
    Object domainObject = getChangedDomainObject();
    try (Lock autoUnlock = writeLock(model)) {
      aRunnable.run();
    }
    model.elementChanged(domainObject, ILcdModel.FIRE_NOW);
    if (!layer.isVisible()) {
      layer.setVisible(true);
    }
    layer.selectObject(domainObject, true, ILcdFireEventMode.FIRE_NOW);
  }

  @Override
  public boolean canUndoImpl() {
    return fDelegate.canUndo();
  }

  @Override
  public boolean canRedoImpl() {
    return fDelegate.canRedo();
  }

  @Override
  public String getDisplayName() {
    return fDelegate.getDisplayName();
  }

  @Override
  public String getRedoDisplayName() {
    return fDelegate.getRedoDisplayName();
  }

  @Override
  public String getUndoDisplayName() {
    return fDelegate.getUndoDisplayName();
  }

  @Override
  public boolean isSignificant() {
    return fDelegate.isSignificant();
  }

  @Override
  public void dieImpl() {
    fDelegate.die();
    fModel.clear();
    fChangedDomainObject.clear();
  }

  private ILcdLayer getLayer() {
    ILcdLayer layer = fLayer.get();
    if (layer == null) {
      throw new TLcdCannotUndoRedoException(LAYER_UNREFERENCED_MESSAGE);
    }
    return layer;
  }

  private ILcdModel getModel() {
    ILcdModel model = fModel.get();
    if (model == null) {
      throw new TLcdCannotUndoRedoException(MODEL_UNREFERENCED_MESSAGE);
    }
    return model;
  }

  private Object getChangedDomainObject() {
    Object domainObject = fChangedDomainObject.get();
    if (domainObject == null) {
      throw new TLcdCannotUndoRedoException(DOMAIN_OBJECT_UNREFERENCED_MESSAGE);
    }
    return domainObject;
  }
}
