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
package samples.gxy.common.toolbar;

import java.awt.Point;
import java.awt.event.ActionEvent;

import com.luciad.gui.ALcdAction;
import com.luciad.gui.TLcdActionAtLocationEvent;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.shape2D.TLcdXYPoint;
import com.luciad.shape.shape3D.TLcdXYZPoint;
import com.luciad.view.animation.ALcdAnimation;
import com.luciad.view.animation.ALcdAnimationManager;
import com.luciad.view.animation.ILcdAnimation;
import com.luciad.view.gxy.ILcdGXYView;
import com.luciad.view.gxy.ILcdGXYViewXYWorldTransformation;

/**
 * Zooms towards the location passed via the action event.
 * @see TLcdActionAtLocationEvent
 */
public class ZoomToAction extends ALcdAction {

  private ILcdGXYView fGXYView;

  public ZoomToAction(ILcdGXYView aGXYView) {
    fGXYView = aGXYView;
  }

  @Override
  public void actionPerformed(ActionEvent aActionEvent) {
    if (aActionEvent instanceof TLcdActionAtLocationEvent) {
      ALcdAnimationManager animationManager = ALcdAnimationManager.getInstance();
      TLcdActionAtLocationEvent me = (TLcdActionAtLocationEvent) aActionEvent;
      ILcdAnimation zoomToAnimation = new ZoomToAnimation(
          new TLcdXYPoint(me.getLocation().getX(), me.getLocation().getY()),
          2.0, // factor
          1.0, // duration
          fGXYView
      );
      animationManager.putAnimation(fGXYView, zoomToAnimation);
    } else {
      throw new IllegalArgumentException("This action expects TLcdActionAtLocationEvent");
    }
  }

  private static class ZoomToAnimation extends ALcdAnimation {

    private double fScale, fScale1;
    private double fPositionX0, fPositionX1, fPositionY0, fPositionY1;
    private ILcdGXYView fView;

    private ZoomToAnimation(
        ILcdPoint aViewPointDestination,
        double aFactor,
        double aDuration,
        ILcdGXYView aView) {
      super(aDuration, aView);
      fView = aView;

      ILcdGXYViewXYWorldTransformation v2w = fView.getGXYViewXYWorldTransformation();

      Point new_view_origin = new Point((int) aViewPointDestination.getX(), (int) aViewPointDestination.getY());
      TLcdXYZPoint new_world_origin = new TLcdXYZPoint();
      v2w.viewXYPoint2worldSFCT(new TLcdXYPoint(new_view_origin.getX(), new_view_origin.getY()), new_world_origin);

      fScale = fView.getScale();
      fScale1 = fScale * aFactor;

      fPositionX0 = aViewPointDestination.getX();
      fPositionY0 = aViewPointDestination.getY();

      fPositionX1 = aView.getWidth() / 2.0;
      fPositionY1 = aView.getHeight() / 2.0;

      fView.setViewOrigin(new_view_origin);
      fView.setWorldOrigin(new_world_origin);
      setInterpolator(Interpolator.SMOOTH_STEP);
    }

    @Override
    protected void setTimeImpl(double aTime) {
      double t = aTime / getDuration();
      double lerpX0 = 1.0 / fScale;
      double lerpX1 = 1.0 / fScale1;

      double scale = 1.0 / ((1 - t) * lerpX0 + t * lerpX1);

      double px = (1 - t) * fPositionX0 + t * fPositionX1;
      double py = (1 - t) * fPositionY0 + t * fPositionY1;

      fView.setScale(scale, false, true);
      fView.setViewOrigin(new Point((int) px, (int) py));
    }
  }

}
