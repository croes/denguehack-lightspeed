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
package samples.common.model;

import java.util.Properties;

import com.luciad.model.ILcdModelReference;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.shape2D.ILcd2DEditableBounds;
import com.luciad.shape.shape2D.TLcdXYBounds;
import com.luciad.shape.shape2D.TLcdXYPoint;
import com.luciad.view.ILcdXYWorldReference;
import com.luciad.view.ILcdXYZWorldReference;

/**
 * A cartesian reference.
 * <p/>
 * Note that this reference does not implement {@link com.luciad.reference.ILcdGeoReference}.
 * <p/>
 * The default transformations only support custom references if the source and destination
 * reference are equal. We only create 1 instance of this class so the default equals
 * implementation will return the correct result.
 */
public class CartesianReference implements ILcdModelReference, ILcdXYWorldReference, ILcdXYZWorldReference {

  private static final TLcdXYBounds CARTESIAN_BOUNDS = new TLcdXYBounds(-Double.MAX_VALUE / 2, -Double.MAX_VALUE / 2, Double.MAX_VALUE, Double.MAX_VALUE);

  private static final CartesianReference INSTANCE = new CartesianReference();

  /**
   * Returns the cartesian reference instance.
   *
   * @return the cartesian reference instance.
   */
  public static CartesianReference getInstance() {
    return INSTANCE;
  }

  private CartesianReference() {
  }

  @Override
  public void loadProperties(String aPrefix, Properties aProperties) throws IllegalArgumentException {
  }

  @Override
  public void writePropertiesSFCT(String aPrefix, Properties aPropertiesSFCT) throws IllegalArgumentException {
  }

  @Override
  public ILcdPoint makeModelPoint() {
    return new TLcdXYPoint();
  }

  @Override
  public boolean isBoundsAvailable() {
    return true;
  }

  @Override
  public ILcd2DEditableBounds get2DEditableBounds() {
    return CARTESIAN_BOUNDS;
  }

  @Override
  public Object clone() {
    return this; // return ourselves since this class is a singleton
  }
}
