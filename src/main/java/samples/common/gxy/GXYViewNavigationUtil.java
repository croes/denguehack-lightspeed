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
package samples.common.gxy;

import com.luciad.model.ILcdModelReference;
import com.luciad.shape.ILcdBounds;
import com.luciad.shape.shape2D.ILcd2DEditableBounds;
import com.luciad.shape.shape2D.TLcdXYBounds;
import com.luciad.transformation.TLcdDefaultModelXYWorldTransformation;
import com.luciad.util.TLcdNoBoundsException;
import com.luciad.view.gxy.ILcdGXYView;
import com.luciad.view.gxy.TLcdGXYFitGXYViewFromXYWorldBounds;

/**
 * Utility class which allows you to programmatically navigate in a GXY view.
 */
public class GXYViewNavigationUtil {

  /**
   * Fits an ILcdGXYView to the provided ILcdBounds in the given ILcdModelReference.
   * @param aView the view to be fitted.
   * @param aBounds the new bounds of the view
   * @param aReference the reference of the bounds
   * @throws TLcdNoBoundsException  if the model bounds doesn't have valid corresponding bounds in world space, or if it is undefined.
   */
  public static void fitOnBounds(ILcdGXYView aView, ILcdBounds aBounds, ILcdModelReference aReference) throws TLcdNoBoundsException {
    TLcdDefaultModelXYWorldTransformation worldTransform = new TLcdDefaultModelXYWorldTransformation(aReference, aView.getXYWorldReference());
    ILcd2DEditableBounds worldBounds = new TLcdXYBounds();
    worldTransform.modelBounds2worldSFCT(aBounds, worldBounds);
    TLcdGXYFitGXYViewFromXYWorldBounds.fitGXYViewFromXYWorldBounds(aView, worldBounds);
  }
}
