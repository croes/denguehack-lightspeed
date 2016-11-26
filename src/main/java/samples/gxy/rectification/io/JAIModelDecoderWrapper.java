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
package samples.gxy.rectification.io;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import com.luciad.format.raster.TLcdJAIRasterModelDecoder;
import com.luciad.io.TLcdIOUtil;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelReference;
import com.luciad.reference.ILcdGridReference;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.reference.TLcdLambert1972BelgiumGridReference;
import com.luciad.reference.TLcdModelReferenceFactory;
import com.luciad.shape.shape2D.TLcdLonLatBounds;
import com.luciad.shape.shape2D.TLcdXYBounds;
import com.luciad.transformation.TLcdGeodetic2Grid;
import com.luciad.util.TLcdNoBoundsException;
import com.luciad.util.TLcdStringUtil;
import com.luciad.view.ILcdXYWorldReference;

/**
 * A model decoder which reads image files. If the image has no associated geographical reference or
 * bounds, it assigns some default value.
 */
public class JAIModelDecoderWrapper extends TLcdJAIRasterModelDecoder {

  private TLcdLonLatBounds fDefaultBounds = new TLcdLonLatBounds(20, 30, 10, 10);
  private ILcdModelReference fDefaultReference = new TLcdLambert1972BelgiumGridReference();

  public ILcdModel decode(String aSourceName) throws IOException {
    Properties jai_properties = new Properties();
    String path = TLcdIOUtil.getDirectoryPath(aSourceName);
    String file = TLcdIOUtil.getFileName(aSourceName);
    jai_properties.setProperty("TLcdJAIRasterModelDecoder.fileName", file);

    try {
      // Look for a .ref file
      setModelBounds(fDefaultBounds);
      String ref_file_name = aSourceName.substring(0, aSourceName.lastIndexOf('.')) + ".ref";
      Properties ref_properties = new Properties();
      FileInputStream fis = new FileInputStream(ref_file_name);
      ref_properties.load(fis);
      ILcdModelReference reference = TLcdModelReferenceFactory.createModelReference("", ref_properties);
      setModelReference(reference);
    } catch (Exception ex) {

      setModelReference(fDefaultReference);

      if (fDefaultReference instanceof ILcdGridReference) {
        // If we chose a non-geodetic reference, transform the default bounds
        // into that reference. 
        TLcdXYBounds grid_bounds = new TLcdXYBounds();
        TLcdGeodetic2Grid transfo = new TLcdGeodetic2Grid(new TLcdGeodeticReference(),
                                                          (ILcdXYWorldReference) fDefaultReference);
        try {
          transfo.modelBounds2worldSFCT(fDefaultBounds, grid_bounds);
        } catch (TLcdNoBoundsException e) {
          e.printStackTrace();
        }
        setModelBounds(grid_bounds);
      } else {
        setModelBounds(fDefaultBounds);
      }
    }
    return decodeProperties(path, jai_properties);
  }

  public boolean canDecodeSource(String aSourceName) {
    return super.canDecodeSource(aSourceName) ||
           TLcdStringUtil.endsWithIgnoreCase(aSourceName, "png") ||
           TLcdStringUtil.endsWithIgnoreCase(aSourceName, "bmp") ||
           TLcdStringUtil.endsWithIgnoreCase(aSourceName, "gif") ||
           TLcdStringUtil.endsWithIgnoreCase(aSourceName, "jpg") ||
           TLcdStringUtil.endsWithIgnoreCase(aSourceName, "tif");
  }
}
