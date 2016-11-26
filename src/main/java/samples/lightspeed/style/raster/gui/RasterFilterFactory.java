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
package samples.lightspeed.style.raster.gui;

import java.util.Collections;

import com.luciad.imaging.operator.util.ALcdColorLookupTable;
import com.luciad.view.lightspeed.style.imagefilter.TLspColorLookupTableFilterStyle;

import samples.lightspeed.style.raster.RasterFilter;

/**
 * Factory to create a specific type of {@link RasterFilter}s from the GUI.
 */
abstract class RasterFilterFactory {
  public static final String VALUES_KEY = "Values";

  private final String fName;
  private final String[] fParameterNames;
  private final float[] fDefaultValues;

  RasterFilterFactory(String aName) {
    this(aName, new String[0], new float[0]);
  }

  RasterFilterFactory(String aName, String[] aParameterNames, float[] aDefaultValues) {
    fName = aName;
    fParameterNames = aParameterNames;
    fDefaultValues = aDefaultValues;
  }

  public RasterFilter create(float[] aValues) {
    ALcdColorLookupTable lut = createLut(aValues);
    if (lut == null) {
      return null;
    }
    return new RasterFilter(fName, Collections.<Object, Object>singletonMap(VALUES_KEY, aValues), TLspColorLookupTableFilterStyle.newBuilder().filter(lut).build());
  }

  protected abstract ALcdColorLookupTable createLut(float[] aValues);

  /**
   * @return the unique filter name
   */
  public String getName() {
    return fName;
  }

  /**
   * @return the number of filter parameters
   */
  public int getParameterCount() {
    return fParameterNames.length;
  }

  /**
   * @return the filter parameter names
   */
  public String[] getParameterNames() {
    return fParameterNames;
  }

  /**
   * @return the filter parameter default values
   */
  public float[] getDefaultValues() {
    return fDefaultValues;
  }

  /**
   * @param aFilter a filter or {@code null}
   *
   * @return {@code true} if the filter is compatible with the filters created by this factory
   */
  public boolean isCompatible(RasterFilter aFilter) {
    return aFilter != null && fName.equals(aFilter.getName());
  }

  @Override
  public String toString() {
    return fName;
  }
}
