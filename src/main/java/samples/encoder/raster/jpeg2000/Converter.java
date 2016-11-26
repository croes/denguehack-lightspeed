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
package samples.encoder.raster.jpeg2000;

import com.luciad.format.raster.*;
import com.luciad.model.*;
import com.luciad.reference.TLcdModelReferenceDecoder;
import com.luciad.util.TLcdSharedBuffer;

/**
 * This utility decodes a raster file in any of a number of formats and
 * encodes it in a JPEG2000 file.
 * <p/>
 * Usage: java samples.encoder.raster.jpeg2000.Converter
 * [-levels  n]
 * [-layers  n]
 * [-quality f]
 * [-lossless]
 * [-tilesize width height]
 * input_raster output_jpeg2000
 * <p/>
 * If the tile size is specified then the image size must be an exact multiple
 * of the tile size.
 * <p/>
 * The decoder performs lazy loading while the encoder is writing out the
 * file, but for large rasters, it may still be necessary to increase the heap
 * size (e.g. -Xmx128m).
 */
public class Converter {

  private static final ILcdModelDecoder[] DECODERS = new ILcdModelDecoder[] {
          new TLcdGeoTIFFModelDecoder( new TLcdModelReferenceDecoder(), 0 ),
          new TLcdTFWRasterModelDecoder( 1, 0.25, new TLcdModelReferenceDecoder() ),
          new TLcdTABRasterModelDecoder( 1, 0.25 ),
          new TLcdJAIRasterModelDecoder(),
          new TLcdJPEG2000ModelDecoder( TLcdSharedBuffer.getBufferInstance() ),
          new TLcdRasterModelDecoder( TLcdSharedBuffer.getBufferInstance() ),
          new TLcdCADRGModelDecoder( TLcdSharedBuffer.getBufferInstance() ),
          new TLcdDMEDModelDecoder( TLcdSharedBuffer.getBufferInstance() ),
          new TLcdDTEDModelDecoder( TLcdSharedBuffer.getBufferInstance() ),
          new TLcdDEMModelDecoder( TLcdSharedBuffer.getBufferInstance() ),
  };


  public static void main( String[] args ) {

    // Parse the input parameters.
    int level_count = -1;
    int layer_count = -1;
    float quality = 0.8f;
    boolean lossless = false;
    int tile_width = -1;
    int tile_height = -1;

    // Get any options.
    int index = 0;
    try {
      while ( index < args.length ) {
        String arg = args[ index ];
        if ( arg.equals( "-levels" ) ) level_count = Integer.parseInt( args[ ++index ] );
        else if ( arg.equals( "-layers" ) ) layer_count = Integer.parseInt( args[ ++index ] );
        else if ( arg.equals( "-quality" ) ) quality = Float.parseFloat( args[ ++index ] );
        else if ( arg.equals( "-lossless" ) ) lossless = true;
        else if ( arg.equals( "-tilesize" ) ) {
          tile_width = Integer.parseInt( args[ ++index ] );
          tile_height = Integer.parseInt( args[ ++index ] );
        } else break;

        index++;
      }
    } catch ( ArrayIndexOutOfBoundsException ex ) {
      System.out.println( "Missing numeric argument" );
      System.exit( -1 );
    } catch ( NumberFormatException ex ) {
      System.out.println( "Invalid numeric argument (" + args[ index ] + ")" );
      System.exit( -1 );
    }

    if ( index != args.length - 2 ) {
      System.out.println( "Decodes a raster file and encodes it to a JPEG2000 file." );
      System.out.println( "Arguments:" );
      System.out.println( "              [-levels  n]" );
      System.out.println( "              [-layers  n]" );
      System.out.println( "              [-quality f]" );
      System.out.println( "              [-lossless]" );
      System.out.println( "              [-tilesize width height]" );
      System.out.println( "              <input-raster-file-name> <output-jpeg2000-file-name>" );
      System.out.println("Supported input file types:");
      for (ILcdModelDecoder decoder : DECODERS) {
        System.out.println("   - " + decoder.getDisplayName());
      }
      System.exit( -1 );
    }

    // Get the file names.
    String filename_in = args[ index++ ];
    String filename_out = args[ index++ ];

    // Create a decoder and the encoder.
    ILcdModelDecoder decoder = findDecoder( filename_in );
    TLcdJPEG2000ModelEncoder encoder = new TLcdJPEG2000ModelEncoder();

    if ( level_count >= 0 )
      encoder.setLevelCount( level_count );

    if ( layer_count >= 0 )
      encoder.setQualityLayerCount( layer_count );

    encoder.setQuality( quality );
    encoder.setLossless( lossless );

    if ( tile_height >= 0 )
      encoder.setTileWidth( tile_width );

    if ( tile_width >= 0 )
      encoder.setTileHeight( tile_height );

    // Perform the actual decoding and encoding.
    try {
      System.out.println( "Decoding [" + filename_in + "] as [" + decoder.getDisplayName() + "]" );
      ILcdModel model = decoder.decode( filename_in );

      System.out.println( "Encoding [" + filename_out + "] as [" + encoder.getDisplayName() + "]" );
      encoder.export( model, filename_out );

    } catch ( Exception ex ) {
      ex.printStackTrace();
    }

    System.exit( 0 );
  }


  /**
   * Returns a decoder that is suitable for decoding the given file.
   */
  private static ILcdModelDecoder findDecoder( String aFilename ) {

    for ( int index = 0; index < DECODERS.length ; index++ ) {
      ILcdModelDecoder decoder = DECODERS[ index ];
      if ( decoder.canDecodeSource( aFilename ) )
        return decoder;
    }

    throw new IllegalArgumentException( "Can't find decoder for [" + aFilename + "]" );
  }
}
