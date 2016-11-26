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
package samples.wms.client.opengl;

import com.luciad.model.ILcdModel;
import com.luciad.view.opengl.ILcdGLLayer;
import com.luciad.view.opengl.ILcdGLLayerFactory;
import com.luciad.view.opengl.ILcdGLView;
import com.luciad.view.opengl.TLcdGLLayer;
import com.luciad.wms.client.model.TLcdWMSProxyModelDescriptor;
import com.luciad.wms.client.opengl.tiled.TLcdGLTiledWMSProxyPainter;

import samples.opengl.common.GLViewSupport;

/**
 * Layer factory to create layers visualizing WMS proxies in 3D.
 *
 * @since 11.0
 */
public class WMS3DLayerFactory implements ILcdGLLayerFactory {

  public ILcdGLLayer createLayer(ILcdModel aModel, ILcdGLView aTargetView) {
    if (isValidModel(aModel, aTargetView)) {
      TLcdGLLayer layer = new TLcdGLLayer(aModel);
      //WMS models will be visualized as a tile set.
      TLcdGLTiledWMSProxyPainter painter = new TLcdGLTiledWMSProxyPainter();
      painter.setQuality(0.5);
      layer.setPainter(painter);
      layer.setLabel(aModel.getModelDescriptor().getDisplayName());
      layer.setPathFactory(GLViewSupport.createPathFactory(aModel.getModelReference()));
      return layer;
    }
    return null;
  }

  public boolean isValidModel(ILcdModel aModel, ILcdGLView aTargetView) {
    return aModel.getModelDescriptor() instanceof TLcdWMSProxyModelDescriptor;
  }
}
