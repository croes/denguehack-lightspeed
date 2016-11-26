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
package samples.lucy.formatbar;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.luciad.lucy.map.action.lightspeed.TLcyLspCreateLayerAction;
import com.luciad.lucy.map.lightspeed.ILcyLspMapComponent;
import com.luciad.lucy.map.lightspeed.controller.ALcyLspCreateControllerModel;
import com.luciad.util.ILcdCloneable;
import com.luciad.view.lightspeed.editor.TLspEditContext;

/**
 * {@link ALcyLspCreateControllerModel} that allows to add
 * action listeners that are informed when a new object has been added to a layer.
 */
public abstract class ObservableLspCreateControllerModel extends ALcyLspCreateControllerModel implements ILcdCloneable {

  private List<ActionListener> fListeners;

  protected ObservableLspCreateControllerModel(ILcyLspMapComponent aMapComponent, TLcyLspCreateLayerAction aCreateLayerAction) {
    super(aMapComponent, aCreateLayerAction);
  }

  @Override
  public void finished(TLspEditContext aEditContext) {
    super.finished(aEditContext);

    if (fListeners != null) {
      ActionEvent actionEvent = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "");
      for (ActionListener listener : fListeners) {
        listener.actionPerformed(actionEvent);
      }
    }
  }

  public void addActionListener(ActionListener aActionListener) {
    if (fListeners == null) {
      fListeners = new CopyOnWriteArrayList<ActionListener>();
    }
    fListeners.add(aActionListener);
  }

  public void removeActionListener(ActionListener aActionListener) {
    if (fListeners != null) {
      fListeners.remove(aActionListener);
    }
  }

  @SuppressWarnings("CloneDoesntDeclareCloneNotSupportedException")
  @Override
  public ObservableLspCreateControllerModel clone() {
    try {
      ObservableLspCreateControllerModel result = (ObservableLspCreateControllerModel) super.clone();
      result.fListeners = null;
      return result;
    } catch (CloneNotSupportedException e) {
      throw new RuntimeException(e);
    }
  }
}
