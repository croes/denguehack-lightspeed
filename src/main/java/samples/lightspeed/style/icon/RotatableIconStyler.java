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
package samples.lightspeed.style.icon;

import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;

import com.luciad.gui.TLcdImageIcon;
import com.luciad.util.ILcdInterval;
import com.luciad.util.TLcdInterval;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.camera.ALspViewXYZWorldTransformation;
import com.luciad.view.lightspeed.style.TLspIconStyle;
import com.luciad.view.lightspeed.style.TLspVerticalLineStyle;
import com.luciad.view.lightspeed.style.styler.ALspStyleCollector;
import com.luciad.view.lightspeed.style.styler.ALspStyler;

/**
 * Styler class for styling rotatable icons. This tyler will apply a variable color to the icons,
 * based on the scale of the view.
 */
public class RotatableIconStyler extends ALspStyler {

  private StyleInvalidator fInvalidator = null;

  public RotatableIconStyler() {
  }

  private static final ILcdInterval[] DETAIL_LEVELS = {
      new TLcdInterval(5e-3, 8e-3),
      new TLcdInterval(8e-3, 1e-2),
      new TLcdInterval(1e-2, 2e-2),
      new TLcdInterval(2e-2, 3e-2),
      new TLcdInterval(3e-2, 4e-2),
      new TLcdInterval(4e-2, 6e-2),
      new TLcdInterval(6e-2, 1e-1),
      new TLcdInterval(1e-1, Double.MAX_VALUE)
  };

  @Override
  public void style(Collection<?> aObjects, ALspStyleCollector aStyleCollector, TLspContext aContext) {
    checkStyleInvalidator(aContext.getView());

    //Obtain the detail level and convert it to a zoom ratio
    float detailLevel = (float) fInvalidator.getDetailLevel();
    float zoomRatio = (float) Math.pow(detailLevel == 0.0f ? 0.01f : detailLevel / (DETAIL_LEVELS.length - 1), 2);

    //Create an icon style with a color that depends on the zoom ratio
    Color color = new Color(zoomRatio, 0, 1 - zoomRatio);
    TLspIconStyle iconStyle = TLspIconStyle.newBuilder()
                                           .useOrientation(true)
                                           .icon(new TLcdImageIcon("samples/lightspeed/icons/marker.png"))
                                           .scalingMode(TLspIconStyle.ScalingMode.WORLD_SCALING_CLAMPED)
        .worldSize(600)
            //Set the icons to enable orientation
        .useOrientation(true)
        .modulationColor(color)
        .opacity(0.8f)
        .build();

    //Create a vertical line for the icons with the same color as the icon
    TLspVerticalLineStyle lineStyle = TLspVerticalLineStyle.newBuilder()
                                                           .color(color)
                                                           .width(2.0f)
                                                           .opacity(0.5f).build();

    //Submit the generated styles to the style collector
    aStyleCollector.objects(aObjects).styles(iconStyle, lineStyle).submit();
  }

  private void checkStyleInvalidator(ILspView aView) {
    if (fInvalidator == null) {
      fInvalidator = new StyleInvalidator(aView);
    }
  }

  private int getLevelOfDetail(ILspView aView) {
    double scale = aView.getViewXYZWorldTransformation().getScale();

    if (DETAIL_LEVELS == null) {
      return 0;
    }

    for (int i = 0; i < DETAIL_LEVELS.length; i++) {
      ILcdInterval interval = DETAIL_LEVELS[i];

      if (interval.getMin() <= scale && scale <= interval.getMax()) {
        return i;
      }
    }

    return 0;
  }

  /**
   * Class that listens for changes in the level of detail of a given view and triggers a style
   * change event when such a change occurs.
   */
  private class StyleInvalidator implements PropertyChangeListener {
    private ILspView fView;
    private ALspViewXYZWorldTransformation fTransformation;
    private int fLevelOfDetail;

    /**
     * Creates a new style invalidator for the given view.
     *
     * @param aView the view for which to create a style invalidator.
     */
    public StyleInvalidator(ILspView aView) {
      fView = aView;
      fTransformation = fView.getViewXYZWorldTransformation();
      fLevelOfDetail = getLevelOfDetail(aView);
      fView.addPropertyChangeListener(this);
      fTransformation.addPropertyChangeListener(this);
    }

    public int getDetailLevel() {
      return fLevelOfDetail;
    }

    /**
     * Checks whether a style change event should be fired.
     *
     * @return true if a style change event should be fired, false otherwise.
     */
    private boolean shouldFireStyleChangeEvent() {
      int levelOfDetail = getLevelOfDetail(fView);
      boolean result = (fLevelOfDetail != levelOfDetail);
      fLevelOfDetail = levelOfDetail;
      return result;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
      if (evt.getSource() == fView && fView.getViewXYZWorldTransformation() != fTransformation) {
        fView.getViewXYZWorldTransformation().addPropertyChangeListener(this);
      }
      if (shouldFireStyleChangeEvent()) {
        fireStyleChangeEvent();
      }
    }
  }

}
