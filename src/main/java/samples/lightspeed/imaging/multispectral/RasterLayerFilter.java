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
package samples.lightspeed.imaging.multispectral;

import com.luciad.imaging.ALcdImage;
import com.luciad.util.ILcdFilter;
import com.luciad.view.ILcdLayer;
import com.luciad.view.lightspeed.layer.raster.TLspRasterLayer;

import samples.gxy.decoder.raster.multispectral.ImageUtil;

class RasterLayerFilter implements ILcdFilter<ILcdLayer> {

  /**
   *
   * @param aLayer checks that the layer can be saved (isa <code>TLspRasterLayer</code> and has an image).
   * @return true if the layer can be saved
   */
  public boolean accept(ILcdLayer aLayer) {

    if (aLayer != null) {
      if (aLayer instanceof TLspRasterLayer) {
        ALcdImage image = ImageUtil.getImage(aLayer);
        if (image != null) {
          return true;
        }
      }
    }
    return false;
  }

}
