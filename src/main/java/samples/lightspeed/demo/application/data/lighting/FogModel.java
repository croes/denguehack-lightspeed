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
package samples.lightspeed.demo.application.data.lighting;

import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.services.effects.TLspFog;
import com.luciad.view.lightspeed.services.effects.TLspGraphicsEffects;

/**
 * Changes the fog of an {@link ILspView}.
 */
public class FogModel {
  protected final TLspFog fFog;

  public FogModel(ILspView aView) {
    // Using fog improves the perception of depth
    fFog = new TLspFog(aView);

    setFogEnabled(true);
  }

  public void activate(ILspView aView) {
    TLspGraphicsEffects fx = aView.getServices().getGraphicsEffects();
    fx.add(fFog);
  }

  /**
   * Enables or disable the fog in the view.
   *
   * @param aEnabled {@code true} if fog should be enabled
   */
  public void setFogEnabled(boolean aEnabled) {
    fFog.setEnabled(aEnabled);
  }

  /**
   * Returns whether the fog in the view is enabled.
   *
   * @return {@code true} if fog is enabled
   */
  public boolean isFogEnabled() {
    return fFog.isEnabled();
  }

  /**
   * Sets the visibility distance used when the camera is at or below MinAltitude.
   *
   * @param aFogVisibility the minimum visibility distance due to fog
   */
  public void setFogVisibilityAtMinAltitude(double aFogVisibility) {
    fFog.setVisibilityAtMinAltitude(aFogVisibility);
  }

  /**
   * Returns the visibility distance when the camera is at or below MinAltitude.
   * The distance is measured in world units. The default value is 100 km.
   *
   * @return the minimum visibility distance due to fog
   */
  public double getFogVisibilityAtMinAltitude() {
    return fFog.getVisibilityAtMinAltitude();
  }
}
