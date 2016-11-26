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
package samples.lightspeed.demo.application.data.support.modelfactories;

import java.util.List;

import com.luciad.format.dafif.model.aerodrome.ILcdDAFIFAerodromeFeature;
import com.luciad.format.dafif.model.aerodrome.TLcdDAFIFAerodromeModelDescriptor;
import com.luciad.format.dafif.model.airspace.ILcdDAFIFAirspaceFeature;
import com.luciad.format.dafif.model.airspace.TLcdDAFIFAirspaceModelDescriptor;
import com.luciad.format.dafif.util.ILcdDAFIFModelFilter;
import com.luciad.model.ILcdModelDescriptor;
import com.luciad.util.ILcdFeatured;

/**
 * Custom DAFIF region filter that only accepts the regions
 * that are passed to it at construction time.
 */
public class DafifRegionFilter implements ILcdDAFIFModelFilter {

  private List<String> fAcceptedCodes;
  private ILcdModelDescriptor fDescriptor;

  /**
   * Creates a new DAFIF region filter for the given set of region codes.
   * <p>
   * When <code>aAcceptedCodes</code> is null, all regions will be accepted.
   *
   * @param aAcceptedCodes the codes that will be accepted (all other codes are discarded)
   */
  public DafifRegionFilter(List<String> aAcceptedCodes) {
    fAcceptedCodes = aAcceptedCodes;
  }

  public void setModelDescriptor(ILcdModelDescriptor aDescriptor) {
    if (aDescriptor instanceof TLcdDAFIFAerodromeModelDescriptor ||
        aDescriptor instanceof TLcdDAFIFAirspaceModelDescriptor) {
      fDescriptor = aDescriptor;
    } else {
      throw new IllegalArgumentException("Cannot accept the given model descriptor: " + aDescriptor);
    }
  }

  public boolean accept(Object o) {
    if (fAcceptedCodes == null) {
      return true;
    }

    if (o instanceof ILcdFeatured) {
      ILcdFeatured featured = (ILcdFeatured) o;

      // Get index of ICAO or Aerodrome code feature
      int index = -1;
      if (fDescriptor instanceof TLcdDAFIFAerodromeModelDescriptor) {
        index = ((TLcdDAFIFAerodromeModelDescriptor) fDescriptor).getFeatureIndex(ILcdDAFIFAerodromeFeature.ICAO_CODE);
        return fAcceptedCodes.contains(featured.getFeature(index));
      } else if (fDescriptor instanceof TLcdDAFIFAirspaceModelDescriptor) {
        index = ((TLcdDAFIFAirspaceModelDescriptor) fDescriptor).getFeatureIndex(ILcdDAFIFAirspaceFeature.ICAO_CODE);
        return fAcceptedCodes.contains(featured.getFeature(index));
      } else {
        // ICAO code or Aerodrome code not found
        return false;
      }
    }
    return false;
  }

}
