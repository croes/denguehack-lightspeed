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
package samples.lightspeed.demo.tools;

import java.io.File;
import java.io.IOException;

import com.luciad.format.shp.TLcdSHPModelDecoder;
import com.luciad.format.shp.TLcdSHPModelEncoder;
import com.luciad.model.ILcdModel;
import com.luciad.shape.shape2D.TLcdLonLatBounds;

/**
 * @author tomn
 * @since 2012.0
 */
public class ClipLAStreets {

  //  private static final String SRC = "//terrain/data/vector/CASIL/transportation/census_2000/local_roads_geodetic.shp";
  private static final String SRC = "E:\\Vector\\OpenStreetMap\\gis.osm_roads_major_v05.shp";
  private static final String DST = "Data/internal.data/OSM/la_majorroads.shp";

  private static final double CX = -118.31;
  private static final double CY = 34;
  private static final double W = 5;
  private static final double H = 5;

}
