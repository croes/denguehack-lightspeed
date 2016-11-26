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
package samples.decoder.arinc.gxy;

import com.luciad.format.arinc.TLcdARINCDefaultLayerFactory;
import com.luciad.format.arinc.TLcdARINCLayerConfiguration;
import com.luciad.model.ILcdModel;
import com.luciad.util.service.LcdService;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.ILcdGXYLayerFactory;

import java.util.Properties;

/**
 * This layerFactory is developed to create layers for ARINC model data
 * in the <code>ARINC</code> sample.
 * <p/>
 * <p/>
 * To create <code>ARINC</code> layers, an instance of the <code>TLcdARINCDefaultLayerFactory</code>
 * class is used.
 */
@LcdService(service = ILcdGXYLayerFactory.class)
public class ARINCLayerFactory implements ILcdGXYLayerFactory {

  private TLcdARINCDefaultLayerFactory fARINCLayerFactory = new TLcdARINCDefaultLayerFactory( getProperties() );

  public ARINCLayerFactory() {
    fARINCLayerFactory.setCreateLayerTreeNodes( true );
  }

  //method of ILcdGXYLayerFactory
  public ILcdGXYLayer createGXYLayer( ILcdModel aModel ) {
    if ( aModel != null ) {
      return fARINCLayerFactory.createGXYLayer( aModel );
    }
    return null;
  }

  /**
   * Returns a set of properties to configure the ARINC layerFactory.
   * <p/>
   * The default properties used by the ARINC layerFactory
   * are listed in <code>TLcdARINCLayerConfiguration</code>.
   * If available, a custom set of properties has precedence
   * above the default properties.
   *
   * @return a set of properties to configure the ARINC layerFactory.
   */
  private Properties getProperties() {
    Properties prop = new Properties();
    // Aerodrome.
    prop.setProperty( TLcdARINCLayerConfiguration.CFG_AIRPORT_ICONSTYLE_COLOR, "(255, 255, 255 )" );
    prop.setProperty( TLcdARINCLayerConfiguration.CFG_AIRPORT_LABELSTYLE_FONTSTYLE_COLOR, "(255, 255, 255 )" );
    prop.setProperty( TLcdARINCLayerConfiguration.CFG_AIRPORT_LABELSTYLE_WITHANCHOR, "true" );
    prop.setProperty( TLcdARINCLayerConfiguration.CFG_AIRPORT_LABELSTYLE_WITHPIN, "true" );
    prop.setProperty( TLcdARINCLayerConfiguration.CFG_AIRPORT_LABELSTYLE_SHIFTLABELPOSITION, "10" );

    // Navaid.
    prop.setProperty( TLcdARINCLayerConfiguration.CFG_TACAN_ICONSTYLE_COLOR, "(255, 255, 255 )" );
    prop.setProperty( TLcdARINCLayerConfiguration.CFG_TACAN_LABELSTYLE_FONTSTYLE_COLOR, "(255, 255, 255 )" );
    prop.setProperty( TLcdARINCLayerConfiguration.CFG_TACAN_LABELSTYLE_WITHANCHOR, "true" );
    prop.setProperty( TLcdARINCLayerConfiguration.CFG_TACAN_LABELSTYLE_WITHPIN, "true" );
    prop.setProperty( TLcdARINCLayerConfiguration.CFG_NDB_ICONSTYLE_COLOR, "(255, 255, 255 )" );
    prop.setProperty( TLcdARINCLayerConfiguration.CFG_NDB_LABELSTYLE_FONTSTYLE_COLOR, "(255, 255, 255 )" );
    prop.setProperty( TLcdARINCLayerConfiguration.CFG_NDB_LABELSTYLE_WITHANCHOR, "true" );
    prop.setProperty( TLcdARINCLayerConfiguration.CFG_NDB_LABELSTYLE_WITHPIN, "true" );
    prop.setProperty( TLcdARINCLayerConfiguration.CFG_VOR_ICONSTYLE_COLOR, "(255, 255, 255 )" );
    prop.setProperty( TLcdARINCLayerConfiguration.CFG_VOR_LABELSTYLE_FONTSTYLE_COLOR, "(255, 255, 255 )" );
    prop.setProperty( TLcdARINCLayerConfiguration.CFG_VOR_LABELSTYLE_WITHANCHOR, "true" );
    prop.setProperty( TLcdARINCLayerConfiguration.CFG_VOR_LABELSTYLE_WITHPIN, "true" );
    prop.setProperty( TLcdARINCLayerConfiguration.CFG_DME_ICONSTYLE_COLOR, "(255, 255, 255 )" );
    prop.setProperty( TLcdARINCLayerConfiguration.CFG_DME_LABELSTYLE_FONTSTYLE_COLOR, "(255, 255, 255 )" );
    prop.setProperty( TLcdARINCLayerConfiguration.CFG_DME_LABELSTYLE_WITHANCHOR, "true" );
    prop.setProperty( TLcdARINCLayerConfiguration.CFG_DME_LABELSTYLE_WITHPIN, "true" );
    prop.setProperty( TLcdARINCLayerConfiguration.CFG_ILS_ICONSTYLE_COLOR, "(255, 255, 255 )" );
    prop.setProperty( TLcdARINCLayerConfiguration.CFG_ILS_LABELSTYLE_FONTSTYLE_COLOR, "(255, 255, 255 )" );
    prop.setProperty( TLcdARINCLayerConfiguration.CFG_ILS_LABELSTYLE_WITHANCHOR, "true" );
    prop.setProperty( TLcdARINCLayerConfiguration.CFG_ILS_LABELSTYLE_WITHPIN, "true" );

    // Waypoint.
    prop.setProperty( TLcdARINCLayerConfiguration.CFG_WAYPOINT_ICONSTYLE_COLOR, "(252, 191, 0 )" );
    prop.setProperty( TLcdARINCLayerConfiguration.CFG_WAYPOINT_LABELSTYLE_FONTSTYLE_COLOR, "(252, 191, 0 )" );

    // Airspace.
    prop.setProperty( TLcdARINCLayerConfiguration.CFG_AIRSPACE_LINESTYLE_COLOR, "( 88,115,165 )" );
    prop.setProperty( TLcdARINCLayerConfiguration.CFG_AIRSPACE_LABELSTYLE_FONTSTYLE_COLOR, "( 88,115,165 )" );
    prop.setProperty( TLcdARINCLayerConfiguration.CFG_AIRSPACE_LABELSTYLE_FILLCOLOR, "( 255, 255, 255, 125 )" );
    prop.setProperty( TLcdARINCLayerConfiguration.CFG_AIRSPACE_LINESTYLE_LINEWIDTH, "2" );
    prop.setProperty( TLcdARINCLayerConfiguration.CFG_AIRSPACE_LINESTYLE_SELECTIONLINEWIDTH, "2" );
    prop.setProperty( TLcdARINCLayerConfiguration.CFG_AIRSPACE_LINESTYLE_CLASSA_COLOR, "(176, 58, 80)" );
    prop.setProperty( TLcdARINCLayerConfiguration.CFG_AIRSPACE_LINESTYLE_CLASSA_SELECTIONCOLOR, "(255,0,0)" );
    prop.setProperty( TLcdARINCLayerConfiguration.CFG_AIRSPACE_LINESTYLE_CLASSA_BANDWIDTH, "400" );
    prop.setProperty( TLcdARINCLayerConfiguration.CFG_AIRSPACE_LINESTYLE_CLASSB_COLOR, "(88, 115, 165)" );
    prop.setProperty( TLcdARINCLayerConfiguration.CFG_AIRSPACE_LINESTYLE_CLASSB_SELECTIONCOLOR, "(255,0,0)" );
    prop.setProperty( TLcdARINCLayerConfiguration.CFG_AIRSPACE_LINESTYLE_CLASSB_BANDWIDTH, "400" );
    prop.setProperty( TLcdARINCLayerConfiguration.CFG_AIRSPACE_LINESTYLE_CLASSC_COLOR, "(88, 115, 165)" );
    prop.setProperty( TLcdARINCLayerConfiguration.CFG_AIRSPACE_LINESTYLE_CLASSC_SELECTIONCOLOR, "(255,0,0)" );
    prop.setProperty( TLcdARINCLayerConfiguration.CFG_AIRSPACE_LINESTYLE_CLASSC_BANDWIDTH, "400" );
    prop.setProperty( TLcdARINCLayerConfiguration.CFG_AIRSPACE_LINESTYLE_CLASSD_COLOR, "(88, 115, 165)" );
    prop.setProperty( TLcdARINCLayerConfiguration.CFG_AIRSPACE_LINESTYLE_CLASSD_SELECTIONCOLOR, "(255,0,0)" );
    prop.setProperty( TLcdARINCLayerConfiguration.CFG_AIRSPACE_LINESTYLE_CLASSD_BANDWIDTH, "400" );
    prop.setProperty( TLcdARINCLayerConfiguration.CFG_AIRSPACE_LINESTYLE_CLASSE_COLOR, "(88, 115, 165)" );
    prop.setProperty( TLcdARINCLayerConfiguration.CFG_AIRSPACE_LINESTYLE_CLASSE_SELECTIONCOLOR, "(255,0,0)" );
    prop.setProperty( TLcdARINCLayerConfiguration.CFG_AIRSPACE_LINESTYLE_CLASSE_BANDWIDTH, "800" );
    prop.setProperty( TLcdARINCLayerConfiguration.CFG_AIRSPACE_LINESTYLE_CLASSF_COLOR, "(88, 115, 165)" );
    prop.setProperty( TLcdARINCLayerConfiguration.CFG_AIRSPACE_LINESTYLE_CLASSF_SELECTIONCOLOR, "(255,0,0)" );
    prop.setProperty( TLcdARINCLayerConfiguration.CFG_AIRSPACE_LINESTYLE_CLASSF_BANDWIDTH, "0" );
    prop.setProperty( TLcdARINCLayerConfiguration.CFG_AIRSPACE_LINESTYLE_CLASSG_COLOR, "(88, 115, 165)" );
    prop.setProperty( TLcdARINCLayerConfiguration.CFG_AIRSPACE_LINESTYLE_CLASSG_SELECTIONCOLOR, "(255,0,0)" );
    prop.setProperty( TLcdARINCLayerConfiguration.CFG_AIRSPACE_LINESTYLE_CLASSG_BANDWIDTH, "200" );
    //ATS route
    prop.setProperty( TLcdARINCLayerConfiguration.CFG_ATS_ROUTE_ICONSTYLE_COLOR, "( 0, 255 , 0 )" );
    prop.setProperty( TLcdARINCLayerConfiguration.CFG_ATS_ROUTE_LABELSTYLE_FONTSTYLE_COLOR, "( 13, 117 , 54 )" );
    prop.setProperty( TLcdARINCLayerConfiguration.CFG_ATS_ROUTE_LINESTYLE_COLOR, "( 0, 255 , 0 )" );
    prop.setProperty( TLcdARINCLayerConfiguration.CFG_ATS_ROUTE_LINESTYLE_LINEWIDTH, "1" );
    prop.setProperty( TLcdARINCLayerConfiguration.CFG_ATS_ROUTE_LINESTYLE_SELECTIONLINEWIDTH, "1" );
    // MORA
    prop.setProperty( TLcdARINCLayerConfiguration.CFG_MORA_LINESTYLE_COLOR, "(128, 128, 128, 64)");
    prop.setProperty( TLcdARINCLayerConfiguration.CFG_MORA_LINESTYLE_LINEWIDTH, "1");
    prop.setProperty( TLcdARINCLayerConfiguration.CFG_MORA_LINESTYLE_SELECTIONCOLOR, "(255, 0, 0)");
    prop.setProperty( TLcdARINCLayerConfiguration.CFG_MORA_LINESTYLE_SELECTIONLINEWIDTH, "1");
    prop.setProperty( TLcdARINCLayerConfiguration.CFG_MORA_LOW_ALTITUDE_LABELSTYLE_FONTSTYLE_COLOR, "(0, 204, 0, 192)");
    prop.setProperty( TLcdARINCLayerConfiguration.CFG_MORA_HIGH_ALTITUDE_LABELSTYLE_FONTSTYLE_COLOR, "(186, 85, 211, 192)");
    prop.setProperty( TLcdARINCLayerConfiguration.CFG_MORA_HIGH_ALTITUDE_THRESHOLD, "14000");
    prop.setProperty( TLcdARINCLayerConfiguration.CFG_MORA_LABELSTYLE_FONTSTYLE_SIZE, "32");
    prop.setProperty( TLcdARINCLayerConfiguration.CFG_MORA_LABELSTYLE_FONTSTYLE_NAME, "arial");
    prop.setProperty( TLcdARINCLayerConfiguration.CFG_MORA_LABELSTYLE_FONTSTYLE_STYLE, "ITALIC,BOLD");

    return prop;
  }
}
