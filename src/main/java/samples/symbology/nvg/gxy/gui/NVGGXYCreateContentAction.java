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
package samples.symbology.nvg.gxy.gui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import com.luciad.format.nvg.nvg20.model.TLcdNVG20SymbolizedContent;
import com.luciad.gui.ALcdAction;
import samples.symbology.common.EMilitarySymbology;
import com.luciad.view.gxy.ILcdGXYController;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.ILcdGXYLayerSubsetList;
import com.luciad.view.gxy.ILcdGXYView;
import com.luciad.view.gxy.controller.ALcdGXYNewControllerModel2;
import com.luciad.view.gxy.controller.TLcdGXYCompositeController;
import com.luciad.view.gxy.controller.TLcdGXYNewController2;

import samples.gxy.common.controller.ControllerUtil;
import samples.gxy.editing.GXYCreateAction;
import samples.symbology.nvg.common.INVGControllerModel;
import samples.symbology.nvg.gui.ANVGCreateContentAction;
import samples.symbology.nvg.gxy.NVGGXYSymbolNewControllerModelFactory;

/**
 * GXY implementation of {@link ANVGCreateContentAction}
 */
public class NVGGXYCreateContentAction extends ANVGCreateContentAction {

  private final ILcdGXYLayer fLayer;
  private final ILcdGXYLayerSubsetList fLayerSubsetList;

  private NVGGXYCreateContentAction(Builder aBuilder) {
    super(aBuilder);
    fLayerSubsetList = aBuilder.fLayerSubsetList;
    fLayer = aBuilder.fLayer;
    initialize();
  }

  @Override
  public ALcdAction createSymbolAction(String aHierarchyMask, EMilitarySymbology aSymbology) {
    return new GXYCreateAction(
        NVGGXYSymbolNewControllerModelFactory.newInstanceForHierarchy(fLayer, (TLcdNVG20SymbolizedContent) getNVGContentForSymbol(), aHierarchyMask, aSymbology),
        (ILcdGXYView) getView(),
        getUndoableListener(),
        fLayerSubsetList
    );
  }

  @Override
  protected ALcdAction createShapeAction() {
    return new GXYCreateAction(
        NVGGXYSymbolNewControllerModelFactory.newInstanceForNVGContent(fLayer, getNVGContentForShape()),
        (ILcdGXYView) getView(),
        getUndoableListener(),
        fLayerSubsetList);
  }

  /**
   * tries to extract a {@link INVGControllerModel} from provided <code>TLcdGXYCompositeController</code>
   * @param aController controller whose inner <code>INVGControllerModel</code> will be extracted
   * @return a <code>INVGControllerModel</code> instance if exists, null otherwise
   */
  private INVGControllerModel extractControllerModel(TLcdGXYCompositeController aController) {
    ILcdGXYController gxyController = ControllerUtil.unwrapController(aController);
    if (gxyController != null && gxyController instanceof TLcdGXYNewController2) {
      ALcdGXYNewControllerModel2 newControllerModel = ((TLcdGXYNewController2) gxyController).getNewControllerModel();
      if (newControllerModel instanceof INVGControllerModel) {
        return (INVGControllerModel) newControllerModel;
      }
    }
    return null;
  }

  @Override
  protected PropertyChangeListener createControllerChangeListener() {
    return new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        if ("GXYController".equals(evt.getPropertyName())) {
          Object controller = evt.getNewValue();
          INVGControllerModel controllerModel;
          if (controller instanceof TLcdGXYCompositeController) {
            controllerModel = extractControllerModel((TLcdGXYCompositeController) controller);
            setActionState(controllerModel);
          }
        }
      }
    };
  }

  public Builder asBuilder() {
    return new Builder(this);
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  /**
   * Builder for <code>NVGGXYCreateContentAction</code>
   */
  public static class Builder extends ANVGCreateContentAction.Builder<Builder> {
    private ILcdGXYLayer fLayer;
    private ILcdGXYLayerSubsetList fLayerSubsetList;

    private Builder() {
    }

    private Builder(NVGGXYCreateContentAction aAction) {
      super(aAction);
      fLayer = aAction.fLayer;
      fLayerSubsetList = aAction.fLayerSubsetList;
    }

    @Override
    public ANVGCreateContentAction build() {
      return new NVGGXYCreateContentAction(this);
    }

    public Builder layer(ILcdGXYLayer aLayer) {
      fLayer = aLayer;
      return this;
    }

    public Builder layerSubsetList(ILcdGXYLayerSubsetList aSubsetList) {
      fLayerSubsetList = aSubsetList;
      return this;
    }
  }

}
