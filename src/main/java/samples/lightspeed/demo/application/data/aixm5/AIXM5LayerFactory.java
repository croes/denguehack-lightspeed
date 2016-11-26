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
package samples.lightspeed.demo.application.data.aixm5;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.luciad.format.aixm51.model.TLcdAIXM51ModelDescriptor;
import com.luciad.format.aixmcommon.view.lightspeed.TLspAIXMStyler;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelTreeNode;
import com.luciad.model.TLcdModelTreeNodeUtil;
import com.luciad.view.lightspeed.layer.ILspInteractivePaintableLayer;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.TLspPaintRepresentationState;
import com.luciad.view.lightspeed.layer.TLspPaintState;
import com.luciad.view.lightspeed.layer.shape.TLspShapeLayerBuilder;

import samples.lightspeed.demo.framework.data.AbstractLayerFactory;

/**
 * <p>{@code AbstractLayerFactory} for AIXM5.1 models.</p>
 *
 * <p>When an {@code ILcdModelTreeNode} is passed to this factory, it will not
 * create a matching {@code ILcdLayerTreeNode} structure. Instead, it will create individual
 * layers for </p>
 *
 */
public class AIXM5LayerFactory extends AbstractLayerFactory {

  @Override
  public boolean canCreateLayers(ILcdModel aModel) {
    return aModel != null && aModel.getModelDescriptor() instanceof TLcdAIXM51ModelDescriptor;
  }

  @Override
  public Collection<ILspLayer> createLayers(ILcdModel aModel) {
    if (aModel instanceof ILcdModelTreeNode) {
      List<ILcdModel> models = TLcdModelTreeNodeUtil.getModels((ILcdModelTreeNode) aModel, false);
      List<ILspLayer> layers = new ArrayList<ILspLayer>();
      for (ILcdModel model : models) {
        if (!(model instanceof ILcdModelTreeNode)) {
          layers.add(createSingleLayer(model));
        }
      }
      return layers;
    } else {
      return Collections.singleton(createSingleLayer(aModel));
    }
  }

  private ILspLayer createSingleLayer(ILcdModel aModel) {
    TLspAIXMStyler styler = new TLspAIXMStyler();
    ILspInteractivePaintableLayer layer = TLspShapeLayerBuilder.newBuilder().
        model(aModel).
                                                                   bodyStyler(TLspPaintState.REGULAR, styler).
                                                                   labelStyler(TLspPaintState.REGULAR, styler).build();
    //by default, only label the selection
    layer.setVisible(TLspPaintRepresentationState.REGULAR_LABEL, false);
    layer.setVisible(TLspPaintRepresentationState.SELECTED_LABEL, true);

    return layer;
  }
}
