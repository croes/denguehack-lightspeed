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
package samples.lightspeed.customization.style.animated;

import java.awt.Color;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.luciad.model.ILcdModel;
import com.luciad.util.ILcdCloneable;
import com.luciad.util.collections.TLcdIdentityHashSet;
import com.luciad.view.animation.ALcdAnimation;
import com.luciad.view.animation.ALcdAnimationManager;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.style.ALspStyle;
import com.luciad.view.lightspeed.style.ILspWorldElevationStyle.ElevationMode;
import com.luciad.view.lightspeed.style.TLspFillStyle;
import com.luciad.view.lightspeed.style.TLspLineStyle;
import com.luciad.view.lightspeed.style.styler.ALspStyleCollector;
import com.luciad.view.lightspeed.style.styler.ALspStyler;

/**
 * Styler that submits a common area style
 * for all objects, except for the one given at construction time.
 * The fill of the latter object is animated to fade between
 * a white and red color while the line width fades between
 * 1 and 7 pixels.
 */
class AnimatedAreaStyler extends ALspStyler {

  // All objects except for fAnimatedObject are painted with this style.
  private final List<ALspStyle> fDefaultStyle;

  // Only fAnimatedObject is painted with this style.
  private List<ALspStyle> fAnimatedStyle;
  private final Object fAnimatedObject;
  private final ILcdModel fModel;

  AnimatedAreaStyler(ILcdModel aModel, Object aAnimatedObject) {
    fModel = aModel;
    fAnimatedObject = aAnimatedObject;
    fDefaultStyle = fAnimatedStyle = createStyle(0);
    ALcdAnimationManager.getInstance().putAnimation(this, new MyAnimation());
  }

  private void fireEvent(List<ALspStyle> aAffectedStyle) {
    fireStyleChangeEvent(
        fModel,
        Collections.singleton(fAnimatedObject),
        aAffectedStyle);
  }

  private void setAnimatedStyle(float aAlpha) {
    List<ALspStyle> oldStyle = fAnimatedStyle;
    fAnimatedStyle = createStyle(aAlpha);
    fireEvent(oldStyle);
  }

  private List<ALspStyle> createStyle(float aAlpha) {
    return Arrays.<ALspStyle>asList(
        TLspFillStyle.newBuilder().color(new Color(1f, 1f - aAlpha, 1f - aAlpha, 0.5f))
                     .elevationMode(ElevationMode.ON_TERRAIN).build(),
        TLspLineStyle.newBuilder().color(new Color(0f, 0f, 0f, 0.75f)).width(1 + 6f * aAlpha)
                     .elevationMode(ElevationMode.ON_TERRAIN).build()
    );
  }

  /**
   * Looping animation that updates the animated style object
   * accordingly to fade in and out between a white and red
   * fill color and a 1 and 2 pixel line width. Note that a
   * small pause of 1.5 seconds is taken on the white fill/1 pixel line
   * style.
   *
   * @see AnimatedAreaStyler#createStyle(float)
   */
  private class MyAnimation extends ALcdAnimation {

    @Override
    protected void setTimeImpl(double aTime) {
      // We animate only the first half of the duration, the
      // second half we pause on the white color.
      if (aTime > getDuration() / 2) {
        return;
      }
      // Scale time between 0 and 1.
      float scaledTime = (float) ((aTime / getDuration()) * 2);
      // First half alpha increases, second half alpha decreases
      float alpha = 2 * (scaledTime < 0.5 ? scaledTime : (1 - scaledTime));
      // Update the style
      setAnimatedStyle(alpha);
    }

    @Override
    public double getDuration() {
      return 3; // seconds
    }

    @Override
    public void start() {
      setAnimatedStyle(0);
    }

    @Override
    public void stop() {
      setAnimatedStyle(1);
    }

    @Override
    public boolean isLoop() {
      return true;
    }
  }

  @Override
  public void style(Collection<?> aObjects, ALspStyleCollector aStyleCollector, TLspContext aContext) {
    //use contains to determine if the animated object is part of the collection in the most efficient way
    if (aObjects.contains(fAnimatedObject)) {
      if (aObjects.size() == 1) {
        aStyleCollector.object(fAnimatedObject).styles(fAnimatedStyle).submit();
      } else {
        final Collection<Object> objectsWithoutAnimated;
        if (aObjects instanceof ILcdCloneable) {
          //some LuciadLightspeed collection implementations such as TLcdIdentityHashSet
          //can be cloned to allow efficient copying
          objectsWithoutAnimated = (Collection<Object>) ((ILcdCloneable) aObjects).clone();
        } else {
          objectsWithoutAnimated = new TLcdIdentityHashSet<Object>((Collection<Object>) aObjects);
        }
        // remove the animated object
        objectsWithoutAnimated.remove(fAnimatedObject);
        aStyleCollector.object(fAnimatedObject).styles(fAnimatedStyle).submit();
        aStyleCollector.objects(objectsWithoutAnimated).styles(fDefaultStyle).submit();
      }
    } else {
      aStyleCollector.objects(aObjects).styles(fDefaultStyle).submit();
    }
  }
}
