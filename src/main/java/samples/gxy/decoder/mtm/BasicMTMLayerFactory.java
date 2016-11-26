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
package samples.gxy.decoder.mtm;

import com.luciad.io.TLcdIOUtil;
import com.luciad.model.ILcdModel;
import com.luciad.model.TLcdMultilevelTiledModelDescriptor;
import com.luciad.util.service.LcdService;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.ILcdGXYLayerFactory;
import com.luciad.view.gxy.TLcdGXYLayer;

import samples.gxy.decoder.MapSupport;

/**
 * This is an example of ILcdGXYLayerFactory for an MTM ILcdGXYLayer. It creates
 * and sets up a TLcdGXYLayer for displaying objects contained in an ILcdModel
 * created by a TLcdMultilevelTiledModelDecoder. It uses a custom painter provider and label
 * painter provider.
 */
@LcdService
public class BasicMTMLayerFactory implements ILcdGXYLayerFactory {

  @Override
  public ILcdGXYLayer createGXYLayer(ILcdModel aModel) {

    if (!(aModel.getModelDescriptor() instanceof TLcdMultilevelTiledModelDescriptor)) {
      return null;
    }

    if (aModel.getModelReference() == null) {
      throw new IllegalArgumentException("[" + aModel.getModelDescriptor().getDisplayName() + "] did not have a reference.");
    }

    TLcdGXYLayer layer = new TLcdGXYLayer(aModel);
    layer.setSelectable(true);
    layer.setEditable(false);
    layer.setLabeled(false);
    layer.setVisible(true);

    BasicMTMPainterProvider painter = new BasicMTMPainterProvider();
    BasicMTMLabelPainterProvider label_provider = new BasicMTMLabelPainterProvider();

    layer.setModel(aModel);
    layer.setGXYPainterProvider(painter);
    layer.setGXYLabelPainterProvider(label_provider);
    layer.setGXYPen(MapSupport.createPen(aModel.getModelReference()));

    String label = TLcdIOUtil.getFileName(aModel.getModelDescriptor().getSourceName());
    if (label.lastIndexOf("." + TLcdMultilevelTiledModelDescriptor.TYPE_NAME) != -1) {
      label = label.substring(0, label.lastIndexOf("." + TLcdMultilevelTiledModelDescriptor.TYPE_NAME));
    }

    layer.setLabel(label);

    return layer;
  }
}
