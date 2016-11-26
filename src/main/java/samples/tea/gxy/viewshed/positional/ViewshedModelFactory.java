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
package samples.tea.gxy.viewshed.positional;

import com.luciad.format.raster.ILcdRaster;
import com.luciad.format.raster.TLcdDEMModelDecoder;
import com.luciad.format.shp.TLcdSHPModelDecoder;
import com.luciad.geodesy.TLcdGeodeticDatum;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelReference;
import com.luciad.model.TLcd2DBoundsIndexedModel;
import com.luciad.model.TLcdModelDescriptor;
import com.luciad.model.TLcdVectorModel;
import com.luciad.reference.ILcdGeoReference;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.shape.ILcdPoint;
import com.luciad.util.ILcdFireEventMode;
import com.luciad.util.TLcdSharedBuffer;
import com.luciad.util.logging.ILcdLogger;
import com.luciad.util.logging.TLcdLoggerFactory;

import java.io.IOException;

/**
 * The main model factory used by the positional viewshed sample.
 */
class ViewshedModelFactory {
  private final static ILcdLogger sLogger = TLcdLoggerFactory.getLogger( ViewshedModelFactory.class.getName() );
  private static final String ITHACA_BUILDINGS = "Data/Ithaca/SHP/buildings.shp";
  private static final String ITHACA_TERRAIN_EAST = "Data/Ithaca/DEM/IthacaEast.dem";
  private static final String ITHACA_TERRAIN_WEST = "Data/Ithaca/DEM/IthacaWest.dem";

  public ILcdModel createPointModel( ILcdPoint aPoint, ILcdGeoReference aReference ) {
    // Create a geodetic model.
    TLcdVectorModel model = new TLcdVectorModel();
    model.setModelReference( ( ILcdModelReference ) aReference );
    model.setModelDescriptor( new TLcdModelDescriptor(
        "Layer containing the location of the sun as a point",
        "Eye Position",
        "Eye Position"
    ) );

    // Add some points to the model.
    model.addElement( aPoint, ILcdFireEventMode.NO_EVENT );
    return model;
  }

  public ILcdModel createTerrainModel() throws IOException {
    TLcdDEMModelDecoder demModelDecoder = new TLcdDEMModelDecoder( TLcdSharedBuffer.getBufferInstance() );
    TLcd2DBoundsIndexedModel eastModel = ( TLcd2DBoundsIndexedModel ) demModelDecoder.decode( ITHACA_TERRAIN_EAST );
    TLcd2DBoundsIndexedModel westModel = ( TLcd2DBoundsIndexedModel ) demModelDecoder.decode( ITHACA_TERRAIN_WEST );
    ILcdRaster eastRaster = ( ILcdRaster ) eastModel.elements().nextElement();
    ILcdRaster westRaster = ( ILcdRaster ) westModel.elements().nextElement();

    TLcd2DBoundsIndexedModel compositeModel = new TLcd2DBoundsIndexedModel();
    compositeModel.setModelReference( eastModel.getModelReference() );
    compositeModel.setModelDescriptor( eastModel.getModelDescriptor() );
    compositeModel.addElement( eastRaster, ILcdModel.NO_EVENT );
    compositeModel.addElement( westRaster, ILcdModel.NO_EVENT );

    return compositeModel;
  }

  public ILcdModel createViewshedModel() {
    // Create a geodetic model.
    TLcdVectorModel model = new TLcdVectorModel();
    model.setModelReference( new TLcdGeodeticReference( new TLcdGeodeticDatum() ) );
    model.setModelDescriptor( new TLcdModelDescriptor(
        "Layer containing the created geodetic line-of-sight",    // source name (is used as tooltip text)
        "Viewshed",            // type name
        "Viewshed model"    // display name
    ) );
    return model;
  }

  public ILcdModel createBuildingsModel(){
    try {
      TLcdSHPModelDecoder shpModelDecoder = new TLcdSHPModelDecoder(  );
      return shpModelDecoder.decode( ITHACA_BUILDINGS );
    } catch ( IOException e ) {
      e.printStackTrace();
    }
    return null;
  }

}
