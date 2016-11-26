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
package samples.lightspeed.customization.controller;

import com.luciad.view.animation.ALcdAnimationManager;
import com.luciad.view.animation.ILcdAnimation;
import com.luciad.view.lightspeed.ILspView;

/**
 * Wrapper class for easy creation of continuous animations.
 */
public abstract class AbstractContinuousAnimation {

  private ILspView fView;
  private ILcdAnimation fAnimation;

  /**
   * Create a new <code>AbstractContinuousAnimation</code> on the specified view.
   *
   * @param aView the view.
   */
  public AbstractContinuousAnimation(ILspView aView) {
    fView = aView;
  }

  /**
   * Start the continuous animation.
   */
  public void start() {
    if (fAnimation == null) {
      fAnimation = createAnimation(fView);
      ALcdAnimationManager.getInstance().putAnimation(fView.getViewXYZWorldTransformation(), fAnimation);
    }
  }

  /**
   * Create a new continuous animation for the specified view.
   *
   * @param aView the view.
   *
   * @return the continuous animation.
   */
  protected abstract ILcdAnimation createAnimation(ILspView aView);

  /**
   * Stop the continuous animation.
   */
  public void stop() {
    if (ALcdAnimationManager.getInstance().getAnimation(fView.getViewXYZWorldTransformation()) == fAnimation) {
      ALcdAnimationManager.getInstance().removeAnimation(fView.getViewXYZWorldTransformation());
    }
    fAnimation = null;
  }
}
