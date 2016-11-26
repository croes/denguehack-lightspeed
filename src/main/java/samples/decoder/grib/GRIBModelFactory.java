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
package samples.decoder.grib;

import com.luciad.contour.TLcdLonLatPolylineContourBuilder;
import com.luciad.contour.TLcdPolylineContourFinder;
import com.luciad.contour.TLcdValuedContour;
import com.luciad.format.grib.ILcdGRIBBulletinFilter;
import com.luciad.format.grib.ILcdGRIBBulletinHeaderFilter;
import com.luciad.format.grib.TLcdGRIBBulletinHeaderDescriptor;
import com.luciad.format.grib.TLcdGRIBBulletinModelDecoder;
import com.luciad.format.grib.TLcdGRIBModelDecoder;
import com.luciad.format.grib.TLcdGRIBModelDescriptor;
import com.luciad.format.raster.ILcdRaster;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelReference;
import com.luciad.model.TLcd2DBoundsIndexedModel;
import com.luciad.model.TLcdModelDescriptor;
import com.luciad.shape.ILcdBounds;
import com.luciad.shape.ILcdShape;
import com.luciad.util.ILcdFireEventMode;
import com.luciad.util.ILcdFunction;

import java.io.IOException;

/**
 * Loads example GRIB models.
 */
public class GRIBModelFactory {

  private GRIBModelDecoder fGRIBModelDecoder = new GRIBModelDecoder();
  private String fCodeBase;

  public GRIBModelFactory( String aCodeBase ) {
    fCodeBase = aCodeBase;
    fGRIBModelDecoder.setMultiValuedRasterCreation( TLcdGRIBModelDecoder.MultiValuedRasterCreation.AUTO );
  }

  public ILcdModel decodeTemperature() throws IOException {
    return fGRIBModelDecoder.decode( fCodeBase + "Data/Grib/rlm13_11_0000_1.grb" );
  }

  public ILcdModel decodeGeoPotential() throws IOException {
    return fGRIBModelDecoder.decode( fCodeBase + "Data/Grib/rlm13_6_0000_1.grb" );
  }

  public ILcdModel decodeHumidityBulletin() throws IOException {
    TLcdGRIBModelDecoder decoder = new TLcdGRIBModelDecoder();
    decoder.setBulletinSupported( true );

    // Add a filter based on the bulletin header.
    // The GRIB data that corresponds with the header will only be decoded if this filter returns true.
    // The data will be skipped if this filter returns false.
    // This filter only applies for messages that contain bulletin headers.
    ILcdGRIBBulletinHeaderFilter headerFilter = new ILcdGRIBBulletinHeaderFilter() {
      public boolean accept( TLcdGRIBBulletinHeaderDescriptor aHeaderDescriptor ) {
        return aHeaderDescriptor.getComponent() == TLcdGRIBBulletinHeaderDescriptor.COMPONENT_HUMIDITY_R;
      }
    };
    decoder.setGRIBBulletinHeaderFilter( headerFilter );

    // Add a filter based on the content of the decoded GRIB data.
    // The decoded GRIB data will only be added to the resulting model list if this filter returns true.
    // This filter applies for all GRIB bulletin messages.
    ILcdGRIBBulletinFilter dataFilter = new ILcdGRIBBulletinFilter() {
      public boolean accept( TLcdGRIBModelDescriptor aModelDescriptor,
                             ILcdModelReference aModelReference,
                             ILcdBounds aBounds ) {
        return ( aModelDescriptor.getParameterAbbreviation().equals( "R H" ) &&
                 aModelDescriptor.getLevelValue() <= 700 );
      }
    };
    decoder.setGRIBBulletinFilter( dataFilter );
    ILcdModel bulletin = decoder.decode( fCodeBase + "Data/Grib/rlm13_52_0000_100.grb" );
    return bulletin;
  }

  public ILcdModel decodeIsobars() throws IOException {
    ILcdModel model = fGRIBModelDecoder.decode( fCodeBase + "Data/Grib/rlm13_1_0000_1.grb" );

    TLcdGRIBModelDescriptor descriptor = ( TLcdGRIBModelDescriptor ) model.getModelDescriptor();

    // Extract the raster from the model.
    ILcdRaster pressureRaster = ( ILcdRaster ) model.elements().nextElement();

    // Create a matrix of the raster.
    int tileWidth = pressureRaster.retrieveTile( 0, 0 ).getWidth();
    int tileHeight = pressureRaster.retrieveTile( 0, 0 ).getHeight();

    GRIBRasterMatrix matrix = new GRIBRasterMatrix(
        pressureRaster, pressureRaster.getBounds(), tileWidth, tileHeight
    );

    // Create a new model.
    final TLcd2DBoundsIndexedModel isobarModel = new TLcd2DBoundsIndexedModel();
    isobarModel.setModelDescriptor(
        new TLcdModelDescriptor( descriptor.getSourceName(), "Isobars", "Isobars" )
    );
    isobarModel.setModelReference( model.getModelReference() );

    // The function to add the created isobars to the model.
    ILcdFunction addPolygonToPressureModel = new ILcdFunction() {
      public boolean applyOn( Object o ) throws IllegalArgumentException {
        TLcdValuedContour valuedContour = ( TLcdValuedContour ) o;
        ILcdShape contour = valuedContour.getShape();
        isobarModel.addElement( contour, ILcdFireEventMode.NO_EVENT );
        return true;
      }
    };

    // The levels for the isobars.
    double[] contourLevels = {
        75000.0,
        80000.0,
        85000.0,
        90000.0,
        95000.0,
        100000.0,
        105000.0
    };

    // Convert the levels to internal values.
    for ( int index = 0; index < contourLevels.length; index++ ) {
      contourLevels[ index ] = descriptor.getInternalValue( contourLevels[ index ] );
    }

    // Find the isobars in our raster.
    TLcdPolylineContourFinder contourFinder = new TLcdPolylineContourFinder();
    contourFinder.findContours( new TLcdLonLatPolylineContourBuilder( addPolygonToPressureModel ),
                                matrix,
                                contourLevels,
                                null );

    return isobarModel;
  }

  public ILcdModel decodeCloudCover() throws IOException {
    return fGRIBModelDecoder.decode( fCodeBase + "Data/Grib/rlm13_71_0000_1.grb" );
  }

  public ILcdModel decodeWind() throws IOException {
    fGRIBModelDecoder.setMultiValuedRasterCreation(
        TLcdGRIBModelDecoder.MultiValuedRasterCreation.AUTO );
    ILcdModel model = fGRIBModelDecoder.decode( fCodeBase + "Data/Grib/rlm13_33_0000_100.grb" );
    return model;
  }

  public ILcdModel decodeNumericTemperature() throws IOException {
    fGRIBModelDecoder.setMultiValuedRasterCreation(
        TLcdGRIBModelDecoder.MultiValuedRasterCreation.ALWAYS );
    ILcdModel model = fGRIBModelDecoder.decode( fCodeBase + "Data/Grib/rlm13_11_0000_1.grb" );
    fGRIBModelDecoder.setMultiValuedRasterCreation(
        TLcdGRIBModelDecoder.MultiValuedRasterCreation.AUTO );
    return model;
  }
}
