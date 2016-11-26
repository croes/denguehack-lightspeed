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
package samples.gxy.labels.offset;

import java.awt.Rectangle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.SwingUtilities;

import com.luciad.util.ILcdFireEventMode;
import com.luciad.view.ALcdLabelLocations;
import com.luciad.view.ILcdLabelLocationListener;
import com.luciad.view.ILcdLabelPaintedListener;
import com.luciad.view.ILcdView;
import com.luciad.view.TLcdLabelIdentifier;
import com.luciad.view.TLcdLabelLocation;
import com.luciad.view.TLcdLabelLocationEvent;
import com.luciad.view.TLcdLabelLocations;
import com.luciad.view.TLcdLabelPaintedEvent;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.ILcdGXYView;

/**
 * This ILcdLabelLocationListener/ILcdLabelPaintedListener checks if one of the label locations
 * or the label painted status of a label has changed. If so, it invalidates the given layer.
 * <p>
 * This listener is useful when labels are used as body representations (TLcdLabelLocation#isBodyLabel()).
 * In that case, when a labels is changed, the bodies need to be repainted as well.
 * <p>
 * This listener is also useful when using a labeling algorithm that incrementally changes the label
 * locations, for example like an animation.
 * This way, the algorithm is triggered until all labels are in a stable state.  In this case,
 * use {@link #setInvalidationDelay(long)} to pace the decluttering.
 * <p>
 * This class also assumes that no extensions of TLcdLabelLocation are used. If so the
 * labelLocationsEqual() method should be overridden.
 */
public class LabelLocationsInvalidationListener implements ILcdLabelLocationListener, ILcdLabelPaintedListener {

  private final ScheduledExecutorService fExecutorService = Executors.newSingleThreadScheduledExecutor();

  private final ALcdLabelLocations fLabelLocations;
  private final ILcdGXYLayer fGXYLayer;
  private final ILcdGXYView fGXYView;

  private float fEqualityThreshold;
  private long fInvalidationDelay = 0;

  /**
   * Creates a new bodies invalidation listener. When a label change is detected, it will
   * invalidate the given layer using the given view. The given <code>ALcdLabelLocations</code>
   *  is used to store the initial state of the labels. It should be set on the given layer.
   *
   * @param aGXYLayer       a gxy layer.
   * @param aLabelLocations the layer's gxy layer.
   * @param aGXYView        a gxy view.
   */
  public LabelLocationsInvalidationListener(ILcdGXYLayer aGXYLayer, ALcdLabelLocations aLabelLocations, ILcdGXYView aGXYView) {
    fGXYLayer = aGXYLayer;
    fGXYView = aGXYView;

    // If no label location is moved more than 0.75 pixels when using free placement
    // (see getLocationX() and getLocationY()), they are considered equal.
    fEqualityThreshold = 0.75f;

    fLabelLocations = new TLcdLabelLocations(aGXYLayer, aLabelLocations.createLabelLocation());
    copyInitialState(aLabelLocations);
  }

  /**
   * Specify a delay before triggering the invalidation.
   * By default, there is no delay (0).
   *
   * @param aDelay the delay, in milliseconds.
   */
  public void setInvalidationDelay(long aDelay) {
    fInvalidationDelay = aDelay;
  }

  private void copyInitialState(ALcdLabelLocations aOriginalLocation) {
    aOriginalLocation.applyOnAllLabelLocations(fGXYView, new ALcdLabelLocations.LabelLocationFunction() {
      public boolean applyOnLabelLocation(Object aObject, int aLabelIndex, int aSubLabelIndex, ILcdView aView, TLcdLabelLocation aLocation) {
        fLabelLocations.putLabelLocation(aObject, aLabelIndex, aSubLabelIndex, aView, aLocation, ILcdFireEventMode.NO_EVENT);
        return true;
      }
    });
    aOriginalLocation.applyOnPaintedLabelLocations(fGXYView, new ALcdLabelLocations.LabelLocationFunction() {
      public boolean applyOnLabelLocation(Object aObject, int aLabelIndex, int aSubLabelIndex, ILcdView aView, TLcdLabelLocation aLocation) {
        fLabelLocations.setPainted(aObject, aLabelIndex, aSubLabelIndex, aView, true, ILcdFireEventMode.NO_EVENT);
        return true;
      }
    });
  }

  public void labelLocationChanged(TLcdLabelLocationEvent aEvent) {
    if (locationChangeDetected(aEvent)) {
      triggerInvalidate();
    }
  }

  private void triggerInvalidate() {
    if (fInvalidationDelay == 0) {
      fGXYView.invalidateGXYLayer(fGXYLayer, true, this, "Invalidate because labels changed");
    } else {
      fExecutorService.schedule(new Runnable() {
        @Override
        public void run() {
          SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
              fGXYView.invalidateGXYLayer(fGXYLayer, true, this, "Invalidate because labels changed");
            }
          });
        }
      }, fInvalidationDelay, TimeUnit.MILLISECONDS);
    }
  }

  private boolean locationChangeDetected(final TLcdLabelLocationEvent aEvent) {
    final ALcdLabelLocations label_locations = aEvent.getLabelLocations();
    final boolean[] change_detected = {false};

    final TLcdLabelLocation old_work_location = fLabelLocations.createLabelLocation();
    final TLcdLabelLocation new_work_location = label_locations.createLabelLocation();
    aEvent.processChangedLabels(new TLcdLabelLocationEvent.LabelFunction() {
      public boolean applyOnLabel(Object aObject, int aLabelIndex, int aSubLabelIndex, ILcdView aView) {
        TLcdLabelLocation old_location = null;
        if (fLabelLocations.getLabelLocationSFCT(aObject, aLabelIndex, aSubLabelIndex, aView, old_work_location)) {
          old_location = old_work_location;
        }
        TLcdLabelLocation new_location = null;
        if (label_locations.getLabelLocationSFCT(aObject, aLabelIndex, aSubLabelIndex, aView, new_work_location)) {
          new_location = new_work_location;
        }

        if (!labelLocationsEqual(old_location, new_location)) {
          change_detected[0] = true;
          if (old_location != null && new_location != null) {

          }
        }

        // Keep the duplicate ALcdLabelLocations in sync
        fLabelLocations.putLabelLocation(aObject, aLabelIndex, aSubLabelIndex, aView, new_location, ILcdFireEventMode.NO_EVENT);

        return true;
      }
    });

    return change_detected[0];
  }

  protected boolean labelLocationsEqual(TLcdLabelLocation aLabelLocation1, TLcdLabelLocation aLabelLocation2) {
    return (aLabelLocation1 == null && aLabelLocation2 == null) ||
           aLabelLocation1 != null && aLabelLocation2 != null &&
           aLabelLocation1.getLocationIndex() == aLabelLocation2.getLocationIndex() &&
           (Math.abs(aLabelLocation1.getLocationX() - aLabelLocation2.getLocationX()) < fEqualityThreshold) &&
           (Math.abs(aLabelLocation1.getLocationY() - aLabelLocation2.getLocationY()) < fEqualityThreshold) &&
           aLabelLocation1.getLabelEditMode() == aLabelLocation2.getLabelEditMode() &&
           aLabelLocation1.getRotation() == aLabelLocation2.getRotation() &&
           parentLabelsEqual(aLabelLocation1.getParentLabel(), aLabelLocation2.getParentLabel()) &&
           parentBoundsEqual(aLabelLocation1.getParentBoundsRectangle(), aLabelLocation2.getParentBoundsRectangle()) &&
           aLabelLocation1.getParentBoundsRotation() == aLabelLocation2.getParentBoundsRotation() &&
           aLabelLocation1.isBodyLabel() == aLabelLocation2.isBodyLabel();
  }

  private boolean parentLabelsEqual(TLcdLabelIdentifier aParent1, TLcdLabelIdentifier aParent2) {
    return (aParent1 == null && aParent2 == null) ||
           aParent1 != null && aParent2 != null &&
           aParent1.equals(aParent2);
  }

  private boolean parentBoundsEqual(Rectangle aBounds1, Rectangle aBounds2) {
    return aBounds1 == null && aBounds2 == null ||
           aBounds1 != null && aBounds2 != null &&
           Math.abs(aBounds1.getX() - aBounds2.getX()) < fEqualityThreshold &&
           Math.abs(aBounds1.getY() - aBounds2.getY()) < fEqualityThreshold &&
           aBounds1.width == aBounds2.width &&
           aBounds1.height == aBounds2.height;
  }

  public void labelPaintedChanged(TLcdLabelPaintedEvent aEvent) {
    if (paintedChangeDetected(aEvent)) {
      triggerInvalidate();
    }
  }

  private boolean paintedChangeDetected(TLcdLabelPaintedEvent aEvent) {
    final boolean[] change_detected = {false};

    aEvent.processChangedPaintedLabels(new TLcdLabelPaintedEvent.LabelPaintedFunction() {
      public boolean applyOnLabel(Object aObject, int aLabelIndex, int aSubLabelIndex, ILcdView aView, boolean aPainted) {
        boolean old_painted = fLabelLocations.isPainted(aObject, aLabelIndex, aSubLabelIndex, aView);
        if (old_painted != aPainted) {
          change_detected[0] = true;
        }

        // Keep the duplicate ALcdLabelLocations in sync
        fLabelLocations.setPainted(aObject, aLabelIndex, aSubLabelIndex, aView, aPainted, ILcdFireEventMode.NO_EVENT);

        return true;
      }
    });

    return change_detected[0];
  }
}
