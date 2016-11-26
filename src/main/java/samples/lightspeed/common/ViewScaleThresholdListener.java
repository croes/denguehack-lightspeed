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
package samples.lightspeed.common;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.camera.ALspViewXYZWorldTransformation;

/**
 * Listener that can be attached to a view to monitor scale changes.
 *
 * It calls {@link ThresholdListener#thresholdChanged(boolean)} whenever the given
 * scale threshold is passed.
 *
 * It automatically re-attaches when the view switches to a different projection.
 */
public class ViewScaleThresholdListener {

  private boolean fBelow;
  private final double fScaleThreshold;
  private final ThresholdListener fListener;

  private final PropertyChangeListener fViewListener = new PropertyChangeListener() {
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
      if ("viewXYZWorldTransformation".equals(evt.getPropertyName())) {
        ((ALspViewXYZWorldTransformation) evt.getOldValue()).removePropertyChangeListener(fScaleListener);
        ((ALspViewXYZWorldTransformation) evt.getNewValue()).addPropertyChangeListener(fScaleListener);
      }
    }
  };

  private final PropertyChangeListener fScaleListener = new PropertyChangeListener() {
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
      boolean wasBelow = fBelow;
      fBelow = (fScaleThreshold < ((ALspViewXYZWorldTransformation) evt.getSource()).getScale());
      if (wasBelow != fBelow) {
        fListener.thresholdChanged(fBelow);
      }
    }
  };

  private ViewScaleThresholdListener(ILspView aView, double aScaleThreshold, ThresholdListener aListener) {
    fScaleThreshold = aScaleThreshold;
    fListener = aListener;
    fBelow = (fScaleThreshold < aView.getViewXYZWorldTransformation().getScale());
    aListener.thresholdChanged(fBelow);
    aView.addPropertyChangeListener(fViewListener);
    aView.getViewXYZWorldTransformation().addPropertyChangeListener(fScaleListener);
  }

  public static ViewScaleThresholdListener attach(ILspView aView, double aScaleThreshold, ThresholdListener aListener) {
    return new ViewScaleThresholdListener(aView, aScaleThreshold, aListener);
  }

  public static void detach(ILspView aView, ViewScaleThresholdListener aListener) {
    aView.getViewXYZWorldTransformation().removePropertyChangeListener(aListener.fScaleListener);
    aView.removePropertyChangeListener(aListener.fViewListener);
  }

  public interface ThresholdListener {
    public void thresholdChanged(boolean aBelowThreshold);
  }
}
