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
package samples.lightspeed.demo.application.data.streets;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashSet;
import java.util.Set;

import javax.swing.SwingUtilities;

import com.luciad.util.ILcdInterval;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.camera.ALspViewXYZWorldTransformation;
import com.luciad.view.lightspeed.style.styler.ALspStyler;

/**
 * Styler that uses a style invalidator to trigger a style change event when the view's level of detail
 * changes from one detail level to the next.
 */
public abstract class DetailLevelStyler extends ALspStyler {

  private final Set<ILspView> fWithPropertyChangeListener = new HashSet<ILspView>();

  private synchronized void checkView(ILspView aView) {
    if (!(fWithPropertyChangeListener.contains(aView))) {
      fWithPropertyChangeListener.add(aView);
      new StyleInvalidator(aView);
    }
  }

  /**
   * Returns the level of detail of the given view.
   *
   * @param aViewXYZWorldTransformation the view to return the level of detail for.
   *
   * @return the level of detail of the given view.
   */
  public int getLevelOfDetail(ALspViewXYZWorldTransformation aViewXYZWorldTransformation) {
    checkView(aViewXYZWorldTransformation.getView());
    return getLevelOfDetail(aViewXYZWorldTransformation, getDetailLevels());
  }

  public static int getLevelOfDetail(ALspViewXYZWorldTransformation aViewXYZWorldTransformation, ILcdInterval[] aIntervals) {
    double scale = aViewXYZWorldTransformation.getScale();

    if (aIntervals == null) {
      return -1;
    }

    for (int i = 0; i < aIntervals.length; i++) {
      ILcdInterval interval = aIntervals[i];

      if (interval.getMin() <= scale && scale <= interval.getMax()) {
        return i;
      }
    }

    return -1;
  }

  // Array with discrete intervals which delimit the detail levels of the view.
  protected abstract ILcdInterval[] getDetailLevels();

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
      fLevelOfDetail = getLevelOfDetail(aView.getViewXYZWorldTransformation());
      fView.removePropertyChangeListener(this);
      fView.addPropertyChangeListener(this);
      fTransformation.addPropertyChangeListener(this);
    }

    /**
     * Checks whether a style change event should be fired.
     *
     * @return true if a style change event should be fired, false otherwise.
     */
    private boolean shouldFireStyleChangeEvent() {
      int levelOfDetail = getLevelOfDetail(fView.getViewXYZWorldTransformation());
      boolean result = (fLevelOfDetail != levelOfDetail);
      fLevelOfDetail = levelOfDetail;
      return result;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
      if (evt.getSource() == fView && fView.getViewXYZWorldTransformation() != fTransformation) {
        fTransformation = fView.getViewXYZWorldTransformation();
        fLevelOfDetail = 0;
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            fTransformation.removePropertyChangeListener(StyleInvalidator.this);
            fTransformation.addPropertyChangeListener(StyleInvalidator.this);
          }
        });
      }
      if (shouldFireStyleChangeEvent()) {
        fireStyleChangeEvent();
      }
    }
  }
}
