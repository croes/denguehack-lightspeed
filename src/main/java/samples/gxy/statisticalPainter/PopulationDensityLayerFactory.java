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
package samples.gxy.statisticalPainter;

import com.luciad.model.ILcdModel;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.ILcdGXYLayerFactory;
import com.luciad.view.gxy.TLcdGXYLayer;
import com.luciad.view.map.TLcdGeodeticPen;

/**
 * Implementation of an ILcdGXYLayerFactory to create ILcdGXYLayer objects that
 * display the density of the population by use of a PopulationDensityPainter.
 */
class PopulationDensityLayerFactory implements ILcdGXYLayerFactory {

  private PopulationDensityPainter fPainter = new PopulationDensityPainter();

  @Override
  public ILcdGXYLayer createGXYLayer(ILcdModel aModel) {
    TLcdGXYLayer gxy_layer = new TLcdGXYLayer(aModel);

    gxy_layer.setGXYPen(new TLcdGeodeticPen());

    gxy_layer.setLabel("Population Density");

    gxy_layer.setSelectable(false);
    gxy_layer.setEditable(false);
    gxy_layer.setLabeled(false);
    gxy_layer.setVisible(true);
    gxy_layer.setGXYPainterProvider(fPainter);

    return gxy_layer;
  }

}



