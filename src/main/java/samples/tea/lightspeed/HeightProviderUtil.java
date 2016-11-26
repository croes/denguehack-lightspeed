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
package samples.tea.lightspeed;

import java.util.HashMap;
import java.util.Map;

import com.luciad.geodesy.TLcdSphereUtil;
import com.luciad.imaging.ALcdImage;
import com.luciad.model.ILcdModel;
import com.luciad.reference.ILcdGeoReference;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.shape2D.TLcdLonLatPoint;
import com.luciad.util.TLcdConstant;
import com.luciad.util.height.ILcdHeightProvider;
import com.luciad.util.height.TLcdImageModelHeightProviderFactory;

public class HeightProviderUtil {

  private HeightProviderUtil() {}

  /**
   *
   * @param aModel model containing the height data.
   * @param aGeoReference reference of the points used to retrieve data from the returned height provider.
   * @param aPoint point at which to estimate the pixel density.  It is assumed to be geodetic.
   * @param aDTEDLevel the DTED level used to approximate the resolution of the height data returned by the height provider.
   * @return height provider using the data of aModel with resolution based on aPixelSideLength.
   */
  public static ILcdHeightProvider getHeightProvider(ILcdModel aModel, ILcdGeoReference aGeoReference, ILcdPoint aPoint, DTEDLevel aDTEDLevel) {
    return getHeightProvider(aModel, aGeoReference, calculateDensity(aPoint, aDTEDLevel.getStepSize()));
  }

  public enum DTEDLevel {
    LEVEL_0("Level 0", 900),
    LEVEL_1("Level 1", 90),
    LEVEL_2("Level 2", 30);

    private final String fName;
    //The step size approximately corresponds to the level
    private final double fStepSize;

    DTEDLevel(String aName, double aStepSize) {
      fName = aName;
      fStepSize = aStepSize;
    }

    @Override
    public String toString() {
      return fName;
    }

    public double getStepSize() {
      return fStepSize;
    }

  }

  private static ILcdHeightProvider getHeightProvider(ILcdModel aModel,ILcdGeoReference aGeoReference, double aPixelDensity) {
    ALcdImage image = ALcdImage.fromDomainObject(aModel.elements().nextElement());
    if (image == null) {
      throw new IllegalArgumentException("Model contains an unsupported type.");
    }
    TLcdImageModelHeightProviderFactory modelHeightProviderFactory = new TLcdImageModelHeightProviderFactory();
    Map<String, Object> requiredPropertiesSFCT = new HashMap<>();
    requiredPropertiesSFCT.put(TLcdImageModelHeightProviderFactory.KEY_GEO_REFERENCE, aGeoReference);
    Map<String, Object> optionalProperties = new HashMap<>();
    optionalProperties.put(TLcdImageModelHeightProviderFactory.KEY_INTERPOLATE_DATA, false);
    optionalProperties.put(TLcdImageModelHeightProviderFactory.KEY_PIXEL_DENSITY, aPixelDensity);
    return modelHeightProviderFactory.createHeightProvider(aModel, requiredPropertiesSFCT, optionalProperties);
  }

  /**
   * Assumes the given point is geodetic.
   */
  private static double calculateDensity(ILcdPoint aPoint, double aPixelSideLength) {
    TLcdLonLatPoint llPoint = new TLcdLonLatPoint(aPoint);
    llPoint.translate2D(0.1, 0.0);
    double lonDistanceInMeters = TLcdSphereUtil.greatCircleDistance(aPoint, llPoint) * TLcdConstant.DEG2RAD * TLcdConstant.EARTH_RADIUS;
    llPoint.translate2D(-0.1, 0.1);
    double latDistanceInMeters = TLcdSphereUtil.greatCircleDistance(aPoint, llPoint) * TLcdConstant.DEG2RAD * TLcdConstant.EARTH_RADIUS;
    // the area in square meters
    double areaInMeters2 = latDistanceInMeters * lonDistanceInMeters;

    // the area in pixels
    double areaInPixels = areaInMeters2 / (aPixelSideLength * aPixelSideLength);
    // the area is 0.01 degrees square, so the resulting density is 100 times larger
    return areaInPixels / (0.1 * 0.1);
  }

}
