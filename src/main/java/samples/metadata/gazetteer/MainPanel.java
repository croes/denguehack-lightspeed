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

import java.io.IOException;

import com.luciad.format.metadata.model.metadataentityset.TLcdISO19115Metadata;
import com.luciad.format.metadata.xml.TLcdISO19139MetadataDecoder;
import com.luciad.geodesy.TLcdGeodeticDatum;
import com.luciad.model.ILcdModel;
import com.luciad.model.TLcdModelDescriptor;
import com.luciad.model.TLcdVectorModel;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.util.ILcdFireEventMode;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.map.TLcdMapJPanel;

import samples.common.serviceregistry.ServiceRegistry;
import samples.gxy.common.GXYSample;
import samples.gxy.common.layers.GXYLayerUtil;
import samples.gxy.common.toolbar.ToolBar;

/**
 * This sample demonstrates a simple gazetteer functionality based on
 * the metadata package. Metadata files in XML are decoded and the boundaries
 * are extracted from the metadata and displayed on the map.
 * <p/>
 * Double click on the boundaries to display meta data available. Right click to actually
 * load the data. The location of the data is assumed to be in
 * Metadata -> IdentificationInfo -> transferOptions -> onLine -> linkage
 */
public class MainPanel extends GXYSample {

  @Override
  protected void createGUI() {
    super.createGUI();

    TLcdMapJPanel map = getView();
    ToolBar toolBar = getToolBars()[0];

    // Set up the action to load the actual data when clicking the right mouse button.
    LoadDataAction loadDataAction = new LoadDataAction( map, toolBar.getGXYControllerEdit(), "", ServiceRegistry.getInstance() );
    toolBar.getGXYControllerEdit().setRightClickAction( loadDataAction );

    // set up the action to display metadata on the double click
    toolBar.getGXYControllerEdit().setDoubleClickAction(
            new DisplayMetadataAction( map, toolBar.getGXYControllerEdit() )
    );
  }

  @Override
  protected void addData() throws IOException {
    // load metadata into the gazetteer model.
    ILcdModel gazetteer_model = makeGazetteerModel("");
    ILcdGXYLayer gazetteer_layer = new GazetteerLayer( gazetteer_model );
    GXYLayerUtil.addGXYLayer( getView(), gazetteer_layer );
  }

  private ILcdModel makeGazetteerModel( String aCodeBase ) {

    // create the model.
    TLcdVectorModel model = new TLcdVectorModel();
    // the reference should be WGS 84. As according to the standard
    // the geographic bounding box is just an approximate reference,
    // so specifying the coordinate reference system is not necessary.
    // As it is not necessary, we take the most common reference system.
    model.setModelReference( new TLcdGeodeticReference( new TLcdGeodeticDatum() ) );
    model.setModelDescriptor( new TLcdModelDescriptor( "Gazetteer", "Gazetteer", "Gazetteer" ) );

    String[] metadata_files = new String[] {
            "Data/metadata/iso19139/wash_spot_small.xml",
            "Data/metadata/iso19139/world.xml",
            "Data/metadata/iso19139/africa.xml",
            "Data/metadata/iso19139/antarctica.xml",
            "Data/metadata/iso19139/asia.xml",
            "Data/metadata/iso19139/australia.xml",
            "Data/metadata/iso19139/europe.xml",
            "Data/metadata/iso19139/north_america.xml",
            "Data/metadata/iso19139/oceania.xml",
            "Data/metadata/iso19139/south_america.xml",
            "Data/metadata/iso19139/usa.xml",
    };

    // setup the decoder.
    TLcdISO19139MetadataDecoder metadataDecoder = new TLcdISO19139MetadataDecoder();
    for ( String metadata_file : metadata_files ) {
      try {
        TLcdISO19115Metadata metadata = metadataDecoder.decodeMetadata( aCodeBase + metadata_file );
        MetadataBounded metadata_bounded = new MetadataBounded( metadata );
        model.addElement( metadata_bounded, ILcdFireEventMode.NO_EVENT );
      } catch ( IOException e ) {
        System.err.println( "Could not load " + metadata_file + " :exception occurred: " + e.getMessage() );
      }
    }

    return model;
  }

  public static void main( final String[] aArgs ) {
    startSample( MainPanel.class, "Metadata gazetteer" );
  }
}
