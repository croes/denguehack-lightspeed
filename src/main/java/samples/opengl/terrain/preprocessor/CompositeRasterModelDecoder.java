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
package samples.opengl.terrain.preprocessor;

import com.luciad.format.raster.*;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelDecoder;
import com.luciad.reference.TLcdModelReferenceDecoder;
import com.luciad.util.ILcdBuffer;
import com.luciad.util.TLcdSharedBuffer;

import java.io.IOException;
import java.util.Vector;

/**
 * A composite ILcdModelDecoder, which wraps several of LuciadLightspeed's raster data
 * decoders into one.
 */
class CompositeRasterModelDecoder implements ILcdModelDecoder {

  private ILcdBuffer fBuffer = TLcdSharedBuffer.getBufferInstance();
  private Vector fDecoders = new Vector();

  public void setBuffer( ILcdBuffer aBuffer ) {
    fBuffer = aBuffer;
  }

  public CompositeRasterModelDecoder() {
    TLcdGeoTIFFModelDecoder geotiff = new TLcdGeoTIFFModelDecoder();
    geotiff.setDefaultValue( 0 );
    geotiff.setModelReferenceDecoder( new TLcdModelReferenceDecoder() );
    addDecoder( geotiff );

    TLcdDMEDModelDecoder dmed = new TLcdDMEDModelDecoder( fBuffer );
    addDecoder( dmed );

    TLcdDTEDModelDecoder dted = new TLcdDTEDModelDecoder( fBuffer );
    addDecoder( dted );

    TLcdDEMModelDecoder dem = new TLcdDEMModelDecoder( fBuffer );
    addDecoder( dem );

    TLcdRasterModelDecoder rst = new TLcdRasterModelDecoder( fBuffer );
    addDecoder( rst );

    TLcdTABRasterModelDecoder tab = new TLcdTABRasterModelDecoder();
    tab.setDefaultValue( 0 );
    addDecoder( tab );

    TLcdCADRGModelDecoder cadrg = new TLcdCADRGModelDecoder( fBuffer );
    addDecoder( cadrg );

    TLcdJAIRasterModelDecoder jai = new TLcdJAIRasterModelDecoder();
    jai.setDefaultValue( 0 );
    addDecoder( jai );
  }

  public void addDecoder( ILcdModelDecoder aDecoder ) {
    fDecoders.add( aDecoder );
  }

  public String getDisplayName() {
    return "Composite raster model decoder";
  }

  public boolean canDecodeSource( String aSource ) {
    // Iterate over the registered decoders until we find one that can decode the given source.
    for ( int i = 0; i < fDecoders.size() ; i++ ) {
      if ( ( (ILcdModelDecoder) fDecoders.get( i ) ).canDecodeSource( aSource ) ) {
        return true;
      }
    }
    return false;
  }

  public ILcdModel decode( String aSource ) throws IOException {
    // Iterate over the registered decoders until we find one that can decode the given source.
    for ( int i = 0; i < fDecoders.size() ; i++ ) {
      if ( ( (ILcdModelDecoder) fDecoders.get( i ) ).canDecodeSource( aSource ) ) {
        return ( (ILcdModelDecoder) fDecoders.get( i ) ).decode( aSource );
      }
    }
    // None of the decoders work for the given source.
    System.err.println( "ERROR: No appropriate decoder found for " + aSource + "!" );
    return null;
  }
}
