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
package samples.lightspeed.imaging.multispectral.histogram;

import java.util.ArrayList;
import java.util.List;

import com.luciad.shape.ILcdPoint;
import com.luciad.shape.ILcdShapeList;
import com.luciad.shape.TLcdShapeList;
import com.luciad.shape.shape2D.ILcd2DEditablePoint;
import com.luciad.shape.shape2D.TLcd2DEditablePointList;
import com.luciad.shape.shape2D.TLcdXYPoint;
import com.luciad.shape.shape2D.TLcdXYPolygon;
import com.luciad.util.collections.TLcdIntArrayList;

/**
 *  Represents a histogram of a band
 */
public class Histogram {

  private int[] fBinValues;
  private ILcdPoint[] fEqualizationCurve;

  /**
   * Creates a new histogram.
   *
   * @param aBinValues the values for the histogram bins
   */
  public Histogram(int[] aBinValues) {
    fBinValues = aBinValues.clone();
  }

  /**
   * Returns the number of bins in the histogram
   *
   * @return the number of bins in the histogram
   */
  public int getBinCount() {
    return fBinValues.length;
  }

  /**
   * Returns the value for a particular bin.
   *
   * @param aBinIndex the bin index in {@code [0,#getBinCount[}
   *
   * @return the bin value
   */
  public int getBinValue(int aBinIndex) {
    return fBinValues[aBinIndex];
  }

  /**
   * Returns the points from an equalization curve.
   *
   * @return a point array representing the equalization curve
   */
  public ILcdPoint[] getEqualizationCurve() {
    if (fEqualizationCurve == null) {
      fEqualizationCurve = createControlPointsFromHistogram(fBinValues, 64);
    }
    return fEqualizationCurve;
  }

  /**
   * Transforms the histogram bins into a shape list containing rectangles.
   * For each bin one rectangle is added to the shape list.
   *
   * @return the shape list containing the rectangles
   */
  public ILcdShapeList asShape() {
    // Normalize histogram
    float maxValue = 0;
    int width = fBinValues.length;
    for (int v : fBinValues) {
      float value = v == 0 ? 0 : (float) Math.log(v);
      maxValue = Math.max(maxValue, value);
    }

    TLcdShapeList shapeList = new TLcdShapeList();
    float stepSize = 1 / (float) width;
    // create rectangle for each bin
    for (int i = 0; i < width; i++) {
      float value = (fBinValues[i] == 0) ? 0 : (float) Math.log(fBinValues[i]);
      float x0 = i * stepSize;
      float x1 = (i + 1) * stepSize;
      value /= maxValue;
      shapeList.addShape(new TLcdXYPolygon(
          new TLcd2DEditablePointList(new ILcd2DEditablePoint[]{
              new TLcdXYPoint(x0, 0),
              new TLcdXYPoint(x1, 0),
              new TLcdXYPoint(x1, value),
              new TLcdXYPoint(x0, value)
          }, false)
      ));
    }
    return shapeList;
  }

  /**
   * Creates control points matching a histogram equalization curve, based on the cumulative distribution function.
   *
   * @param aBinValues the histogram for which to compute a tone mapping curve
   * @param aMaxNumberOfCP the maximum maximum number of control points, excluding the endpoints.
   * @return control points that can be used with the curves operator
   */
  static ILcdPoint[] createControlPointsFromHistogram(int[] aBinValues, int aMaxNumberOfCP) {
    int binCount = aBinValues.length;

    int[] unNormalizedCdf = new int[binCount];
    for (int i = 1; i < binCount; i++) {
      unNormalizedCdf[i] = unNormalizedCdf[i - 1] + aBinValues[i];
    }

    float[] cdf = new float[binCount];
    for (int bin = 0; bin < binCount; bin++) {
      cdf[bin] = unNormalizedCdf[bin] / (unNormalizedCdf[binCount - 1] + 0.5f);
    }

    float binOffset = 1 / (float) binCount;
    float position = binOffset / 2;
    List<ILcdPoint> controlPoints = new ArrayList<>();
    controlPoints.add(new TLcdXYPoint(0, 0));

    for (int bin = 0; bin < binCount; bin++) {
      TLcdXYPoint point = new TLcdXYPoint(position, cdf[bin]);
      // Avoid adding duplicate points
      if (!controlPoints.get(controlPoints.size() - 1).equals(point)) {
        controlPoints.add(point);
      }
      position += binOffset;
    }

    TLcdXYPoint lastPoint = new TLcdXYPoint(1, 1);
    // Avoid adding duplicate points
    if (!controlPoints.get(controlPoints.size() - 1).equals(lastPoint)) {
      controlPoints.add(lastPoint);
    }

    return reduce(controlPoints, aMaxNumberOfCP);
  }

  /**
   * Reduce the number of control points to the given maximum.
   * @param aControlPoints control points of the curve.
   * @param aMaxCP maximum number of control points.
   * @return the <code>aMaxCP</code> most significant control points.
   */
  private static ILcdPoint[] reduce(List<ILcdPoint> aControlPoints, int aMaxCP) {
    int cpCount = aControlPoints.size();
    if (cpCount <= aMaxCP) {
      return aControlPoints.toArray(new ILcdPoint[cpCount]);
    } else {
      int smoothing = cpCount / (2 * aMaxCP) | 1;
      int[] extrema = findLocalExtrema(diff2(aControlPoints, Math.max(smoothing, 3)), aMaxCP);
      List<ILcdPoint> cp = new ArrayList<ILcdPoint>(extrema.length + 2);
      cp.add(aControlPoints.get(0));

      for (int extremeIndex : extrema) {
        ILcdPoint point = aControlPoints.get(extremeIndex);
        // Don't add duplicates
        if (!cp.get(cp.size() - 1).equals(point)) {
          cp.add(point);
        }
      }
      ILcdPoint point = aControlPoints.get(cpCount - 1);
      // Don't add duplicates
      if (!cp.get(cp.size() - 1).equals(point)) {
        cp.add(point);
      }
      return cp.toArray(new ILcdPoint[cp.size()]);
    }
  }

  /**
   * Calculate the 2nd derivative of the low-pass filtered version of the given curve.
   * Dx is assumed to be constant (points uniformly spaced among x)
   * @param aPoints points defining the curve.
   * @param aSmoothing amount of smoothing (moving average window size). Must be odd and at least 3.
   * @return y-coordinates of the points defining the 2nd derivative. The points are scaled
   * by the smoothing factor for efficiency.
   */
  private static double[] diff2(List<ILcdPoint> aPoints, int aSmoothing) {
    int halfWnd = aSmoothing / 2;
    double[] wt = new double[aPoints.size()];
    double windowTotal = 0;
    for (int i = 0; i < aSmoothing; ++i) {
      windowTotal += aPoints.get(i).getY();
    }
    double[] d = new double[aPoints.size() - 2];
    for (int i = 0; i < aPoints.size(); ++i) {
      if (i > halfWnd && i < aPoints.size() - halfWnd - 1) {
        windowTotal = windowTotal - aPoints.get(i - halfWnd - 1).getY() + aPoints.get(i + halfWnd).getY();
      }
      wt[i] = windowTotal;
      if (i > 1) {
        d[i - 2] = wt[i] - 2 * wt[i - 1] + wt[i - 2];
      }
    }
    return d;
  }

  /**
   * Find local extrema in the given array.
   * Used to distribute control points more fairly if the 2nd derivative contains multiple
   * peaks of different heights, in which case global extrema are biased.
   * @param aValues the input array.
   * @param aMaxCount maximum number of extrema to find.
   * @return the indices of the found local extrema in ascending order.
   */
  private static int[] findLocalExtrema(double[] aValues, int aMaxCount) {
    int[] segments = findSegments(aValues, aMaxCount);
    int[] extrema = new int[segments.length];
    if (segments.length > 0) {
      extrema[0] = findMaximum(aValues, 0, segments[0]);
      for (int i = 1; i < segments.length; ++i) {
        extrema[i] = findMaximum(aValues, segments[i - 1], segments[i]);
      }
    }
    return extrema;
  }

  /**
   * Find the index of the maximum value (in absolute value) of the given array segment.
   * @param aValues the input array.
   * @param aStart starting offset (inclusive).
   * @param aEnd ending offset (exclusive).
   * @return index of the biggest element in the array segment.
   */
  private static int findMaximum(double[] aValues, int aStart, int aEnd) {
    double max = -1;
    int maxIndex = -1;
    for (int i = aStart; i < aEnd; ++i) {
      double abs = Math.abs(aValues[i]);
      if (abs > max) {
        max = abs;
        maxIndex = i;
      }
    }
    return maxIndex;
  }

  /**
   * Divide the given array in segments of equal weight.
   * @param aValues array to divide.
   * @param aMaxCount maximum number of segments to divide the array in.
   * @return right boundary indexes of the found segments.
   * I.e. <code>segment_i = [result[i-1], result[i])</code>
   */
  private static int[] findSegments(double[] aValues, int aMaxCount) {
    double[] cs = cumulativeSum(aValues);
    double threshold = cs[cs.length - 1] / aMaxCount;
    TLcdIntArrayList intervals = new TLcdIntArrayList(aMaxCount);
    double t = threshold;
    for (int i = 1; i < aValues.length; ++i) {
      if (cs[i] > t) {
        intervals.addInt(i);
        t = threshold + cs[i];
      }
    }
    if (!intervals.isEmpty() && intervals.getInt(intervals.size() - 1) != aValues.length) {
      intervals.addInt(aValues.length);
    }
    return intervals.toIntArray();
  }

  /**
   * Calculate the cumulative sum of the given array in absolute values.
   * @param aValues the input array.
   * @return array of the cumulative sums.
   */
  private static double[] cumulativeSum(double[] aValues) {
    double[] cs = new double[aValues.length];
    cs[0] = Math.abs(aValues[0]);
    for (int i = 1; i < cs.length; ++i) {
      cs[i] = cs[i - 1] + Math.abs(aValues[i]);
    }
    return cs;
  }
}

