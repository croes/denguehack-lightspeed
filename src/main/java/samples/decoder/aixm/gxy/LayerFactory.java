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
package samples.decoder.aixm.gxy;

import com.luciad.format.aixm.TLcdAIXMDefaultLayerFactory;
import com.luciad.model.ILcdModel;
import com.luciad.util.service.LcdService;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.ILcdGXYLayerFactory;

/**
 * This layerFactory is developed to create layers for AIXM model data
 * in the <code>AIXM</code> sample.
 * <p>
 * To create <code>AIXM</code> layers, an instance of the <code>TLcdAIXMDefaultLayerFactory</code>
 * class is used.
 * </p>
 */
@LcdService(service = ILcdGXYLayerFactory.class)
public class LayerFactory implements ILcdGXYLayerFactory {

  //method of ILcdGXYLayerFactory
  public ILcdGXYLayer createGXYLayer( ILcdModel aModel ) {
    if ( aModel != null ) { // TLcdAIXMDefaultLayerFactory will check if the model is supported
      return createAIXMLayerFactory().createGXYLayer( aModel );
    }
    return null;
  }

  private ILcdGXYLayerFactory createAIXMLayerFactory() {
    TLcdAIXMDefaultLayerFactory layerFactory = new TLcdAIXMDefaultLayerFactory();
    layerFactory.setCreateLayerTreeNodes( true );
    return layerFactory;
  }
}
