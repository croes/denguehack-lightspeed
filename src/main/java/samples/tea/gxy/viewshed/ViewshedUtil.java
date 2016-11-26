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
package samples.tea.gxy.viewshed;

import java.util.HashMap;

import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelReference;
import com.luciad.reference.ILcdGeoReference;
import com.luciad.reference.TLcdTopocentricReference;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.shape3D.ILcd3DEditablePoint;
import com.luciad.transformation.TLcdGeoReference2GeoReference;
import com.luciad.util.TLcdOutOfBoundsException;
import com.luciad.util.height.ALcdRasterModelHeightProviderFactory;
import com.luciad.util.height.ILcdHeightProvider;
import com.luciad.util.height.ILcdModelHeightProviderFactory;
import com.luciad.util.height.TLcdImageModelHeightProviderFactory;

/**
 * Utility class used for convenience in transformations..
 */
public class ViewshedUtil {

  public static ILcd3DEditablePoint transform(ILcdPoint aPoint, ILcdGeoReference aSourceReference, ILcdGeoReference aDestReference, ILcd3DEditablePoint aPointSFCT) {
    TLcdGeoReference2GeoReference geo2geo = new TLcdGeoReference2GeoReference();
    if(aSourceReference.equals(aDestReference)) {
      aPointSFCT.move3D( aPoint );
      return aPointSFCT;
    }

    geo2geo.setSourceReference( aSourceReference );
    geo2geo.setDestinationReference( aDestReference );
    try {
      geo2geo.sourcePoint2destinationSFCT( aPoint, aPointSFCT );
    } catch ( TLcdOutOfBoundsException e ) {
      //
    }
    return aPointSFCT;
  }


  public static TLcdGeoReference2GeoReference createTransformation( ILcdModelReference aSourceReference, ILcdGeoReference aTargetReference ) {
    TLcdGeoReference2GeoReference buildingTransformation = new TLcdGeoReference2GeoReference();
    buildingTransformation.setSourceReference( aSourceReference );
    buildingTransformation.setDestinationReference( aTargetReference );
    return buildingTransformation;
  }

  public static ILcdGeoReference createViewshedReference(ILcdPoint aCenterPoint, ILcdGeoReference aCenterPointReference){
    //We create a topocentric reference around the eye for increased local accuracy in viewshed calculations
    // (As opposed to a geocentric which has its origin in the center of the earth).
    return new TLcdTopocentricReference( aCenterPointReference.getGeodeticDatum(),aCenterPoint );
  }

  public static ILcdHeightProvider createHeightProvider( ILcdModelReference aQueryingReference, ILcdModel aTerrainModel ) {
    ILcdModelHeightProviderFactory heightProviderFactory = new TLcdImageModelHeightProviderFactory();
    HashMap<String, Object> requiredPropertiesSFCT = new HashMap<String, Object>();
    requiredPropertiesSFCT.put( ALcdRasterModelHeightProviderFactory.KEY_GEO_REFERENCE, aQueryingReference );
    HashMap<String, Object> optionalProperties = new HashMap<String, Object>();
    optionalProperties.put( ALcdRasterModelHeightProviderFactory.KEY_INTERPOLATE_DATA, false );
    return heightProviderFactory.createHeightProvider( aTerrainModel, requiredPropertiesSFCT, optionalProperties );
  }
}
