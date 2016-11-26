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
package samples.lightspeed.demo.application.data.airspaces;

import static java.util.Collections.synchronizedMap;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;

import com.luciad.model.ILcdModel;
import com.luciad.view.animation.ALcdAnimation;
import com.luciad.view.animation.ALcdAnimationManager;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.layer.TLspPaintRepresentationState;
import com.luciad.view.lightspeed.layer.TLspPaintState;
import com.luciad.view.lightspeed.painter.label.TLspLabelID;
import com.luciad.view.lightspeed.style.styler.ALspStyler;

import samples.lightspeed.customization.style.highlighting.HighlightController;

/**
 * @author tomn
 * @since 2012.0
 */
public abstract class AAnimatedHighlightStyler extends ALspStyler implements HighlightController.HighlightListener {

  // The object that is currently under the mouse cursor (there can be only one object which is fading in)
  protected Object fFadeInObject;
  protected ILcdModel fFadeInModel;
  // All the objects being animated (i.e. 1 fading in, all the others fading out)
  protected Map<Object, Double> fObject2Alpha = synchronizedMap(new IdentityHashMap<Object, Double>());

  protected double getAlphaForObject(Object aObject) {
    Double a = fObject2Alpha.get(aObject);
    if (a == null) {
      a = 0.0;
    }
    return a;
  }

  @Override
  public void objectHighlighted(Object aObject, TLspPaintRepresentationState aPaintRepresentationState, TLspContext aContext) {
    if (fFadeInObject != aObject) {
      playFadeOutAnimation(fFadeInObject, fFadeInModel);
      ILcdModel model = aContext == null ? null : aContext.getModel();
      playFadeInAnimation(aObject, model);
      fFadeInObject = aObject;
      fFadeInModel = model;
    }
  }

  @Override
  public void labelHighlighted(TLspLabelID aLabelID, TLspPaintState aPaintState, TLspContext aContext) {
    // Do nothing
  }

  /**
   * Plays a fade out animation for the given object.
   *
   * @param aFadeOutObject the object for which the animation is played
   * @param aFadeOutModel  the model that contains the given object
   */
  protected void playFadeOutAnimation(Object aFadeOutObject, ILcdModel aFadeOutModel) {
    if (aFadeOutObject != null) {
      Double currentAlpha = fObject2Alpha.get(aFadeOutObject);
      if (currentAlpha == null) {
        currentAlpha = 1.0;
      }
      double fadeOutDuration = getFadeOutDuration(aFadeOutObject, aFadeOutModel);
      ALcdAnimationManager.getInstance().putAnimation(
          getAnimationKey(aFadeOutObject),
          new StyleAnimation(
              false,
              fadeOutDuration * currentAlpha,
              currentAlpha,
              aFadeOutModel,
              aFadeOutObject
          )
      );
    }
  }

  /**
   * Plays a fade in animation for the given object.
   *
   * @param aFadeInObject the object for which the animation is played
   * @param aFadeInModel  the model that contains the given object
   */
  protected void playFadeInAnimation(Object aFadeInObject, ILcdModel aFadeInModel) {
    if (aFadeInObject != null) {
      Double currentAlpha = fObject2Alpha.get(aFadeInObject);
      if (currentAlpha == null) {
        currentAlpha = 0.0;
      }
      double fadeInDuration = getFadeInDuration(aFadeInObject, aFadeInModel);
      ALcdAnimationManager.getInstance().putAnimation(
          getAnimationKey(aFadeInObject),
          new StyleAnimation(
              true,
              fadeInDuration * (1.0 - currentAlpha),
              currentAlpha,
              aFadeInModel,
              aFadeInObject
          )
      );
    }
  }

  protected abstract double getFadeInDuration(Object aFadeInObject, ILcdModel aFadeInModel);

  protected abstract double getFadeOutDuration(Object aFadeOutObject, ILcdModel aFadeOutModel);

  /**
   * Returns an animation key for the given domain object
   * @param aDomainObject the domain object for which to return an animation key
   * @return an animation key for the given domain object
   */
  private Object[] getAnimationKey(Object aDomainObject) {
    return new Object[]{this, aDomainObject};
  }

  private class StyleAnimation extends ALcdAnimation {
    private double fStartAlpha;
    private ILcdModel fModel;
    private double fTargetAlpha;
    private Object fObject;

    private StyleAnimation(boolean aFadeIn, double aDuration, double aStartAlpha, ILcdModel aModel, Object aObject) {
      super(Math.max(aDuration, 0.01));
      fStartAlpha = aStartAlpha;
      fModel = aModel;
      fTargetAlpha = aFadeIn ? 1.0 : 0.0;
      fObject = aObject;
    }

    @Override
    protected void setTimeImpl(double aTime) {
      double fraction = aTime / getDuration();
      double alpha = interpolate(fStartAlpha, fTargetAlpha, fraction);
      fObject2Alpha.put(fObject, alpha);
      fireStyleChangeForObject();
    }

    private void fireStyleChangeForObject() {
      fireStyleChangeEvent(fModel, Collections.singletonList(fObject), null);
    }

    @Override
    public void start() {
      setTime(0);
    }

    @Override
    public void stop() {
      // Remove object from map if faded out
      if (fTargetAlpha < 0.5) {
        fObject2Alpha.remove(fObject);
      }
      fireStyleChangeForObject();
    }
  }
}
