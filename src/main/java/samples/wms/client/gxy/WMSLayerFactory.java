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
package samples.wms.client.gxy;

import com.luciad.model.ILcdModel;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.ILcdGXYPainterProvider;
import com.luciad.view.gxy.asynchronous.TLcdGXYAsynchronousLayerWrapper;
import com.luciad.wms.client.gxy.TLcdWMSProxyGXYLayer;
import com.luciad.wms.client.gxy.TLcdWMSProxyGXYLayerFactory;
import com.luciad.wms.client.gxy.TLcdWMSProxyGXYPainter;
import com.luciad.wms.client.gxy.tiled.TLcdGXYTiledWMSProxyPainter;
import com.luciad.wms.client.model.TLcdWMSProxyModelDescriptor;

/**
 * This layer factory uses a tiled WMS painter instead of the standard painter.
 */
public class WMSLayerFactory extends TLcdWMSProxyGXYLayerFactory {

  private boolean fTiled = true;

  public void setTiled(boolean aTiled) {
    fTiled = aTiled;
  }

  public boolean canCreateGXYLayer(ILcdModel aModel) {
    return aModel.getModelDescriptor() instanceof TLcdWMSProxyModelDescriptor;
  }

  @Override
  public ILcdGXYLayer createGXYLayer(ILcdModel aModel) {
    if (!canCreateGXYLayer(aModel)) {
      return null;
    }

    TLcdWMSProxyGXYLayer wmsLayer = (TLcdWMSProxyGXYLayer) super.createGXYLayer(aModel);
    wmsLayer.setGXYPainterProvider(createPainterProvider());
    ILcdGXYLayer layer = wmsLayer;

    if (!fTiled) {
      // Enable asynchronous painting for the WMS layer.
      // This is not required though, but advisable for potentially slow layers such as the WMS.
      // Without asynchronous painting, the WMS layer is painted on the event dispatching thread,
      // which causes the graphical user interface to freeze during repaint. With a slow WMS,
      // this can considerably diminish the experience of an end-user. With asynchronous painting,
      // the WMS layer is painted on a background thread, causing the GUI to remain responsive during each repaint.
      // More information on asynchronous painting in LuciadMap can be found in the LuciadMap Developer's Guide.
      layer = new TLcdGXYAsynchronousLayerWrapper(wmsLayer);
    }

    return layer;
  }

  private ILcdGXYPainterProvider createPainterProvider() {
    return fTiled ?
           new TLcdGXYTiledWMSProxyPainter() :
           new TLcdWMSProxyGXYPainter();
  }
}
