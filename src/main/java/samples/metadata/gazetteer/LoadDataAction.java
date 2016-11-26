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
package samples.metadata.gazetteer;

import com.luciad.datamodel.expression.ILcdDataObjectExpression;
import com.luciad.datamodel.expression.TLcdDataObjectExpressionContext;
import com.luciad.datamodel.expression.TLcdDataObjectExpressionLanguage;
import com.luciad.format.metadata.model.metadataentityset.TLcdISO19115Metadata;
import com.luciad.gui.TLcdUserDialog;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelDecoder;
import com.luciad.model.TLcdCompositeModelDecoder;
import com.luciad.model.TLcdOpenAction;
import com.luciad.util.ILcdFormatter;
import com.luciad.util.logging.ILcdLogger;
import com.luciad.util.logging.TLcdLoggerFactory;
import samples.common.serviceregistry.ServiceRegistry;
import com.luciad.view.ILcdLayer;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.ILcdGXYLayerFactory;
import com.luciad.view.gxy.ILcdGXYView;
import com.luciad.view.gxy.TLcdCompositeGXYLayerFactory;
import com.luciad.view.gxy.controller.TLcdGXYEditController2;

import samples.gxy.common.layers.GXYLayerUtil;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.net.URI;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;
import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

/**
 * Loads data which is referred to in a metadata object.
 * It will look for data locations in:
 * Metadata -> IdentificationInfo -> transferOptions -> onLine -> linkage
 */
class LoadDataAction extends AbstractAction {

  private static ILcdLogger sLogger = TLcdLoggerFactory.getLogger(LoadDataAction.class.getName());

  private ILcdGXYView fGXYView;
  private TLcdGXYEditController2 fEditController;

  private TouchedMetadataUtil fTouchedMetadataUtil = new TouchedMetadataUtil();
  private ILcdDataObjectExpression fExpression;
  private TLcdDataObjectExpressionContext fContext;
  private TLcdOpenAction fOpenAction = new TLcdOpenAction();
  private TLcdCompositeGXYLayerFactory fLayerFactory;
  private String fCodeBase;


  /**
   * @param aGXYView the view to which the data should be added.
   *                 It should have a layer factory set which can create a layer for the data loaded.
   * @param aServiceRegistry the registry to use for decoding models and creating layers
   */
  public LoadDataAction( ILcdGXYView aGXYView,
                         TLcdGXYEditController2 aEditController,
                         String aCodeBase,
                         ServiceRegistry aServiceRegistry ) {
    fGXYView = aGXYView;
    fEditController = aEditController;
    fCodeBase = aCodeBase;
    fOpenAction.setLoadInSeparateThread( false );
    TLcdDataObjectExpressionLanguage lang = new TLcdDataObjectExpressionLanguage();
    fContext = lang.createContext( null );
    fExpression = lang.compile( "distributionInfo.MD_Distribution.transferOptions.MD_DigitalTransferOptions.onLine.CI_OnlineResource.linkage.URL" );
    List<ILcdModelDecoder> modelDecoders = new TLcdCompositeModelDecoder( aServiceRegistry.query( ILcdModelDecoder.class ) ).getModelDecoders();
    fOpenAction.setModelDecoder( modelDecoders.toArray( new ILcdModelDecoder[modelDecoders.size()] ) );
    fLayerFactory = new TLcdCompositeGXYLayerFactory( aServiceRegistry.query( ILcdGXYLayerFactory.class ) );
  }

  public void actionPerformed( ActionEvent e ) {

    int x = fEditController.lastXReleased();
    int y = fEditController.lastYReleased();

    TLcdISO19115Metadata metadata = fTouchedMetadataUtil.findTouchedMetadata( x, y, fGXYView );

    if ( metadata == null ) {
      sLogger.info("No metadata selected, can not load data." );
      return;
    }

    if ( fOpenAction.getModelDecoder() == null || fOpenAction.getModelDecoder().length == 0 ) {
      sLogger.info("No decoders are set, can not load data." );
      return;
    }

    URI[] locations = findLocations( metadata );
    if ( locations.length == 0 ) {
      JOptionPane.showMessageDialog(
              (Component) fGXYView,
              "Could not find any location in the metadata to load the data.",
              "No location found",
              JOptionPane.INFORMATION_MESSAGE
      );
      return;
    }

    URI location;
    if ( locations.length == 1 ) {
      location = locations[ 0 ];
    } else {
      location = selectLocation( locations );   // select one from the list.
    }

    if ( location != null ) {
      String location_as_string;
      // this is a hack to enable loading local data in the sample. Normally this would have to be
      // a full fledged URL.
      if ( "file".equals( location.getScheme()) ) {
        location_as_string = location.getHost() + "/" + location.getPath();
      } else {
        location_as_string = location.toString();
      }
      // check if the view does not already contain a model loaded from this location
      boolean model_found = false;
      Enumeration<?> layers = fGXYView.layers();
      while ( layers.hasMoreElements() && !model_found ) {
        ILcdLayer layer = (ILcdLayer) layers.nextElement();
        String source = layer.getModel().getModelDescriptor().getSourceName();
        model_found = source.equals( location_as_string );
      }

      if ( !model_found ) {
        ILcdModel model = fOpenAction.loadFile( fCodeBase + location_as_string );
        ILcdGXYLayer layer = fLayerFactory.createGXYLayer( model );
        GXYLayerUtil.addGXYLayer( fGXYView, layer );
        // we expect the grid and gazetteer layer to stay on top.
        GXYLayerUtil.moveGXYLayer( fGXYView, fGXYView.layerCount() - 3, layer );
      }
    }
  }

  private URI[] findLocations( TLcdISO19115Metadata aMetadata ) {
    fContext.setRoot( aMetadata );
    List<Object> locations_v = (List<Object>) fExpression.evaluate( fContext );
    URI[] locations = new URI[ locations_v.size() ];
    for ( int location_index = 0; location_index < locations_v.size() ; location_index++ ) {
        locations[ location_index ] = (URI) locations_v.get( location_index );
    }
    return locations;
  }

  private URI selectLocation( URI[] aPossibleLocations ) {
    Vector<URI> locations_v = new Vector<URI>();
    for ( int location = 0; location < aPossibleLocations.length ; location++ ) {
      locations_v.addElement( aPossibleLocations[ location ] );
    }

    ILcdFormatter url_formatter = new ILcdFormatter() {
      public String format( Object aObject ) {
        URI url = (URI) aObject;
        return url.toString();
      }
    };

    Object result = TLcdUserDialog.choose(
            locations_v,
            url_formatter,
            "Select a source for the metadata",
            null,
            (Component) fGXYView
    );
    return (URI) result;
  }

}
