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
package samples.opengl.orthorectification.satellite;

import com.luciad.format.shp.TLcdSHPModelDecoder;
import com.luciad.format.raster.terrain.TLcdTerrainModelDecoder;
import com.luciad.geodesy.TLcdGeodeticDatum;
import com.luciad.model.*;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.util.ILcdFireEventMode;
import com.luciad.view.map.TLcdLonLatGrid;

import java.io.IOException;

/**
 * A factory that creates models for the 3D sample application.
 */
class ModelFactory {

  /**
   * Creates a grid model.
   */
  public static ILcdModel createGridModel() {
    TLcdLonLatGrid        grid       = new TLcdLonLatGrid(10, 10);
    TLcdModelDescriptor   descriptor = new TLcdModelDescriptor("Grid", "Grid",   "Grid");
    TLcdGeodeticReference reference  = new TLcdGeodeticReference(new TLcdGeodeticDatum());
    TLcdVectorModel       model      = new TLcdVectorModel(reference, descriptor);

    model.addElement(grid, ILcdFireEventMode.NO_EVENT);

    return model;
  }


  /**
   * Loads a SHP model from the given source.
   */
  public static ILcdModel createSHPModel(String aSourceName)
    throws IOException {

    TLcdSHPModelDecoder decoder = new TLcdSHPModelDecoder();
    return decoder.decode(aSourceName);
  }


  /**
   * Loads a preprocessed terrain model from the given source.
   */
  public static ILcdModel createTerrainModel(String aSourceName)
    throws IOException {

    TLcdTerrainModelDecoder decoder = new TLcdTerrainModelDecoder();
    return decoder.decode(aSourceName);
  }
}
