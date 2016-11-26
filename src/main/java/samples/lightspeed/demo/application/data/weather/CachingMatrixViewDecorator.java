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
package samples.lightspeed.demo.application.data.weather;

import com.luciad.shape.ILcdMatrixView;

/**
 * Decorates an <code>ILcdMatrixView</code> with caching functionality.
 * One can optionally set a <code>CachingMode</code>.
 * By default all values are eagerly fetched and cached.
 */
class CachingMatrixViewDecorator implements ILcdMatrixView {

  private final CachingMode fCachingMode;
  private final ILcdMatrixView fMatrixView;
  private Double[] fCache;
  private Double fMin;
  private Double fMax;

  public enum CachingMode {
    LAZY,
    EAGER;
  }

  public CachingMatrixViewDecorator(ILcdMatrixView aMatrixView) {
    this(aMatrixView, CachingMode.EAGER);
  }

  public CachingMatrixViewDecorator(ILcdMatrixView aMatrixView, CachingMode aCachingMode) {
    fMatrixView = aMatrixView;
    fCachingMode = aCachingMode;
    fCache = new Double[getColumnCount() * getRowCount()];
    if (fCachingMode == CachingMode.EAGER) {
      initializeCachedValues();
    }
  }

  private void initializeCachedValues() {
    double currentMin = Double.POSITIVE_INFINITY;
    double currentMax = Double.NEGATIVE_INFINITY;
    for (int i = 0; i < getColumnCount(); i++) {
      for (int j = 0; j < getRowCount(); j++) {
        double value = getValue(i, j);
        if (!Double.isNaN(value)) {
          if (value < currentMin) {
            currentMin = value;
          }
          if (value > currentMax) {
            currentMax = value;
          }
        }
        cacheValue(i, j, value);
      }
    }
    fMin = currentMin == Double.POSITIVE_INFINITY ? Double.NaN : currentMin;
    fMax = currentMax == Double.NEGATIVE_INFINITY ? Double.NaN : currentMax;
  }

  @Override
  public double getValue(int i, int j) {
    Double cachedValue = getCachedValue(i, j);
    if (cachedValue != null) {
      return cachedValue;
    }

    double result = fMatrixView.getValue(i, j);
    cacheValue(i, j, result);
    return result;
  }

  private Double getCachedValue(int i, int j) {
    return fCache[j * getColumnCount() + i];
  }

  private void cacheValue(int i, int j, double aValue) {
    fCache[j * getColumnCount() + i] = aValue;
  }

  @Override
  public double retrieveAssociatedPointX(int i, int j) {
    return fMatrixView.retrieveAssociatedPointX(i, j);
  }

  @Override
  public double retrieveAssociatedPointY(int i, int j) {
    return fMatrixView.retrieveAssociatedPointY(i, j);
  }

  @Override
  public int getColumnCount() {
    return fMatrixView.getColumnCount();
  }

  @Override
  public int getRowCount() {
    return fMatrixView.getRowCount();
  }

  public double getMin() {
    if (fMin == null) {
      initializeCachedValues();
    }
    return fMin;
  }

  public double getMax() {
    if (fMax == null) {
      initializeCachedValues();
    }
    return fMax;
  }

}
