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
package samples.common.lightspeed;

import samples.common.AScaleSupport;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.shape3D.ILcd3DEditablePoint;
import com.luciad.view.ILcdXYZWorldReference;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.camera.ALspViewXYZWorldTransformation;
import com.luciad.view.lightspeed.util.TLspViewNavigationUtil;

/**
 * An {@code AScaleSupport} implementation for an {@link ILspView}.
 */
public class LspScaleSupport extends AScaleSupport {

  private ILspView fView;

  public LspScaleSupport(ILspView aView) {
    fView = aView;
  }

  @Override
  protected double getScale() {
    return fView.getViewXYZWorldTransformation().getScaleX();
  }

  @Override
  protected void setScale(double aScale) {
    ALspViewXYZWorldTransformation transformation = fView.getViewXYZWorldTransformation();
    // navigation util nicely zooms in on the center of the map
    TLspViewNavigationUtil navigationUtil = new TLspViewNavigationUtil(fView);
    int i=0;
    // because in 3D the scale is a derived property, zooming is an iterative process.
    while (Math.abs(aScale - transformation.getScale()) > 1e-12 && i < 100) {
     i++;
      double factor = aScale / transformation.getScale();
      navigationUtil.zoom(factor);
    }
  }

  @Override
  protected int getWidth() {
    return fView.getWidth();
  }

  @Override
  protected int getHeight() {
    return fView.getHeight();
  }

  @Override
  protected ILcdXYZWorldReference getWorldReference() {
    return fView.getXYZWorldReference();
  }

  @Override
  protected void view2World(ILcdPoint aViewPoint, ILcd3DEditablePoint aWorldPointSFCT) {
    fView.getViewXYZWorldTransformation().viewPoint2WorldSFCT(aViewPoint, aWorldPointSFCT);
  }

  /**
   * Returns the map scale at the origin of the projection. This is the scale
   * that is commonly used on paper maps. The accuracy depends on the distance
   * (in meters) between the origin of the projection, and what is currently
   * visible on screen. Typically, the larger the distance, the greater the
   * distortion will be that is caused by the projection, and the less accurate
   * the scale is.
   *
   * If for example the projection is centered on Paris, the scale is calculated
   * for Paris. Therefore, if the view is showing the US, the scale calculated
   * by this method is potentially way off compared to what is visible on
   * screen.
   *
   * @param  aView            The view.
   * @param aScreenResolution The screen resolution, in dots per inch (dpi).
   *  Provide a negative value for the default screen resolution.
   *
   * @return The scale at the origin of the projection of the map (contrary to
   *  the center of the view). The result is a map scale, e.g. 1/13000.
   */
  public static double getMapProjectionOriginScale(ILspView aView, double aScreenResolution) {
    return new LspScaleSupport(aView).getMapProjectionOriginScale(aScreenResolution);
  }

  /**
   * Returns the approximate map scale at the center of the current view
   * extents. Contrary to {@link #getMapProjectionOriginScale}, it calculates
   * the scale at the center of the current view extents. So if the projection
   * is centered on a spot far away of the current view extents, the scale
   * calculated by this method is still accurate (it is measured horizontally).
   * This does imply however that the result of this method changes by simply
   * panning the map around.
   *
   * @param aView             The view.
   * @param aScreenResolution The screen resolution, in dots per inch (dpi).
   *  Provide a negative value for the default screen resolution.
   *
   * @return The scale in the center of the view of the map (contrary to the
   *  origin of the projection).  The result is a map scale, e.g. 1/13000
   */
  public static double getMapCenterScale(ILspView aView, double aScreenResolution) {
    return new LspScaleSupport(aView).getMapCenterScale(aScreenResolution);
  }
}