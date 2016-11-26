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
package samples.lightspeed.integration.gl;


import com.luciad.view.ILcdLayer;
import com.luciad.view.lightspeed.layer.integration.gl.TLspGLLayerAdapter;
import com.luciad.view.lightspeed.layer.integration.gl.TLspGLLayerTreeNodeAdapter;
import com.luciad.view.opengl.ILcdGLLayer;
import com.luciad.view.opengl.TLcdGLLayerTreeNode;

import java.util.Enumeration;

/**
 * This factory creates a proper TLspGLLayerFactory for instances of ILcdGLLayer,
 * TLcdGLLayerTreeNode and ILcdLayered.
 */
public class GLLayerAdapterFactory{

  private boolean isGLLayer(ILcdLayer aLayer){
    return aLayer instanceof ILcdGLLayer;
  }

  private ILcdGLLayer retrieveGLLayer(ILcdLayer aLayer){
    if(!isGLLayer( aLayer ))
      throw new IllegalArgumentException( " Layer is not an ILcdGLLayer" );
    return (ILcdGLLayer) aLayer;
  }

  private TLspGLLayerAdapter createSingleLayerGLAdapter(ILcdGLLayer aLayer){
    TLspGLLayerAdapter adapter = new TLspGLLayerAdapter( aLayer );
    return adapter;
  }

  private TLspGLLayerAdapter createGLLayerTreeNodeAdapter(TLcdGLLayerTreeNode aLayerTreeNode){
    TLspGLLayerTreeNodeAdapter result = new TLspGLLayerTreeNodeAdapter(aLayerTreeNode);
    Enumeration elements = aLayerTreeNode.layers();
    while(elements.hasMoreElements()){
      ILcdGLLayer layer = ( ILcdGLLayer ) elements.nextElement();
      if(layer instanceof TLcdGLLayerTreeNode)
        result.addLayer( createGLLayerTreeNodeAdapter( (TLcdGLLayerTreeNode) layer ) );
      else
        result.addLayer( createSingleLayerGLAdapter( layer ) );
    }
    return result;
  }


  public TLspGLLayerAdapter createGLAdapter( ILcdGLLayer aLayer ) {
    if ( aLayer instanceof TLcdGLLayerTreeNode ) {
      return createGLLayerTreeNodeAdapter( ( TLcdGLLayerTreeNode ) aLayer );
    }
    else {
      return createSingleLayerGLAdapter( aLayer );
    }
  }
}
