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
package samples.opengl.dted;

import com.luciad.format.mif.TLcdMIFModelDecoder;
import com.luciad.format.raster.TLcdDMEDModelDecoder;
import com.luciad.geodesy.TLcdGeodeticDatum;
import com.luciad.model.*;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.util.*;
import com.luciad.view.map.TLcdLonLatGrid;

import java.io.IOException;

/**
 * A utility class that can create some hard coded models. These models are used as sample data in the sample
 * application.
 */
class ModelFactory {

  public static final String GRID_MODEL_DISPLAY_NAME = "Grid";
  public static final String GRID_MODEL_TYPE_NAME = GRID_MODEL_DISPLAY_NAME;

  public static ILcdModel createDTEDModel( String aCodebase ) {
    TLcdDMEDModelDecoder decoder = new TLcdDMEDModelDecoder( TLcdSharedBuffer.getBufferInstance() );
    try {
      return decoder.decode( aCodebase + "Data/Dted/Alps/dmed" );
    } catch ( IOException e ) {
      e.printStackTrace();
      return null;
    }
  }

  public static ILcdModel createGridModel() {
    TLcdLonLatGrid grid = new TLcdLonLatGrid( 10, 10 );
    TLcdModelDescriptor descriptor = new TLcdModelDescriptor( "", GRID_MODEL_DISPLAY_NAME, GRID_MODEL_TYPE_NAME );
    TLcdGeodeticReference reference = new TLcdGeodeticReference( new TLcdGeodeticDatum() );
    TLcdVectorModel model = new TLcdVectorModel( reference, descriptor );
    model.addElement( grid, ILcdFireEventMode.NO_EVENT );
    return model;
  }

  public static ILcdModel createMIFModel( String aCodebase ) {
    TLcdMIFModelDecoder decoder = new TLcdMIFModelDecoder();
    try {
      return decoder.decode( aCodebase + "Data/Mif/World/major_cities.mif" );
    } catch ( IOException e ) {
      e.printStackTrace();
      return null;
    }
  }
}
