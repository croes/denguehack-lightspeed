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
package samples.decoder.aixmcommon.lightspeed;

import com.luciad.format.aixm5.model.TLcdAIXM5ModelDescriptor;
import com.luciad.format.aixm51.model.TLcdAIXM51ModelDescriptor;
import com.luciad.format.aixmcommon.view.lightspeed.TLspAIXMStyler;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelTreeNode;
import com.luciad.util.service.LcdService;
import com.luciad.view.lightspeed.layer.ALspSingleLayerFactory;
import com.luciad.view.lightspeed.layer.ILspInteractivePaintableLayer;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.ILspLayerFactory;
import com.luciad.view.lightspeed.layer.TLspLayerTreeNode;
import com.luciad.view.lightspeed.layer.TLspPaintState;
import com.luciad.view.lightspeed.layer.shape.TLspShapeLayerBuilder;
import com.luciad.view.lightspeed.painter.label.TLspLabelPainter;

import java.util.Collection;
import java.util.Enumeration;

/**
 * <p>Layer factory for AIXM5.x Lightspeed layers.</p>
 *
 * <p>The layer factory can handle single {@code ILcdModel} instances as well as {@code
 * ILcdModelTreeNode} instances with child models. In case of the latter, the layer factory will
 * create a corresponding layer structure: an {@code ILcdLayerTreeNode} representing the {@code
 * ILcdModelTreeNode}, and {@code ILspLayer} instances representing the child models.</p>
 *
 * <p>The layers which contain the actual data are created in the {@link
 * #createSingleLayer(ILcdModel)} method.</p>
 *
 * @see TLspAIXMStyler
 */
@LcdService(service=ILspLayerFactory.class)
public class AIXM5xLayerFactory extends ALspSingleLayerFactory {

  @Override
  public boolean canCreateLayers( ILcdModel aModel ) {
    return aModel.getModelDescriptor() instanceof TLcdAIXM51ModelDescriptor || aModel.getModelDescriptor() instanceof TLcdAIXM5ModelDescriptor;
  }

  @Override
  public ILspLayer createLayer( ILcdModel aModel ) {
    if ( aModel instanceof ILcdModelTreeNode ) {
      TLspLayerTreeNode layerNode = new TLspLayerTreeNode( aModel.getModelDescriptor().getDisplayName() );
      final Enumeration<ILcdModel> models = ( ( ILcdModelTreeNode ) aModel ).models();
      while ( models.hasMoreElements() ) {
        Collection<ILspLayer> childLayers = createLayers( models.nextElement() );
        for ( ILspLayer childLayer : childLayers ) {
          layerNode.addLayer( childLayer );
        }
      }
      return layerNode;
    }
    else {
      return createSingleLayer( aModel );
    }
  }

  /**
   * <p>Creates an AIXM5.x layer for a single {@code ILcdModel} (cannot handle {@code
   * ILcdModelTreeNode} instances).</p>
   *
   * <p>The most important part is the {@link TLspAIXMStyler}: this styler has all the required
   * knowledge about the geometries and styling of the AIXM data model.</p>
   *
   * @param aModel The model
   *
   * @return The layer representing {@code aModel}
   */
  private ILspLayer createSingleLayer( ILcdModel aModel ) {
    TLspAIXMStyler styler = new TLspAIXMStyler();
    TLspLabelPainter labelPainter = new TLspLabelPainter();
    // better results in combination with transparent shapes
    labelPainter.setOverlayLabels( true );
    ILspInteractivePaintableLayer layer = TLspShapeLayerBuilder.newBuilder().
        model( aModel ).
        bodyStyler( TLspPaintState.REGULAR, styler ).
        bodyEditable( true ).labelPainter( labelPainter ).
        labelEditable( true ).
        labelStyler( TLspPaintState.REGULAR, styler ).build();
    layer.setEditable( false );
    return layer;
  }
}
