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
package samples.tea.gxy.los;

import com.luciad.geodesy.ILcdGeodeticDatum;
import com.luciad.geodesy.TLcdGeoidGeodeticDatumFactory;
import com.luciad.model.ILcdModel;
import com.luciad.model.TLcdModelDescriptor;
import com.luciad.model.TLcdVectorModel;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.reference.TLcdLambertFrenchGridReference;
import com.luciad.shape.shape3D.TLcdLonLatHeightPoint;
import com.luciad.shape.shape3D.TLcdXYZPoint;
import com.luciad.util.ILcdFireEventMode;

/**
 * A model factory creating all models needed by the LOS sample.
 */
class ModelFactory {

  private static TLcdGeoidGeodeticDatumFactory sGeoidGeodeticDatumFactory = new TLcdGeoidGeodeticDatumFactory();

  public ILcdModel createModel( String aType ) {
    if ( "GeodeticPoint".equals( aType ) ) { return createGeodeticPointModel(); }
    if ( "GridPoint"    .equals( aType ) ) { return createGridPointModel    (); }

    if ( "GeodeticLOS".equals( aType ) ) { return createGeodeticLOSModel(); }
    if ( "GridLOS"    .equals( aType ) ) { return createGridLOSModel    (); }

    if ( "GeodeticP2P".equals( aType ) ) { return createGeodeticP2PModel(); }
    if ( "GridP2P"    .equals( aType ) ) { return createGridP2PModel    (); }

    throw new UnsupportedOperationException( "Type not supported" );
  }

  private ILcdModel createGeodeticPointModel() {
    // Create a geodetic model.
    TLcdVectorModel model = new TLcdVectorModel();
    model.setModelReference( new TLcdGeodeticReference( getGeoidGeodeticDatum() ) );
    model.setModelDescriptor( new TLcdModelDescriptor(
            "Layer containing some geodetic points",                  // source name (is used as tooltip text)
            "Geodetic points",// type name
            "Geodetic points" // display name
    ) );

    // Add some points to the model.
    model.addElement( new TLcdLonLatHeightPoint( 10, 45, 0 ), ILcdFireEventMode.NO_EVENT );
    model.addElement( new TLcdLonLatHeightPoint( 11, 46, 0 ), ILcdFireEventMode.NO_EVENT );
    model.addElement( new TLcdLonLatHeightPoint( 11, 45, 0 ), ILcdFireEventMode.NO_EVENT );
    return model;
  }

  public static ILcdGeodeticDatum getGeoidGeodeticDatum() {
    return sGeoidGeodeticDatumFactory.createGeodeticDatum( TLcdGeoidGeodeticDatumFactory.EGM84_MODEL );
  }

  private ILcdModel createGridPointModel() {
    // Create a grid model.
    TLcdVectorModel model = new TLcdVectorModel();
    model.setModelReference( new TLcdLambertFrenchGridReference( 3 ) );
    model.setModelDescriptor( new TLcdModelDescriptor(
            "Layer containing some grid points",                      // source name (is used as tooltip text)
            "Grid points",    // type name
            "Grid points"     // display name
    ) );

    model.addElement( new TLcdXYZPoint( 1200000, 3500000, 0 ), ILcdFireEventMode.NO_EVENT );
    model.addElement( new TLcdXYZPoint( 1250000, 3550000, 0 ), ILcdFireEventMode.NO_EVENT );
    model.addElement( new TLcdXYZPoint( 1200000, 3550000, 0 ), ILcdFireEventMode.NO_EVENT );
    return model;
  }

  private ILcdModel createGeodeticLOSModel() {
    // Create a geodetic model.
    TLcdVectorModel model = new TLcdVectorModel();
    model.setModelReference( new TLcdGeodeticReference( getGeoidGeodeticDatum() ) );
    model.setModelDescriptor( new TLcdModelDescriptor(
            "Layer containing the created geodetic line-of-sight",    // source name (is used as tooltip text)
            "LOS",            // type name
            "Geodetic LOS"    // display name
    ) );
    return model;
  }

  private ILcdModel createGridLOSModel() {
    // Create a grid model.
    TLcdVectorModel model = new TLcdVectorModel();
    model.setModelReference( new TLcdLambertFrenchGridReference() );
    model.setModelDescriptor( new TLcdModelDescriptor(
            "Layer containing the created grid line-of-sight",        // source name (is used as tooltip text)
            "LOS",            // type name
            "Grid LOS"        // display name
    ) );
    return model;
  }

  private ILcdModel createGeodeticP2PModel() {
    // Create a geodetic model.
    TLcdVectorModel model = new TLcdVectorModel();
    model.setModelReference( new TLcdGeodeticReference( getGeoidGeodeticDatum() ) ) ;
    model.setModelDescriptor( new TLcdModelDescriptor(
            "Layer containing the created geodetic point-to-point",   // source name (is used as tooltip text)
            "P2P",            // type name
            "Geodetic P2P"    // display name
    ) );
    return model;
  }

  private ILcdModel createGridP2PModel() {
    // Create a grid model.
    TLcdVectorModel model = new TLcdVectorModel();
    model.setModelReference( new TLcdLambertFrenchGridReference() );
    model.setModelDescriptor( new TLcdModelDescriptor(
            "Layer containing the created grid point-to-point",       // source name (is used as tooltip text)
            "P2P",            // type name
            "Grid P2P"        // display name
    ) );
    return model;
  }

}
