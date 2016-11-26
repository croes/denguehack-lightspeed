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
package samples.gxy.statisticalPainter;

import com.luciad.datamodel.ILcdDataObject;

/**
 * Utility class to produce derivations of specific object properties.
 */
class PopulationUtil {
  private static double fMaxDensity = 2.5;
  private static double fMaxDensityChange = 0.5;
  private static final String POPULATION_1990_PROPERTY_NAME = "POP1990";
  private static final String POPULATION_1996_PROPERTY_NAME = "POP1996";
  private static final String AREA_PROPERTY_NAME = "AREA";

  public PopulationUtil() {
  }

  public static double getDensity(ILcdDataObject aDataObject) {
    // Retrieve the population value and the area.
    Object pop = aDataObject.getValue(POPULATION_1996_PROPERTY_NAME);
    Object area = aDataObject.getValue(AREA_PROPERTY_NAME);

    // Convert the objects to doubles.
    double nominator = 0;
    double denominator = 1.0;
    if (pop != null) {
      nominator = Double.valueOf(pop.toString());
    }
    if (area != null) {
      denominator = Double.valueOf(area.toString());
    }

    // Compute the density.
    double density_factor =
        Math.log(1 + nominator / denominator) / (Math.log(10) * fMaxDensity);
    if (density_factor > 1.0) {
      density_factor = 1.0;
    }

    return density_factor;
  }

  public static int getPopulationChange(ILcdDataObject aDataObject) {

    // Retrieve the population values and the area.
    Object pop1990 = aDataObject.getValue(POPULATION_1990_PROPERTY_NAME);
    Object pop1996 = aDataObject.getValue(POPULATION_1996_PROPERTY_NAME);
    Object area = aDataObject.getValue(AREA_PROPERTY_NAME);

    // Convert the objects to doubles.
    int p90 = 0;
    int p96 = 0;
    double ar = 1.0;

    if (pop1990 != null) {
      p90 = Double.valueOf(pop1990.toString()).intValue();
    }
    if (pop1996 != null) {
      p96 = Double.valueOf(pop1996.toString()).intValue();
    }
    if (area != null) {
      ar = Double.valueOf(area.toString());
    }

    int size;

    // Compute the population change.
    if (p96 >= p90) {
      size = 1 + (int) (Math.log(1 + (p96 - p90) / ar) / fMaxDensityChange);
    } else {
      size = -(1 + (int) (Math.log(1 + (p90 - p96) / ar) / fMaxDensityChange));
    }
    return size;
  }

  public static double getMaxDensity() {
    return fMaxDensity;
  }

  public static double getMaxDensityChange() {
    return fMaxDensityChange;
  }

}

