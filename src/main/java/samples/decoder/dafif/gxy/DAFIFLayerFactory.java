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
package samples.decoder.dafif.gxy;

import java.util.Properties;

import com.luciad.format.dafif.TLcdDAFIFDefaultLayerFactory;
import com.luciad.format.dafif.TLcdDAFIFLayerConfiguration;
import com.luciad.model.ILcdModel;
import com.luciad.model.TLcdModelTreeNode;
import com.luciad.util.service.LcdService;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.ILcdGXYLayerFactory;
import com.luciad.view.gxy.TLcdGXYLayerTreeNode;

/**
 * This layerFactory is developed to create layers for DAFIF model data
 * in the <code>DAFIF</code> sample.
 * <p/>
 * <p/>
 * To create <code>DAFIF</code> layers, an instance of the <code>TLcdDAFIFDefaultLayerFactory</code>
 * class is used.
 */

@LcdService(service = ILcdGXYLayerFactory.class)
public class DAFIFLayerFactory implements ILcdGXYLayerFactory {

  private TLcdDAFIFDefaultLayerFactory fDAFIFLayerFactory = new TLcdDAFIFDefaultLayerFactory( getProperties() );

  public DAFIFLayerFactory() {
    fDAFIFLayerFactory.setCreateLayerTreeNodes( true );
  }

  //method of ILcdGXYLayerFactory
  public ILcdGXYLayer createGXYLayer( ILcdModel aModel ) {
    if (aModel != null && aModel.getModelDescriptor().getTypeName().equals("DAFIF")) {
      // If the model is a model tree node, it can encompass multiple models;
      // in that case, we create a corresponding layer tree node, loop over all
      // models, create layers for them and add them to the layer tree node.
      if(aModel instanceof TLcdModelTreeNode ) {
        TLcdGXYLayerTreeNode result = new TLcdGXYLayerTreeNode();
        TLcdModelTreeNode treeNode = (TLcdModelTreeNode) aModel;
        int l = treeNode.modelCount();
        for(int it = 0; it < l; it++) {
          ILcdGXYLayer layer = createGXYLayer( treeNode.getModel( it ) );
          result.addLayer( layer );
        }
        result.setLabel( aModel.getModelDescriptor().getDisplayName()  );
        return result;
      }
      ILcdGXYLayer layer = fDAFIFLayerFactory.createGXYLayer( aModel );

      // We now have a layer, which is sufficient for visualization.
      // In this sample, we will also determine an appropriate layer label,
      // which is a display name to be shown in a layer control panel.
      String source = aModel.getModelDescriptor().getSourceName();

      String search_string = "Data/Dafif/";
      String dafif_suffix  = " (v7)";
      int start_search_string = source.indexOf( search_string );
      if ( start_search_string == -1 ) {
        search_string = "Data/Dafift/";
        dafif_suffix  = " (v8)";
        start_search_string = source.indexOf( search_string );
      }
      if ( start_search_string != -1 ) {
        int    start_icao_code = start_search_string + search_string.length();
        String icao_code       = source.substring( start_icao_code, start_icao_code + 2 );
        layer.setLabel( icao_code + " - " + layer.getLabel() + dafif_suffix );
      }
      return layer;
    }
    return null;
  }

  /**
   * Returns a set of properties to configure the DAFIF layerFactory.
   * <p/>
   * The default properties used by the DAFIF layerFactory
   * are listed in <code>TLcdDAFIFLayerConfiguration</code>.
   * If available, a custom set of properties has precedence
   * above the default properties.
   *
   * @return a set of properties to configure the DAFIF layerFactory.
   */
  private Properties getProperties() {
    Properties prop = new Properties();

    // Aerodrome.
    prop.setProperty( TLcdDAFIFLayerConfiguration.CFG_AIRPORT_ICONSTYLE_COLOR, "(255, 255, 255 )" );
    prop.setProperty( TLcdDAFIFLayerConfiguration.CFG_AIRPORT_LABELSTYLE_FONTSTYLE_COLOR, "(255, 255, 255 )" );
    prop.setProperty( TLcdDAFIFLayerConfiguration.CFG_AIRPORT_LABELSTYLE_WITHANCHOR, "true" );
    prop.setProperty( TLcdDAFIFLayerConfiguration.CFG_AIRPORT_LABELSTYLE_WITHPIN, "true" );
    prop.setProperty( TLcdDAFIFLayerConfiguration.CFG_AIRPORT_LABELSTYLE_SHIFTLABELPOSITION, "10" );

    // Navaid.
    prop.setProperty( TLcdDAFIFLayerConfiguration.CFG_TACAN_ICONSTYLE_COLOR, "(255, 255, 255 )" );
    prop.setProperty( TLcdDAFIFLayerConfiguration.CFG_TACAN_LABELSTYLE_FONTSTYLE_COLOR, "(255, 255, 255 )" );
    prop.setProperty( TLcdDAFIFLayerConfiguration.CFG_TACAN_LABELSTYLE_WITHANCHOR, "true" );
    prop.setProperty( TLcdDAFIFLayerConfiguration.CFG_TACAN_LABELSTYLE_WITHPIN, "true" );
    prop.setProperty( TLcdDAFIFLayerConfiguration.CFG_NDB_ICONSTYLE_COLOR, "(255, 255, 255 )" );
    prop.setProperty( TLcdDAFIFLayerConfiguration.CFG_NDB_LABELSTYLE_FONTSTYLE_COLOR, "(255, 255, 255 )" );
    prop.setProperty( TLcdDAFIFLayerConfiguration.CFG_NDB_LABELSTYLE_WITHANCHOR, "true" );
    prop.setProperty( TLcdDAFIFLayerConfiguration.CFG_NDB_LABELSTYLE_WITHPIN, "true" );
    prop.setProperty( TLcdDAFIFLayerConfiguration.CFG_VOR_ICONSTYLE_COLOR, "(255, 255, 255 )" );
    prop.setProperty( TLcdDAFIFLayerConfiguration.CFG_VOR_LABELSTYLE_FONTSTYLE_COLOR, "(255, 255, 255 )" );
    prop.setProperty( TLcdDAFIFLayerConfiguration.CFG_VOR_LABELSTYLE_WITHANCHOR, "true" );
    prop.setProperty( TLcdDAFIFLayerConfiguration.CFG_VOR_LABELSTYLE_WITHPIN, "true" );
    prop.setProperty( TLcdDAFIFLayerConfiguration.CFG_DME_ICONSTYLE_COLOR, "(255, 255, 255 )" );
    prop.setProperty( TLcdDAFIFLayerConfiguration.CFG_DME_LABELSTYLE_FONTSTYLE_COLOR, "(255, 255, 255 )" );
    prop.setProperty( TLcdDAFIFLayerConfiguration.CFG_DME_LABELSTYLE_WITHANCHOR, "true" );
    prop.setProperty( TLcdDAFIFLayerConfiguration.CFG_DME_LABELSTYLE_WITHPIN, "true" );
    prop.setProperty( TLcdDAFIFLayerConfiguration.CFG_ILS_ICONSTYLE_COLOR, "(255, 255, 255 )" );
    prop.setProperty( TLcdDAFIFLayerConfiguration.CFG_ILS_LABELSTYLE_FONTSTYLE_COLOR, "(255, 255, 255 )" );
    prop.setProperty( TLcdDAFIFLayerConfiguration.CFG_ILS_LABELSTYLE_WITHANCHOR, "true" );
    prop.setProperty( TLcdDAFIFLayerConfiguration.CFG_ILS_LABELSTYLE_WITHPIN, "true" );

    // Waypoint.
    prop.setProperty( TLcdDAFIFLayerConfiguration.CFG_WAYPOINT_ICONSTYLE_COLOR, "(252, 191, 0 )" );
    prop.setProperty( TLcdDAFIFLayerConfiguration.CFG_WAYPOINT_LABELSTYLE_FONTSTYLE_COLOR, "(252, 191, 0 )" );

    // Airspace.
    prop.setProperty( TLcdDAFIFLayerConfiguration.CFG_AIRSPACE_LINESTYLE_COLOR, "( 88,115,165 )" );
    prop.setProperty( TLcdDAFIFLayerConfiguration.CFG_AIRSPACE_LABELSTYLE_FONTSTYLE_COLOR, "( 88,115,165 )" );
    prop.setProperty( TLcdDAFIFLayerConfiguration.CFG_AIRSPACE_LABELSTYLE_FILLCOLOR, "( 255, 255, 255, 125 )" );
    prop.setProperty( TLcdDAFIFLayerConfiguration.CFG_AIRSPACE_LINESTYLE_LINEWIDTH, "2" );
    prop.setProperty( TLcdDAFIFLayerConfiguration.CFG_AIRSPACE_LINESTYLE_SELECTIONLINEWIDTH, "2" );
    prop.setProperty( TLcdDAFIFLayerConfiguration.CFG_AIRSPACE_LINESTYLE_CLASSA_COLOR, "(176, 58, 80)" );
    prop.setProperty( TLcdDAFIFLayerConfiguration.CFG_AIRSPACE_LINESTYLE_CLASSA_SELECTIONCOLOR, "(255,0,0)" );
    prop.setProperty( TLcdDAFIFLayerConfiguration.CFG_AIRSPACE_LINESTYLE_CLASSA_BANDWIDTH, "400" );
    prop.setProperty( TLcdDAFIFLayerConfiguration.CFG_AIRSPACE_LINESTYLE_CLASSB_COLOR, "(88, 115, 165)" );
    prop.setProperty( TLcdDAFIFLayerConfiguration.CFG_AIRSPACE_LINESTYLE_CLASSB_SELECTIONCOLOR, "(255,0,0)" );
    prop.setProperty( TLcdDAFIFLayerConfiguration.CFG_AIRSPACE_LINESTYLE_CLASSB_BANDWIDTH, "400" );
    prop.setProperty( TLcdDAFIFLayerConfiguration.CFG_AIRSPACE_LINESTYLE_CLASSC_COLOR, "(88, 115, 165)" );
    prop.setProperty( TLcdDAFIFLayerConfiguration.CFG_AIRSPACE_LINESTYLE_CLASSC_SELECTIONCOLOR, "(255,0,0)" );
    prop.setProperty( TLcdDAFIFLayerConfiguration.CFG_AIRSPACE_LINESTYLE_CLASSC_BANDWIDTH, "400" );
    prop.setProperty( TLcdDAFIFLayerConfiguration.CFG_AIRSPACE_LINESTYLE_CLASSD_COLOR, "(88, 115, 165)" );
    prop.setProperty( TLcdDAFIFLayerConfiguration.CFG_AIRSPACE_LINESTYLE_CLASSD_SELECTIONCOLOR, "(255,0,0)" );
    prop.setProperty( TLcdDAFIFLayerConfiguration.CFG_AIRSPACE_LINESTYLE_CLASSD_BANDWIDTH, "400" );
    prop.setProperty( TLcdDAFIFLayerConfiguration.CFG_AIRSPACE_LINESTYLE_CLASSE_COLOR, "(88, 115, 165)" );
    prop.setProperty( TLcdDAFIFLayerConfiguration.CFG_AIRSPACE_LINESTYLE_CLASSE_SELECTIONCOLOR, "(255,0,0)" );
    prop.setProperty( TLcdDAFIFLayerConfiguration.CFG_AIRSPACE_LINESTYLE_CLASSE_BANDWIDTH, "800" );
    prop.setProperty( TLcdDAFIFLayerConfiguration.CFG_AIRSPACE_LINESTYLE_CLASSF_COLOR, "(88, 115, 165)" );
    prop.setProperty( TLcdDAFIFLayerConfiguration.CFG_AIRSPACE_LINESTYLE_CLASSF_SELECTIONCOLOR, "(255,0,0)" );
    prop.setProperty( TLcdDAFIFLayerConfiguration.CFG_AIRSPACE_LINESTYLE_CLASSF_BANDWIDTH, "0" );
    prop.setProperty( TLcdDAFIFLayerConfiguration.CFG_AIRSPACE_LINESTYLE_CLASSG_COLOR, "(88, 115, 165)" );
    prop.setProperty( TLcdDAFIFLayerConfiguration.CFG_AIRSPACE_LINESTYLE_CLASSG_SELECTIONCOLOR, "(255,0,0)" );
    prop.setProperty( TLcdDAFIFLayerConfiguration.CFG_AIRSPACE_LINESTYLE_CLASSG_BANDWIDTH, "200" );
    //SUAS
    prop.setProperty( TLcdDAFIFLayerConfiguration.CFG_SPECIAL_USE_AIRSPACE_LINESTYLE_LINEWIDTH, "2" );
    prop.setProperty( TLcdDAFIFLayerConfiguration.CFG_SPECIAL_USE_AIRSPACE_LINESTYLE_SELECTIONLINEWIDTH, "2" );
    //ATS route
    prop.setProperty( TLcdDAFIFLayerConfiguration.CFG_ATS_ROUTE_ICONSTYLE_COLOR, "( 0, 255 , 0 )" );
    prop.setProperty( TLcdDAFIFLayerConfiguration.CFG_ATS_ROUTE_LABELSTYLE_FONTSTYLE_COLOR, "( 13, 117 , 54 )" );
    prop.setProperty( TLcdDAFIFLayerConfiguration.CFG_ATS_ROUTE_LINESTYLE_COLOR, "( 0, 255 , 0 )" );
    prop.setProperty( TLcdDAFIFLayerConfiguration.CFG_ATS_ROUTE_LINESTYLE_LINEWIDTH, "1" );
    prop.setProperty( TLcdDAFIFLayerConfiguration.CFG_ATS_ROUTE_LINESTYLE_SELECTIONLINEWIDTH, "1" );

    return prop;
  }
}
