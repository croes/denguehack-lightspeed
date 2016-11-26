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
package samples.decoder.kml22.gxy;

import com.luciad.format.kml22.model.TLcdKML22DynamicModel;
import com.luciad.format.kml22.model.TLcdKML22ModelDescriptor;
import com.luciad.format.kml22.model.TLcdKML22RenderableModel;
import com.luciad.format.kml22.util.TLcdKML22ResourceProvider;
import com.luciad.format.kml22.view.gxy.TLcdKML22GXYLabelPainterProvider;
import com.luciad.format.kml22.view.gxy.TLcdKML22GXYLayer;
import com.luciad.format.kml22.view.gxy.TLcdKML22GXYPainterProvider;
import com.luciad.model.ILcdModel;
import com.luciad.util.logging.ILcdLogger;
import com.luciad.util.logging.TLcdLoggerFactory;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.ILcdGXYLayerFactory;
import com.luciad.view.gxy.ILcdGXYView;
import com.luciad.view.gxy.asynchronous.ILcdGXYAsynchronousPaintQueue;
import com.luciad.view.gxy.asynchronous.TLcdGXYAsynchronousEditableLabelsLayerWrapper;
import com.luciad.view.gxy.asynchronous.TLcdGXYAsynchronousPaintQueue;
import samples.gxy.decoder.MapSupport;

/**
 * The main layer factory used by this sample application.
 */
public class LayerFactory implements ILcdGXYLayerFactory {

  private static ILcdLogger sLogger = TLcdLoggerFactory.getLogger( LayerFactory.class.getName() );

  private TLcdKML22ResourceProvider fResourceProvider;
  private ILcdGXYView fView;
  private TLcdGXYAsynchronousPaintQueue fQueue = null;


  /**
   * Creates a new layer factory that can create layers for KML data.
   *
   * @param aResourceProvider A resource provider that can supply documents and images
   * @param aView             If given, the layers will be wrapped in an asynchronous layer. Can be
   *                          null.
   */
  public LayerFactory( TLcdKML22ResourceProvider aResourceProvider,
                       ILcdGXYView aView ) {
    fResourceProvider = aResourceProvider;
    fView = aView;
  }

  public ILcdGXYLayer createGXYLayer( ILcdModel aModel ) {
    if ( aModel.getModelDescriptor() instanceof TLcdKML22ModelDescriptor ) {
      return createKMLLayer( aModel );
    }
    else {
      return null;
    }
  }

  /**
   * Creates a KML layer based on a KML model
   *
   * @param aModel <code>A TLcdKML22DynamicModel</code>
   *
   * @return A <code>TLcdKML22GXYLayer</code> with a <code>TLcdKML22RenderableModel</code>
   *         based on the given model.
   */
  private ILcdGXYLayer createKMLLayer( ILcdModel aModel ) {
    if ( aModel instanceof TLcdKML22DynamicModel ) {
      TLcdKML22DynamicModel kml22Model = ( TLcdKML22DynamicModel ) aModel;
      TLcdKML22GXYLayer gxyLayer = new TLcdKML22GXYLayer( MainPanel.getLabelString( aModel ) );

      TLcdKML22RenderableModel filteredModel = new TLcdKML22RenderableModel( kml22Model );
      gxyLayer.setModel( filteredModel );
      gxyLayer.setGXYLabelPainterProvider( new TLcdKML22GXYLabelPainterProvider( fResourceProvider ) );
      gxyLayer.setGXYPainterProvider( new TLcdKML22GXYPainterProvider( fResourceProvider ) );
      gxyLayer.setSelectable( true );
      gxyLayer.setEditable( false );
      gxyLayer.setLabeled( false );
      gxyLayer.setSelectionLabeled( true );
      gxyLayer.setGXYPen( MapSupport.createPen( aModel.getModelReference() ) );
      ILcdGXYAsynchronousPaintQueue aQueue = getQueue();
      if ( aQueue != null ) {
        int nrKMLLayersLeft = 0;
        for ( int i = 0; i < fView.layerCount(); i++ ) {
          if ( fView.getLayer( i ).getModel().getModelDescriptor() instanceof TLcdKML22ModelDescriptor ) {
            nrKMLLayersLeft++;
          }
        }
        if ( nrKMLLayersLeft == 0 ) {
          try {
            aQueue.reset();
          }
          catch ( InterruptedException interruptedException ) {
            sLogger.error( "Couldn't load reset Queue (" + interruptedException.getMessage() + ")",
                           interruptedException );
            return null;
          }
        }
        return new TLcdGXYAsynchronousEditableLabelsLayerWrapper( gxyLayer, aQueue );
      }
      else {
        return gxyLayer;
      }
    }
    else {
      return null;
    }
  }

  /**
   * Returns an asynchronous paint queue that can be used to wrap layers in asynchronous layers.
   *
   * @return Either a valid asynchronous paint queue, or null if no asynchronous paint queue could
   *         be created.
   */
  private ILcdGXYAsynchronousPaintQueue getQueue() {
    if ( fQueue == null ) {
      if ( fView != null ) {
        fQueue = new TLcdGXYAsynchronousPaintQueue( fView, TLcdGXYAsynchronousPaintQueue.EVERYTHING );
        fQueue.setInterruptPainting( false );
      }
      else {
        return null;
      }
    }
    return fQueue;
  }


}
