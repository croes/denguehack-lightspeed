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
package samples.symbology.nvg.lightspeed.gui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;

import com.luciad.gui.ALcdAction;
import samples.symbology.common.EMilitarySymbology;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.controller.manipulation.TLspCreateController;
import com.luciad.view.lightspeed.layer.ILspInteractivePaintableLayer;

import samples.lightspeed.editing.LspCreateAction;
import samples.symbology.nvg.common.INVGControllerModel;
import samples.symbology.nvg.gui.ANVGCreateContentAction;
import samples.symbology.nvg.lightspeed.NVGLspSymbolCreateControllerModelFactory;

/**
 * LSP implementation of {@link ANVGCreateContentAction}
 */
public class NVGLSPCreateContentAction extends ANVGCreateContentAction {

  private ILspInteractivePaintableLayer fLayer;

  protected NVGLSPCreateContentAction(Builder aBuilder) {
    super(aBuilder);
    fLayer = aBuilder.fLayer;
    initialize();
  }

  @Override
  protected PropertyChangeListener createControllerChangeListener() {
    return new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        if ("controller".equals(evt.getPropertyName())) {
          Object controller = evt.getNewValue();
          INVGControllerModel controllerModel = null;
          if (controller instanceof TLspCreateController) {
            Object controllerModel2 = ((TLspCreateController) controller).getControllerModel();
            if (controllerModel2 instanceof INVGControllerModel) {
              controllerModel = (INVGControllerModel) controllerModel2;
            }
          }
          setActionState(controllerModel);
        }
      }
    };
  }

  @Override
  protected ALcdAction createShapeAction() {
    return new LspCreateAction(
        NVGLspSymbolCreateControllerModelFactory.newInstanceForNVGContent(getNVGContentForShape(),
                                                                          fLayer),
        Collections.singletonList((ILspView) getView()),
        getUndoableListener()
    );
  }

  @Override
  public ALcdAction createSymbolAction(String aSidc, EMilitarySymbology aMilitarySymbology) {
    return new LspCreateAction(
        NVGLspSymbolCreateControllerModelFactory.newInstanceForHierarchy(getNVGContentForSymbol(), fLayer, aSidc,
                                                                         aMilitarySymbology
        ),
        Collections.singletonList((ILspView) getView()),
        getUndoableListener()
    );
  }

  public Builder asBuilder() {
    return new Builder(this);
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  /**
   * Builder for <code>NVGLSPCreateContentAction</code>
   */
  public static class Builder extends ANVGCreateContentAction.Builder<Builder> {
    private ILspInteractivePaintableLayer fLayer;

    private Builder() {
    }

    private Builder(NVGLSPCreateContentAction aAction) {
      super(aAction);
      fLayer = aAction.fLayer;
    }

    @Override
    public ANVGCreateContentAction build() {
      return new NVGLSPCreateContentAction(this);
    }

    public Builder layer(ILspInteractivePaintableLayer aLayer) {
      fLayer = aLayer;
      return this;
    }
  }
}
