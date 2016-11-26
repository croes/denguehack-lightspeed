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
package samples.gxy.contour;

import java.util.HashMap;

import com.luciad.contour.TLcdIntervalContour;
import com.luciad.contour.TLcdValuedContour;
import com.luciad.shape.ILcdShape;
import com.luciad.util.ILcdInterval;

/**
 * Utility to get view related information more easily from contour domain objects in the model.
 * <p/>
 * Contours returned by the contour finders are domain objects that can be either an <code>TLcdValuedContour</code>
 * or an <code>TLcdIntervalContour</code>. These shapes contain one or two double values representing the
 * contour level. This requires multiple casts, and mapping a double to a style, to get the color or
 * label of the contours. This utility does this so that it doesn't have to be repeated anywhere
 * else.
 * <p/>
 * This utility returns the index of contour objects as an integer, based on their value.
 * This contour index can then be used to get the color, label, or other styles required for
 * painting in the view if you place those in an indexed list. Two separate types of
 * indexes exist, one for level values and one for special values. The index is gotten with
 * {@link #getIndex(Object)} , whether it's a special or a level index needs to be checked with {@link #isSpecial(Object)}.
 * <p/>
 * The utility also allows retrieving
 * the base shape inside the <code>TLcdValuedContour</code> or <code>TLcdIntervalContour</code>.
 * <p/>
 * This utility is only used by the painters.
 */
public class ContourPaintUtil {
  private HashMap<Double, Integer> fLevelIndexes = new HashMap<Double, Integer>();
  private HashMap<Double, Integer> fSpecialIndexes = new HashMap<Double, Integer>();

  /**
   * Creates a new ContourViewUtil
   *
   * @param aLevelValues   Values from the objects this painter recognises. There must be a much
   *                       values as colors.
   * @param aSpecialValues Values from the objects this painter recognises. There must be a much
   *                       values as colors.
   */
  public ContourPaintUtil(double[] aLevelValues, double[] aSpecialValues) {
    for (int i = 0; i < aLevelValues.length; i++) {
      fLevelIndexes.put(aLevelValues[i], i);
    }

    for (int i = 0; i < aSpecialValues.length; i++) {
      fSpecialIndexes.put(aSpecialValues[i], i);
    }
  }

  /**
   * Returns whether the object is a valid object known by this utility, that is, if it is a
   * TLcdValuedContour or a TLcdIntervalContour.
   *
   * @param aObject The object to check.
   *
   * @return True if and only if the object is a TLcdValuedContour or TLcdIntervalContour.
   */
  public boolean isValidObject(Object aObject) {
    return aObject instanceof TLcdValuedContour || aObject instanceof TLcdIntervalContour;
  }

  /**
   * This returns whether the value of the object is a special value or a level value. This
   * determines how the index returned by {@link #getIndex} should be interpreted: either it is an index in
   * the list of special values, or an index in the list of special values.
   *
   * @param aObject The object to check.
   *
   * @return True if the object has a special value, false if it has a level value.
   */
  public boolean isSpecial(Object aObject) {
    double value = aObject instanceof TLcdValuedContour ? ((TLcdValuedContour) aObject).getValue() : ((TLcdIntervalContour) aObject).getInterval().getMin();
    return fSpecialIndexes.containsKey(value);
  }

  /**
   * Returns the contour index of the object. If {@link #isSpecial} is true, this is an index in
   * the special values, otherwise it is an index in the level values. If the value is not found, it
   * returns -1.
   *
   * @param aObject The object to check.
   *
   * @return The index corresponding to the given contour object.
   */
  public int getIndex(Object aObject) {

    if (aObject instanceof TLcdValuedContour) {
      double value = ((TLcdValuedContour) aObject).getValue();
      if (fLevelIndexes.containsKey(value)) {
        return fLevelIndexes.get(value);
      }
      if (fSpecialIndexes.containsKey(value)) {
        return fSpecialIndexes.get(value);
      }
    } else {
      ILcdInterval interval = ((TLcdIntervalContour) aObject).getInterval();
      if (fLevelIndexes.containsKey(interval.getMin())) {
        return fLevelIndexes.get(interval.getMin());
      }
      if (fLevelIndexes.containsKey(interval.getMax())) {
        return fLevelIndexes.get(interval.getMax()) - 1;
      }
      //special values never get an TLcdIntervalContour so no need to check that map
    }

    return -1; //not found
  }

  /**
   * Get the base shape of the contour.
   * @param aObject The contour to retrieve the base shape from.
   * @return The base shape.
   */
  public static ILcdShape getShape(Object aObject) {
    return aObject instanceof TLcdValuedContour ? ((TLcdValuedContour) aObject).getShape() : ((TLcdIntervalContour) aObject).getShape();
  }
}
