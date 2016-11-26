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
package samples.lucy.layerpropertyeditor;

import java.awt.BorderLayout;
import java.util.EventObject;

import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.lucy.gui.ALcyActiveSettable;
import com.luciad.lucy.gui.ILcyApplicationPane;
import com.luciad.lucy.gui.ILcyApplicationPaneFactory;
import com.luciad.lucy.gui.ILcyApplicationPaneOwner;
import com.luciad.lucy.map.ILcyGenericMapComponent;
import com.luciad.lucy.util.language.TLcyLang;
import com.luciad.util.ILcdChangeListener;
import com.luciad.util.TLcdChangeEvent;
import com.luciad.view.ILcdLayer;
import com.luciad.view.ILcdView;

/**
 * Adds a panel to show and/or modify the properties of the selected layer.
 */
public class LayerPropertyEditorActiveSettable extends ALcyActiveSettable {

  ILcyApplicationPane fApplicationPane;

  private ILcyLucyEnv fLucyEnv;
  private ILcyGenericMapComponent<? extends ILcdView, ? extends ILcdLayer> fMapComponent;
  private LayerPropertyEditor fLayerPropertyEditor;

  private boolean fActive = false;

  /**
   * Creates a new LayerPropertyEditorActiveSettable.
   *
   * @param aLucyEnv the lucy environment to use.
   * @param aMapComponent the map component for this active settable.
   */
  public LayerPropertyEditorActiveSettable(ILcyLucyEnv aLucyEnv, ILcyGenericMapComponent<? extends ILcdView, ? extends ILcdLayer> aMapComponent) {
    fLucyEnv = aLucyEnv;
    fMapComponent = aMapComponent;

    setName(TLcyLang.getString("Show layer properties"));
    setShortDescription(TLcyLang.getString("Editor for the properties of the selected layer."));
  }

  @Override
  public boolean isActive() {
    return fActive;
  }

  @Override
  public void setActive(boolean aActive) {
    if (isActive() != aActive) {
      if (aActive) {
        MyApplicationPaneOwner owner = new MyApplicationPaneOwner();
        fApplicationPane = fLucyEnv.getApplicationPaneFactory().createApplicationPane(
            ILcyApplicationPaneFactory.VERTICAL_PANE, owner);
        fApplicationPane.setAppTitle(TLcyLang.getString("Layer properties"));
        fLayerPropertyEditor = new LayerPropertyEditor(fLucyEnv);
        fLayerPropertyEditor.setMapComponent(fMapComponent);

        fApplicationPane.getAppContentPane().setLayout(new BorderLayout());

        // Don't add a scroll pane here.  The contents of the panels is not known, so if for example
        // a panel has tabs inside it, it would be very weird that one can scroll the tabs away.
        fApplicationPane.getAppContentPane().add(fLayerPropertyEditor, BorderLayout.CENTER);

        if (fApplicationPane.canPackApp()) {
          fApplicationPane.packApp();
        }
        fApplicationPane.setResizable(true);
        fApplicationPane.setAppVisible(true);
        fApplicationPane.bringAppToFront();

        fLayerPropertyEditor.addChangeListener(new ILcdChangeListener() {
          @Override
          public void stateChanged(TLcdChangeEvent event) {
            fApplicationPane.bringAppToFront();
          }
        });
      } else {
        fApplicationPane.disposeApp();
      }
      updateActiveState();
    }
  }

  private void updateActiveState() {
    boolean active = fApplicationPane != null;
    if (active != fActive) {
      fActive = active;
      firePropertyChange("active", !fActive, fActive);
    }
  }

  private class MyApplicationPaneOwner implements ILcyApplicationPaneOwner {
    @Override
    public void applicationPaneDisposing(EventObject aEvent) {
      if (fApplicationPane != null) {
        fApplicationPane = null;
        //remove listener from mapcomponent
        fLayerPropertyEditor.setMapComponent(null);
        fLayerPropertyEditor = null;
        updateActiveState();
      }
    }
  }
}
