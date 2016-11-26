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
package samples.lightspeed.demo.application.data.uav;

import java.util.Collections;
import java.util.List;

import com.luciad.model.ILcdModelReference;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.shape3D.TLcdXYZPoint;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.editor.TLspHandleGeometryType;
import com.luciad.view.lightspeed.editor.handle.TLspPointTranslationHandle;
import com.luciad.view.lightspeed.style.styler.ALspStyleTargetProvider;

/**
 * An edit handle that allows the user to resize a
 * bounds that is defined in screen space.
 * <p>
 * The handle can be configured to be positioned at one
 * of four locations on the bounds: up, down, left, right.
 */
class ScreenSpaceBoundsHandle extends TLspPointTranslationHandle {

  private ScreenSpaceBoundsEditor.Location fLocation;
  private ScreenSpaceBounds fBounds;
  private final ILspView fView;

  /**
   * Creates a new screen space bounds handle.
   *
   * @param aBounds      the bounds object for which the handle is created
   * @param aLocation    the location at which the handle is to be positioned
   * @param aView        the view this handle is in
   */
  public ScreenSpaceBoundsHandle(ScreenSpaceBounds aBounds,
                                 ScreenSpaceBoundsEditor.Location aLocation,
                                 ILspView aView) {
    super(aBounds, null, null);
    fLocation = aLocation;
    fBounds = aBounds;
    fView = aView;
    setPriority(PRIORITY_2D_POINT + 5);
    //We set the user property of the editor, so it is copied to our edit operations by TLspPointTranslationHandle
    getProperties().put(ScreenSpaceBoundsEditor.LOCATION_IDENTIFIER, aLocation);
  }

  /**
   * Returns the bounds location of the handle.
   *
   * @return the bounds location of the handle
   *
   * @see ScreenSpaceBoundsEditor.Location
   */
  public ScreenSpaceBoundsEditor.Location getLocation() {
    return fLocation;
  }

  /**
   * Returns the actual screen space point at which the handle is positioned.
   *
   * @param aView the view in which the handle is shown
   *
   * @return the actual screen space location of the handle
   */
  public ILcdPoint getLocationPoint(ILspView aView) {
    TLcdXYZPoint pointSFCT = new TLcdXYZPoint();
    fBounds.retrieveAbsolutePointSFCT(aView.getWidth(), aView.getHeight(),
                                      fLocation.getX(), fLocation.getY(),
                                      pointSFCT);
    pointSFCT.translate3D(0, 0, 0.1);
    return pointSFCT;
  }

  @Override
  public ILcdPoint getPoint() {
    return getLocationPoint(fView);
  }

  @Override
  public List<ALspStyleTargetProvider> getStyleTargetProviders(TLspHandleGeometryType aType) {
    if (aType == TLspHandleGeometryType.POINT) {
      return Collections.<ALspStyleTargetProvider>singletonList(new ALspStyleTargetProvider() {
        @Override
        public void getStyleTargetsSFCT(Object aObject, TLspContext aContext, List<Object> aResultSFCT) {
          //Update the model point before returning the visualization
          aResultSFCT.add(getPoint());
        }

        @Override
        public ILcdModelReference getStyleTargetReference(TLspContext aContext) {
          // The given point is defined in a view reference.
          return null;
        }
      });
    }
    return Collections.emptyList();
  }
}
