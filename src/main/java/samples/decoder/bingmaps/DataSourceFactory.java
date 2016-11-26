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
package samples.decoder.bingmaps;

import java.awt.Component;
import java.io.IOException;
import java.util.Properties;

import javax.swing.JOptionPane;

import com.luciad.format.bingmaps.ELcdBingMapsMapStyle;
import com.luciad.format.bingmaps.ILcdBingMapsDataSource;
import com.luciad.format.bingmaps.TLcdBingMapsDataSource;
import com.luciad.format.bingmaps.TLcdBingMapsDataSourceBuilder;
import com.luciad.format.bingmaps.TLcdBingMapsModelDecoder;
import com.luciad.format.bingmaps.TLcdBingMapsModelDescriptor;
import com.luciad.io.TLcdInputStreamFactory;
import com.luciad.model.ILcdDataSource;
import com.luciad.model.ILcdModel;
import com.luciad.util.logging.TLcdLoggerFactory;

/**
 * Class that creates BingMaps data sources.
 */
public class DataSourceFactory {

  private final static String PROPERTIES_FILE = "samples/decoder/bingmaps/bingmaps.properties";

  public static ILcdModel createDefaultBingModel( ELcdBingMapsMapStyle aMapStyle, Component aParentComponent ) {
    try {
      // Create default data source object.
      ILcdBingMapsDataSource source = createDefaultDataSource( aMapStyle );
      TLcdBingMapsModelDecoder decoder = new TLcdBingMapsModelDecoder();
      return decoder.decodeSource( source );
    }
    catch ( IOException e ) {
      TLcdLoggerFactory.getLogger( "com.luciad" ).warn( "Could not connect with Bing Maps servers", e );
      JOptionPane.showMessageDialog( aParentComponent, "To connect with the Bing Maps servers, a live internet connection and a Bing Maps key is required.\n\n" +
                                                       "The Bing Maps key must be stored in the bingmaps.properties file (under resources/samples/decoder/bingmaps).\n" +
                                                       "Please refer to the Bing Maps developer's guide for more information on how to obtain a key."
          ,
                                     "Unable to create Bing Maps model", JOptionPane.ERROR_MESSAGE );
      return null;
    }
  }

  public static ILcdBingMapsDataSource createDefaultDataSource( ELcdBingMapsMapStyle aMapStyle ) throws IOException {
    Properties properties = new Properties();
    properties.load( new TLcdInputStreamFactory().createInputStream( PROPERTIES_FILE ) );
    String bingKey = properties.getProperty( "applicationId", "" );

    ILcdBingMapsDataSource source = new TLcdBingMapsDataSourceBuilder( bingKey ).
        mapStyle( aMapStyle ).build();
    return source;
  }

  public static boolean containsText( TLcdBingMapsModelDescriptor aModelDescriptor ) {
    if ( aModelDescriptor.getDataSource() instanceof TLcdBingMapsDataSource) {
      ILcdDataSource dataSource= aModelDescriptor.getDataSource();
      ELcdBingMapsMapStyle style = ( ( TLcdBingMapsDataSource ) dataSource ).getMapStyle();
      if (style == ELcdBingMapsMapStyle.AERIAL_WITH_LABELS || style == ELcdBingMapsMapStyle.ROAD ) {
        return true;
      }
    }
    return false;
  }
}
