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
package samples.decoder.gdf.filter;

import com.luciad.format.gdf.ILcdGDFFeature;
import com.luciad.network.graph.TLcdTraversalDirection;
import com.luciad.util.ILcdFilter;

import samples.decoder.gdf.network.function.GDFRoadClassFunction;

/**
 * A simple filter with three levels of detail:
 * <p/>
 * <ul>
 * <li>level 0 - passes only roads with road class less than 3, and waterways</li>
 * <li>level 1 - passes only roads with road class less than 4, and waterways</li>
 * <li>level 2 - passes everything</li>
 * </ul>
 */
public class GDFLevelOfDetailFilter implements ILcdFilter {

  private int fLevel;

  private GDFRoadClassFunction fRoadClassFunction = new GDFRoadClassFunction();

  /**
   * Returns the number of levels in this filter.
   *
   * @return
   */
  public int getLevelCount() {
    return 3;
  }

  /**
   * Sets the zoom level for this filter.
   *
   * @param aLevel
   */
  public void setLevel(int aLevel) {
    fLevel = aLevel;
  }

  public boolean accept(Object object) {
    if (!(object instanceof ILcdGDFFeature)) {
      throw new IllegalArgumentException("This filter accepts only ILcdGDFFeature objects");
    }
    ILcdGDFFeature feature = (ILcdGDFFeature) object;
    switch (fLevel) {

    case 0:
      if (feature.getFeatureClass().getFeatureClassCode() == 4110) {
        int road_class = (int) fRoadClassFunction.computeEdgeValue(null, null, (Object) feature, TLcdTraversalDirection.FORWARD);
        if (road_class < 4) {
          return true;
        } else {
          return false;
        }
      } else if (feature.getFeatureClass().getFeatureClassCode() == 4310) {
        return true;
      } else {
        return false;
      }

    case 1:
      if (feature.getFeatureClass().getFeatureClassCode() == 4110) {
        int road_class = (int) fRoadClassFunction.computeEdgeValue(null, null, (Object) feature, TLcdTraversalDirection.FORWARD);
        if (road_class < 6) {
          return true;
        } else {
          return false;
        }
      } else if (feature.getFeatureClass().getFeatureClassCode() == 4310) {
        return true;
      } else {
        return false;
      }

    default:
      return true;
    }
  }
}
