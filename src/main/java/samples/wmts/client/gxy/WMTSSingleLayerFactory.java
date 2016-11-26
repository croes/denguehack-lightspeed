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
package samples.wmts.client.gxy;

import com.luciad.gui.ILcdIcon;
import com.luciad.gui.TLcdCompositeIcon;
import com.luciad.gui.TLcdImageIcon;
import com.luciad.gui.TLcdResizeableIcon;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelTreeNode;
import com.luciad.ogc.wmts.client.TLcdWMTSModelDescriptor;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.ILcdGXYLayerFactory;
import com.luciad.view.gxy.TLcdGXYLayer;
import com.luciad.view.gxy.painter.TLcdGXYImagePainter;

/**
 * A layer factory that can create layers for single WMTS models. It <i>cannot</i> create layers for WMTS tree models.
 * It is intended as a delegate for {@link WMTSLayerFactory}, which <i>can</i> create layers for
 * both single WMTS models and WMTS tree models.
 *
 * @see WMTSLayerFactory
 * @since 2013.1
 */
public class WMTSSingleLayerFactory implements ILcdGXYLayerFactory {

  @Override
  public ILcdGXYLayer createGXYLayer(ILcdModel aModel) {
    if (!(aModel.getModelDescriptor() instanceof TLcdWMTSModelDescriptor) || aModel instanceof ILcdModelTreeNode) {
      // Don't create layers when the model is either not a WMTS model or not a _single_ WMTS model.
      return null;
    }
    TLcdGXYLayer layer = new TLcdGXYLayer(aModel, aModel.getModelDescriptor().getDisplayName());
    final TLcdGXYImagePainter imagePainter = new TLcdGXYImagePainter();
    // Typically the WMTS layers have rendered text on them.
    // We don't want to paint them too small, so that's why we go for 0.625 being the average of 0.25 and 1.0.
    imagePainter.setLevelSwitchFactor(0.625);
    imagePainter.setWarpBlockSize(16);
    imagePainter.setStartResolutionFactor(10);
    layer.setGXYPainterProvider(imagePainter);
    layer.setIcon(getIcon(aModel));
    return layer;
  }

  /**
   * Decorates the layer icon with a warning icon when the model's tile structure is suboptimal.
   *
   * @see TLcdWMTSModelDescriptor#hasCompatibleTileStructure()
   */
  public static ILcdIcon getIcon(ILcdModel aModel) {
    ILcdIcon icon = new TLcdImageIcon("images/icons/map_3D_16.png");
    if (!((TLcdWMTSModelDescriptor) aModel.getModelDescriptor()).hasCompatibleTileStructure()) {
      TLcdCompositeIcon compositeIcon = new TLcdCompositeIcon();
      compositeIcon.addIcon(new TLcdResizeableIcon(icon));
      compositeIcon.addIcon(new TLcdResizeableIcon(new TLcdImageIcon("images/icons/error_deco_16.png")));
      icon = compositeIcon;
    }
    return icon;
  }
}
